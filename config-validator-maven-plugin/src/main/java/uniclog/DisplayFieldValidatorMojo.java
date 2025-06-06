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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Mojo(name = "validate-display-values", defaultPhase = LifecyclePhase.VERIFY)
public class DisplayFieldValidatorMojo extends AbstractMojo {

    @Parameter(property = "inputFile", required = true)
    private File inputFile;

    @Parameter(property = "allowedValuesFile", required = true)
    private File allowedValuesFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode allowedRoot = mapper.readTree(allowedValuesFile);
            JsonNode allowedArray = allowedRoot.path("allowedDisplayValues");
            if (!allowedArray.isArray()) {
                throw new MojoFailureException("allowedDisplayValues must be an array");
            }

            Set<String> allowedDisplayValues = new HashSet<>();
            for (JsonNode node : allowedArray) {
                allowedDisplayValues.add(node.asText());
            }

            JsonNode root = mapper.readTree(inputFile);
            JsonNode managedApprole = root.path("managed/approle");

            if (managedApprole.isMissingNode() || !managedApprole.isObject()) {
                throw new MojoFailureException("Missing or invalid 'managed/approle' section in JSON");
            }

            Iterator<Map.Entry<String, JsonNode>> fields = managedApprole.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> fieldEntry = fields.next();
                JsonNode managedNode = fieldEntry.getValue().path("managed");

                if (managedNode.has("display")) {
                    JsonNode displayNode = managedNode.get("display");

                    if (displayNode.isArray()) {
                        for (JsonNode value : displayNode) {
                            String val = value.asText();
                            if (!allowedDisplayValues.contains(val)) {
                                throw new MojoFailureException("Invalid display value '" + val +
                                        "' in field '" + fieldEntry.getKey() + "'. Allowed: " + allowedDisplayValues);
                            }
                        }
                    }
                }
            }

            getLog().info("Display field validation passed.");
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read JSON files", e);
        }
    }
}