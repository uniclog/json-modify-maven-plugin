package com.github.uniclog.utils;

import org.apache.maven.plugin.MojoExecutionException;

@FunctionalInterface
public interface ExecuteConsumer<DocumentContext, ExecutionMojo, Integer> {
    void accept(DocumentContext t, ExecutionMojo u, Integer v) throws MojoExecutionException;
}