package io.github.uniclog;

import io.github.uniclog.execution.ExecutionMojo;
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
class RegexMojoTest implements TestUtils {
    private RegExModifyMojo service;

    @Override
    public UtilsInterface getService() {
        return service;
    }

    @BeforeEach
    public void setUp() {
        service = spy(new RegExModifyMojo());
        doReturn(outputPath).when(service).getJsonOutputPath();
        doReturn(inputPath).when(service).getJsonInputPath();
    }

    @MethodSource("modifyElement")
    @ParameterizedTest
    void testModifyMojo(ExecutionMojo execution, String validation, String token) {
        doReturn(List.of(execution)).when(service).getExecutions();
        assertDoesNotThrow(() -> service.execute());
        assertDoesNotThrow(() -> validation(execution, validation, token));
    }

    private Stream<Arguments> modifyElement() {
        return Stream.of(
                of(getSpyExecutionMojo("text-1", null, "123456\",\n\"123\":\"123",
                                null, null, false, null),
                        "\"123\"", "$.resource[1].content[0].123"),
                of(getSpyExecutionMojo("[1-9].[1-9]", null, "123.456,\n\"number3\" : 123",
                                null, null, false, null),
                        "123", "$.number3")
        );
    }

}