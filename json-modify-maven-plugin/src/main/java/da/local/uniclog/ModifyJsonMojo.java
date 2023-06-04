package da.local.uniclog;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;

@Mojo(name = "modify", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class ModifyJsonMojo extends AbstractMojo {
    private Log log;
    @Parameter(alias = "json.in", required = true)
    private String jsonInputPath;
    @Parameter(alias = "json.out")
    private String jsonOutputPath;
    @Parameter(required = true)
    private List<ModifyExecution> executions;

    @Override
    public void execute() throws MojoExecutionException {

        // log.info(":: pr: " + jsonInputPath);
        // log.info(":: pr: " + jsonOutputPath);
        // executions.forEach(ex -> log.info(":: pr: "
        //         + ex.getToken() + " : " + ex.getValue() + " : " + ex.getType()));

        DocumentContext json = getJsonFile();
        log.debug(":: in: " + json.jsonString());
        for (ModifyExecution ex : executions) {
            try {
                json.set(ex.getToken(), getElement(ex.getType(), ex.getValue()));
                log.info(String.format(":: %s -> %s", ex.getToken(), ex.getValue()));
            } catch (JsonPathException e) {
                String err = String.format("Not found json element \"%s\"", ex.getToken());
                log.error(err);
                throw new MojoExecutionException(err, e);
            }
        }

        saveJsonFile(json);
        log.debug(":: out: " + json.jsonString());
    }

    private Object getElement(ModifyElementType type, String value) throws MojoExecutionException {
        switch (type) {
            case BOOLEAN:
                return getBooleanValue(value);
            case INTEGER:
                return getIntegerValue(value);
            case DOUBLE:
                return getDoubleValue(value);
            case JSON:
                return getJsonValue(value);
            default:
                return value;
        }
    }

    @Override
    public Log getLog() {
        return log;
    }

    @Override
    public void setLog(Log log) {
        this.log = log;
    }

    private DocumentContext getJsonFile() throws MojoExecutionException {
        Configuration configuration = Configuration.builder()
                .mappingProvider(new JacksonMappingProvider())
                .jsonProvider(new JacksonJsonNodeJsonProvider())
                .build();
        try (InputStream in = Files.newInputStream(FileSystems.getDefault().getPath(jsonInputPath))) {
            return JsonPath.using(configuration).parse(in, UTF_8.name());
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read file " + jsonInputPath, e);
        }
    }

    private void saveJsonFile(DocumentContext json) throws MojoExecutionException {
        jsonOutputPath = isNull(jsonOutputPath) ? jsonInputPath : jsonOutputPath;
        try {
            ObjectWriter writer = new ObjectMapper().writer(new DefaultPrettyPrinter());
            writer.writeValue(Paths.get(jsonOutputPath).toFile(), json.json());
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write file " + jsonOutputPath, e);
        }
    }

    private <T> T getJsonValue(String value) throws MojoExecutionException {
        Configuration configuration = Configuration.builder()
                .mappingProvider(new JacksonMappingProvider())
                .jsonProvider(new JacksonJsonNodeJsonProvider())
                .build();
        DocumentContext valueAsJson = JsonPath.using(configuration).parse(value);
        log.debug(":: JSON: " + valueAsJson.jsonString());
        return valueAsJson.json();
    }

    private Integer getIntegerValue(String value) throws MojoExecutionException {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            String err = String.format("Convert format exception: %s -> INTEGER", value);
            log.error(err);
            throw new MojoExecutionException(err, ex);
        }
    }

    private Double getDoubleValue(String value) throws MojoExecutionException {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException ex) {
            String err = String.format("Convert format exception: %s -> DOUBLE", value);
            log.error(err);
            throw new MojoExecutionException(err, ex);
        }
    }

    private Boolean getBooleanValue(String value) throws MojoExecutionException {
        if (!Boolean.TRUE.toString().equalsIgnoreCase(value) &&
                !Boolean.FALSE.toString().equalsIgnoreCase(value)) {
            String err = String.format("Convert format exception: %s -> BOOLEAN", value);
            log.error(err);
            throw new MojoExecutionException(err);
        }
        return Boolean.valueOf(value);
    }
}
