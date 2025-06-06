package uniclog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Mojo(name = "validate-display-values", defaultPhase = LifecyclePhase.VERIFY)
public class DisplayFieldValidatorMojo extends AbstractMojo {

    @Parameter(property = "inputFile", required = true)
    private File inputFile;

    @Parameter(property = "allowedValuesFile")
    private File allowedValuesFile;

    private static boolean isError = false;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode allowedRoot;
            if (allowedValuesFile != null && allowedValuesFile.exists()) {
                allowedRoot = mapper.readTree(allowedValuesFile);
            } else {
                try (InputStream in = getClass().getResourceAsStream("/default-allowed-display-values.json")) {
                    if (in == null) {
                        throw new MojoExecutionException("Default allowed values file not found in resources.");
                    }
                    allowedRoot = mapper.readTree(in);
                }
            }
            JsonNode allowedArray = allowedRoot.path("allowedDisplayValues");
            if (!allowedArray.isArray()) {
                throw new MojoFailureException("allowedDisplayValues must be an array");
            }

            Set<String> allowedDisplayValues = new HashSet<>();
            for (JsonNode node : allowedArray) {
                allowedDisplayValues.add(node.asText());
            }
            JsonNode root = mapper.readTree(inputFile);
            validateRecursively(root, allowedDisplayValues, "");
            if (isError) {
                throw new MojoFailureException("Display value validation failed.");
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read JSON files", e);
        }
    }

    private void validateRecursively(JsonNode node, Set<String> allowedValues, String path) throws MojoFailureException {
        if (node.isObject()) {
            if (node.has("managed")) {
                JsonNode managedNode = node.get("managed");
                if (managedNode.has("display") && managedNode.get("display").isArray()) {
                    for (JsonNode val : managedNode.get("display")) {
                        String value = val.asText();
                        if (!allowedValues.contains(value)) {
                            getLog().info("Invalid display value '" + value +
                                    "' at path '" + path + ".managed.display'.");
                            isError = true;
                        }
                    }
                }
            }
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String childPath = path.isEmpty() ? entry.getKey() : path + "." + entry.getKey();
                validateRecursively(entry.getValue(), allowedValues, childPath);
            }
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                validateRecursively(element, allowedValues, path);
            }
        }
    }
}