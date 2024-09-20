package io.github.uniclog.utils;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.github.uniclog.execution.ExecutionMojo;
import io.github.uniclog.execution.ExecutionType;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.util.function.Function;

import static io.github.uniclog.utils.FileUtils.getConfiguration;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class DataUtils {

    static boolean validation(DocumentContext json, ExecutionMojo ex, Integer exIndex, Log log) {
        Object object = json.read(ex.getToken());
        String node = object.toString();
        log.info(String.format("(%d) :: validation: %s == %s", exIndex, ex.getValidation(), node));
        return !node.equals(ex.getValidation());
    }

    public static Object getElement(ExecutionType type, String value) throws MojoExecutionException {
        switch (type) {
            case BOOLEAN:
                return getPrimitiveValue(value, Boolean::valueOf, ExecutionType.BOOLEAN);
            case INTEGER:
                return getPrimitiveValue(value, Integer::valueOf, ExecutionType.INTEGER);
            case DOUBLE:
                return getPrimitiveValue(value, Double::valueOf, ExecutionType.DOUBLE);
            case JSON:
                return getJsonValue(value);
            case STRING:
                return isNull(value) ? EMPTY : value;
            default:
                return value;
        }
    }

    static private <T> T getJsonValue(String value) {
        DocumentContext valueAsJson = JsonPath.using(getConfiguration()).parse(value);
        return valueAsJson.json();
    }

    static private <T> T getPrimitiveValue(String value, Function<String, T> fun, ExecutionType type) throws MojoExecutionException {
        try {
            return fun.apply(value);
        } catch (NumberFormatException ex) {
            String err = String.format("Convert format exception: %s -> %s", value, type.getValue());
            throw new MojoExecutionException(err, ex);
        }
    }
}
