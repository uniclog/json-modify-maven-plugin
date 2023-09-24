package da.local.uniclog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import da.local.uniclog.execution.ExecutionMojo;
import da.local.uniclog.utils.ExecuteConsumer;
import da.local.uniclog.utils.JmLogger;
import da.local.uniclog.utils.UtilsInterface;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

import static da.local.uniclog.execution.ExecutionType.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Mojo(name = "insert", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class InsertJsonMojo extends AbstractMojo implements UtilsInterface, JmLogger {
    @Parameter(alias = "json.in", required = true)
    private String jsonInputPath;
    @Parameter(alias = "json.out")
    private String jsonOutputPath;
    @Parameter(alias = "executions", required = true)
    private List<ExecutionMojo> executions;

    @Override
    public void execute() throws MojoExecutionException {
        ExecuteConsumer<DocumentContext, ExecutionMojo, Integer> executeConsumer = (json, ex, exIndex) -> {
            Object value = getElement(ex.getType(), ex.getValue());

            JsonPath pathToArray = JsonPath.compile(ex.getToken());
            debug("pathToArray=" + pathToArray.getPath());

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
            info(String.format(":%d: ad: %s | %s | %s", exIndex, ex.getToken(), ex.getKey(), ex.getValue()));
        };

        executeAction(executeConsumer);
    }

    private void addElement(ExecutionMojo ex, ArrayNode outArrayNode, Object value) {
        if (ex.getType().equals(JSON)) {
            outArrayNode.add((JsonNode) value);
        } else if (ex.getType().equals(STRING)) {
            outArrayNode.add((String) value);
        } else if (ex.getType().equals(INTEGER)) {
            outArrayNode.add((Integer) value);
        } else if (ex.getType().equals(DOUBLE)) {
            outArrayNode.add((Double) value);
        } else if (ex.getType().equals(BOOLEAN)) {
            outArrayNode.add((Boolean) value);
        } else if (ex.getType().equals(NULL)) {
            outArrayNode.add((JsonNode) null);
        }
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

    @Override
    public Log getLogger() {
        return getLog();
    }
}
