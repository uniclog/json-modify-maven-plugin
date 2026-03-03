package io.github.uniclog;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;

import javax.inject.Inject;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.join;
import static java.util.stream.Collectors.joining;

@Mojo(
        name = "generate-json",
        threadSafe = true,
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class JsonGeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}/src/main/java")
    private File sourceDirectory;

    @Parameter(defaultValue = ".")
    private String subpackages;

    @Parameter(defaultValue = "${project.build.directory}/bpmn-ui")
    private File outputDirectory;

    @Parameter(defaultValue = "services.json")
    private String outputFileName;

    @Parameter(defaultValue = "${plugin}", readonly = true)
    private PluginDescriptor pluginDescriptor;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(required = true)
    private String doclet;

    @Parameter(defaultValue = "true")
    private boolean failOnError;

    @Parameter
    private List<DocletDependency> docletDependencies;

    @Inject
    private RepositorySystem repoSystem;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    private void handleError(String message) throws MojoFailureException {
        if (failOnError) {
            throw new MojoFailureException(message);
        } else {
            getLog().error(message);
        }
    }

    @Override
    public void execute() throws MojoFailureException {

        if (!validateConfiguration()) {
            return;
        }

        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            handleError("Failed to create output directory: " + outputDirectory.getAbsolutePath());
            return;
        }

        File outputFile = new File(outputDirectory, outputFileName);
        getLog().info("Generating BPMN JSON to: " + outputFile.getAbsolutePath());

        List<String> args = buildJavadocArgs(outputFile);
        if (args.isEmpty()) {
            return;
        }

        var documentationTool = ToolProvider.getSystemDocumentationTool();
        if (documentationTool == null) {
            handleError("Javadoc tool is not available. Run Maven with a full JDK (module jdk.javadoc).");
            return;
        }

        int result = documentationTool.run(null, null, null, args.toArray(new String[0]));

        if (result != 0) {
            handleError(
                    "Javadoc execution failed with exit code " + result +
                            ". See Javadoc output above for details."
            );
            return;
        }

        getLog().info("JSON generated successfully");
    }

    private boolean validateConfiguration() throws MojoFailureException {
        boolean valid = true;

        if (subpackages == null) {
            subpackages = "";
        }

        if (!sourceDirectory.exists()) {
            handleError("Source directory does not exist: " + sourceDirectory.getAbsolutePath());
            valid = false;
        }

        if (doclet == null || doclet.isBlank()) {
            handleError("Doclet class is not specified.");
            valid = false;
        }

        return valid;
    }

    private List<String> buildJavadocArgs(File outputFile) throws MojoFailureException {

        List<String> args = new ArrayList<>();

        args.add("-docletpath");
        args.add(join(
                File.pathSeparator,
                buildDocletPath(),
                buildDocletPathFromArtifacts()
        ));

        args.add("-doclet");
        args.add(doclet);

        args.add("-sourcepath");
        args.add(sourceDirectory.getAbsolutePath());

        args.add("-classpath");
        args.add(buildProjectClasspath());

        args.add("-outputDir");
        //args.add(outputFile.getAbsolutePath()); // not work!
        String outputDir = outputFile.getAbsolutePath();
        List<String> dirParts = new ArrayList<>();
        dirParts.add(outputDir);
        while (dirParts.size() < 10) {
            dirParts.add("skip.java");
        }
        args.addAll(dirParts);

        List<File> javaFiles = collectJavaFiles(sourceDirectory, subpackages);
        if (javaFiles.isEmpty()) {
            handleError("No Java source files found for subpackages: " + subpackages);
            return Collections.emptyList();
        }

        javaFiles.forEach(f -> args.add(f.getAbsolutePath()));

        return args;
    }

    private List<File> collectJavaFiles(File sourceRoot, String basePackage)
            throws MojoFailureException {

        File pkgDir;
        if (basePackage.isBlank()) {
            pkgDir = sourceRoot;
            getLog().info("Scanning entire source directory: " + sourceRoot.getAbsolutePath());
        } else {
            String relativePath = basePackage.replace('.', File.separatorChar);
            pkgDir = new File(sourceRoot, relativePath);
            getLog().info("Scanning package directory: " + pkgDir.getAbsolutePath());
        }

        if (!pkgDir.exists() || !pkgDir.isDirectory()) {
            getLog().warn("Directory not found: " + pkgDir.getAbsolutePath());
            return List.of();
        }

        try (var stream = Files.walk(pkgDir.toPath())) {
            return stream
                    .filter(p -> p.getFileName().toString().endsWith(".java"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new MojoFailureException(
                    "Failed to scan Java source files in directory: " + pkgDir.getAbsolutePath(), e
            );
        }
    }

    private String buildDocletPathFromArtifacts() throws MojoFailureException {
        if (docletDependencies == null || docletDependencies.isEmpty()) return "";

        List<String> resolvedPaths = new ArrayList<>();
        for (DocletDependency dep : docletDependencies) {
            try {
                ArtifactRequest request = getArtifactRequest(dep);
                ArtifactResult result = repoSystem.resolveArtifact(repoSession, request);
                File f = result.getArtifact().getFile();
                if (f != null && f.exists() && f.getName().endsWith(".jar")) {
                    getLog().info("Resolved doclet artifact: " + f.getAbsolutePath() + " size=" + f.length());
                    resolvedPaths.add(f.getAbsolutePath());
                } else {
                    String message = "Doclet artifact not found or invalid: " + dep.groupId + ":" + dep.artifactId;
                    handleError(message);
                }
            } catch (Exception e) {
                String message = "Failed to resolve doclet artifact: " + dep.groupId + ":" + dep.artifactId;
                if (failOnError) {
                    throw new MojoFailureException(message, e);
                }
                getLog().error(message, e);
            }
        }

        return String.join(File.pathSeparator, resolvedPaths);
    }

    private ArtifactRequest getArtifactRequest(DocletDependency dep) {
        DefaultArtifact artifact = dep.classifier != null && !dep.classifier.isEmpty()
                ? new DefaultArtifact(dep.groupId, dep.artifactId, dep.classifier, "jar", dep.version)
                : new DefaultArtifact(dep.groupId, dep.artifactId, "jar", dep.version);

        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        request.setRepositories(project.getRemoteProjectRepositories());
        return request;
    }

    private String buildProjectClasspath() {
        return Stream.concat(
                        Stream.of(project.getBuild().getOutputDirectory()),
                        project.getArtifacts().stream()
                                .map(Artifact::getFile)
                                .filter(Objects::nonNull)
                                .map(File::getAbsolutePath))
                .collect(joining(File.pathSeparator));
    }

    private String buildDocletPath() {
        StringBuilder cp = new StringBuilder();
        for (Artifact a : pluginDescriptor.getArtifacts()) {
            if (a.getFile() == null) continue;
            if (cp.length() > 0) cp.append(File.pathSeparator);
            cp.append(a.getFile().getAbsolutePath());
        }
        return cp.toString();
    }
}
