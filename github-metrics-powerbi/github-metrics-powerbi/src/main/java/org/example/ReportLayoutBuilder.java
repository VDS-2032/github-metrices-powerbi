package org.example;

/**
 * Builds the Report/Layout JSON that is embedded inside the .pbit archive.
 *
 * <p>The layout defines four report pages:
 * <ol>
 *   <li>Executive Summary  – high-level KPIs, charts and the repository table</li>
 *   <li>Developer Details  – drill-through page with developer-level metrics</li>
 *   <li>Threshold Violations – PRs that exceed the 48-hour review SLA</li>
 *   <li>Time Analysis       – merge time distribution and trend charts</li>
 * </ol>
 *
 * <p>Pages are pre-configured with the correct drill-through settings and contain
 * placeholder visual containers.  Users can add and arrange visuals manually in
 * Power BI Desktop following the {@code POWERBI_IMPORT_GUIDE.md} instructions.
 */
public class ReportLayoutBuilder {

    // Standard canvas dimensions (16:9 at 1280 × 720)
    private static final int PAGE_WIDTH  = 1280;
    private static final int PAGE_HEIGHT = 720;

    // Theme config embedded in every section
    private static final String THEME_CFG =
            "{\\\"version\\\":\\\"5.56\\\",\\\"themeCollection\\\":{\\\"baseTheme\\\":"
            + "{\\\"name\\\":\\\"CY24SU10\\\",\\\"version\\\":\\\"5.56\\\",\\\"type\\\":2}}}";

    /** Returns the fully-formed Report/Layout JSON string. */
    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"id\": 0,\n");
        sb.append("  \"resourcePackages\": [],\n");
        sb.append("  \"sections\": [\n");

        // Page 1 – Executive Summary
        appendSection(sb, 0, "ReportSection",  "Executive Summary",
                buildExecutiveSummaryVisuals(), false, 0);

        // Page 2 – Developer Details (drill-through)
        sb.append(",\n");
        appendSection(sb, 1, "ReportSection1", "Developer Details",
                buildDeveloperDetailsVisuals(), true, 1);

        // Page 3 – Threshold Violations (drill-through)
        sb.append(",\n");
        appendSection(sb, 2, "ReportSection2", "Threshold Violations",
                buildThresholdViolationsVisuals(), true, 2);

        // Page 4 – Time Analysis
        sb.append(",\n");
        appendSection(sb, 3, "ReportSection3", "Time Analysis",
                buildTimeAnalysisVisuals(), false, 3);

        sb.append("\n  ],\n");

        // Report-level config
        sb.append("  \"config\": \"{\\\"version\\\":\\\"5.56\\\",\\\"activeSectionIndex\\\":0,");
        sb.append("\\\"defaultDrillFilterOtherVisuals\\\":true,");
        sb.append("\\\"themeCollection\\\":{\\\"baseTheme\\\":{\\\"name\\\":\\\"CY24SU10\\\",");
        sb.append("\\\"version\\\":\\\"5.56\\\",\\\"type\\\":2}}}\",\n");
        sb.append("  \"layoutOptimization\": 0,\n");
        sb.append("  \"publicCustomVisuals\": [],\n");
        sb.append("  \"resourcePackages\": []\n");
        sb.append("}");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Section builder
    // -------------------------------------------------------------------------

    private void appendSection(StringBuilder sb, int id, String name, String displayName,
                                String visualContainers, boolean isDrillThrough, int ordinal) {
        sb.append("    {\n");
        sb.append("      \"id\": ").append(id).append(",\n");
        sb.append("      \"name\": \"").append(name).append("\",\n");
        sb.append("      \"displayName\": \"").append(displayName).append("\",\n");
        sb.append("      \"filters\": \"[]\",\n");
        sb.append("      \"ordinal\": ").append(ordinal).append(",\n");

        // Drill-through filter setup (Developer Details and Threshold Violations)
        if (isDrillThrough) {
            sb.append("      \"drillFilters\": [\n");
            sb.append("        {\"hierarchyLevel\": \"pull_requests.repository\"},\n");
            sb.append("        {\"hierarchyLevel\": \"pull_requests.author\"}\n");
            sb.append("      ],\n");
        }

        sb.append("      \"visualContainers\": [\n");
        sb.append(visualContainers);
        sb.append("      ],\n");

        sb.append("      \"config\": \"").append(THEME_CFG).append("\",\n");
        sb.append("      \"width\": ").append(PAGE_WIDTH).append(",\n");
        sb.append("      \"height\": ").append(PAGE_HEIGHT).append(",\n");
        sb.append("      \"background\": 1,\n");
        sb.append("      \"defaultDisplayOption\": 0\n");
        sb.append("    }");
    }

    // -------------------------------------------------------------------------
    // Visual container builders
    // Each page pre-defines the key visuals described in the import guide.
    // -------------------------------------------------------------------------

    private String buildExecutiveSummaryVisuals() {
        StringBuilder sb = new StringBuilder();

        // Row 1: Four KPI cards
        sb.append(cardVisual("vc-exec-001", "Total PRs",            "_Measures",  20,  20, 2000));
        sb.append(",\n");
        sb.append(cardVisual("vc-exec-002", "Total Merged",         "_Measures", 330,  20, 3000));
        sb.append(",\n");
        sb.append(cardVisual("vc-exec-003", "Total Rejected",       "_Measures", 640,  20, 4000));
        sb.append(",\n");
        sb.append(cardVisual("vc-exec-004", "PRs Beyond Threshold", "_Measures", 950,  20, 5000));
        sb.append(",\n");

        // Row 2: Donut chart (PR state breakdown) and clustered-bar chart (by repo)
        sb.append(donutChartVisual("vc-exec-005", "pull_requests", "state",        20, 170, 6000));
        sb.append(",\n");
        sb.append(barChartVisual( "vc-exec-006", "pull_requests", "repository",
                                  "Total PRs",    "_Measures",  650, 170, 7000));
        sb.append(",\n");

        // Row 3: Repository summary table
        sb.append(tableVisual("vc-exec-007", "repository_aggregates",
                new String[]{"repository", "total_prs", "merged_prs",
                             "rejected_prs", "open_prs", "avg_time_to_merge_hours"},
                20, 470, 8000));
        sb.append("\n");

        return sb.toString();
    }

    private String buildDeveloperDetailsVisuals() {
        StringBuilder sb = new StringBuilder();

        // Back button placeholder comment (actual back button is added manually)
        // Developer table
        sb.append(tableVisual("vc-dev-001", "developer_aggregates",
                new String[]{"developer", "total_prs", "merged_prs",
                             "rejected_prs", "avg_time_to_merge_hours", "merge_rate"},
                20, 80, 2000));
        sb.append(",\n");

        // Bar chart: PRs per developer
        sb.append(barChartVisual("vc-dev-002", "developer_aggregates", "developer",
                                 "total_prs", "developer_aggregates", 700, 80, 3000));
        sb.append("\n");

        return sb.toString();
    }

    private String buildThresholdViolationsVisuals() {
        StringBuilder sb = new StringBuilder();

        // KPI card
        sb.append(cardVisual("vc-thr-001", "PRs Beyond Threshold", "_Measures", 20, 20, 2000));
        sb.append(",\n");

        // Violations table
        sb.append(tableVisual("vc-thr-002", "threshold_violations",
                new String[]{"repository", "pr_number", "pr_title",
                             "author", "hours_open", "hours_overdue"},
                20, 170, 3000));
        sb.append("\n");

        return sb.toString();
    }

    private String buildTimeAnalysisVisuals() {
        StringBuilder sb = new StringBuilder();

        // KPI cards for time metrics
        sb.append(cardVisual("vc-time-001", "Avg Time to Merge (Hours)", "_Measures",  20, 20, 2000));
        sb.append(",\n");
        sb.append(cardVisual("vc-time-002", "Median Time to Merge",      "_Measures", 330, 20, 3000));
        sb.append(",\n");
        sb.append(cardVisual("vc-time-003", "Min Time to Merge",         "_Measures", 640, 20, 4000));
        sb.append(",\n");
        sb.append(cardVisual("vc-time-004", "Max Time to Merge",         "_Measures", 950, 20, 5000));
        sb.append(",\n");

        // Line chart: merge time trend
        sb.append(lineChartVisual("vc-time-005", "pull_requests", "created_at",
                                  "Avg Time to Merge (Hours)", "_Measures", 20, 170, 6000));
        sb.append("\n");

        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Individual visual helpers
    // -------------------------------------------------------------------------

    /**
     * Generates a Card visual JSON object referencing a DAX measure.
     *
     * @param id          unique visual id (string)
     * @param measureName name of the DAX measure in the _Measures table
     * @param tableAlias  table name that contains the measure
     * @param x,y         canvas position in pixels
     * @param z           z-order / tab order
     */
    private String cardVisual(String id, String measureName, String tableAlias,
                               int x, int y, int z) {
        int w = 290, h = 120;
        String queryRef  = "[" + measureName + "]";
        String tableChar = tableAlias.substring(0, 1).toLowerCase();

        String protoQuery = buildMeasureQuery(tableChar, tableAlias, measureName, queryRef);
        String configJson = buildVisualConfig(id, "card", x, y, z, w, h,
                "\\\"Values\\\":[{\\\"queryRef\\\":\\\"" + esc(queryRef)
                + "\\\",\\\"active\\\":false}]",
                protoQuery,
                "\\\"" + esc(queryRef) + "\\\":{\\\"displayName\\\":\\\"" + esc(measureName) + "\\\"}");

        return buildContainer(x, y, z, w, h, configJson, protoQuery, measureName);
    }

    /** Generates a Donut Chart visual showing a column's value distribution. */
    private String donutChartVisual(String id, String table, String column,
                                    int x, int y, int z) {
        int w = 600, h = 270;
        String tableChar = table.substring(0, 1).toLowerCase();
        String colRef    = table + "." + column;

        String protoQuery = "{"
                + "\\\"Version\\\":2,"
                + "\\\"From\\\":[{\\\"Name\\\":\\\"" + esc(tableChar) + "\\\","
                + "\\\"Entity\\\":\\\"" + esc(table) + "\\\",\\\"Type\\\":0}],"
                + "\\\"Select\\\":["
                + "{\\\"Column\\\":{\\\"Expression\\\":{\\\"SourceRef\\\":{\\\"Source\\\":\\\"" + esc(tableChar) + "\\\"}},"
                + "\\\"Property\\\":\\\"" + esc(column) + "\\\"},\\\"Name\\\":\\\"" + esc(colRef) + "\\\"}"
                + "]}";

        String configJson = buildVisualConfig(id, "donutChart", x, y, z, w, h,
                "\\\"Category\\\":[{\\\"queryRef\\\":\\\"" + esc(colRef) + "\\\",\\\"active\\\":false}]",
                protoQuery, "");

        return buildContainer(x, y, z, w, h, configJson, protoQuery, column);
    }

    /** Generates a Clustered Bar Chart visual. */
    private String barChartVisual(String id, String categoryTable, String categoryColumn,
                                  String measureName, String measureTable,
                                  int x, int y, int z) {
        int w = 590, h = 270;
        String catChar     = categoryTable.substring(0, 1).toLowerCase();
        String measureChar = measureTable.equals(categoryTable)
                ? catChar : measureTable.substring(0, 1).toLowerCase() + "2";
        String colRef      = categoryTable + "." + categoryColumn;
        String queryRef    = "[" + measureName + "]";

        String protoQuery = "{"
                + "\\\"Version\\\":2,"
                + "\\\"From\\\":["
                + "{\\\"Name\\\":\\\"" + esc(catChar) + "\\\","
                + "\\\"Entity\\\":\\\"" + esc(categoryTable) + "\\\",\\\"Type\\\":0}";
        if (!measureChar.equals(catChar)) {
            protoQuery += ",{\\\"Name\\\":\\\"" + esc(measureChar) + "\\\","
                    + "\\\"Entity\\\":\\\"" + esc(measureTable) + "\\\",\\\"Type\\\":0}";
        }
        protoQuery += "],"
                + "\\\"Select\\\":["
                + "{\\\"Column\\\":{\\\"Expression\\\":{\\\"SourceRef\\\":{\\\"Source\\\":\\\"" + esc(catChar) + "\\\"}},"
                + "\\\"Property\\\":\\\"" + esc(categoryColumn) + "\\\"},\\\"Name\\\":\\\"" + esc(colRef) + "\\\"},"
                + "{\\\"Measure\\\":{\\\"Expression\\\":{\\\"SourceRef\\\":{\\\"Source\\\":\\\"" + esc(measureChar) + "\\\"}},"
                + "\\\"Property\\\":\\\"" + esc(measureName) + "\\\"},\\\"Name\\\":\\\"" + esc(queryRef) + "\\\"}"
                + "]}";

        String configJson = buildVisualConfig(id, "clusteredBarChart", x, y, z, w, h,
                "\\\"Category\\\":[{\\\"queryRef\\\":\\\"" + esc(colRef) + "\\\",\\\"active\\\":false}],"
                + "\\\"Y\\\":[{\\\"queryRef\\\":\\\"" + esc(queryRef) + "\\\",\\\"active\\\":false}]",
                protoQuery, "");

        return buildContainer(x, y, z, w, h, configJson, protoQuery, measureName);
    }

    /** Generates a Table visual displaying a set of columns from a table. */
    private String tableVisual(String id, String table, String[] columns,
                               int x, int y, int z) {
        int w = PAGE_WIDTH - 40, h = 220;
        String tableChar = table.substring(0, 1).toLowerCase();

        StringBuilder selectCols = new StringBuilder();
        StringBuilder projCols   = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                selectCols.append(",");
                projCols.append(",");
            }
            String colRef = table + "." + columns[i];
            selectCols.append("{\\\"Column\\\":{\\\"Expression\\\":{\\\"SourceRef\\\":{\\\"Source\\\":\\\"")
                      .append(esc(tableChar)).append("\\\"}},"
                      + "\\\"Property\\\":\\\"").append(esc(columns[i]))
                      .append("\\\"},\\\"Name\\\":\\\"").append(esc(colRef)).append("\\\"}");
            projCols.append("{\\\"queryRef\\\":\\\"").append(esc(colRef)).append("\\\",\\\"active\\\":false}");
        }

        String protoQuery = "{"
                + "\\\"Version\\\":2,"
                + "\\\"From\\\":[{\\\"Name\\\":\\\"" + esc(tableChar) + "\\\","
                + "\\\"Entity\\\":\\\"" + esc(table) + "\\\",\\\"Type\\\":0}],"
                + "\\\"Select\\\":[" + selectCols + "]}";

        String configJson = buildVisualConfig(id, "tableEx", x, y, z, w, h,
                "\\\"Values\\\":[" + projCols + "]",
                protoQuery, "");

        return buildContainer(x, y, z, w, h, configJson, protoQuery, table);
    }

    /** Generates a Line Chart visual for time-based trends. */
    private String lineChartVisual(String id, String table, String axisColumn,
                                   String measureName, String measureTable,
                                   int x, int y, int z) {
        int w = PAGE_WIDTH - 40, h = 270;
        String tableChar   = table.substring(0, 1).toLowerCase();
        String measureChar = measureTable.equals(table) ? tableChar
                : measureTable.substring(0, 1).toLowerCase() + "2";
        String colRef   = table + "." + axisColumn;
        String queryRef = "[" + measureName + "]";

        String protoQuery = "{"
                + "\\\"Version\\\":2,"
                + "\\\"From\\\":["
                + "{\\\"Name\\\":\\\"" + esc(tableChar) + "\\\","
                + "\\\"Entity\\\":\\\"" + esc(table) + "\\\",\\\"Type\\\":0},"
                + "{\\\"Name\\\":\\\"" + esc(measureChar) + "\\\","
                + "\\\"Entity\\\":\\\"" + esc(measureTable) + "\\\",\\\"Type\\\":0}"
                + "],"
                + "\\\"Select\\\":["
                + "{\\\"Column\\\":{\\\"Expression\\\":{\\\"SourceRef\\\":{\\\"Source\\\":\\\"" + esc(tableChar) + "\\\"}},"
                + "\\\"Property\\\":\\\"" + esc(axisColumn) + "\\\"},\\\"Name\\\":\\\"" + esc(colRef) + "\\\"},"
                + "{\\\"Measure\\\":{\\\"Expression\\\":{\\\"SourceRef\\\":{\\\"Source\\\":\\\"" + esc(measureChar) + "\\\"}},"
                + "\\\"Property\\\":\\\"" + esc(measureName) + "\\\"},\\\"Name\\\":\\\"" + esc(queryRef) + "\\\"}"
                + "]}";

        String configJson = buildVisualConfig(id, "lineChart", x, y, z, w, h,
                "\\\"Category\\\":[{\\\"queryRef\\\":\\\"" + esc(colRef) + "\\\",\\\"active\\\":false}],"
                + "\\\"Y\\\":[{\\\"queryRef\\\":\\\"" + esc(queryRef) + "\\\",\\\"active\\\":false}]",
                protoQuery, "");

        return buildContainer(x, y, z, w, h, configJson, protoQuery, measureName);
    }

    // -------------------------------------------------------------------------
    // Low-level JSON helpers
    // -------------------------------------------------------------------------

    /** Builds the prototype query JSON for a single-measure selection. */
    private String buildMeasureQuery(String tableChar, String tableName,
                                     String measureName, String queryRef) {
        return "{"
                + "\\\"Version\\\":2,"
                + "\\\"From\\\":[{\\\"Name\\\":\\\"" + esc(tableChar) + "\\\","
                + "\\\"Entity\\\":\\\"" + esc(tableName) + "\\\",\\\"Type\\\":0}],"
                + "\\\"Select\\\":[{"
                + "\\\"Measure\\\":{"
                + "\\\"Expression\\\":{\\\"SourceRef\\\":{\\\"Source\\\":\\\"" + esc(tableChar) + "\\\"}},"
                + "\\\"Property\\\":\\\"" + esc(measureName) + "\\\"},"
                + "\\\"Name\\\":\\\"" + esc(queryRef) + "\\\""
                + "}]}";
    }

    /**
     * Builds the visual container config JSON (encoded as a JSON string value).
     *
     * @param id          unique visual id
     * @param visualType  Power BI visual type identifier
     * @param x,y,z,w,h   position and size
     * @param projections projection JSON fragment (already escaped for embedding)
     * @param protoQuery  prototype query JSON (already escaped)
     * @param colProps    column properties JSON (already escaped)
     */
    private String buildVisualConfig(String id, String visualType,
                                     int x, int y, int z, int w, int h,
                                     String projections, String protoQuery, String colProps) {
        return "{\\\"name\\\":\\\"" + id + "\\\","
                + "\\\"layouts\\\":[{\\\"id\\\":0,\\\"position\\\":{"
                + "\\\"x\\\":" + x + ",\\\"y\\\":" + y + ",\\\"z\\\":" + z
                + ",\\\"width\\\":" + w + ",\\\"height\\\":" + h
                + ",\\\"tabOrder\\\":" + z + "}}],"
                + "\\\"singleVisual\\\":{"
                + "\\\"visualType\\\":\\\"" + visualType + "\\\","
                + "\\\"projections\\\":{" + projections + "},"
                + "\\\"prototypeQuery\\\":" + protoQuery + ","
                + "\\\"columnProperties\\\":{" + colProps + "},"
                + "\\\"objects\\\":{}}}";
    }

    /**
     * Builds the outer visual container JSON object.
     *
     * @param configJson  config JSON string (already formatted for embedding)
     * @param protoQuery  prototype query JSON (already escaped, used for the query field)
     * @param label       human-readable label used in comments only
     */
    private String buildContainer(int x, int y, int z, int w, int h,
                                  String configJson, String protoQuery, String label) {
        return "        {\n"
                + "          \"x\": " + x + ",\n"
                + "          \"y\": " + y + ",\n"
                + "          \"z\": " + z + ",\n"
                + "          \"width\": " + w + ",\n"
                + "          \"height\": " + h + ",\n"
                + "          \"config\": \"" + configJson + "\",\n"
                + "          \"filters\": \"[]\",\n"
                + "          \"query\": \"" + protoQuery + "\",\n"
                + "          \"dataTransforms\": \"{}\"\n"
                + "        }";
    }

    /** Escapes a string for embedding inside an already-quoted JSON string value. */
    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
