package da.local.uniclog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import da.local.uniclog.execution.ExecutionMojo;
import da.local.uniclog.execution.ExecutionType;
import da.local.uniclog.utils.UtilsInterface;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Mojo(name = "insert", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class InsertJsonMojo extends AbstractMojo implements UtilsInterface {
    @Parameter(alias = "json.in")
    private String jsonInputPath;
    @Parameter(alias = "json.out")
    private String jsonOutputPath;
    @Parameter(alias = "executions", required = true)
    private List<ExecutionMojo> executions;

    @Override
    public void execute() throws MojoExecutionException {

        getLog().info(":: pr: " + getJsonInputPath());
        getLog().info(":: pr: " + getJsonOutputPath());
        getExecutions().forEach(ex -> getLog().info(":: pr: " + ex.toString()));

        DocumentContext json = readJsonObject(getJsonInputPath());
        getLog().info(":: in: " + json.jsonString());

        for (ExecutionMojo ex : getExecutions()) {
            try {
                if (nonNull(ex.getValidation()) && validation(json, ex)) {
                    String err = String.format("Not valid element \"%s\" = %s", ex.getToken(), ex.getValidation());
                    getLog().error(err);
                    throw new MojoExecutionException(err);
                }

                Object value = getElement(ex.getType(), ex.getValue());

                JsonPath pathToArray = JsonPath.compile(ex.getToken());
                getLog().info("pathToArray=" + pathToArray.getPath());

                if (nonNull(ex.getKey())) {
                    json.put(ex.getToken(), ex.getKey(), value);
                } else {
                    ArrayNode array = json.read(ex.getToken());
                    ArrayNode outArrayNode = new ArrayNode(new JsonNodeFactory(true));
                    for (int index = 0; index < array.size(); index++) {
                        if (nonNull(ex.getArrayIndex()) && ex.getArrayIndex() == index) {
                            addElement(ex, outArrayNode, value);
                        }
                        outArrayNode.add(array.get(index));
                    }
                    if (isNull(ex.getArrayIndex())) {
                        addElement(ex, outArrayNode, value);
                    }
                    json.set(ex.getToken(), outArrayNode);
                }

                getLog().info(String.format(":: add -> %s : %s : %s", ex.getToken(), ex.getKey(), ex.getValue()));
            } catch (JsonPathException e) {
                String err = String.format("Not found json element \"%s\"", ex.getToken());
                getLog().error(err);
                if (!ex.isSkipIfNotFoundElement()) {
                    throw new MojoExecutionException(err, e);
                }
            }
        }
        getLog().info("");
        var out = isNull(getJsonOutputPath()) ? getJsonInputPath() : getJsonOutputPath();
        writeJsonObject(json, out);
        getLog().info(":: out: " + json.jsonString());
    }

    private void addElement(ExecutionMojo ex, ArrayNode outArrayNode, Object value) {
        if (ex.getType().equals(ExecutionType.JSON)) {
            outArrayNode.add((JsonNode) value);
        } else if (ex.getType().equals(ExecutionType.STRING)) {
            outArrayNode.add((String) value);
        } else if (ex.getType().equals(ExecutionType.INTEGER)) {
            outArrayNode.add((Integer) value);
        } else if (ex.getType().equals(ExecutionType.DOUBLE)) {
            outArrayNode.add((Double) value);
        } else if (ex.getType().equals(ExecutionType.BOOLEAN)) {
            outArrayNode.add((Boolean) value);
        } else if (ex.getType().equals(ExecutionType.NULL)) {
            outArrayNode.add((JsonNode) null);
        }
    }

    private boolean validation(DocumentContext json, ExecutionMojo ex) {
        Object object = json.read(ex.getToken());
        String node = object.toString();
        getLog().info(String.format(":: validation: %s == %s", ex.getValidation(), node));
        return !node.equals(ex.getValidation());
    }

    @Override
    public String getJsonInputPath() {
        return jsonInputPath;
    }

    @Override
    public String getJsonOutputPath() {
        return jsonOutputPath;
    }

    @Override
    public List<ExecutionMojo> getExecutions() {
        return executions;
    }

}
