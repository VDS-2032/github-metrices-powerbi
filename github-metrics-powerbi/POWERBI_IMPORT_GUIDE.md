# Power BI Desktop Import Guide
## GitHub Repository Metrics Dashboard

This guide provides step-by-step instructions to create the Power BI dashboard from scratch.

---

## Prerequisites

- Power BI Desktop (latest version)
- Data files in `C:\ikea\order-modification-persist\github-metrics-powerbi\data\`
- Approximately 30 minutes for setup

---

## Part 1: Import Data Tables

### Step 1.1: Open Power BI Desktop
1. Launch Power BI Desktop
2. Click **File** > **New** to create a new report

### Step 1.2: Import pull_requests.csv
1. Click **Home** > **Get Data** > **Text/CSV**
2. Navigate to: `C:\ikea\order-modification-persist\github-metrics-powerbi\data\`
3. Select `pull_requests.csv` and click **Open**
4. In the preview dialog, click **Transform Data**
5. In Power Query Editor:
   - Verify all columns are detected correctly
   - Change column types:
     - `pr_number` → Whole Number
     - `is_merged`, `is_rejected`, `is_open`, `is_beyond_threshold` → True/False
     - `created_at`, `merged_at`, `closed_at`, `extraction_date` → Date/Time
     - `time_to_merge_hours`, `hours_open` → Decimal Number
     - `additions`, `deletions`, `changed_files`, `threshold_hours` → Whole Number
6. Click **Close & Apply**

### Step 1.3: Import Remaining CSV Files
Repeat the process for:
- `developer_aggregates.csv`
- `repository_aggregates.csv`
- `overall_aggregates.csv`
- `threshold_violations.csv`

---

## Part 2: Create Relationships

### Step 2.1: Open Model View
1. Click the **Model** icon on the left sidebar (three connected boxes)

### Step 2.2: Create Relationships
Drag and drop to create these relationships:

| From Table | From Column | To Table | To Column | Cardinality |
|------------|-------------|----------|-----------|-------------|
| pull_requests | repository | repository_aggregates | repository | Many-to-One |
| developer_aggregates | repository | repository_aggregates | repository | Many-to-One |
| threshold_violations | repository | repository_aggregates | repository | Many-to-One |
| threshold_violations | pr_number | pull_requests | pr_number | One-to-One |

---

## Part 3: Create DAX Measures

### Step 3.1: Create Measures Table
1. Click **Home** > **Enter Data**
2. Name the table: `_Measures`
3. Add one column named `Helper` with value `1`
4. Click **Load**
5. This table will hold all your measures

### Step 3.2: Add Core Measures
Right-click on `_Measures` table > **New Measure** for each:

#### Measure 1: Total PRs
```dax
Total PRs = COUNTROWS('pull_requests')
```

#### Measure 2: Total Merged
```dax
Total Merged = 
CALCULATE(
    COUNTROWS('pull_requests'),
    'pull_requests'[is_merged] = TRUE
)
```

#### Measure 3: Total Rejected
```dax
Total Rejected = 
CALCULATE(
    COUNTROWS('pull_requests'),
    'pull_requests'[is_rejected] = TRUE
)
```

#### Measure 4: Total Open
```dax
Total Open = 
CALCULATE(
    COUNTROWS('pull_requests'),
    'pull_requests'[is_open] = TRUE
)
```

#### Measure 5: PRs Beyond Threshold
```dax
PRs Beyond Threshold = 
CALCULATE(
    COUNTROWS('pull_requests'),
    'pull_requests'[is_beyond_threshold] = TRUE
)
```

#### Measure 6: Avg Time to Merge
```dax
Avg Time to Merge (Hours) = 
AVERAGE('pull_requests'[time_to_merge_hours])
```

#### Measure 7: Rejection Rate
```dax
Rejection Rate = 
DIVIDE([Total Rejected], [Total PRs], 0) * 100
```

#### Measure 8: Merge Rate
```dax
Merge Rate = 
DIVIDE([Total Merged], [Total PRs], 0) * 100
```

---

## Part 4: Create Dashboard Pages

### Page 1: Executive Summary

#### Step 4.1: Rename Page
- Double-click the page tab at bottom
- Rename to "Executive Summary"

#### Step 4.2: Add KPI Cards (Top Row)

**Card 1 - Total PRs:**
1. Click **Visualizations** > **Card**
2. Drag `Total PRs` measure to the card
3. Position at top-left
4. Format:
   - Title: "Total Pull Requests"
   - Background: Light gray (#F5F5F5)

**Card 2 - Merged PRs:**
1. Add another Card
2. Use `Total Merged` measure
3. Title: "Merged PRs"
4. Format with green accent (#4CAF50)

**Card 3 - Rejected PRs:**
1. Add another Card
2. Use `Total Rejected` measure
3. Title: "Rejected PRs"
4. Format with red accent (#F44336)

**Card 4 - Beyond Threshold:**
1. Add another Card
2. Use `PRs Beyond Threshold` measure
3. Title: "PRs Open > 48 Hours"
4. Format with yellow accent (#FFC107)

#### Step 4.3: Add Donut Chart
1. Click **Visualizations** > **Donut Chart**
2. Legend: `pull_requests[state]`
3. Values: Count of `pr_number`
4. Position below the cards on the left

#### Step 4.4: Add Bar Chart
1. Click **Visualizations** > **Clustered Bar Chart**
2. Y-axis: `pull_requests[repository]`
3. X-axis: Count of `pr_number`
4. Position below cards on the right

#### Step 4.5: Add Repository Table
1. Click **Visualizations** > **Table**
2. Add columns:
   - `repository_aggregates[repository]`
   - `repository_aggregates[total_prs]`
   - `repository_aggregates[merged_prs]`
   - `repository_aggregates[rejected_prs]`
   - `repository_aggregates[open_prs]`
   - `repository_aggregates[avg_time_to_merge_hours]`
3. Position at the bottom of the page

---

### Page 2: Developer Details (Drill-Through)

#### Step 4.6: Create New Page
1. Click the **+** at the bottom to add new page
2. Rename to "Developer Details"

#### Step 4.7: Configure Drill-Through
1. In the Visualizations pane, expand **Drill through**
2. Drag `pull_requests[repository]` to the Drill through area
3. Drag `pull_requests[author]` to the Drill through area

#### Step 4.8: Add Back Button
1. Click **Insert** > **Buttons** > **Back**
2. Position at top-left corner

#### Step 4.9: Add Developer Table
1. Add a **Table** visualization
2. Columns:
   - `developer_aggregates[developer]`
   - `developer_aggregates[total_prs]`
   - `developer_aggregates[merged_prs]`
   - `developer_aggregates[rejected_prs]`
   - `developer_aggregates[avg_time_to_merge_hours]`
   - `developer_aggregates[merge_rate]`

#### Step 4.10: Add Developer Bar Chart
1. Add **Clustered Bar Chart**
2. Y-axis: `developer_aggregates[developer]`
3. X-axis: `developer_aggregates[total_prs]`

---

### Page 3: Threshold Violations

#### Step 4.11: Create Page
1. Add new page, rename to "Threshold Violations"

#### Step 4.12: Add Critical PRs Card
1. Add **Card** with `PRs Beyond Threshold` measure
2. Title: "PRs Requiring Attention"
3. Format with red background

#### Step 4.13: Add Violations Table
1. Add **Table** visualization
2. Columns:
   - `threshold_violations[repository]`
   - `threshold_violations[pr_number]`
   - `threshold_violations[pr_title]`
   - `threshold_violations[author]`
   - `threshold_violations[hours_open]`
   - `threshold_violations[hours_overdue]`
3. Enable conditional formatting on `hours_overdue`:
   - Right-click column > Conditional formatting > Background color
   - Format by rules: > 48 = Red, > 24 = Yellow

#### Step 4.14: Add PR URL as Hyperlink
1. Select the table
2. Click on `pr_url` column settings
3. Set "URL" as the data category

---

### Page 4: Time Analysis

#### Step 4.15: Create Page
1. Add new page, rename to "Time Analysis"

#### Step 4.16: Add Time KPIs
Add cards for:
- `Avg Time to Merge (Hours)`
- `MEDIAN('pull_requests'[time_to_merge_hours])` (create as new measure)
- `MIN('pull_requests'[time_to_merge_hours])`
- `MAX('pull_requests'[time_to_merge_hours])`

#### Step 4.17: Add Histogram
1. Add **Clustered Column Chart**
2. Create bins for time_to_merge_hours:
   - Right-click `time_to_merge_hours` > New Group
   - Bin size: 8 (hours)
3. X-axis: time_to_merge_hours (bins)
4. Y-axis: Count of pr_number

---

## Part 5: Configure Interactions

### Step 5.1: Enable Cross-Filtering
1. Go to "Executive Summary" page
2. Click **Format** > **Edit Interactions**
3. Click on each KPI card
4. Set other visuals to "Filter" (funnel icon)

### Step 5.2: Test Drill-Through
1. On Executive Summary, right-click any data point
2. Select **Drill through** > **Developer Details**
3. Verify the page shows filtered data
4. Use Back button to return

---

## Part 6: Add Slicers

### Step 6.1: Repository Slicer
1. On Executive Summary, add **Slicer**
2. Field: `pull_requests[repository]`
3. Format as dropdown

### Step 6.2: Date Range Slicer
1. Add another **Slicer**
2. Field: `pull_requests[created_at]`
3. Format as "Between" date range

---

## Part 7: Apply Theme

### Step 7.1: Import Theme
1. Click **View** > **Themes** > **Browse for themes**
2. Navigate to: `C:\ikea\order-modification-persist\github-metrics-powerbi\powerbi\`
3. Select `GitHubMetricsTheme.json`
4. Click **Open**

---

## Part 8: Save and Publish

### Step 8.1: Save Project
1. Click **File** > **Save As**
2. Save as: `GitHubMetrics.pbix`
3. Location: `C:\ikea\order-modification-persist\github-metrics-powerbi\`

### Step 8.2: Publish to Service (Optional)
1. Click **Home** > **Publish**
2. Sign in to Power BI Service
3. Select workspace
4. Click **Select**

---

## Quick Reference: All DAX Measures

Copy all measures from `powerbi/measures.md` file.

Key measures to create first:
```dax
Total PRs = COUNTROWS('pull_requests')

Total Merged = CALCULATE(COUNTROWS('pull_requests'), 'pull_requests'[is_merged] = TRUE)

Total Rejected = CALCULATE(COUNTROWS('pull_requests'), 'pull_requests'[is_rejected] = TRUE)

Total Open = CALCULATE(COUNTROWS('pull_requests'), 'pull_requests'[is_open] = TRUE)

PRs Beyond Threshold = CALCULATE(COUNTROWS('pull_requests'), 'pull_requests'[is_beyond_threshold] = TRUE)

Avg Time to Merge (Hours) = AVERAGE('pull_requests'[time_to_merge_hours])

Merge Rate = DIVIDE([Total Merged], [Total PRs], 0) * 100

Rejection Rate = DIVIDE([Total Rejected], [Total PRs], 0) * 100
```

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| CSV import errors | Check file encoding (should be UTF-8) |
| Relationship errors | Ensure column types match between tables |
| Measures not working | Verify table names match exactly |
| Drill-through not working | Ensure drill-through fields are in both tables |

---

## Time Estimate

| Task | Time |
|------|------|
| Import data | 10 min |
| Create relationships | 5 min |
| Create measures | 10 min |
| Build pages | 15 min |
| Configure interactions | 5 min |
| **Total** | **~45 min** |

---

## Next Steps

1. Run `refresh_data.bat` to get live GitHub data
2. Update data source in Power BI
3. Schedule refresh in Power BI Service
4. Share dashboard with team

