---
id: 11
title: Analytics dashboard
status: Done
priority: high
labels:
  - backend
  - frontend
created: 2026-02-13 00:00
type: feature
dependencies:
  - task-3
  - task-5
  - task-6
  - task-7
---

## Description

Implement analytics dashboard showing personal growth metrics, mood trends, activity completion rates, and goal progress. Data is aggregated daily into growth metrics for efficient querying.

## Plan

1. Implement `GrowthMetric` JPA entity
2. Create metrics computation service for daily aggregation
3. Implement `AnalyticsController` with dashboard data endpoints
4. Build frontend DashboardView with Chart.js visualizations
5. Create charts: mood trend line, activity completion bar, goal progress, weekly summary
6. Create Pinia store for analytics state
7. Write tests for aggregation logic and endpoints

## Acceptance Criteria

- [ ] Dashboard shows mood trends over time (line chart)
- [ ] Activity completion rates displayed (bar chart)
- [ ] Goal progress shown with progress indicators
- [ ] Date range filter for all charts
- [ ] Metrics computed daily via scheduled job
- [ ] All operations tested
