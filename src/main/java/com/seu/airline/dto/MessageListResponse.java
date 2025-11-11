package com.seu.airline.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageListResponse {
    private List<MessageDTO> list;
    private Long total;
    private Integer page;
    private Integer pageSize;
}
