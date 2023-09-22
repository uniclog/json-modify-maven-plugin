package da.local.uniclog;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPathException;
import da.local.uniclog.execution.ExecutionMojo;
import da.local.uniclog.utils.JmLogger;
import da.local.uniclog.utils.UtilsInterface;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.isNull;

@Mojo(name = "modify", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class ModifyJsonMojo extends AbstractMojo implements UtilsInterface, JmLogger {
    @Parameter(alias = "json.in", required = true)
    private String jsonInputPath;
    @Parameter(alias = "json.out")
    private String jsonOutputPath;
    @Parameter(alias = "executions", required = true)
    private List<ExecutionMojo> executions;

    @Override
    public void execute() throws MojoExecutionException {

        debug(":: pr: " + getJsonInputPath());
        debug(":: pr: " + getJsonOutputPath());
        getExecutions().forEach(ex -> debug(":: pr: " + ex.toString()));

        DocumentContext json = readJsonObject(getJsonInputPath());
        debug(":: in: " + json.jsonString());
        var exIndex = 1;
        for (ExecutionMojo ex : getExecutions()) {
            try {
                validation(json, ex, exIndex);

                Object value = getElement(ex.getType(), ex.getValue());
                var old = json.read(ex.getToken());
                json.set(ex.getToken(), value);
                info(format(":%d: md: %s: %s -> %s", exIndex, ex.getToken(), old, value));
                exIndex++;
            } catch (JsonPathException e) {
                String err = format(":%d: not found json element %s : %s : %s", exIndex, ex.getToken(), ex.getKey(), ex.getValue());
                if (!ex.isSkipIfNotFoundElement()) {
                    error(err);
                    throw new MojoExecutionException(err, e);
                }
                error("Skip: " + err);
            } catch (UnsupportedOperationException e) {
                String err = format(":%d: not modify json element %s : %s : %s", exIndex, ex.getToken(), ex.getKey(), ex.getValue());
                if (!ex.isSkipIfNotFoundElement()) {
                    error(err);
                    throw new MojoExecutionException(err, e);
                }
                error("Skip: " + err);
            }
        }

        writeJsonObject(json, isNull(getJsonOutputPath()) ? getJsonInputPath() : getJsonOutputPath());
        debug(":: out: " + json.jsonString());
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
