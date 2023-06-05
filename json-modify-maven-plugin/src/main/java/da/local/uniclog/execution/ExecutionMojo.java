package da.local.uniclog.execution;

import org.apache.maven.plugins.annotations.Parameter;

public class ExecutionMojo {
    @Parameter(required = true)
    private String token;
    @Parameter
    private String value;
    @Parameter(alias = "key.value")
    private String keyValue;
    @Parameter
    private String type;
    @Parameter
    private String validation;
    @Parameter(defaultValue = "false")
    private boolean skipIfNotFoundElement;

    public String getToken() {
        return token;
    }

    public String getValue() {
        return value;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public ExecutionType getType() {
        return ExecutionType.getType(type);
    }

    public String getValidation() {
        return validation;
    }

    public boolean isSkipIfNotFoundElement() {
        return skipIfNotFoundElement;
    }
}
