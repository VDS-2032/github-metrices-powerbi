package org.example;

/**
 * Builds the TMSL (Tabular Model Scripting Language) DataModelSchema JSON that
 * is embedded inside the .pbit archive.
 *
 * <p>The schema defines:
 * <ul>
 *   <li>Five CSV-sourced tables (pull_requests, developer_aggregates,
 *       repository_aggregates, overall_aggregates, threshold_violations)</li>
 *   <li>A generated DateTable for time-intelligence calculations</li>
 *   <li>A _Measures table that hosts all DAX measures</li>
 *   <li>All relationships between the tables</li>
 *   <li>A <em>DataFolderPath</em> parameter expression so the data-source path
 *       can be changed in Power BI Desktop without editing queries individually</li>
 * </ul>
 */
public class DataModelSchemaBuilder {

    // Default data path shown in Power BI's parameter dialog
    private static final String DEFAULT_DATA_PATH = "C:\\GitHubMetrics\\data";

    // -------------------------------------------------------------------------
    // Table column definitions  [columnName, tmslDataType]
    // -------------------------------------------------------------------------

    private static final String[][] PULL_REQUESTS_COLS = {
        {"repository", "string"}, {"pr_number", "int64"}, {"pr_title", "string"},
        {"pr_url", "string"}, {"author", "string"}, {"author_avatar", "string"},
        {"state", "string"}, {"is_merged", "boolean"}, {"is_rejected", "boolean"},
        {"is_open", "boolean"}, {"created_at", "dateTime"}, {"merged_at", "dateTime"},
        {"closed_at", "dateTime"}, {"time_to_merge_hours", "double"}, {"hours_open", "double"},
        {"is_beyond_threshold", "boolean"}, {"threshold_hours", "int64"}, {"labels", "string"},
        {"reviewers_requested", "string"}, {"base_branch", "string"}, {"head_branch", "string"},
        {"additions", "int64"}, {"deletions", "int64"}, {"changed_files", "int64"},
        {"extraction_date", "dateTime"}
    };

    private static final String[][] DEVELOPER_AGGREGATES_COLS = {
        {"repository", "string"}, {"developer", "string"}, {"total_prs", "int64"},
        {"merged_prs", "int64"}, {"rejected_prs", "int64"}, {"open_prs", "int64"},
        {"avg_time_to_merge_hours", "double"}, {"prs_beyond_threshold", "int64"},
        {"merge_rate", "double"}, {"rejection_rate", "double"}, {"extraction_date", "dateTime"}
    };

    private static final String[][] REPOSITORY_AGGREGATES_COLS = {
        {"repository", "string"}, {"total_prs", "int64"}, {"merged_prs", "int64"},
        {"rejected_prs", "int64"}, {"open_prs", "int64"}, {"avg_time_to_merge_hours", "double"},
        {"prs_beyond_threshold", "int64"}, {"unique_contributors", "int64"},
        {"merge_rate", "double"}, {"rejection_rate", "double"}, {"extraction_date", "dateTime"}
    };

    private static final String[][] OVERALL_AGGREGATES_COLS = {
        {"total_repositories", "int64"}, {"total_prs", "int64"}, {"total_merged", "int64"},
        {"total_rejected", "int64"}, {"total_open", "int64"}, {"avg_time_to_merge_hours", "double"},
        {"total_beyond_threshold", "int64"}, {"unique_contributors", "int64"},
        {"overall_merge_rate", "double"}, {"overall_rejection_rate", "double"},
        {"extraction_date", "dateTime"}
    };

    private static final String[][] THRESHOLD_VIOLATIONS_COLS = {
        {"repository", "string"}, {"pr_number", "int64"}, {"pr_title", "string"},
        {"pr_url", "string"}, {"author", "string"}, {"hours_open", "double"},
        {"hours_overdue", "double"}, {"is_beyond_threshold", "boolean"},
        {"extraction_date", "dateTime"}
    };

    // Lookup table: CSV table name -> column definitions
    private static final Object[][] CSV_TABLES = {
        {"pull_requests",          PULL_REQUESTS_COLS},
        {"developer_aggregates",   DEVELOPER_AGGREGATES_COLS},
        {"repository_aggregates",  REPOSITORY_AGGREGATES_COLS},
        {"overall_aggregates",     OVERALL_AGGREGATES_COLS},
        {"threshold_violations",   THRESHOLD_VIOLATIONS_COLS}
    };

    // DateTable column definitions (generated, not from CSV)
    private static final String[][] DATE_TABLE_COLS = {
        {"Date", "dateTime"}, {"Year", "int64"}, {"Month", "int64"},
        {"Month Name", "string"}, {"Quarter", "string"}, {"Week Number", "int64"},
        {"Day Name", "string"}, {"Is Weekend", "boolean"}
    };

    // -------------------------------------------------------------------------
    // DAX Measures  [name, expression, formatString]
    // -------------------------------------------------------------------------

    private static final String[][] MEASURES = {
        {"Total PRs",
            "COUNTROWS('pull_requests')",
            "#,##0"},
        {"Total Merged",
            "CALCULATE(COUNTROWS('pull_requests'), 'pull_requests'[is_merged] = TRUE)",
            "#,##0"},
        {"Total Rejected",
            "CALCULATE(COUNTROWS('pull_requests'), 'pull_requests'[is_rejected] = TRUE)",
            "#,##0"},
        {"Total Open",
            "CALCULATE(COUNTROWS('pull_requests'), 'pull_requests'[is_open] = TRUE)",
            "#,##0"},
        {"PRs Beyond Threshold",
            "CALCULATE(COUNTROWS('pull_requests'), 'pull_requests'[is_beyond_threshold] = TRUE)",
            "#,##0"},
        {"Avg Time to Merge (Hours)",
            "AVERAGE('pull_requests'[time_to_merge_hours])",
            "#,##0.0"},
        {"Avg Time to Merge (Days)",
            "DIVIDE([Avg Time to Merge (Hours)], 24, 0)",
            "#,##0.0"},
        {"Median Time to Merge",
            "MEDIAN('pull_requests'[time_to_merge_hours])",
            "#,##0.0"},
        {"Min Time to Merge",
            "MIN('pull_requests'[time_to_merge_hours])",
            "#,##0.0"},
        {"Max Time to Merge",
            "MAX('pull_requests'[time_to_merge_hours])",
            "#,##0.0"},
        {"Merge Rate",
            "DIVIDE([Total Merged], [Total PRs], 0) * 100",
            "0.0\\%"},
        {"Rejection Rate",
            "DIVIDE([Total Rejected], [Total PRs], 0) * 100",
            "0.0\\%"},
        {"Total Repositories",
            "DISTINCTCOUNT('pull_requests'[repository])",
            "#,##0"},
        {"Total Contributors",
            "DISTINCTCOUNT('pull_requests'[author])",
            "#,##0"},
        {"Critical PRs",
            "CALCULATE(COUNTROWS('pull_requests'), 'pull_requests'[hours_open] > 72,"
                + " 'pull_requests'[is_open] = TRUE)",
            "#,##0"},
        {"Threshold Violation Rate",
            "DIVIDE([PRs Beyond Threshold], [Total Open], 0) * 100",
            "0.0\\%"},
        {"PRs This Month",
            "CALCULATE(COUNTROWS('pull_requests'),"
                + " MONTH('pull_requests'[created_at]) = MONTH(TODAY()) &&"
                + " YEAR('pull_requests'[created_at]) = YEAR(TODAY()))",
            "#,##0"},
        {"PRs Last 7 Days",
            "CALCULATE(COUNTROWS('pull_requests'), 'pull_requests'[created_at] >= TODAY() - 7)",
            "#,##0"},
    };

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Returns the fully-formed DataModelSchema JSON string. */
    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"name\": \"Model\",\n");
        sb.append("  \"compatibilityLevel\": 1567,\n");
        sb.append("  \"model\": {\n");
        sb.append("    \"culture\": \"en-US\",\n");
        sb.append("    \"dataAccessOptions\": {\n");
        sb.append("      \"legacyRedirects\": true,\n");
        sb.append("      \"returnErrorValuesAsNull\": true\n");
        sb.append("    },\n");
        sb.append("    \"defaultPowerBIDataSourceVersion\": \"powerBI_V3\",\n");
        sb.append("    \"sourceQueryCulture\": \"en-US\",\n");

        // Tables
        sb.append("    \"tables\": [\n");
        for (int i = 0; i < CSV_TABLES.length; i++) {
            if (i > 0) sb.append(",\n");
            appendCsvTable(sb, (String) CSV_TABLES[i][0], (String[][]) CSV_TABLES[i][1]);
        }
        sb.append(",\n");
        appendDateTable(sb);
        sb.append(",\n");
        appendMeasuresTable(sb);
        sb.append("\n    ],\n");

        // Relationships
        sb.append("    \"relationships\": [\n");
        appendRelationships(sb);
        sb.append("\n    ],\n");

        // Expressions (parameters)
        sb.append("    \"expressions\": [\n");
        appendExpressions(sb);
        sb.append("\n    ],\n");

        // Annotations
        sb.append("    \"annotations\": [\n");
        sb.append("      {\"name\": \"PBIDesktopVersion\",          \"value\": \"2.124.1052.0 (24.06)\"},\n");
        sb.append("      {\"name\": \"__PBI_TimeIntelligenceEnabled\", \"value\": \"1\"}\n");
        sb.append("    ]\n");

        sb.append("  }\n");
        sb.append("}");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Table builders
    // -------------------------------------------------------------------------

    private void appendCsvTable(StringBuilder sb, String tableName, String[][] columns) {
        sb.append("      {\n");
        sb.append("        \"name\": \"").append(tableName).append("\",\n");

        // Columns
        sb.append("        \"columns\": [\n");
        for (int i = 0; i < columns.length; i++) {
            sb.append("          {");
            sb.append("\"name\": \"").append(columns[i][0]).append("\", ");
            sb.append("\"dataType\": \"").append(columns[i][1]).append("\", ");
            sb.append("\"sourceColumn\": \"").append(columns[i][0]).append("\"");
            sb.append("}");
            if (i < columns.length - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("        ],\n");

        // Partition with Power Query M source
        sb.append("        \"partitions\": [\n");
        sb.append("          {\n");
        sb.append("            \"name\": \"").append(tableName).append("\",\n");
        sb.append("            \"mode\": \"import\",\n");
        sb.append("            \"source\": {\n");
        sb.append("              \"type\": \"m\",\n");
        sb.append("              \"expression\": [\n");
        sb.append("                \"let\",\n");
        // The M expression references the DataFolderPath parameter (defined in expressions[])
        // Backslash is a path separator in M strings (not an escape character).
        sb.append("                \"    Source = Csv.Document(")
          .append("File.Contents(DataFolderPath & \\\"\\\\").append(tableName)
          .append(".csv\\\"), [Delimiter=\\\",\\\", Encoding=65001, QuoteStyle=QuoteStyle.Csv]),\",\n");
        sb.append("                \"    PromotedHeaders = Table.PromoteHeaders(Source, [PromoteAllScalars=true])\",\n");
        sb.append("                \"in\",\n");
        sb.append("                \"    PromotedHeaders\"\n");
        sb.append("              ]\n");
        sb.append("            }\n");
        sb.append("          }\n");
        sb.append("        ]\n");
        sb.append("      }");
    }

    private void appendDateTable(StringBuilder sb) {
        sb.append("      {\n");
        sb.append("        \"name\": \"DateTable\",\n");

        // Columns
        sb.append("        \"columns\": [\n");
        for (int i = 0; i < DATE_TABLE_COLS.length; i++) {
            sb.append("          {");
            sb.append("\"name\": \"").append(DATE_TABLE_COLS[i][0]).append("\", ");
            sb.append("\"dataType\": \"").append(DATE_TABLE_COLS[i][1]).append("\"");
            if ("Date".equals(DATE_TABLE_COLS[i][0])) {
                sb.append(", \"sourceColumn\": \"Date\", \"isKey\": true");
            }
            sb.append("}");
            if (i < DATE_TABLE_COLS.length - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("        ],\n");

        // Partition – generated calendar table
        sb.append("        \"partitions\": [\n");
        sb.append("          {\n");
        sb.append("            \"name\": \"DateTable\",\n");
        sb.append("            \"mode\": \"import\",\n");
        sb.append("            \"source\": {\n");
        sb.append("              \"type\": \"m\",\n");
        sb.append("              \"expression\": [\n");
        sb.append("                \"let\",\n");
        sb.append("                \"    StartDate = #date(2024, 1, 1),\",\n");
        sb.append("                \"    EndDate   = #date(2026, 12, 31),\",\n");
        sb.append("                \"    NbDays    = Duration.Days(EndDate - StartDate) + 1,\",\n");
        sb.append("                \"    DateList  = List.Dates(StartDate, NbDays, #duration(1, 0, 0, 0)),\",\n");
        sb.append("                \"    DateTable = Table.FromList(DateList, Splitter.SplitByNothing(), {\\\"Date\\\"}, null, ExtraValues.Error),\",\n");
        sb.append("                \"    ChangedType  = Table.TransformColumnTypes(DateTable, {{\\\"Date\\\", type date}}),\",\n");
        sb.append("                \"    AddYear      = Table.AddColumn(ChangedType,   \\\"Year\\\",       each Date.Year([Date]),            Int64.Type),\",\n");
        sb.append("                \"    AddMonth     = Table.AddColumn(AddYear,       \\\"Month\\\",      each Date.Month([Date]),           Int64.Type),\",\n");
        sb.append("                \"    AddMonthName = Table.AddColumn(AddMonth,      \\\"Month Name\\\", each Date.MonthName([Date]),       type text),\",\n");
        sb.append("                \"    AddQuarter   = Table.AddColumn(AddMonthName,  \\\"Quarter\\\",    each \\\"Q\\\" & Text.From(Date.QuarterOfYear([Date])), type text),\",\n");
        sb.append("                \"    AddWeek      = Table.AddColumn(AddQuarter,    \\\"Week Number\\\",each Date.WeekOfYear([Date]),      Int64.Type),\",\n");
        sb.append("                \"    AddDayName   = Table.AddColumn(AddWeek,       \\\"Day Name\\\",   each Date.DayOfWeekName([Date]),   type text),\",\n");
        sb.append("                \"    AddIsWeekend = Table.AddColumn(AddDayName,    \\\"Is Weekend\\\", each Date.DayOfWeek([Date], Day.Monday) >= 5, type logical)\",\n");
        sb.append("                \"in\",\n");
        sb.append("                \"    AddIsWeekend\"\n");
        sb.append("              ]\n");
        sb.append("            }\n");
        sb.append("          }\n");
        sb.append("        ],\n");
        sb.append("        \"annotations\": [{\"name\": \"__PBI_MarkAsDateTable\", \"value\": \"{\\\"TimeIntelligenceEnabled\\\":1,\\\"EventColumn\\\":\\\"Date\\\"}\"}]\n");
        sb.append("      }");
    }

    private void appendMeasuresTable(StringBuilder sb) {
        sb.append("      {\n");
        sb.append("        \"name\": \"_Measures\",\n");

        // Hidden helper column (required so the table can be loaded)
        sb.append("        \"columns\": [\n");
        sb.append("          {\"name\": \"Helper\", \"dataType\": \"int64\", \"isHidden\": true}\n");
        sb.append("        ],\n");

        // DAX measures
        sb.append("        \"measures\": [\n");
        for (int i = 0; i < MEASURES.length; i++) {
            if (i > 0) sb.append(",\n");
            String name   = MEASURES[i][0];
            String expr   = MEASURES[i][1];
            String fmt    = MEASURES[i][2];
            sb.append("          {\n");
            sb.append("            \"name\": \"").append(jsonEscape(name)).append("\",\n");
            sb.append("            \"expression\": \"").append(jsonEscape(expr)).append("\",\n");
            sb.append("            \"formatString\": \"").append(jsonEscape(fmt)).append("\"\n");
            sb.append("          }");
        }
        sb.append("\n        ],\n");

        // Partition – static one-row table
        sb.append("        \"partitions\": [\n");
        sb.append("          {\n");
        sb.append("            \"name\": \"_Measures\",\n");
        sb.append("            \"mode\": \"import\",\n");
        sb.append("            \"source\": {\n");
        sb.append("              \"type\": \"m\",\n");
        sb.append("              \"expression\": [\n");
        sb.append("                \"let\",\n");
        sb.append("                \"    Source = #table(type table [Helper = Int64.Type], {{1}})\",\n");
        sb.append("                \"in\",\n");
        sb.append("                \"    Source\"\n");
        sb.append("              ]\n");
        sb.append("            }\n");
        sb.append("          }\n");
        sb.append("        ],\n");
        sb.append("        \"isHidden\": false\n");
        sb.append("      }");
    }

    // -------------------------------------------------------------------------
    // Relationships
    // -------------------------------------------------------------------------

    /**
     * Relationships in TMSL format.
     * Each entry: {fromTable, fromColumn, toTable, toColumn, crossFilter, joinOnDate}
     * crossFilter: "oneDirection" | "bothDirections"
     * joinOnDate : "datePartOnly" | "" (empty = not a date relationship)
     */
    private void appendRelationships(StringBuilder sb) {
        String[][] rels = {
            {"pull_requests",        "repository", "repository_aggregates", "repository", "bothDirections", ""},
            {"developer_aggregates", "repository", "repository_aggregates", "repository", "oneDirection",   ""},
            {"threshold_violations", "repository", "repository_aggregates", "repository", "oneDirection",   ""},
            {"threshold_violations", "pr_number",  "pull_requests",         "pr_number",  "bothDirections", ""},
            {"pull_requests",        "created_at", "DateTable",             "Date",       "oneDirection",   "datePartOnly"},
        };

        for (int i = 0; i < rels.length; i++) {
            if (i > 0) sb.append(",\n");
            String fromTable  = rels[i][0];
            String fromCol    = rels[i][1];
            String toTable    = rels[i][2];
            String toCol      = rels[i][3];
            String crossFilter = rels[i][4];
            String joinOnDate  = rels[i][5];

            // Build a deterministic relationship name
            String relName = "rel_" + fromTable + "_" + fromCol + "_" + toTable;

            sb.append("      {\n");
            sb.append("        \"name\": \"").append(relName).append("\",\n");
            sb.append("        \"fromTable\": \"").append(fromTable).append("\",\n");
            sb.append("        \"fromColumn\": \"").append(fromCol).append("\",\n");
            sb.append("        \"toTable\": \"").append(toTable).append("\",\n");
            sb.append("        \"toColumn\": \"").append(toCol).append("\"");
            if ("bothDirections".equals(crossFilter)) {
                sb.append(",\n        \"crossFilteringBehavior\": \"bothDirections\"");
            }
            if (!joinOnDate.isEmpty()) {
                sb.append(",\n        \"joinOnDateBehavior\": \"").append(joinOnDate).append("\"");
            }
            sb.append("\n      }");
        }
    }

    // -------------------------------------------------------------------------
    // Expressions (Power Query parameters)
    // -------------------------------------------------------------------------

    private void appendExpressions(StringBuilder sb) {
        // DataFolderPath parameter – user changes this in Power BI Desktop to
        // point at the actual CSV output folder produced by the Python extractor.
        // The M expression follows the Power BI parameter query convention.
        String mExpr = "\"" + DEFAULT_DATA_PATH + "\" "
                + "meta [IsParameterQuery=true, Type=\"Text\", IsParameterQueryRequired=true]";

        sb.append("      {\n");
        sb.append("        \"name\": \"DataFolderPath\",\n");
        sb.append("        \"kind\": \"m\",\n");
        sb.append("        \"expression\": \"").append(jsonEscape(mExpr)).append("\"\n");
        sb.append("      }");
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /** Escapes a Java string so it can be embedded inside a JSON string value. */
    private static String jsonEscape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
