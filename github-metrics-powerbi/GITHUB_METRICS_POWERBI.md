# GitHub Repository Metrics - Power BI Dashboard

## Executive Summary

This Power BI project provides comprehensive GitHub repository metrics visualization for monitoring Pull Request activities, developer performance, and process compliance across IKEA repositories.

---

## Project Overview

### Target Repositories
| Repository | Path | Description |
|------------|------|-------------|
| cc-order-creation | C:\ikea\cc-order-creation\cc-order-creation | Order creation service |
| order-modification-persist | C:\ikea\order-modification-persist | Order modification persistence service |
| shopping-list-creation | C:\ikea\shopping-list-creation\shopping-list-creation | Shopping list creation service |

### Date Created
February 18, 2026

---

## Metrics Captured

### 1. Pull/Merge Requests
| Metric | Aggregate View | Drill-Down View |
|--------|---------------|-----------------|
| Total PRs | Sum across all repositories | Count per developer per repository |
| Merged PRs | Sum of all merged PRs | Individual merge counts |
| Open PRs | Current open PR count | Open PRs by developer |

**Dashboard Interaction:**
- Click on the aggregate KPI card to drill through to developer-level details
- Filter by repository using the slicer
- Time-based filtering available via date range selector

### 2. Pull/Merge Rejects
| Metric | Aggregate View | Drill-Down View |
|--------|---------------|-----------------|
| Total Rejected | Sum of rejected PRs | Rejections per developer |
| Rejection Rate | Overall percentage | Individual rejection rates |

**Key Insights:**
- Helps identify patterns in code quality issues
- Tracks which developers may need additional support
- Repository-level rejection trends

### 3. Time from PR Open to Merge
| Metric | Aggregate View | Drill-Down View |
|--------|---------------|-----------------|
| Average Time | Mean hours across all merged PRs | Individual developer averages |
| Median Time | Median merge time | Distribution per developer |
| Min/Max Time | Range indicators | Outlier identification |

**Visualization:**
- Histogram showing distribution of merge times
- Trend line over time
- Box plots by repository

### 4. Pull Requests Open Beyond Threshold (48 Hours)
| Metric | Aggregate View | Drill-Down View |
|--------|---------------|-----------------|
| Beyond 48hrs | Count of overdue PRs | List with PR details |
| Hours Overdue | Average overdue hours | Individual PR timelines |
| Critical PRs | PRs > 72 hours | Prioritized action list |

**Alert Indicators:**
- 🟢 Green: Within threshold
- 🟡 Yellow: Approaching threshold (36-48 hours)
- 🔴 Red: Beyond threshold (>48 hours)

---

## Dashboard Pages

### Page 1: Executive Summary
**Purpose:** High-level overview of all metrics

**Layout:**
```
┌──────────────────────────────────────────────────────────────────────────┐
│  📊 TOTAL PRs    │  ✅ MERGED     │  ❌ REJECTED    │  ⚠️ OVERDUE      │
│      15          │      9         │       2         │       3          │
│  [Click to       │  [Click to     │  [Click to      │  [Click to       │
│   drill-down]    │   drill-down]  │   drill-down]   │   drill-down]    │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  [PIE CHART: PR Status]          [BAR CHART: PRs by Repository]         │
│                                                                          │
├──────────────────────────────────────────────────────────────────────────┤
│  [LINE CHART: PR Trend Over Time]                                        │
│                                                                          │
├──────────────────────────────────────────────────────────────────────────┤
│  Repository          │ Total │ Merged │ Rejected │ Open │ Avg Time      │
│  ─────────────────────────────────────────────────────────────────────   │
│  cc-order-creation   │   5   │    3   │    1     │   1  │  19.0 hrs     │
│  order-modification  │   5   │    3   │    1     │   1  │  25.7 hrs     │
│  shopping-list       │   5   │    3   │    0     │   2  │  19.7 hrs     │
└──────────────────────────────────────────────────────────────────────────┘
```

### Page 2: Developer Details (Drill-Through)
**Purpose:** Individual developer performance metrics

**Access:** Right-click on any aggregate metric → Drill Through → Developer Details

**Columns Displayed:**
- Developer Name
- Total PRs Raised
- Merged PRs
- Rejected PRs  
- Open PRs
- Average Time to Merge
- PRs Beyond Threshold
- Merge Rate (%)
- Rejection Rate (%)

### Page 3: Threshold Violations
**Purpose:** Track PRs requiring immediate attention

**Features:**
- Real-time list of overdue PRs
- Direct links to GitHub PRs
- Owner/reviewer information
- Hours overdue calculation
- Color-coded severity

### Page 4: Time Analysis
**Purpose:** Deep dive into merge time patterns

**Visualizations:**
- Histogram of merge times
- Box plot by repository
- Trend analysis
- Developer comparison scatter plot

---

## Data Flow Architecture

```
┌─────────────────┐     ┌──────────────────────┐     ┌─────────────────┐
│   GitHub API    │────▶│  Python Extractor    │────▶│   CSV Files     │
│                 │     │  (github_data_       │     │   (data/)       │
│  - PRs          │     │   extractor.py)      │     │                 │
│  - Reviews      │     │                      │     │  - pull_        │
│  - Users        │     │  Runs daily via      │     │    requests.csv │
│                 │     │  Task Scheduler      │     │  - developer_   │
└─────────────────┘     └──────────────────────┘     │    aggregates   │
                                                      │  - repository_  │
                                                      │    aggregates   │
                                                      │  - threshold_   │
                                                      │    violations   │
                                                      └────────┬────────┘
                                                               │
                                                               ▼
                                                      ┌─────────────────┐
                                                      │   Power BI      │
                                                      │   Desktop       │
                                                      │                 │
                                                      │  - Transform    │
                                                      │  - Model        │
                                                      │  - Visualize    │
                                                      └────────┬────────┘
                                                               │
                                                               ▼
                                                      ┌─────────────────┐
                                                      │   Power BI      │
                                                      │   Service       │
                                                      │                 │
                                                      │  - Publish      │
                                                      │  - Share        │
                                                      │  - Schedule     │
                                                      └─────────────────┘
```

---

## Project Structure

```
github-metrics-powerbi/
├── README.md                              # Main documentation
├── GITHUB_METRICS_POWERBI.md             # This file
├── scripts/
│   ├── config.json                       # Repository configuration
│   ├── github_data_extractor.py          # Data extraction script
│   └── requirements.txt                  # Python dependencies
├── data/
│   ├── pull_requests.csv                 # Detailed PR data
│   ├── developer_aggregates.csv          # Per-developer metrics
│   ├── repository_aggregates.csv         # Per-repo metrics
│   ├── overall_aggregates.csv            # Overall summary
│   └── threshold_violations.csv          # Overdue PRs
├── powerbi/
│   ├── dashboard_config.json             # Dashboard configuration
│   ├── measures.md                       # DAX measures documentation
│   └── power_query_scripts.m             # Power Query M scripts
└── docs/
    └── setup_guide.md                    # Detailed setup instructions
```

---

## Quick Start Guide

### Step 1: Configure GitHub Token
```json
// Edit scripts/config.json
{
    "github": {
        "access_token": "YOUR_GITHUB_TOKEN_HERE"
    }
}
```

### Step 2: Run Data Extraction
```powershell
cd C:\ikea\order-modification-persist\github-metrics-powerbi\scripts
pip install -r requirements.txt
python github_data_extractor.py
```

### Step 3: Import to Power BI
1. Open Power BI Desktop
2. Get Data → Text/CSV
3. Import all CSV files from the `data/` folder
4. Create relationships between tables
5. Add DAX measures from `powerbi/measures.md`
6. Build visualizations

### Step 4: Publish
1. Click Publish in Power BI Desktop
2. Select your workspace
3. Configure scheduled refresh

---

## Key DAX Measures

### Total PRs
```dax
Total PRs = COUNTROWS('pull_requests')
```

### Rejection Rate
```dax
Rejection Rate = 
DIVIDE(
    CALCULATE(COUNTROWS('pull_requests'), 'pull_requests'[is_rejected] = TRUE),
    COUNTROWS('pull_requests'),
    0
) * 100
```

### PRs Beyond Threshold
```dax
PRs Beyond Threshold = 
CALCULATE(
    COUNTROWS('pull_requests'),
    'pull_requests'[is_beyond_threshold] = TRUE
)
```

### Average Time to Merge
```dax
Avg Time to Merge = AVERAGE('pull_requests'[time_to_merge_hours])
```

---

## Sample Data Summary

Based on the sample data included:

| Metric | Value |
|--------|-------|
| Total Repositories | 3 |
| Total PRs | 15 |
| Merged PRs | 9 |
| Rejected PRs | 2 |
| Open PRs | 4 |
| Avg Time to Merge | 21.44 hours |
| PRs Beyond 48hrs | 3 |
| Unique Contributors | 5 |
| Overall Merge Rate | 60% |
| Overall Rejection Rate | 13.33% |

---

## Drill-Through Behavior

When users click on an aggregate KPI card:

1. **Total PRs Card** → Drills to Developer Details page showing all developers and their PR counts
2. **Merged PRs Card** → Drills to Developer Details filtered to merged PRs
3. **Rejected PRs Card** → Drills to Developer Details filtered to rejected PRs
4. **Beyond Threshold Card** → Drills to Threshold Violations page with overdue PR list

### Enabling Drill-Through
1. On the target page, add fields to "Drill through" bucket in Visualizations pane
2. Enable "Back button" for easy navigation
3. Configure visual interactions for cross-filtering

---

## Refresh Schedule

### Recommended Schedule
- **Production**: Daily at 6:00 AM
- **Development**: On-demand

### Automation Script (Windows Task Scheduler)
```powershell
# Save as refresh_github_metrics.ps1
cd C:\ikea\order-modification-persist\github-metrics-powerbi\scripts
python github_data_extractor.py
```

---

## Contact Information

For questions or issues with this dashboard, contact the Development Team.

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-02-18 | Initial release |


