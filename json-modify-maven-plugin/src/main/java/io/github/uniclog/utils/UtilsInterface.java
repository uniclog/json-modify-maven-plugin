package io.github.uniclog.utils;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPathException;
import io.github.uniclog.execution.ExecutionMojo;
import io.github.uniclog.execution.ExecutionType;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public interface UtilsInterface {
    Utils utils = new Utils();

    String getJsonInputPath();

    String getJsonOutputPath();

    List<ExecutionMojo> getExecutions();

    Log getLogger();

    default void executeAction(ExecuteConsumer<DocumentContext, ExecutionMojo, Integer> executeConsumer) throws MojoExecutionException {
        getLogger().debug(":: pr: " + getJsonInputPath());
        getLogger().debug(":: pr: " + getJsonOutputPath());
        getExecutions().forEach(ex -> getLogger().debug(":: pr: " + ex.toString()));

        DocumentContext json = readJsonObject(getJsonInputPath());
        getLogger().debug(":: in: " + json.jsonString());
        var exIndex = 1;
        for (ExecutionMojo ex : getExecutions()) {
            try {
                validation(json, ex, exIndex);

                executeConsumer.accept(json, ex, exIndex);

                exIndex++;
            } catch (JsonPathException e) {
                String err = format(":%d: not found json element %s : %s : %s", exIndex, ex.getToken(), ex.getKey(), ex.getValue());
                if (!ex.isSkipIfNotFoundElement()) {
                    getLogger().error(err);
                    throw new MojoExecutionException(err, e);
                }
                getLogger().error("Skip: " + err);
            } catch (UnsupportedOperationException e) {
                String err = format(":%d: not modify json element %s : %s : %s", exIndex, ex.getToken(), ex.getKey(), ex.getValue());
                if (!ex.isSkipIfNotFoundElement()) {
                    getLogger().error(err);
                    throw new MojoExecutionException(err, e);
                }
                getLogger().error("Skip: " + err);
            }
        }

        writeJsonObject(json, isNull(getJsonOutputPath()) ? getJsonInputPath() : getJsonOutputPath());
        getLogger().debug(":: out: " + json.jsonString());
    }

    default Object getElement(ExecutionType type, String value) throws MojoExecutionException {
        return utils.getElement(type, value);
    }

    default DocumentContext readJsonObject(String jsonInputPath) throws MojoExecutionException {
        return utils.readJsonObject(jsonInputPath);
    }

    default void writeJsonObject(DocumentContext json, String jsonOutputPath) throws MojoExecutionException {
        utils.writeJsonObject(json, jsonOutputPath);
    }

    default void validation(DocumentContext json, ExecutionMojo ex, Integer exIndex) throws UnsupportedOperationException {
        if (nonNull(ex.getValidation()) && utils.validation(json, ex, exIndex, getLogger())) {
            String err = String.format(":%d: Not valid element \"%s\" = %s", exIndex, ex.getToken(), ex.getValidation());
            getLogger().error(err);
            throw new UnsupportedOperationException(err);
        }
    }

}
