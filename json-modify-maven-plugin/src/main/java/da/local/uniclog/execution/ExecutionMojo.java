package da.local.uniclog.execution;

import org.apache.maven.plugins.annotations.Parameter;

public class ExecutionMojo {
    @Parameter(required = true)
    private String token;
    @Parameter
    private String value;
    @Parameter
    private String key;
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

    public String getKey() {
        return key;
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

    @Override
    public String toString() {
        return "ExecutionMojo{" +
                "token='" + token + '\'' +
                ", value='" + value + '\'' +
                ", key='" + key + '\'' +
                ", type='" + type + '\'' +
                ", validation='" + validation + '\'' +
                ", skipIfNotFoundElement=" + skipIfNotFoundElement +
                '}';
    }
}
