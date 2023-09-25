package com.github.uniclog.execution;

import org.apache.maven.plugins.annotations.Parameter;

public class ExecutionMojo {
    @Parameter(required = true)
    private String token;
    @Parameter
    private String key;
    @Parameter
    private String value;
    @Parameter
    private Integer arrayIndex;
    @Parameter
    private String type;
    @Parameter
    private String validation;
    @Parameter(defaultValue = "false")
    private boolean skipIfNotFoundElement;

    public String getToken() {
        return token;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public Integer getArrayIndex() {
        return arrayIndex;
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
                "token='" + getToken() + '\'' +
                ", value='" + getValue() + '\'' +
                ", key='" + getKey() + '\'' +
                ", type='" + getType() + '\'' +
                ", validation='" + getValidation() + '\'' +
                ", arrayIndex='" + getArrayIndex() + '\'' +
                ", skipIfNotFoundElement=" + isSkipIfNotFoundElement() +
                '}';
    }
}
