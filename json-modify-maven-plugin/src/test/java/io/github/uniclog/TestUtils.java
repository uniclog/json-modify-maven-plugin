package io.github.uniclog;

import io.github.uniclog.execution.ExecutionMojo;
import io.github.uniclog.execution.ExecutionType;
import io.github.uniclog.utils.UtilsInterface;
import org.apache.maven.plugin.MojoExecutionException;

import static io.github.uniclog.utils.FileUtils.readJsonObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

interface TestUtils {

    String outputPath = "src/test/resources/testInsert_out.json";
    String inputPath = "src/test/resources/testInsert_in.json";

    UtilsInterface getService();

    default ExecutionMojo getSpyExecutionMojo(String token, String key, String value, ExecutionType type, String validation, Boolean skipIfNotFound, Integer arrayIndex) {
        ExecutionMojo execution = spy(new ExecutionMojo());
        doReturn(token).when(execution).getToken();
        doReturn(key).when(execution).getKey();
        doReturn(value).when(execution).getValue();
        doReturn(type).when(execution).getType();
        doReturn(validation).when(execution).getValidation();
        doReturn(arrayIndex).when(execution).getArrayIndex();
        doReturn(skipIfNotFound).when(execution).isSkipIfNotFoundElement();
        return execution;
    }

    default void validation(ExecutionMojo execution, String validation, String token) throws MojoExecutionException {
        setValidation(execution, validation);
        setToken(execution, token);
        var document = readJsonObject(outputPath);
        getService().validation(document, execution, null);
    }

    default void setValidation(ExecutionMojo execution, String validation) {
        doReturn(validation).when(execution).getValidation();
    }

    default void setToken(ExecutionMojo execution, String validation) {
        doReturn(validation).when(execution).getToken();
    }
}
