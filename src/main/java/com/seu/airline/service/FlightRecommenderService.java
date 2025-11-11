package com.seu.airline.service;

import com.seu.airline.dto.FlightDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smile.classification.MLP;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import smile.base.mlp.Layer;
import smile.base.mlp.OutputFunction;

@Service
public class FlightRecommenderService {

    private static final Logger logger = LoggerFactory.getLogger(FlightRecommenderService.class);

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightViewHistoryService flightViewHistoryService;

    private static final int INPUT_LAYER_SIZE = 6;
    private static final int HIDDEN_LAYER_SIZE = 20;
    private static final int EPOCHS = 100;

    // 并发安全的模型缓存
    private final Map<Long, MLP> userModels = new ConcurrentHashMap<>();

    private MLP getUserModel(Long userId, List<FlightDTO> trainingData) {
        if (userModels.containsKey(userId)) {
            return userModels.get(userId);
        }

        List<TrainingSample> samples = generateTrainingSamples(userId, trainingData);
        if (samples.isEmpty()) {
            logger.warn("Could not generate training samples for user {}", userId);
            return null;
        }

        int n = samples.size();
        double[][] x = new double[n][INPUT_LAYER_SIZE];
        int[] y = new int[n];
        for (int i = 0; i < n; i++) {
            x[i] = samples.get(i).getFeatures();
            y[i] = samples.get(i).getLabel();
        }

        // 改为单输出 + Sigmoid（输出为概率），并明确注释
        MLP model = new MLP(
                INPUT_LAYER_SIZE,
                Layer.rectifier(HIDDEN_LAYER_SIZE),
                Layer.mle(1, OutputFunction.SIGMOID) // 单输出，Sigmoid
        );

        // Online training: 逐样本 update
        for (int epoch = 0; epoch < EPOCHS; epoch++) {
            for (int i = 0; i < n; i++) {
                try {
                    model.update(x[i], y[i]);
                } catch (Exception e) {
                    logger.warn("Model update failed at epoch {} sample {}: {}", epoch, i, e.getMessage());
                }
            }
        }

        userModels.put(userId, model);
        return model;
    }

    private double predict(MLP model, double[] features) {
        if (model == null) return 0.5;
        try {
            // 明确使用 predict(features, out) 来拿到 sigmoid 概率
            double[] out = new double[1];
            model.predict(features, out);
            double prob = out[0];
            return Math.max(0.0, Math.min(1.0, prob));
        } catch (Exception e) {
            logger.error("Prediction error: {}", e.getMessage());
            return 0.5;
        }
    }

    private List<TrainingSample> generateTrainingSamples(Long userId, List<FlightDTO> recentViews) {
        List<TrainingSample> samples = new ArrayList<>();
        List<FlightDTO> allFlights = flightService.getUpcomingFlights().stream()
                .map(FlightDTO::new)
                .collect(Collectors.toList());

        // 正样本
        for (FlightDTO viewedFlight : recentViews) {
            double[] features = extractFeatures(userId, viewedFlight);
            samples.add(new TrainingSample(features, 1));
        }

        // 负样本：先 shuffle 再 limit
        List<FlightDTO> negatives = allFlights.stream()
                .filter(f -> recentViews.stream().noneMatch(v -> v.getId().equals(f.getId())))
                .collect(Collectors.toList());

        Collections.shuffle(negatives);
        int negCount = Math.min(negatives.size(), recentViews.size() * 2);
        for (int i = 0; i < negCount; i++) {
            double[] features = extractFeatures(userId, negatives.get(i));
            samples.add(new TrainingSample(features, 0));
        }

        return samples;
    }

    private double[] extractFeatures(Long userId, FlightDTO flight) {
        double[] features = new double[6];

        // 1 & 2 城市热度
        features[0] = calculateCityPopularity(userId, flight.getDepartureCity());
        features[1] = calculateCityPopularity(userId, flight.getArrivalCity());

        // 3 剩余座位比例（防 null）
        int economy = Objects.requireNonNullElse(flight.getEconomySeats(), 0);
        int business = Objects.requireNonNullElse(flight.getBusinessSeats(), 0);
        int first = Objects.requireNonNullElse(flight.getFirstClassSeats(), 0);
        int totalAvailableSeats = economy + business + first;
        double seatRatio = Math.min(totalAvailableSeats / 300.0, 1.0);
        features[2] = seatRatio * 0.1;

        // 4 价格（防 null）
        double price = flight.getPrice() != null ? flight.getPrice() : 0.0;
        features[3] = normalize(price, 100.0, 10000.0) * 0.6;

        // 5 飞行时长（小时）
        double durationHours = calculateDurationInHours(flight.getDepartureTime(), flight.getArrivalTime());
        features[4] = normalize(durationHours, 0.0, 24.0) * 0.2;

        // 6 起飞小时偏好
        int hourOfDay = extractHourFromDepartureTime(flight.getDepartureTime());
        features[5] = normalize(hourOfDay, 0.0, 23.0) * 0.3;

        return features;
    }

    private double calculateCityPopularity(Long userId, String city) {
        List<FlightDTO> history = flightViewHistoryService.getViewHistory(userId, 20);
        if (history == null || history.isEmpty() || city == null) return 0.0;

        long count = history.stream().filter(f ->
                city.equals(f.getDepartureCity()) || city.equals(f.getArrivalCity())
        ).count();

        return count / (double) history.size();
    }

    private int extractHourFromDepartureTime(String departureTime) {
        if (departureTime == null || departureTime.isBlank()) return 12;
        try {
            String normalized = departureTime;
            if (normalized.endsWith("Z")) {
                normalized = normalized.substring(0, normalized.length() - 1) + "+00:00";
            }
            OffsetDateTime odt = OffsetDateTime.parse(normalized, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return odt.getHour();
        } catch (Exception e) {
            logger.error("Error parsing departure time '{}': {}", departureTime, e.getMessage());
            return 12;
        }
    }

    private double calculateDurationInHours(String departureTime, String arrivalTime) {
        if (departureTime == null || arrivalTime == null) return 0.0;
        try {
            String dep = departureTime.endsWith("Z") ? departureTime.substring(0, departureTime.length() - 1) + "+00:00" : departureTime;
            String arr = arrivalTime.endsWith("Z") ? arrivalTime.substring(0, arrivalTime.length() - 1) + "+00:00" : arrivalTime;

            OffsetDateTime odtDep = OffsetDateTime.parse(dep, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            OffsetDateTime odtArr = OffsetDateTime.parse(arr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            long seconds = Duration.between(odtDep.toInstant(), odtArr.toInstant()).getSeconds();
            double hours = seconds / 3600.0;
            // 若出现负时长（异常数据），返回 0
            return hours > 0 ? hours : 0.0;
        } catch (Exception e) {
            logger.error("Error calculating duration between '{}' and '{}': {}", departureTime, arrivalTime, e.getMessage());
            return 0.0;
        }
    }

    private double normalize(double value, double min, double max) {
        if (Double.isNaN(value)) return 0.0;
        if (max == min) return 0.0;
        double v = (value - min) / (max - min);
        if (Double.isInfinite(v) || Double.isNaN(v)) return 0.0;
        return Math.max(0.0, Math.min(1.0, v));
    }

    public List<FlightDTO> getPersonalizedRecommendations(Long userId, int limit) {
        List<FlightDTO> recentViews = flightViewHistoryService.getViewHistory(userId, 20);
        List<FlightDTO> upcomingFlights = flightService.getUpcomingFlights().stream()
                .map(FlightDTO::new)
                .collect(Collectors.toList());

        if (recentViews == null || recentViews.size() < 3) {
            logger.info("User {} has insufficient history, falling back to rule-based recommendations", userId);
            return flightViewHistoryService.getRecommendedFlights(userId, limit);
        }

        MLP model = getUserModel(userId, recentViews);

        List<FlightScore> scoredFlights = new ArrayList<>();
        for (FlightDTO flight : upcomingFlights) {
            boolean isRecentlyViewed = recentViews.stream()
                    .anyMatch(v -> v.getId().equals(flight.getId()));
            if (isRecentlyViewed) continue;

            double[] features = extractFeatures(userId, flight);
            double score = predict(model, features);
            scoredFlights.add(new FlightScore(flight, score));
        }

        return scoredFlights.stream()
                .sorted(Comparator.comparingDouble(FlightScore::getScore).reversed())
                .limit(limit)
                .map(fs -> {
                    // 如果 FlightDTO 上有推荐分数字段（可选），可以在这里设置：
                    // fs.getFlight().setRecommendScore(fs.getScore());
                    return fs.getFlight();
                })
                .collect(Collectors.toList());
    }

    public void clearUserModel(Long userId) {
        userModels.remove(userId);
    }

    private static class TrainingSample {
        private final double[] features;
        private final int label;

        public TrainingSample(double[] features, int label) {
            this.features = features;
            this.label = label;
        }

        public double[] getFeatures() {
            return features;
        }

        public int getLabel() {
            return label;
        }
    }

    private static class FlightScore {
        private final FlightDTO flight;
        private final double score;

        public FlightScore(FlightDTO flight, double score) {
            this.flight = flight;
            this.score = score;
        }

        public FlightDTO getFlight() {
            return flight;
        }

        public double getScore() {
            return score;
        }
    }
}
