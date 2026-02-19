// ==============================================================================
// POWER BI DESKTOP - DATA MODEL SETUP SCRIPT
// ==============================================================================
// This file contains all Power Query M code to set up the GitHub Metrics model.
// Follow the instructions below to import into Power BI Desktop.
// ==============================================================================

// STEP 1: Create Parameter for Data Path
// ------------------------------------------------------------------------------
// In Power BI Desktop: Home > Transform Data > Manage Parameters > New Parameter
// Name: DataFolderPath
// Type: Text
// Current Value: C:\ikea\order-modification-persist\github-metrics-powerbi\data

let
    DataFolderPath = "C:\ikea\order-modification-persist\github-metrics-powerbi\data"
in
    DataFolderPath


// ==============================================================================
// TABLE 1: pull_requests
// ==============================================================================
// In Power Query Editor: Home > New Source > Blank Query
// Paste this code in the Advanced Editor

let
    Source = Csv.Document(
        File.Contents(DataFolderPath & "\pull_requests.csv"),
        [Delimiter=",", Columns=25, Encoding=65001, QuoteStyle=QuoteStyle.Csv]
    ),
    PromotedHeaders = Table.PromoteHeaders(Source, [PromoteAllScalars=true]),

    // Transform column types
    TransformedTypes = Table.TransformColumnTypes(PromotedHeaders, {
        {"repository", type text},
        {"pr_number", Int64.Type},
        {"pr_title", type text},
        {"pr_url", type text},
        {"author", type text},
        {"author_avatar", type text},
        {"state", type text},
        {"is_merged", type logical},
        {"is_rejected", type logical},
        {"is_open", type logical},
        {"created_at", type datetime},
        {"merged_at", type datetime},
        {"closed_at", type datetime},
        {"time_to_merge_hours", type number},
        {"hours_open", type number},
        {"is_beyond_threshold", type logical},
        {"threshold_hours", Int64.Type},
        {"labels", type text},
        {"reviewers_requested", type text},
        {"base_branch", type text},
        {"head_branch", type text},
        {"additions", Int64.Type},
        {"deletions", Int64.Type},
        {"changed_files", Int64.Type},
        {"extraction_date", type datetime}
    }),

    // Add calculated columns
    AddedPRAge = Table.AddColumn(TransformedTypes, "PR Age (Days)",
        each Duration.Days(DateTime.LocalNow() - [created_at]), type number),

    AddedStatus = Table.AddColumn(AddedPRAge, "Status Category", each
        if [is_merged] then "Merged"
        else if [is_rejected] then "Rejected"
        else if [is_open] then "Open"
        else "Unknown", type text),

    AddedUrgency = Table.AddColumn(AddedStatus, "Urgency Level", each
        if [is_open] = false then "Closed"
        else if [hours_open] > 72 then "Critical"
        else if [hours_open] > 48 then "Warning"
        else "Normal", type text)
in
    AddedUrgency


// ==============================================================================
// TABLE 2: developer_aggregates
// ==============================================================================

let
    Source = Csv.Document(
        File.Contents(DataFolderPath & "\developer_aggregates.csv"),
        [Delimiter=",", Columns=11, Encoding=65001, QuoteStyle=QuoteStyle.Csv]
    ),
    PromotedHeaders = Table.PromoteHeaders(Source, [PromoteAllScalars=true]),
    TransformedTypes = Table.TransformColumnTypes(PromotedHeaders, {
        {"repository", type text},
        {"developer", type text},
        {"total_prs", Int64.Type},
        {"merged_prs", Int64.Type},
        {"rejected_prs", Int64.Type},
        {"open_prs", Int64.Type},
        {"avg_time_to_merge_hours", type number},
        {"prs_beyond_threshold", Int64.Type},
        {"merge_rate", type number},
        {"rejection_rate", type number},
        {"extraction_date", type datetime}
    }),

    // Add performance rating
    AddedRating = Table.AddColumn(TransformedTypes, "Performance Rating", each
        if [merge_rate] >= 80 and [avg_time_to_merge_hours] <= 24 then "Excellent"
        else if [merge_rate] >= 60 and [avg_time_to_merge_hours] <= 48 then "Good"
        else if [merge_rate] >= 40 then "Average"
        else "Needs Improvement", type text)
in
    AddedRating


// ==============================================================================
// TABLE 3: repository_aggregates
// ==============================================================================

let
    Source = Csv.Document(
        File.Contents(DataFolderPath & "\repository_aggregates.csv"),
        [Delimiter=",", Columns=11, Encoding=65001, QuoteStyle=QuoteStyle.Csv]
    ),
    PromotedHeaders = Table.PromoteHeaders(Source, [PromoteAllScalars=true]),
    TransformedTypes = Table.TransformColumnTypes(PromotedHeaders, {
        {"repository", type text},
        {"total_prs", Int64.Type},
        {"merged_prs", Int64.Type},
        {"rejected_prs", Int64.Type},
        {"open_prs", Int64.Type},
        {"avg_time_to_merge_hours", type number},
        {"prs_beyond_threshold", Int64.Type},
        {"unique_contributors", Int64.Type},
        {"merge_rate", type number},
        {"rejection_rate", type number},
        {"extraction_date", type datetime}
    }),

    // Add health status
    AddedHealth = Table.AddColumn(TransformedTypes, "Health Status", each
        if [prs_beyond_threshold] = 0 and [merge_rate] >= 70 then "Healthy"
        else if [prs_beyond_threshold] <= 2 and [merge_rate] >= 50 then "Moderate"
        else "At Risk", type text)
in
    AddedHealth


// ==============================================================================
// TABLE 4: overall_aggregates
// ==============================================================================

let
    Source = Csv.Document(
        File.Contents(DataFolderPath & "\overall_aggregates.csv"),
        [Delimiter=",", Columns=11, Encoding=65001, QuoteStyle=QuoteStyle.Csv]
    ),
    PromotedHeaders = Table.PromoteHeaders(Source, [PromoteAllScalars=true]),
    TransformedTypes = Table.TransformColumnTypes(PromotedHeaders, {
        {"total_repositories", Int64.Type},
        {"total_prs", Int64.Type},
        {"total_merged", Int64.Type},
        {"total_rejected", Int64.Type},
        {"total_open", Int64.Type},
        {"avg_time_to_merge_hours", type number},
        {"total_beyond_threshold", Int64.Type},
        {"unique_contributors", Int64.Type},
        {"overall_merge_rate", type number},
        {"overall_rejection_rate", type number},
        {"extraction_date", type datetime}
    })
in
    TransformedTypes


// ==============================================================================
// TABLE 5: threshold_violations
// ==============================================================================

let
    Source = Csv.Document(
        File.Contents(DataFolderPath & "\threshold_violations.csv"),
        [Delimiter=",", Columns=9, Encoding=65001, QuoteStyle=QuoteStyle.Csv]
    ),
    PromotedHeaders = Table.PromoteHeaders(Source, [PromoteAllScalars=true]),
    TransformedTypes = Table.TransformColumnTypes(PromotedHeaders, {
        {"repository", type text},
        {"pr_number", Int64.Type},
        {"pr_title", type text},
        {"pr_url", type text},
        {"author", type text},
        {"hours_open", type number},
        {"hours_overdue", type number},
        {"is_beyond_threshold", type logical},
        {"extraction_date", type datetime}
    }),

    // Add severity level
    AddedSeverity = Table.AddColumn(TransformedTypes, "Severity", each
        if [hours_overdue] > 48 then "Critical"
        else if [hours_overdue] > 24 then "High"
        else "Medium", type text),

    // Add days overdue
    AddedDays = Table.AddColumn(AddedSeverity, "Days Overdue",
        each Number.Round([hours_overdue] / 24, 1), type number)
in
    AddedDays


// ==============================================================================
// TABLE 6: DateTable (Calendar Table for Time Intelligence)
// ==============================================================================

let
    StartDate = #date(2024, 1, 1),
    EndDate = #date(2026, 12, 31),
    NumberOfDays = Duration.Days(EndDate - StartDate) + 1,
    DateList = List.Dates(StartDate, NumberOfDays, #duration(1, 0, 0, 0)),
    DateTable = Table.FromList(DateList, Splitter.SplitByNothing(), {"Date"}, null, ExtraValues.Error),

    ChangedType = Table.TransformColumnTypes(DateTable, {{"Date", type date}}),
    AddYear = Table.AddColumn(ChangedType, "Year", each Date.Year([Date]), Int64.Type),
    AddMonth = Table.AddColumn(AddYear, "Month", each Date.Month([Date]), Int64.Type),
    AddMonthName = Table.AddColumn(AddMonth, "Month Name", each Date.MonthName([Date]), type text),
    AddMonthShort = Table.AddColumn(AddMonthName, "Month Short", each Text.Start(Date.MonthName([Date]), 3), type text),
    AddQuarter = Table.AddColumn(AddMonthShort, "Quarter", each "Q" & Text.From(Date.QuarterOfYear([Date])), type text),
    AddWeek = Table.AddColumn(AddQuarter, "Week Number", each Date.WeekOfYear([Date]), Int64.Type),
    AddDayOfWeek = Table.AddColumn(AddWeek, "Day of Week", each Date.DayOfWeek([Date], Day.Monday) + 1, Int64.Type),
    AddDayName = Table.AddColumn(AddDayOfWeek, "Day Name", each Date.DayOfWeekName([Date]), type text),
    AddIsWeekend = Table.AddColumn(AddDayName, "Is Weekend", each Date.DayOfWeek([Date], Day.Monday) >= 5, type logical),
    AddYearMonth = Table.AddColumn(AddIsWeekend, "Year-Month", each Text.From([Year]) & "-" & Text.PadStart(Text.From([Month]), 2, "0"), type text),
    AddYearQuarter = Table.AddColumn(AddYearMonth, "Year-Quarter", each Text.From([Year]) & "-" & [Quarter], type text)
in
    AddYearQuarter


// ==============================================================================
// TABLE 7: Status Lookup (for slicers)
// ==============================================================================

let
    Source = Table.FromRecords({
        [Status = "All", StatusOrder = 0, StatusIcon = "📊"],
        [Status = "Merged", StatusOrder = 1, StatusIcon = "✅"],
        [Status = "Open", StatusOrder = 2, StatusIcon = "🔵"],
        [Status = "Rejected", StatusOrder = 3, StatusIcon = "❌"]
    }),
    ChangedTypes = Table.TransformColumnTypes(Source, {
        {"Status", type text},
        {"StatusOrder", Int64.Type},
        {"StatusIcon", type text}
    })
in
    ChangedTypes


// ==============================================================================
// TABLE 8: Urgency Lookup (for conditional formatting)
// ==============================================================================

let
    Source = Table.FromRecords({
        [Urgency = "Normal", UrgencyOrder = 1, UrgencyColor = "#4CAF50", UrgencyIcon = "🟢"],
        [Urgency = "Warning", UrgencyOrder = 2, UrgencyColor = "#FFC107", UrgencyIcon = "🟡"],
        [Urgency = "Critical", UrgencyOrder = 3, UrgencyColor = "#F44336", UrgencyIcon = "🔴"],
        [Urgency = "Closed", UrgencyOrder = 0, UrgencyColor = "#9E9E9E", UrgencyIcon = "⚫"]
    }),
    ChangedTypes = Table.TransformColumnTypes(Source, {
        {"Urgency", type text},
        {"UrgencyOrder", Int64.Type},
        {"UrgencyColor", type text},
        {"UrgencyIcon", type text}
    })
in
    ChangedTypes

