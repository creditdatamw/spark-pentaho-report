package com.creditdatamw.labs.sparkpentaho.autogen;

import com.creditdatamw.labs.sparkpentaho.config.ApiConfiguration;
import com.creditdatamw.labs.sparkpentaho.config.Method;
import com.creditdatamw.labs.sparkpentaho.config.ReportConfiguration;
import com.creditdatamw.labs.sparkpentaho.reportdefinition.ReportDefinitionDirectoryWalker;
import com.creditdatamw.labs.sparkpentaho.reports.OutputType;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Pentaho API Generator
 * <br/>
 * Generates an API configuration for <code>kapenta</code> using pentaho report files (*.prpt)
 * in a the given directory to the given output file.<br/>
 * The YAML configuration is generated out with all the fields defined.
 * 
 * @author Zikani
 */
public class PentahoApiGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PentahoApiGenerator.class);
    private static final YAMLMapper yamlMapper = new YAMLMapper();
    private final ResourceManager resourceManager;
    private ReportDefinitionDirectoryWalker directoryWalker;
    private final Path sourceDir;
    private final Path outputFile;

    /**
     * Creates an ApiConfiguration generator
     *
     * @param sourceDir The directory to search for reports from
     * @param outputFile The file to write the YAML configuration to
     */
    public PentahoApiGenerator(Path sourceDir, Path outputFile) {
        this.sourceDir = sourceDir;
        this.outputFile = outputFile;
        ClassicEngineBoot.getInstance().start();
        this.resourceManager = new ResourceManager();
        resourceManager.registerDefaults();
        this.directoryWalker = new ReportDefinitionDirectoryWalker(
            this.sourceDir,
            -1,
            this.resourceManager
        );
    }

    public void generate() {
        ApiConfiguration apiConfiguration = buildConfiguration();

        Objects.requireNonNull(apiConfiguration);

        try (OutputStream fos = Files.newOutputStream(outputFile)) {
            yamlMapper.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
            yamlMapper.writeValue(fos, apiConfiguration);
            LOGGER.debug("Wrote output YAML to: {}", outputFile);
        } catch(Exception e) {
            LOGGER.error("Failed to write output YAML", e);
            throw new RuntimeException("Failed to write YAML to: " + outputFile);
        }
    }

    private ApiConfiguration buildConfiguration() {
        ApiConfiguration apiConfiguration = new ApiConfiguration();

        apiConfiguration.setApiRoot("/api");
        apiConfiguration.setHost("localhost");
        apiConfiguration.setPort(4567);
        apiConfiguration.setReports(directoryWalker.getReportDefinitionStream()
            .map(reportDefinition -> {
                ReportConfiguration reportConfiguration = new ReportConfiguration();

                String reportPath = reportDefinition.getReportName().toLowerCase().replace(" ", "_");

                reportConfiguration.setPath(reportPath.startsWith("/") ? reportPath : "/".concat(reportPath));

                reportConfiguration.setReportName(reportDefinition.getReportName());
                reportConfiguration.setDescription(reportDefinition.getDescription());
                reportConfiguration.setVersion(reportDefinition.getVersion());
                reportConfiguration.setParameters(reportDefinition.getParameters());
                reportConfiguration.setReportFilePath(reportDefinition.getReportFilePath());

                // Support both GET and POST by default
                reportConfiguration.setMethods(Method.ALL);

                // Add all the output types for this report
                reportConfiguration.setExtensions(Arrays.stream(OutputType.values())
                    .filter(o -> !o.equals(OutputType.NONE))
                    .map(OutputType::name)
                    .collect(Collectors.toList()));

                return reportConfiguration;
            })
            .collect(Collectors.toList()));

        return apiConfiguration;
    }


}