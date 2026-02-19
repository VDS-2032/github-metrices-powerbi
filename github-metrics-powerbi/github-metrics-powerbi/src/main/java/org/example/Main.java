package org.example;

import java.io.File;
import java.io.IOException;

/**
 * Entry point for the GitHub Metrics Power BI Desktop Import Generator.
 *
 * <p>Usage: java Main [projectDir] [outputFile]
 * <ul>
 *   <li>projectDir  - root folder containing the powerbi/ and data/ sub-folders
 *                     (defaults to the parent of the current working directory)</li>
 *   <li>outputFile  - path for the generated .pbit template file
 *                     (defaults to GitHubMetrics.pbit in the current directory)</li>
 * </ul>
 */
public class Main {
    public static void main(String[] args) {
        String projectDir = args.length > 0 ? args[0]
                : new File("..").getAbsolutePath();
        String outputPath = args.length > 1 ? args[1] : "GitHubMetrics.pbit";

        System.out.println("GitHub Metrics - Power BI Desktop Import Generator");
        System.out.println("====================================================");
        System.out.printf("Source directory : %s%n", new File(projectDir).getAbsolutePath());
        System.out.printf("Output file      : %s%n%n", new File(outputPath).getAbsolutePath());

        try {
            PowerBIProjectGenerator generator = new PowerBIProjectGenerator(projectDir);
            generator.generate(outputPath);

            System.out.printf("%nSuccessfully generated: %s%n", outputPath);
            System.out.println("Open this file in Power BI Desktop to start building your dashboard.");
            System.out.println();
            System.out.println("The template includes:");
            System.out.println("  - 7 data tables with correct column types");
            System.out.println("  - 18 pre-built DAX measures");
            System.out.println("  - All table relationships configured");
            System.out.println("  - DataFolderPath parameter for easy data refresh");
            System.out.println("  - 4 report pages ready for visuals");
        } catch (IOException e) {
            System.err.println("Error generating Power BI project: " + e.getMessage());
            System.exit(1);
        }
    }
}