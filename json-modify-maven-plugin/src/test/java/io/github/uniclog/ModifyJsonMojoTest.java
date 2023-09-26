package io.github.uniclog;

import io.github.uniclog.execution.ExecutionMojo;
import io.github.uniclog.execution.ExecutionType;
import io.github.uniclog.utils.UtilsInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ModifyJsonMojoTest implements TestUtils {
    private ModifyJsonMojo service;

    @Override
    public UtilsInterface getService() {
        return service;
    }

    @BeforeEach
    public void setUp() {
        service = spy(new ModifyJsonMojo());
        doReturn(outputPath).when(service).getJsonOutputPath();
        doReturn(inputPath).when(service).getJsonInputPath();
    }

    @MethodSource({
            "modifyJsonElement", "notModifyJsonElement_notValid_or_skipIfNotFound"
    })
    @ParameterizedTest
    public void testModifyJsonMojo(ExecutionMojo execution, String validation, String token) {
        doReturn(List.of(execution)).when(service).getExecutions();
        assertDoesNotThrow(() -> service.execute());
        assertDoesNotThrow(() -> validation(execution, validation, token));
    }

    private Stream<Arguments> modifyJsonElement() {
        return Stream.of(
                of(getSpyExecutionMojo("$.text", null, "newText",
                                ExecutionType.STRING, "\"text\"", false, null),
                        "\"newText\"", "$.text"),
                of(getSpyExecutionMojo("$.flag", null, "false",
                                ExecutionType.BOOLEAN, null, false, null),
                        "false", "$.flag"),
                of(getSpyExecutionMojo("$.jsonArr[0]", null, "newText",
                                ExecutionType.STRING, "\"test1\"", false, null),
                        "\"newText\"", "$.jsonArr[0]"),
                of(getSpyExecutionMojo("$.l1.[0].i1", null, "newText",
                                ExecutionType.STRING, "\"test1\"", false, null),
                        "\"newText\"", "$.l1.[0].i1"),
                of(getSpyExecutionMojo("$.l1.[0]", null, "{ \"i1\": \"newText\" }",
                                ExecutionType.JSON, null, false, null),
                        "{\"i1\":\"newText\"}", "$.l1.[0]")
        );
    }

    private Stream<Arguments> notModifyJsonElement_notValid_or_skipIfNotFound() {
        return Stream.of(
                of(getSpyExecutionMojo("$.text", null, "newText",
                                ExecutionType.STRING, "test2", true, null),
                        null, null),
                of(getSpyExecutionMojo("$.none", null, "newText",
                                ExecutionType.STRING, "test2", true, null),
                        null, null)
        );
    }

}