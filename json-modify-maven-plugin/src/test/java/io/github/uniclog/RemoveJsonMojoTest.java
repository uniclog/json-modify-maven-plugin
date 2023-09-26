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
class RemoveJsonMojoTest implements TestUtils {
    private RemoveJsonMojo service;

    @Override
    public UtilsInterface getService() {
        return service;
    }

    @BeforeEach
    public void setUp() {
        service = spy(new RemoveJsonMojo());
        doReturn(outputPath).when(service).getJsonOutputPath();
        doReturn(inputPath).when(service).getJsonInputPath();
    }

    @MethodSource({
            "notRemoveJsonElement_notValid_or_skipIfNotFound", "removeJsonElement"
    })
    @ParameterizedTest
    public void testRemoveJsonMojo(ExecutionMojo execution, String validation, String token) {
        doReturn(List.of(execution)).when(service).getExecutions();
        assertDoesNotThrow(() -> service.execute());
        assertDoesNotThrow(() -> validation(execution, validation, token));
    }

    private Stream<Arguments> notRemoveJsonElement_notValid_or_skipIfNotFound() {
        return Stream.of(
                of(getSpyExecutionMojo("$.jsonArr[1]", null, null,
                                ExecutionType.STRING, "test2", true, null),
                        "\"text\"", "$.jsonArr[1]"),
                of(getSpyExecutionMojo("$.notFound", null, null,
                                ExecutionType.STRING, null, true, null),
                        null, null)
        );
    }

    private Stream<Arguments> removeJsonElement() {
        return Stream.of(
                of(getSpyExecutionMojo("$.flag", null, null,
                                ExecutionType.BOOLEAN, null, false, null),
                        null, null),
                of(getSpyExecutionMojo("$.l1[2].i2", null, null,
                                ExecutionType.STRING, null, false, null),
                        null, null),
                of(getSpyExecutionMojo("$.l1[0].i1", null, null,
                                ExecutionType.STRING, null, false, null),
                        "{}", "$.l1[0]")
        );
    }

}