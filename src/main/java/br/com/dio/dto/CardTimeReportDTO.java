package br.com.dio.dto;

import lombok.Data;

import java.time.Duration;
import java.util.Map;

@Data
public class CardTimeReportDTO {
    private String cardTitle;
    private Map<String, Duration> timePerColumn;
    private Duration totalTime;


    public CardTimeReportDTO(String title, Map<String, Duration> timePerColumn, Duration totalTime) {
    }
}
