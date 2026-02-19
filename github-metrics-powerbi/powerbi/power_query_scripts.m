// Power Query M Scripts for GitHub Metrics Dashboard
// Copy these scripts into Power BI's Advanced Editor

// ============================================================
// Table: pull_requests
// ============================================================
let
    Source = Csv.Document(
        File.Contents("C:\ikea\order-modification-persist\github-metrics-powerbi\data\pull_requests.csv"),
        [Delimiter=",", Columns=24, Encoding=65001, QuoteStyle=QuoteStyle.None]
    ),
    PromotedHeaders = Table.PromoteHeaders(Source, [PromoteAllScalars=true]),
    ChangedTypes = Table.TransformColumnTypes(PromotedHeaders,{
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
    })
in
    ChangedTypes

// ============================================================
// Table: developer_aggregates
// ============================================================
let
    Source = Csv.Document(
        File.Contents("C:\ikea\order-modification-persist\github-metrics-powerbi\data\developer_aggregates.csv"),
        [Delimiter=",", Columns=11, Encoding=65001, QuoteStyle=QuoteStyle.None]
    ),
    PromotedHeaders = Table.PromoteHeaders(Source, [PromoteAllScalars=true]),
    ChangedTypes = Table.TransformColumnTypes(PromotedHeaders,{
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
    })
in
    ChangedTypes

// ============================================================
// Table: repository_aggregates
// ============================================================
let
    Source = Csv.Document(
        File.Contents("C:\ikea\order-modification-persist\github-metrics-powerbi\data\repository_aggregates.csv"),
        [Delimiter=",", Columns=11, Encoding=65001, QuoteStyle=QuoteStyle.None]
    ),
    PromotedHeaders = Table.PromoteHeaders(Source, [PromoteAllScalars=true]),
    ChangedTypes = Table.TransformColumnTypes(PromotedHeaders,{
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
    })
in
    ChangedTypes

// ============================================================
// Table: overall_aggregates
// ============================================================
let
    Source = Csv.Document(
        File.Contents("C:\ikea\order-modification-persist\github-metrics-powerbi\data\overall_aggregates.csv"),
        [Delimiter=",", Columns=11, Encoding=65001, QuoteStyle=QuoteStyle.None]
    ),
    PromotedHeaders = Table.PromoteHeaders(Source, [PromoteAllScalars=true]),
    ChangedTypes = Table.TransformColumnTypes(PromotedHeaders,{
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
    ChangedTypes

// ============================================================
// Table: threshold_violations
// ============================================================
let
    Source = Csv.Document(
        File.Contents("C:\ikea\order-modification-persist\github-metrics-powerbi\data\threshold_violations.csv"),
        [Delimiter=",", Columns=8, Encoding=65001, QuoteStyle=QuoteStyle.None]
    ),
    PromotedHeaders = Table.PromoteHeaders(Source, [PromoteAllScalars=true]),
    ChangedTypes = Table.TransformColumnTypes(PromotedHeaders,{
        {"repository", type text},
        {"pr_number", Int64.Type},
        {"pr_title", type text},
        {"pr_url", type text},
        {"author", type text},
        {"hours_open", type number},
        {"hours_overdue", type number},
        {"is_beyond_threshold", type logical},
        {"extraction_date", type datetime}
    })
in
    ChangedTypes

// ============================================================
// Table: DateTable (for time intelligence)
// ============================================================
let
    StartDate = #date(2024, 1, 1),
    EndDate = #date(2026, 12, 31),
    NumberOfDays = Duration.Days(EndDate - StartDate) + 1,
    DateList = List.Dates(StartDate, NumberOfDays, #duration(1, 0, 0, 0)),
    DateTable = Table.FromList(DateList, Splitter.SplitByNothing(), {"Date"}, null, ExtraValues.Error),
    ChangedType = Table.TransformColumnTypes(DateTable,{{"Date", type date}}),
    AddYear = Table.AddColumn(ChangedType, "Year", each Date.Year([Date]), Int64.Type),
    AddMonth = Table.AddColumn(AddYear, "Month", each Date.Month([Date]), Int64.Type),
    AddMonthName = Table.AddColumn(AddMonth, "MonthName", each Date.MonthName([Date]), type text),
    AddQuarter = Table.AddColumn(AddMonthName, "Quarter", each "Q" & Text.From(Date.QuarterOfYear([Date])), type text),
    AddWeek = Table.AddColumn(AddQuarter, "WeekNumber", each Date.WeekOfYear([Date]), Int64.Type),
    AddDayOfWeek = Table.AddColumn(AddWeek, "DayOfWeek", each Date.DayOfWeek([Date]), Int64.Type),
    AddDayName = Table.AddColumn(AddDayOfWeek, "DayName", each Date.DayOfWeekName([Date]), type text),
    AddIsWeekend = Table.AddColumn(AddDayName, "IsWeekend", each Date.DayOfWeek([Date]) >= 5, type logical)
in
    AddIsWeekend

