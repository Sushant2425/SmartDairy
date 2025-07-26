package com.ss.smartdairy;

public class FarmerReportModel {
    private String name;
    private String mobile;
    private String address;
    private boolean hasCow;
    private boolean hasBuffalo;
    private String cowMilkCode;
    private String buffaloMilkCode;

    // ✅ Constructor with 7 fields
    public FarmerReportModel(String name, String mobile, String address, boolean hasCow, boolean hasBuffalo, String cowMilkCode, String buffaloMilkCode) {
        this.name = name;
        this.mobile = mobile;
        this.address = address;
        this.hasCow = hasCow;
        this.hasBuffalo = hasBuffalo;
        this.cowMilkCode = cowMilkCode;
        this.buffaloMilkCode = buffaloMilkCode;
    }

    // ✅ Getters
    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }

    public String getAddress() {
        return address;
    }

    public boolean isHasCow() {
        return hasCow;
    }

    public boolean isHasBuffalo() {
        return hasBuffalo;
    }

    public String getCowMilkCode() {
        return cowMilkCode;
    }

    public String getBuffaloMilkCode() {
        return buffaloMilkCode;
    }
}
