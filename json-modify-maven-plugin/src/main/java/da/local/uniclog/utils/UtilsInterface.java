package da.local.uniclog.utils;

import com.jayway.jsonpath.DocumentContext;
import da.local.uniclog.execution.ExecutionMojo;
import da.local.uniclog.execution.ExecutionType;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.util.List;

import static java.util.Objects.nonNull;

public interface UtilsInterface {
    Utils utils = new Utils();

    String getJsonInputPath();
    String getJsonOutputPath();
    List<ExecutionMojo> getExecutions();
    Log getLogger();

    default Object getElement(ExecutionType type, String value) throws MojoExecutionException {
        return utils.getElement(type, value);
    }

    default DocumentContext readJsonObject(String jsonInputPath) throws MojoExecutionException {
        return utils.readJsonObject(jsonInputPath);
    }

    default void writeJsonObject(DocumentContext json, String jsonOutputPath) throws MojoExecutionException {
        utils.writeJsonObject(json, jsonOutputPath);
    }

    default void validation(DocumentContext json, ExecutionMojo ex, Integer exIndex) throws UnsupportedOperationException  {
        if (nonNull(ex.getValidation()) && utils.validation(json, ex, exIndex, getLogger())) {
            String err = String.format(":%d: Not valid element \"%s\" = %s", exIndex, ex.getToken(), ex.getValidation());
            getLogger().error(err);
            throw new UnsupportedOperationException(err);
        }
    }

}
