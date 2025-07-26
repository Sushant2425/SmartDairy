package com.ss.smartdairy;

public class RateModel {
    private String fat;
    private double rate;

    // 🔹 Required empty constructor for Firebase
    public RateModel() {}

    // 🔹 Parameterized constructor for creating new objects
    public RateModel(String fat, double rate) {
        this.fat = fat;
        this.rate = rate;
    }

    // 🔹 Getter for FAT value
    public String getFat() {
        return fat;
    }

    // 🔹 Setter for FAT value
    public void setFat(String fat) {
        this.fat = fat;
    }

    // 🔹 Getter for rate
    public double getRate() {
        return rate;
    }

    // 🔹 Setter for rate
    public void setRate(double rate) {
        this.rate = rate;
    }
}
