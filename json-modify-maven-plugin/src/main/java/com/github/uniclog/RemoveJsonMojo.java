package com.github.uniclog;

import com.github.uniclog.execution.ExecutionMojo;
import com.github.uniclog.utils.ExecuteConsumer;
import com.github.uniclog.utils.JmLogger;
import com.github.uniclog.utils.UtilsInterface;
import com.jayway.jsonpath.DocumentContext;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

import static java.lang.String.format;

@Mojo(name = "remove", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class RemoveJsonMojo extends AbstractMojo implements UtilsInterface, JmLogger {
    @Parameter(alias = "json.in", required = true)
    private String jsonInputPath;
    @Parameter(alias = "json.out")
    private String jsonOutputPath;
    @Parameter(alias = "executions", required = true)
    private List<ExecutionMojo> executions;

    @Override
    public void execute() throws MojoExecutionException {
        ExecuteConsumer<DocumentContext, ExecutionMojo, Integer> executeConsumer = (json, ex, exIndex) -> {
            json.delete(ex.getToken());
            info(format(":%d: rm: %s", exIndex, ex.getToken()));
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
