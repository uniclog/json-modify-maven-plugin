package da.local.uniclog;

import da.local.uniclog.execution.ExecutionMojo;
import da.local.uniclog.execution.ExecutionType;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class InsertJsonMojoTest {
    private static final String outputPath = "src/test/resources/testInsert_out.json";
    private static final String inputPath = "src/test/resources/testInsert_in.json";
    private InsertJsonMojo service;

    @BeforeEach
    public void setUp() {
        service = spy(new InsertJsonMojo());
        doReturn(outputPath).when(service).getJsonOutputPath();
        doReturn(inputPath).when(service).getJsonInputPath();
    }

    @Test
    public void testInsertValueKeyField() {
        var execution1 = getSpyExecutionMojo(
                "@", "key1", "value1",
                ExecutionType.STRING, null, null, null);
        var execution2 = getSpyExecutionMojo(
                "$.jo", "key2", "value2",
                ExecutionType.STRING, null, null, null);
        var execution3 = getSpyExecutionMojo(
                "$.l1[2]", "key3", "value3",
                ExecutionType.STRING, null, null, null);
        var execution4 = getSpyExecutionMojo(
                "$.none", null, "value4",
                ExecutionType.STRING, "none", true, null);

        var executions = List.of(execution1, execution2, execution3, execution4);
        doReturn(executions).when(service).getExecutions();

        assertAll(
                () -> assertDoesNotThrow(() -> service.execute()),
                () -> assertDoesNotThrow(() -> validation(execution1, "\"value1\"", "@.key1")),
                () -> assertDoesNotThrow(() -> validation(execution2, "\"value2\"", "$.jo.key2")),
                () -> assertDoesNotThrow(() -> validation(execution3, "\"value3\"", "$.l1[2].key3"))
        );
    }

    @Test
    public void testInsertNewArrayItem() {
        var execution1 = getSpyExecutionMojo(
                "$.l1", null, "{\"a1\": \"a2\"}",
                ExecutionType.JSON, null, null, null);
        var execution2 = getSpyExecutionMojo(
                "$.l1", null, "value1",
                ExecutionType.STRING, null, null, null);
        var execution3 = getSpyExecutionMojo(
                "$.l1", null, null,
                ExecutionType.NULL, null, null, null);
        var execution4 = getSpyExecutionMojo(
                "$.l1[1]", "i1", "test4",
                ExecutionType.STRING, "{\"i1\":\"test2\"}", null, null);
        var executions = List.of(execution1, execution2, execution3, execution4);
        doReturn(executions).when(service).getExecutions();

        assertAll(
                () -> assertDoesNotThrow(() -> service.execute()),
                () -> {
                    doReturn("$.l1[3]").when(execution1).getToken();
                    assertDoesNotThrow(() -> validation(execution1, "{\"a1\":\"a2\"}", "$.l1.[3]"));
                },
                () -> {
                    doReturn("$.l1[4]").when(execution2).getToken();
                    assertDoesNotThrow(() -> validation(execution2, "\"value1\"", "$.l1.[4]"));
                },
                () -> {
                    doReturn("$.l1[5]").when(execution3).getToken();
                    assertDoesNotThrow(() -> validation(execution3, null, "$.l1.[5]"));
                },
                () -> {
                    doReturn(null).when(execution4).getKey();
                    assertDoesNotThrow(() -> validation(execution4, "{\"i1\":\"test4\"}", "$.l1.[1]"));
                }
        );
    }

    @Test
    public void testInsertNewArrayItemToIndex() {
        var execution = getSpyExecutionMojo(
                "$.l1", null, "{\"l2\": \"test5\"}",
                ExecutionType.JSON, null, null, 1);
        doReturn(List.of(execution)).when(service).getExecutions();

        assertDoesNotThrow(() -> service.execute());
        assertDoesNotThrow(() -> validation(execution, "{\"l2\":\"test5\"}", "$.l1.[1]"));
    }

    private ExecutionMojo getSpyExecutionMojo(String token, String key, String value, ExecutionType type, String validation, Boolean skipIfNotFound, Integer arrayIndex) {
        ExecutionMojo execution = spy(new ExecutionMojo());
        doReturn(token).when(execution).getToken();
        doReturn(key).when(execution).getKey();
        doReturn(value).when(execution).getValue();
        doReturn(type).when(execution).getType();
        doReturn(validation).when(execution).getValidation();
        doReturn(arrayIndex).when(execution).getArrayIndex();
        doReturn(!Objects.isNull(skipIfNotFound)).when(execution).isSkipIfNotFoundElement();
        return execution;
    }

    private void validation(ExecutionMojo execution, String validation, String token) throws MojoExecutionException {
        setValidation(execution, validation);
        setToken(execution, token);
        var document = service.readJsonObject(outputPath);
        service.validation(document, execution, null);
    }

    private void setValidation(ExecutionMojo execution, String validation) {
        doReturn(validation).when(execution).getValidation();
    }

    private void setToken(ExecutionMojo execution, String validation) {
        doReturn(validation).when(execution).getToken();
    }

}