package com.seu.airline.service;

import com.seu.airline.dto.FlightDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import smile.classification.MLP;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

import java.util.*;
import java.util.stream.Collectors;

import smile.base.mlp.Layer;
import smile.base.mlp.OutputFunction;
import smile.base.mlp.LayerBuilder; // 如果你还需要用到 builder 类型
import smile.classification.MLP;

@Service
public class FlightRecommenderService {

    private static final Logger logger = LoggerFactory.getLogger(FlightRecommenderService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightViewHistoryService flightViewHistoryService;

    // 神经网络相关参数
    private static final int INPUT_LAYER_SIZE = 6;    // 输入特征数量
    private static final int HIDDEN_LAYER_SIZE = 10;  // 隐藏层神经元数量
    private static final int OUTPUT_LAYER_SIZE = 1;   // 输出层大小（推荐分数）
    private static final double LEARNING_RATE = 0.01; // 学习率
    private static final int EPOCHS = 100;            // 训练轮数

    // 存储用户的神经网络模型
    private final Map<Long, MLP> userModels = new HashMap<>();
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

    // 使用 SMILE 的 Layer 工厂：hidden 层用 rectifier（ReLU），输出层用 mle + SOFTMAX
    // 输出层设置为 2 个神经元（binary classification: 0 / 1）
    MLP model = new MLP(
            INPUT_LAYER_SIZE,
            Layer.rectifier(HIDDEN_LAYER_SIZE),          // hidden layer (ReLU / rectifier)
            Layer.mle(2, OutputFunction.SOFTMAX)        // output layer: 2 neurons + softmax (likelihood)
    );

    // Online training: 逐样本调用 update
    for (int epoch = 0; epoch < EPOCHS; epoch++) {
        for (int i = 0; i < n; i++) {
            model.update(x[i], y[i]);
        }
    }

    userModels.put(userId, model);
    return model;
}

    private double predict(MLP model, double[] features) {
        if (model == null) return 0.5; // fallback

        try {
            double[] posterior = new double[2]; // 两类概率
            model.predict(features, posterior); // fills posterior[]
            // 返回属于“感兴趣”(label==1) 的概率
            return posterior.length > 1 ? posterior[1] : posterior[0];
        } catch (Exception e) {
            logger.error("Prediction error: {}", e.getMessage());
            return 0.5;
        }
    }

    /**
     * 生成训练样本
     */
    private List<TrainingSample> generateTrainingSamples(Long userId, List<FlightDTO> recentViews) {
        List<TrainingSample> samples = new ArrayList<>();
        List<FlightDTO> allFlights = flightService.getUpcomingFlights().stream()
                .map(FlightDTO::new)
                .collect(Collectors.toList());

        // 为用户看过的航班生成正样本（标签为1）
        for (FlightDTO viewedFlight : recentViews) {
            double[] features = extractFeatures(userId, viewedFlight);
            samples.add(new TrainingSample(features, 1));
        }

        // 生成负样本（标签为0）- 随机选择一些用户没看过的航班
        List<FlightDTO> negativeSamples = allFlights.stream()
                .filter(f -> !recentViews.stream().anyMatch(v -> v.getId().equals(f.getId())))
                .limit(recentViews.size() * 2)  // 负样本数量是正样本的2倍
                .collect(Collectors.toList());

        for (FlightDTO flight : negativeSamples) {
            double[] features = extractFeatures(userId, flight);
            samples.add(new TrainingSample(features, 0));
        }

        return samples;
    }

    /**
     * 提取航班特征用于规则推荐
     */
    private double[] extractFeatures(Long userId, FlightDTO flight) {
        double[] features = new double[6];
        
        // 1. 出发城市热度 - 基于用户历史
        features[0] = calculateCityPopularity(userId, flight.getDepartureCity());
        
        // 2. 到达城市热度 - 基于用户历史
        features[1] = calculateCityPopularity(userId, flight.getArrivalCity());
        
        // 3. 剩余座位比例（归一化）
        int totalAvailableSeats = flight.getEconomySeats() + flight.getBusinessSeats() + flight.getFirstClassSeats();
        // 假设最大座位数为300，进行归一化
        double seatRatio = Math.min(totalAvailableSeats / 300.0, 1.0);
        features[2] = seatRatio;
        
        // 4. 价格（归一化，假设价格范围在100-10000之间）
        features[3] = normalize(flight.getPrice(), 100, 10000);
        
        // 5. 飞行时长（归一化，假设最长飞行时间为24小时）
        double durationHours = calculateDurationInHours(flight.getDepartureTime(), flight.getArrivalTime());
        features[4] = normalize(durationHours, 0, 24);
        
        // 6. 起飞时间偏好（归一化的小时数）
        int hourOfDay = extractHourFromDepartureTime(flight.getDepartureTime());
        features[5] = normalize(hourOfDay, 0, 23);
        
        return features;
    }

    /**
     * 计算城市对用户的热门程度
     */
    private double calculateCityPopularity(Long userId, String city) {
        List<FlightDTO> history = flightViewHistoryService.getViewHistory(userId, 20);
        if (history.isEmpty()) return 0.0;
        
        long count = history.stream().filter(f -> 
            city.equals(f.getDepartureCity()) || city.equals(f.getArrivalCity())
        ).count();
        
        return count / (double) history.size();
    }

    /**
     * 从起飞时间字符串中提取小时数
     */
    private int extractHourFromDepartureTime(String departureTime) {
        try {
            // 解析ISO-8601格式的时间字符串
            String normalizedTime = departureTime;
            if (normalizedTime.endsWith("Z")) {
                normalizedTime = normalizedTime.substring(0, normalizedTime.length() - 1) + "+00:00";
            }
            LocalDateTime dateTime = LocalDateTime.parse(normalizedTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return dateTime.getHour();
        } catch (Exception e) {
            logger.error("Error parsing departure time: {}", e.getMessage());
            return 12; // 默认中午12点
        }
    }

    /**
     * 计算飞行时长（小时）
     */
    private double calculateDurationInHours(String departureTime, String arrivalTime) {
        try {
            // 解析ISO-8601格式的时间字符串
            String normalizedDepTime = departureTime;
            String normalizedArrTime = arrivalTime;
            
            if (normalizedDepTime.endsWith("Z")) {
                normalizedDepTime = normalizedDepTime.substring(0, normalizedDepTime.length() - 1) + "+00:00";
            }
            if (normalizedArrTime.endsWith("Z")) {
                normalizedArrTime = normalizedArrTime.substring(0, normalizedArrTime.length() - 1) + "+00:00";
            }
            
            LocalDateTime dep = LocalDateTime.parse(normalizedDepTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            LocalDateTime arr = LocalDateTime.parse(normalizedArrTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            
            return Duration.between(dep, arr).toHours();
        } catch (Exception e) {
            logger.error("Error calculating duration: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 归一化数值
     */
    private double normalize(double value, double min, double max) {
        return (value - min) / (max - min);
    }

    /**
     * 获取个性化航班推荐
     */
    public List<FlightDTO> getPersonalizedRecommendations(Long userId, int limit) {
        // 获取用户历史记录
        List<FlightDTO> recentViews = flightViewHistoryService.getViewHistory(userId, 20);
        List<FlightDTO> upcomingFlights = flightService.getUpcomingFlights().stream()
                .map(FlightDTO::new)
                .collect(Collectors.toList());

        // 如果用户历史记录太少，返回基于规则的推荐
        if (recentViews.size() < 3) {
            logger.info("User {} has insufficient history, falling back to rule-based recommendations", userId);
            return flightViewHistoryService.getRecommendedFlights(userId, limit);
        }

        // 获取或训练用户的神经网络模型
        MLP model = getUserModel(userId, recentViews);

        // 对所有即将起飞的航班进行评分
        List<FlightScore> scoredFlights = new ArrayList<>();
        for (FlightDTO flight : upcomingFlights) {
            // 跳过用户最近查看过的航班
            boolean isRecentlyViewed = recentViews.stream()
                    .anyMatch(v -> v.getId().equals(flight.getId()));
            if (isRecentlyViewed) continue;

            // 提取特征并计算推荐分数
            double[] features = extractFeatures(userId, flight);
            double score = predict(model, features);
            scoredFlights.add(new FlightScore(flight, score));
        }

        // 按分数排序并返回前N个推荐
        return scoredFlights.stream()
                .sorted(Comparator.comparingDouble(FlightScore::getScore).reversed())
                .limit(limit)
                .map(FlightScore::getFlight)
                .collect(Collectors.toList());
    }

    /**
     * 清除用户模型（用于模型重置或缓存管理）
     */
    public void clearUserModel(Long userId) {
        userModels.remove(userId);
    }

    // 内部类：训练样本
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

    // 内部类：带分数的航班
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