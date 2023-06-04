package da.local.uniclog;

import java.util.Arrays;

public enum ModifyElementType {
    STRING("string"),
    INTEGER("integer"),
    DOUBLE("double"),
    BOOLEAN("boolean"),
    JSON("json");

    private final String value;

    ModifyElementType(String value) {
        this.value = value;
    }

    public static ModifyElementType getType(String msg) {
        return Arrays.stream(ModifyElementType.values())
                .filter(it -> it.value.equalsIgnoreCase(msg))
                .findFirst()
                .orElse(STRING);
    }

    public String getValue() {
        return value;
    }
}
