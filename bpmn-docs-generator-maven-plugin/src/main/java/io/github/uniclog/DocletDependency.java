package io.github.uniclog;

public class DocletDependency {
    public String groupId;
    public String artifactId;
    public String version;
    public String classifier;

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}