package da.local.uniclog;

import com.jayway.jsonpath.DocumentContext;
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

import static java.lang.String.format;

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
        ExecuteConsumer<DocumentContext, ExecutionMojo, Integer>  executeConsumer = (json, ex, exIndex) -> {
            Object value = getElement(ex.getType(), ex.getValue());
            var old = json.read(ex.getToken());
            json.set(ex.getToken(), value);
            info(format(":%d: md: %s: %s -> %s", exIndex, ex.getToken(), old, value));
        };

        executeAction(executeConsumer);
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
