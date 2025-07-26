package com.ss.smartdairy;

public class RateChartModel {
    private String fat;
    private double rate;

    public RateChartModel() {}

    public RateChartModel(String fat, double rate) {
        this.fat = fat;
        this.rate = rate;
    }

    public String getFat() {
        return fat;
    }

    public double getRate() {
        return rate;
    }
}
