package da.local.uniclog;

import org.apache.maven.plugins.annotations.Parameter;

public class ModifyExecution {
    @Parameter(required = true)
    private String token;
    @Parameter
    private String value;
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

    public ModifyElementType getType() {
        return ModifyElementType.getType(type);
    }

    public String getValidation() {
        return validation;
    }

    public boolean isSkipIfNotFoundElement() {
        return skipIfNotFoundElement;//Boolean.TRUE.toString().equalsIgnoreCase(skipIfNotFoundElement);
    }
}
