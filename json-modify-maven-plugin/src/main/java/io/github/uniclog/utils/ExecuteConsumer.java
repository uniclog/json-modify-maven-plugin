package io.github.uniclog.utils;

import org.apache.maven.plugin.MojoExecutionException;

@FunctionalInterface
public interface ExecuteConsumer<T, E, R> {
    void accept(T t, E e, R v) throws MojoExecutionException;
}