package org.example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Generates a Power BI Desktop Template (.pbit) file from the GitHub Metrics
 * project folder.
 *
 * <p>A .pbit file is a ZIP archive containing:
 * <ul>
 *   <li>[Content_Types].xml – standard Open Packaging Convention content types</li>
 *   <li>Version            – format version string ("3.0")</li>
 *   <li>Settings           – Power BI settings JSON</li>
 *   <li>Metadata           – report metadata JSON</li>
 *   <li>SecurityBindings   – empty security bindings</li>
 *   <li>DataModelSchema    – TMSL data model definition (tables, measures, relationships)</li>
 *   <li>Report/Layout      – report page layout JSON</li>
 *   <li>DiagramLayout      – model diagram layout JSON</li>
 * </ul>
 */
public class PowerBIProjectGenerator {

    private final String projectDir;

    public PowerBIProjectGenerator(String projectDir) {
        this.projectDir = projectDir;
    }

    /**
     * Generates the .pbit file at the specified output path.
     *
     * @param outputPath destination file path (should end with .pbit)
     * @throws IOException if the file cannot be written
     */
    public void generate(String outputPath) throws IOException {
        System.out.println("Building Power BI template ...");

        try (FileOutputStream fos = new FileOutputStream(outputPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            System.out.println("  Writing [Content_Types].xml ...");
            addEntry(zos, "[Content_Types].xml", buildContentTypes());

            System.out.println("  Writing Version ...");
            addEntry(zos, "Version", "3.0");

            System.out.println("  Writing Settings ...");
            addEntry(zos, "Settings", buildSettings());

            System.out.println("  Writing Metadata ...");
            addEntry(zos, "Metadata", buildMetadata());

            System.out.println("  Writing SecurityBindings ...");
            addEntry(zos, "SecurityBindings", "");

            System.out.println("  Building DataModelSchema ...");
            DataModelSchemaBuilder schemaBuilder = new DataModelSchemaBuilder();
            addEntry(zos, "DataModelSchema", schemaBuilder.build());

            System.out.println("  Building Report/Layout ...");
            ReportLayoutBuilder layoutBuilder = new ReportLayoutBuilder();
            addEntry(zos, "Report/Layout", layoutBuilder.build());

            System.out.println("  Writing DiagramLayout ...");
            addEntry(zos, "DiagramLayout", buildDiagramLayout());
        }

        System.out.println("  Template file created successfully.");
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void addEntry(ZipOutputStream zos, String name, String content) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private String buildContentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n"
                + "  <Default Extension=\"json\" ContentType=\"application/json\"/>\n"
                + "  <Default Extension=\"xml\"  ContentType=\"application/xml\"/>\n"
                + "  <Override PartName=\"/Report/Layout\"   ContentType=\"application/json\"/>\n"
                + "  <Override PartName=\"/DataModelSchema\" ContentType=\"application/json\"/>\n"
                + "  <Override PartName=\"/DiagramLayout\"   ContentType=\"application/json\"/>\n"
                + "  <Override PartName=\"/SecurityBindings\" ContentType=\"application/json\"/>\n"
                + "  <Override PartName=\"/Settings\"        ContentType=\"application/json\"/>\n"
                + "  <Override PartName=\"/Metadata\"        ContentType=\"application/json\"/>\n"
                + "  <Override PartName=\"/Version\"         ContentType=\"application/octet-stream\"/>\n"
                + "</Types>";
    }

    private String buildSettings() {
        return "{\"Version\":\"3.0\",\"AutoRecoveryOriginalFileName\":\"\"}";
    }

    private String buildMetadata() {
        return "{"
                + "\"version\":\"3.0\","
                + "\"createdFrom\":\"GitHub Metrics Power BI Generator\""
                + "}";
    }

    private String buildDiagramLayout() {
        return "{\"version\":1}";
    }
}
