package io.github.uniclog.utils;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPathException;
import io.github.uniclog.execution.DocumentType;
import io.github.uniclog.execution.ExecutionMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.List;

import static io.github.uniclog.utils.FileUtils.*;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public interface UtilsInterface extends JmLogger {

    String getJsonInputPath();

    String getJsonOutputPath();

    List<ExecutionMojo> getExecutions();

    default void executeAction(ExecuteConsumer<Object, ExecutionMojo, Integer> executeConsumer, DocumentType docType) throws MojoExecutionException {
        getLogger().debug(":: pr: " + getJsonInputPath());
        getLogger().debug(":: pr: " + getJsonOutputPath());
        getExecutions().forEach(ex -> getLogger().debug(":: pr: " + ex.toString()));

        Object out;
        switch (docType) {
            case DOCUMENT: {
                out = documentExecution(executeConsumer);
                break;
            }
            case JSON:
            default:
                out = jsonExecution(executeConsumer);
        }

        getLogger().debug(":: out: " + out);
    }

    default DocumentContext jsonExecution(ExecuteConsumer<Object, ExecutionMojo, Integer> executeConsumer) throws MojoExecutionException {
        DocumentContext json = readJsonObject(getJsonInputPath());
        getLogger().debug(":: in: " + json.jsonString());
        var executions = getExecutions();
        for (int exIndex = 0; exIndex < executions.size(); exIndex++) {
            ExecutionMojo ex = executions.get(exIndex);
            try {
                validation(json, ex, exIndex);
                executeConsumer.accept(json, ex, exIndex);
            } catch (JsonPathException e) {
                String err = format("(%d) not found json element %s : %s : %s", exIndex, ex.getToken(), ex.getKey(), ex.getValue());
                if (!ex.isSkipIfNotFoundElement()) {
                    getLogger().error(err);
                    throw new MojoExecutionException(err, e);
                }
                getLogger().warn("Skip: " + err);
            } catch (UnsupportedOperationException e) {
                String err = format("(%d) not modify json element %s : %s : %s", exIndex, ex.getToken(), ex.getKey(), ex.getValue());
                if (!ex.isSkipIfNotFoundElement()) {
                    getLogger().error(err);
                    throw new MojoExecutionException(err, e);
                }
                getLogger().warn("Skip: " + err);
            }
        }

        return writeJsonObject(json, isNull(getJsonOutputPath()) ? getJsonInputPath() : getJsonOutputPath());
    }

    default Object documentExecution(ExecuteConsumer<Object, ExecutionMojo, Integer> executeConsumer) throws MojoExecutionException {
        StringBuilder document = new StringBuilder(readObject(getJsonInputPath()));
        getLogger().debug(":: in: " + document);
        var executions = getExecutions();
        for (int i = 0; i < executions.size(); i++) {
            executeConsumer.accept(document, executions.get(i), i);
        }

        return writeObject(document, isNull(getJsonOutputPath()) ? getJsonInputPath() : getJsonOutputPath());
    }

    default void validation(DocumentContext json, ExecutionMojo ex, Integer exIndex) throws UnsupportedOperationException {
        if (nonNull(ex.getValidation()) && DataUtils.validation(json, ex, exIndex, getLogger())) {
            String err = format("(%d) Not valid element \"%s\" = %s", exIndex, ex.getToken(), ex.getValidation());
            getLogger().error(err);
            throw new UnsupportedOperationException(err);
        }
    }

}
