package com.ss.smartdairy;

public class Farmer {
    private String mobile;
    private String name;
    private String address;
    private String cowMilkCode;
    private String buffaloMilkCode;
    private boolean hasCow;
    private boolean hasBuffalo;

    public Farmer() {
        // Default constructor required for Firebase
    }

    public Farmer(String mobile, String name, String address, String cowMilkCode,
                  String buffaloMilkCode, boolean hasCow, boolean hasBuffalo) {
        this.mobile = mobile;
        this.name = name;
        this.address = address;
        this.cowMilkCode = cowMilkCode;
        this.buffaloMilkCode = buffaloMilkCode;
        this.hasCow = hasCow;
        this.hasBuffalo = hasBuffalo;
    }

    // Getters and setters
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCowMilkCode() { return cowMilkCode; }
    public void setCowMilkCode(String cowMilkCode) { this.cowMilkCode = cowMilkCode; }

    public String getBuffaloMilkCode() { return buffaloMilkCode; }
    public void setBuffaloMilkCode(String buffaloMilkCode) { this.buffaloMilkCode = buffaloMilkCode; }

    public boolean isHasCow() { return hasCow; }
    public void setHasCow(boolean hasCow) { this.hasCow = hasCow; }

    public boolean isHasBuffalo() { return hasBuffalo; }
    public void setHasBuffalo(boolean hasBuffalo) { this.hasBuffalo = hasBuffalo; }

    public String getAnimalsText() {
        if (hasCow && hasBuffalo) {
            return "Cow & Buffalo";
        } else if (hasCow) {
            return "Cow Only";
        } else if (hasBuffalo) {
            return "Buffalo Only";
        } else {
            return "No Animals";
        }
    }

    public String getMilkCodesText() {
        StringBuilder codes = new StringBuilder();
        if (hasCow && !cowMilkCode.isEmpty()) {
            codes.append("Cow: ").append(cowMilkCode);
        }
        if (hasBuffalo && !buffaloMilkCode.isEmpty()) {
            if (codes.length() > 0) codes.append(" | ");
            codes.append("Buffalo: ").append(buffaloMilkCode);
        }
        return codes.length() > 0 ? codes.toString() : "No Codes";
    }
}

