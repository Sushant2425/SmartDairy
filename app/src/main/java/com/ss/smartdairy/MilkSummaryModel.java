package com.ss.smartdairy;

import java.util.HashMap;
import java.util.Map;

public class MilkSummaryModel {
    private String date;
    private String cowMilk;
    private String buffaloMilk;
    private String totalMilk;
    private boolean hasData;

    // Session-wise data storage
    private Map<String, SessionData> sessionData;

    public MilkSummaryModel(String date, String cowMilk, String buffaloMilk, String totalMilk) {
        this.date = date;
        this.cowMilk = cowMilk;
        this.buffaloMilk = buffaloMilk;
        this.totalMilk = totalMilk;
        this.hasData = true;
        this.sessionData = new HashMap<>();
    }

    public MilkSummaryModel(String date) {
        this.date = date;
        this.cowMilk = "0";
        this.buffaloMilk = "0";
        this.totalMilk = "0";
        this.hasData = false;
        this.sessionData = new HashMap<>();
    }

    // Constructor with session data
    public MilkSummaryModel(String date, Map<String, SessionData> sessionData) {
        this.date = date;
        this.sessionData = sessionData;
        this.hasData = !sessionData.isEmpty();
        calculateTotalFromSessions();
    }

    // Inner class for session data
    public static class SessionData {
        private double cowMilk;
        private double buffaloMilk;

        public SessionData(double cowMilk, double buffaloMilk) {
            this.cowMilk = cowMilk;
            this.buffaloMilk = buffaloMilk;
        }

        public double getCowMilk() { return cowMilk; }
        public double getBuffaloMilk() { return buffaloMilk; }
        public double getTotalMilk() { return cowMilk + buffaloMilk; }
    }

    private void calculateTotalFromSessions() {
        double totalCow = 0.0;
        double totalBuffalo = 0.0;

        for (SessionData session : sessionData.values()) {
            totalCow += session.getCowMilk();
            totalBuffalo += session.getBuffaloMilk();
        }

        this.cowMilk = String.format("%.1f", totalCow);
        this.buffaloMilk = String.format("%.1f", totalBuffalo);
        this.totalMilk = String.format("%.1f", totalCow + totalBuffalo);
    }

    // Get session-specific data
    public SessionData getSessionData(String session) {
        return sessionData.get(session);
    }

    public void addSessionData(String session, double cowMilk, double buffaloMilk) {
        sessionData.put(session, new SessionData(cowMilk, buffaloMilk));
        calculateTotalFromSessions();
        this.hasData = true;
    }

    // Getters
    public String getDate() { return date; }
    public String getCowMilk() { return cowMilk; }
    public String getBuffaloMilk() { return buffaloMilk; }
    public String getTotalMilk() { return totalMilk; }
    public boolean hasData() { return hasData; }
    public Map<String, SessionData> getAllSessionData() { return sessionData; }

    // Setters
    public void setCowMilk(String cowMilk) { this.cowMilk = cowMilk; }
    public void setBuffaloMilk(String buffaloMilk) { this.buffaloMilk = buffaloMilk; }
    public void setTotalMilk(String totalMilk) { this.totalMilk = totalMilk; }
    public void setHasData(boolean hasData) { this.hasData = hasData; }
}
