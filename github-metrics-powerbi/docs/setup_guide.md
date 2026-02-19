# Power BI Setup Guide for GitHub Metrics Dashboard

## Prerequisites

Before setting up the Power BI dashboard, ensure you have:

1. **Power BI Desktop** (Latest version recommended)
2. **Python 3.8+** with required packages installed
3. **GitHub Personal Access Token** with `repo` scope
4. **Network access** to GitHub API

---

## Step 1: Configure GitHub Access

### 1.1 Create a Personal Access Token

1. Go to [GitHub Settings > Developer Settings > Personal Access Tokens](https://github.com/settings/tokens)
2. Click "Generate new token (classic)"
3. Select the following scopes:
   - `repo` (Full control of private repositories)
   - `read:org` (Read organization membership)
4. Copy the generated token

### 1.2 Update Configuration

Edit the `scripts/config.json` file:

```json
{
    "github": {
        "base_url": "https://api.github.com",
        "access_token": "ghp_YOUR_TOKEN_HERE",
        "organization": "ikea"
    },
    "repositories": [
        {
            "name": "cc-order-creation",
            "owner": "ikea",
            "branch": "main"
        },
        {
            "name": "order-modification-persist",
            "owner": "ikea",
            "branch": "main"
        },
        {
            "name": "shopping-list-creation",
            "owner": "ikea",
            "branch": "main"
        }
    ]
}
```

---

## Step 2: Extract Data

### 2.1 Install Python Dependencies

```powershell
cd github-metrics-powerbi\scripts
pip install -r requirements.txt
```

### 2.2 Run the Extractor

```powershell
python github_data_extractor.py
```

This will generate the following CSV files in the `data/` folder:
- `pull_requests.csv` - All PR details
- `developer_aggregates.csv` - Developer-level metrics
- `repository_aggregates.csv` - Repository-level metrics
- `overall_aggregates.csv` - Overall aggregates
- `threshold_violations.csv` - PRs beyond 48-hour threshold

---

## Step 3: Set Up Power BI

### 3.1 Create New Report

1. Open Power BI Desktop
2. Click "Get Data" > "Text/CSV"
3. Import all CSV files from the `data/` folder

### 3.2 Configure Relationships

Set up the following relationships in Model view:

| From Table | From Column | To Table | To Column | Cardinality |
|------------|-------------|----------|-----------|-------------|
| pull_requests | repository | repository_aggregates | repository | Many-to-One |
| pull_requests | author | developer_aggregates | developer | Many-to-One |
| threshold_violations | pr_number | pull_requests | pr_number | One-to-One |

### 3.3 Create Date Table

Add a Date table for time intelligence:

```dax
DateTable = 
ADDCOLUMNS(
    CALENDAR(DATE(2024, 1, 1), DATE(2026, 12, 31)),
    "Year", YEAR([Date]),
    "Month", MONTH([Date]),
    "MonthName", FORMAT([Date], "MMMM"),
    "Quarter", "Q" & FORMAT([Date], "Q"),
    "Week", WEEKNUM([Date])
)
```

---

## Step 4: Create Dashboard Pages

### Page 1: Executive Summary (Aggregate View)

**Layout:**
```
┌─────────────────────────────────────────────────────────────────┐
│  Total PRs    │  Merged PRs   │  Rejected PRs  │  Beyond 48hrs │
│    [KPI]      │    [KPI]      │    [KPI]       │    [KPI]      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  [Donut Chart: PR Status Distribution]   [Bar: PRs by Repo]    │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  [Line Chart: PR Trend Over Time]                               │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│  [Table: Repository Summary with click-through]                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**KPI Card Configuration:**
- Click Action: Drill-through to "Developer Details" page

### Page 2: Developer Details (Drill-through)

**Drill-through fields:** `repository`, `author`

**Layout:**
```
┌─────────────────────────────────────────────────────────────────┐
│  Back Button  │  Repository: [Selected]  │  Developer: [All]   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  [Table: Developer Metrics]                                     │
│  Columns: Developer, Total PRs, Merged, Rejected, Avg Time,    │
│           Beyond Threshold                                      │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  [Bar Chart: Top 10 Contributors]  [Scatter: Merge Time vs PRs]│
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Page 3: Threshold Violations

**Layout:**
```
┌─────────────────────────────────────────────────────────────────┐
│  PRs Beyond 48 Hours: [Count]  │  Critical (>72hrs): [Count]   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  [Table: Overdue PRs]                                           │
│  Columns: PR#, Title, Author, Hours Open, Repository, URL      │
│                                                                 │
│  [Conditional formatting: Red if >72hrs, Yellow if >48hrs]     │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  [Treemap: Violations by Repository]                            │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Page 4: Time Analysis

**Layout:**
```
┌─────────────────────────────────────────────────────────────────┐
│  Avg Time to Merge  │  Median Time  │  Min Time  │  Max Time   │
│      [KPI]          │    [KPI]      │   [KPI]    │   [KPI]     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  [Histogram: Distribution of Merge Times]                       │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  [Box Plot: Merge Time by Repository]                           │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  [Table: Developer Merge Time Rankings]                         │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Step 5: Configure Interactions

### 5.1 Enable Cross-Filtering

1. Go to Format > Edit Interactions
2. Configure KPI cards to filter other visuals when clicked
3. Enable drill-through on tables

### 5.2 Configure Drill-Through

1. On the Developer Details page, add `repository` and `author` to the drill-through bucket
2. Add a back button for navigation

### 5.3 Configure Tooltips

Create a custom tooltip page showing:
- PR count
- Average merge time
- Last PR date

---

## Step 6: Schedule Data Refresh

### Option A: Manual Refresh

Run the Python extractor manually and refresh Power BI:
```powershell
cd scripts
python github_data_extractor.py
```

### Option B: Automated Refresh (Windows Task Scheduler)

1. Create a batch file `refresh_data.bat`:
```batch
@echo off
cd /d "C:\path\to\github-metrics-powerbi\scripts"
python github_data_extractor.py
```

2. Create a scheduled task to run daily

### Option C: Power Automate Integration

Use Power Automate to:
1. Trigger the Python script
2. Refresh the Power BI dataset

---

## Step 7: Publish to Power BI Service

1. Save the .pbix file
2. Click "Publish" in Power BI Desktop
3. Select your workspace
4. Configure scheduled refresh in the Power BI Service
5. Set up Row-Level Security if needed

---

## Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| API rate limit exceeded | Reduce extraction frequency or use a GitHub App token |
| Empty data | Verify repository names and access permissions |
| Slow performance | Filter to recent PRs only (e.g., last 90 days) |
| Missing columns | Re-run the extractor to get the latest schema |

### Error Codes

| Code | Meaning |
|------|---------|
| 401 | Invalid or expired token |
| 403 | Rate limit exceeded or insufficient permissions |
| 404 | Repository not found |

---

## Contact & Support

For issues with this dashboard, contact the development team.

