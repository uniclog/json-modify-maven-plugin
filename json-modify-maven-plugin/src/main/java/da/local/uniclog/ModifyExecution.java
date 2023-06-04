package da.local.uniclog;

import org.apache.maven.plugins.annotations.Parameter;

public class ModifyExecution {
    @Parameter(required = true)
    private String token;
    @Parameter(required = true)
    private String value;

    @Parameter
    private String type;

    public String getToken() {
        return token;
    }

    public String getValue() {
        return value;
    }

    public ModifyElementType getType() {
        return ModifyElementType.getType(type);
    }

}
