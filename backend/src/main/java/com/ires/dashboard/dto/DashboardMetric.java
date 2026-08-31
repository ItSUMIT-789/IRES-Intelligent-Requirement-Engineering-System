package com.ires.dashboard.dto;

public record DashboardMetric(
        String key,
        String label,
        Long value,
        boolean available,
        String emptyMessage
) {
    public static DashboardMetric count(String key, String label, long value, String emptyMessage) {
        return new DashboardMetric(key, label, value, true, emptyMessage);
    }

    public static DashboardMetric unavailable(String key, String label) {
        return new DashboardMetric(key, label, null, false, "Not available in the current workflow model.");
    }
}
