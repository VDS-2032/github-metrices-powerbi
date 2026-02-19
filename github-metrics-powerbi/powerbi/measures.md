# Power BI DAX Measures for GitHub Metrics Dashboard

This document contains all the DAX measures required for the Power BI dashboard.

## Table of Contents
1. [Aggregate Measures](#aggregate-measures)
2. [Pull Request Measures](#pull-request-measures)
3. [Rejection Measures](#rejection-measures)
4. [Time Metrics](#time-metrics)
5. [Threshold Measures](#threshold-measures)
6. [Developer-Level Measures](#developer-level-measures)
7. [Drill-Through Measures](#drill-through-measures)

---

## Aggregate Measures

### Total PR Count (All Repositories)
```dax
Total PRs = 
COUNTROWS('pull_requests')
```

### Total Merged PRs
```dax
Total Merged = 
CALCULATE(
    COUNTROWS('pull_requests'),
    'pull_requests'[is_merged] = TRUE
)
```

### Total Open PRs
```dax
Total Open = 
CALCULATE(
    COUNTROWS('pull_requests'),
    'pull_requests'[is_open] = TRUE
)
```

### Total Repositories
```dax
Total Repositories = 
DISTINCTCOUNT('pull_requests'[repository])
```

### Total Contributors
```dax
Total Contributors = 
DISTINCTCOUNT('pull_requests'[author])
```

---

## Pull Request Measures

### PR Count by Status
```dax
PR Count by Status = 
SWITCH(
    SELECTEDVALUE('PR Status'[Status]),
    "Merged", [Total Merged],
    "Open", [Total Open],
    "Rejected", [Total Rejected],
    [Total PRs]
)
```

### PRs This Month
```dax
PRs This Month = 
CALCULATE(
    COUNTROWS('pull_requests'),
    MONTH('pull_requests'[created_at]) = MONTH(TODAY()) &&
    YEAR('pull_requests'[created_at]) = YEAR(TODAY())
)
```

### PRs Last 7 Days
```dax
PRs Last 7 Days = 
CALCULATE(
    COUNTROWS('pull_requests'),
    'pull_requests'[created_at] >= TODAY() - 7
)
```

---

## Rejection Measures

### Total Rejected PRs
```dax
Total Rejected = 
CALCULATE(
    COUNTROWS('pull_requests'),
    'pull_requests'[is_rejected] = TRUE
)
```

### Rejection Rate (%)
```dax
Rejection Rate = 
DIVIDE(
    [Total Rejected],
    [Total PRs],
    0
) * 100
```

### Rejections by Developer
```dax
Rejections by Developer = 
CALCULATE(
    COUNTROWS('pull_requests'),
    'pull_requests'[is_rejected] = TRUE,
    ALLEXCEPT('pull_requests', 'pull_requests'[author])
)
```

### Rejections by Repository
```dax
Rejections by Repository = 
CALCULATE(
    COUNTROWS('pull_requests'),
    'pull_requests'[is_rejected] = TRUE,
    ALLEXCEPT('pull_requests', 'pull_requests'[repository])
)
```

---

## Time Metrics

### Average Time to Merge (Hours)
```dax
Avg Time to Merge (Hours) = 
AVERAGE('pull_requests'[time_to_merge_hours])
```

### Average Time to Merge (Days)
```dax
Avg Time to Merge (Days) = 
DIVIDE(
    [Avg Time to Merge (Hours)],
    24,
    0
)
```

### Median Time to Merge
```dax
Median Time to Merge = 
MEDIAN('pull_requests'[time_to_merge_hours])
```

### Min Time to Merge
```dax
Min Time to Merge = 
MIN('pull_requests'[time_to_merge_hours])
```

### Max Time to Merge
```dax
Max Time to Merge = 
MAX('pull_requests'[time_to_merge_hours])
```

### Time to Merge by Developer
```dax
Avg Time to Merge by Developer = 
CALCULATE(
    AVERAGE('pull_requests'[time_to_merge_hours]),
    ALLEXCEPT('pull_requests', 'pull_requests'[author])
)
```

---

## Threshold Measures

### PRs Beyond 48 Hours (Threshold)
```dax
PRs Beyond Threshold = 
CALCULATE(
    COUNTROWS('pull_requests'),
    'pull_requests'[is_beyond_threshold] = TRUE
)
```

### Threshold Violation Rate (%)
```dax
Threshold Violation Rate = 
DIVIDE(
    [PRs Beyond Threshold],
    [Total Open],
    0
) * 100
```

### Average Hours Overdue
```dax
Avg Hours Overdue = 
CALCULATE(
    AVERAGE('pull_requests'[hours_open]) - 48,
    'pull_requests'[is_beyond_threshold] = TRUE
)
```

### Critical PRs (>72 Hours)
```dax
Critical PRs = 
CALCULATE(
    COUNTROWS('pull_requests'),
    'pull_requests'[hours_open] > 72,
    'pull_requests'[is_open] = TRUE
)
```

---

## Developer-Level Measures

### Developer PR Count
```dax
Developer PR Count = 
CALCULATE(
    COUNTROWS('pull_requests'),
    ALLEXCEPT('pull_requests', 'pull_requests'[author])
)
```

### Developer Merge Rate (%)
```dax
Developer Merge Rate = 
VAR TotalPRs = [Developer PR Count]
VAR MergedPRs = 
    CALCULATE(
        COUNTROWS('pull_requests'),
        'pull_requests'[is_merged] = TRUE,
        ALLEXCEPT('pull_requests', 'pull_requests'[author])
    )
RETURN
    DIVIDE(MergedPRs, TotalPRs, 0) * 100
```

### Developer Average Merge Time
```dax
Developer Avg Merge Time = 
CALCULATE(
    AVERAGE('pull_requests'[time_to_merge_hours]),
    ALLEXCEPT('pull_requests', 'pull_requests'[author])
)
```

### Developer Threshold Violations
```dax
Developer Threshold Violations = 
CALCULATE(
    COUNTROWS('pull_requests'),
    'pull_requests'[is_beyond_threshold] = TRUE,
    ALLEXCEPT('pull_requests', 'pull_requests'[author])
)
```

### Top Contributor (by PR Count)
```dax
Top Contributor = 
TOPN(
    1,
    SUMMARIZE(
        'pull_requests',
        'pull_requests'[author],
        "PRCount", COUNTROWS('pull_requests')
    ),
    [PRCount],
    DESC
)
```

---

## Drill-Through Measures

### Selected Repository Details
```dax
Selected Repository PRs = 
IF(
    ISFILTERED('pull_requests'[repository]),
    COUNTROWS('pull_requests'),
    BLANK()
)
```

### Selected Developer Details
```dax
Selected Developer PRs = 
IF(
    ISFILTERED('pull_requests'[author]),
    COUNTROWS('pull_requests'),
    BLANK()
)
```

---

## Conditional Formatting Measures

### Merge Time Status Color
```dax
Merge Time Color = 
SWITCH(
    TRUE(),
    [Avg Time to Merge (Hours)] <= 24, "#4CAF50",  // Green - Excellent
    [Avg Time to Merge (Hours)] <= 48, "#FFC107",  // Yellow - Warning
    "#F44336"  // Red - Critical
)
```

### Threshold Status Icon
```dax
Threshold Icon = 
IF(
    [PRs Beyond Threshold] > 0,
    "⚠️",
    "✅"
)
```

### Health Score
```dax
Repository Health Score = 
VAR MergeRateScore = [Total Merged] / [Total PRs] * 40
VAR TimeScore = IF([Avg Time to Merge (Hours)] <= 48, 30, 30 - ([Avg Time to Merge (Hours)] - 48) / 10)
VAR ThresholdScore = 30 - ([PRs Beyond Threshold] * 5)
RETURN
    MIN(100, MAX(0, MergeRateScore + TimeScore + ThresholdScore))
```

---

## Usage Instructions

### Setting Up Drill-Through
1. Create a "PR Details" page for drill-through
2. Add `repository` and `author` fields to the drill-through filters
3. Users can right-click on aggregate numbers to drill through to details

### Setting Up Click-to-Filter
1. Enable cross-filtering between visuals
2. Configure visual interactions to filter other visuals when clicked
3. The aggregate KPI cards will filter the detail tables when clicked

### Recommended Visual Layout
1. **Top Row**: KPI Cards showing aggregates (Total PRs, Merged, Rejected, Beyond Threshold)
2. **Middle Row**: Charts (Time to Merge trends, PR Distribution by Repository)
3. **Bottom Row**: Detail tables (Developer metrics, Threshold violations)

