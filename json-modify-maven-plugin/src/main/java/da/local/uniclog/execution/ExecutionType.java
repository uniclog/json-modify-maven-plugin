package da.local.uniclog.execution;

import java.util.Arrays;

public enum ExecutionType {
    STRING("string"),
    INTEGER("integer"),
    DOUBLE("double"),
    BOOLEAN("boolean"),
    NULL("null"),
    JSON("json");

    private final String value;

    ExecutionType(String value) {
        this.value = value;
    }

    public static ExecutionType getType(String name) {
        return Arrays.stream(ExecutionType.values())
                .filter(it -> it.value.equalsIgnoreCase(name))
                .findFirst()
                .orElse(STRING);
    }

    public String getValue() {
        return value;
    }
}
