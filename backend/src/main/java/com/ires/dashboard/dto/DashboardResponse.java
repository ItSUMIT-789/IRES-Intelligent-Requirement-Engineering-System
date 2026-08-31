package com.ires.dashboard.dto;

import java.util.List;

public record DashboardResponse(String role, List<DashboardMetric> metrics) {
}
