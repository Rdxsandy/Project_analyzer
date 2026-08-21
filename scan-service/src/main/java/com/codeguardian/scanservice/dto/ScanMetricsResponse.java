package com.codeguardian.scanservice.dto;

public class ScanMetricsResponse {

    private int critical;
    private int high;
    private int medium;
    private int low;
    private int total;
    private int qualityScore;

    public ScanMetricsResponse(
            int critical,
            int high,
            int medium,
            int low,
            int qualityScore
    ) {
        this.critical = critical;
        this.high = high;
        this.medium = medium;
        this.low = low;
        this.total = critical + high + medium + low;
        this.qualityScore = qualityScore;
    }

    public int getCritical() {
        return critical;
    }

    public int getHigh() {
        return high;
    }

    public int getMedium() {
        return medium;
    }

    public int getLow() {
        return low;
    }

    public int getTotal() {
        return total;
    }

    public int getQualityScore() {
        return qualityScore;
    }
}
