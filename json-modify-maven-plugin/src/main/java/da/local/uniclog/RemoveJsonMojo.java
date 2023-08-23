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

@Mojo(name = "remove", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class RemoveJsonMojo extends AbstractMojo implements UtilsInterface {
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
        // executions.forEach(ex -> log.info(":: pr: " + ex.getToken() + " : " + ex.getValue() + " : " + ex.getType()));

        DocumentContext json = readJsonObject(jsonInputPath);
        getLogger().debug(":: in: " + json.jsonString());
        var exIndex = 1;
        for (ExecutionMojo ex : executions) {
            try {
                validation(json, ex, exIndex);

                json.delete(ex.getToken());
                getLogger().info(String.format(":%d: rm: %s", exIndex,  ex.getToken()));
                exIndex++;
            } catch (JsonPathException e) {
                String err = String.format("Not found json element \"%s\"", ex.getToken());
                getLogger().error(err);
                if (!ex.isSkipIfNotFoundElement()) {
                    throw new MojoExecutionException(err, e);
                }
            }
        }

        jsonOutputPath = isNull(jsonOutputPath) ? jsonInputPath : jsonOutputPath;
        writeJsonObject(json, jsonOutputPath);
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
