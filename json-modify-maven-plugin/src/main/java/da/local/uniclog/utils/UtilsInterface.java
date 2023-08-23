package da.local.uniclog.utils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import da.local.uniclog.execution.ExecutionMojo;
import da.local.uniclog.execution.ExecutionType;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.List;

public interface UtilsInterface {
    Utils utils = new Utils();

    String getJsonInputPath();
    String getJsonOutputPath();
    List<ExecutionMojo> getExecutions();

    default Object getElement(ExecutionType type, String value) throws MojoExecutionException {
        return utils.getElement(type, value);
    }

    default DocumentContext readJsonObject(String jsonInputPath) throws MojoExecutionException {
        return utils.readJsonObject(jsonInputPath);
    }

    default Configuration getConfiguration() throws MojoExecutionException {
        return utils.getConfiguration();
    }

    default void writeJsonObject(DocumentContext json, String jsonOutputPath) throws MojoExecutionException {
        utils.writeJsonObject(json, jsonOutputPath);
    }
}
