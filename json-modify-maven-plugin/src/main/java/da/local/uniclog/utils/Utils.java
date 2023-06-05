package da.local.uniclog.utils;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;

public class Utils {
    private final Configuration configuration = Configuration.builder()
            .mappingProvider(new JacksonMappingProvider())
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .build();

    public Configuration getConfiguration() {
        return configuration;
    }

    public DocumentContext readJsonObject(String jsonInputPath) throws MojoExecutionException {
        if (isNull(jsonInputPath)) {
            throw new MojoExecutionException("Parameter 'json.in' can't be null.");
        }

        try (InputStream in = Files.newInputStream(FileSystems.getDefault().getPath(jsonInputPath))) {
            return JsonPath.using(configuration).parse(in, UTF_8.name());
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read file " + jsonInputPath, e);
        }
    }

    public void writeJsonObject(DocumentContext json, String jsonOutputPath) throws MojoExecutionException {
        try {
            ObjectWriter writer = new ObjectMapper().writer(new DefaultPrettyPrinter());
            writer.writeValue(Paths.get(jsonOutputPath).toFile(), json.json());
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write file " + jsonOutputPath, e);
        }
    }

}
