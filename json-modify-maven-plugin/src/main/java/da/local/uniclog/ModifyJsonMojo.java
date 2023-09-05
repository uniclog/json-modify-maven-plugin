package da.local.uniclog;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPathException;
import da.local.uniclog.execution.ExecutionMojo;
import da.local.uniclog.utils.UtilsInterface;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

import static java.util.Objects.isNull;

@Mojo(name = "modify", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class ModifyJsonMojo extends AbstractMojo implements UtilsInterface {
    @Parameter(alias = "json.in")
    private String jsonInputPath;
    @Parameter(alias = "json.out")
    private String jsonOutputPath;
    @Parameter(alias = "executions", required = true)
    private List<ExecutionMojo> executions;

    @Override
    public void execute() throws MojoExecutionException {

        // log.info(":: pr: " + jsonInputPath);
        // log.info(":: pr: " + jsonOutputPath);
        // executions.forEach(ex -> log.info(":: pr: "
        //         + ex.getToken() + " : " + ex.getValue() + " : " + ex.getType()));

        DocumentContext json = readJsonObject(jsonInputPath);
        getLogger().debug(":: in: " + json.jsonString());
        var exIndex = 1;
        for (ExecutionMojo ex : executions) {
            try {
                validation(json, ex, exIndex);

                Object value = getElement(ex.getType(), ex.getValue());
                var old = json.read(ex.getToken());
                json.set(ex.getToken(), value);
                getLogger().info(String.format(":%d: md: %s: %s -> %s", exIndex, ex.getToken(), old, value));
                exIndex++;
            } catch (JsonPathException e) {
                String err = String.format("Not found json element \"%s\"", ex.getToken());
                getLogger().error(err);
                throw new MojoExecutionException(err, e);
            }
        }

        writeJsonObject(json, isNull(getJsonOutputPath()) ? getJsonInputPath() : getJsonOutputPath());
        getLogger().debug(":: out: " + json.jsonString());
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
