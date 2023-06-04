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
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Mojo(name = "modify", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class ModifyJsonMojo extends AbstractMojo {
    private Log log;
    @Parameter(alias = "json.in")
    private String jsonInputPath;
    @Parameter(alias = "json.out")
    private String jsonOutputPath;
    @Parameter(alias = "executions", required = true)
    private List<ModifyExecution> executions;

    private final Configuration configuration = Configuration.builder()
            .mappingProvider(new JacksonMappingProvider())
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .build();

    @Override
    public void execute() throws MojoExecutionException {

        // log.info(":: pr: " + jsonInputPath);
        // log.info(":: pr: " + jsonOutputPath);
        // executions.forEach(ex -> log.info(":: pr: "
        //         + ex.getToken() + " : " + ex.getValue() + " : " + ex.getType()));

        DocumentContext json = readJsonObject();
        log.debug(":: in: " + json.jsonString());

        for (ModifyExecution ex : executions) {
            try {
                Object value = getElement(ex.getType(), ex.getValue());
                json.set(ex.getToken(), value);
                log.info(String.format(":: %s -> %s", ex.getToken(), value));
            } catch (JsonPathException e) {
                String err = String.format("Not found json element \"%s\"", ex.getToken());
                log.error(err);
                throw new MojoExecutionException(err, e);
            }
        }

        writeJsonObject(json);
        log.debug(":: out: " + json.jsonString());
    }

    private Object getElement(ModifyElementType type, String value) throws MojoExecutionException {
        switch (type) {
            case BOOLEAN:
                return getPrimitiveValue(value, Boolean::valueOf, ModifyElementType.BOOLEAN);
            case INTEGER:
                return getPrimitiveValue(value, Integer::valueOf, ModifyElementType.INTEGER);
            case DOUBLE:
                return getPrimitiveValue(value, Double::valueOf, ModifyElementType.DOUBLE);
            case JSON:
                return getJsonValue(value);
            case STRING:
                return isNull(value) ? EMPTY : value;
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

    private DocumentContext readJsonObject() throws MojoExecutionException {
        if (isNull(jsonInputPath)) {
            throw new MojoExecutionException("Parameter 'json.in' can't be null.");
        }

        try (InputStream in = Files.newInputStream(FileSystems.getDefault().getPath(jsonInputPath))) {
            return JsonPath.using(configuration).parse(in, UTF_8.name());
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read file " + jsonInputPath, e);
        }
    }

    private void writeJsonObject(DocumentContext json) throws MojoExecutionException {
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

    private <T> T getPrimitiveValue(String value, Function<String, T> fun, ModifyElementType type) throws MojoExecutionException {
        try {
            return fun.apply(value);
        } catch (NumberFormatException ex) {
            String err = String.format("Convert format exception: %s -> %s", value, type.getValue());
            log.error(err);
            throw new MojoExecutionException(err, ex);
        }
    }
}
