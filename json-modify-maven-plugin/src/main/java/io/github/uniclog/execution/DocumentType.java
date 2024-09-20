package io.github.uniclog.execution;

public enum DocumentType {
    JSON("json"),
    DOCUMENT("document");

    private final String value;

    DocumentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
