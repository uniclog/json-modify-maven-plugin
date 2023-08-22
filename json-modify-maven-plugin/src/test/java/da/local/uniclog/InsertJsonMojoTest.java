package da.local.uniclog;

import da.local.uniclog.execution.ExecutionMojo;
import da.local.uniclog.execution.ExecutionType;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class InsertJsonMojoTest {
    private InsertJsonMojo service;

    @BeforeEach
    public void setUp() {
        service = spy(new InsertJsonMojo());
        doReturn("testInsert_out.json").when(service).getJsonOutputPath();
        doReturn("testInsert_in.json").when(service).getJsonInputPath();
    }

    @Test
    public void testInsertValueKeyField() throws MojoExecutionException {
        ExecutionMojo execution1 = getSpyExecutionMojo(
                "@","key1", "value1",
                ExecutionType.STRING, null,null);
        ExecutionMojo execution2 = getSpyExecutionMojo(
                "$.jo","key2", "value2",
                ExecutionType.STRING, null,null);
        ExecutionMojo execution3 = getSpyExecutionMojo(
                "$.l1[2]","key3", "value3",
                ExecutionType.STRING, null,null);

        List<ExecutionMojo> executions = List.of(execution1, execution2, execution3);
        doReturn(executions).when(service).getExecutions();
        service.execute();
    }

    @Test
    public void testInsertNewArrayItem() throws MojoExecutionException {
        ExecutionMojo execution = getSpyExecutionMojo(
                "$.l1",null, "{\"a1\": \"a2\"}",
                ExecutionType.JSON, null,null);
        List<ExecutionMojo> executions = List.of(execution);
        doReturn(executions).when(service).getExecutions();
        service.execute();
    }

    private ExecutionMojo getSpyExecutionMojo(String token, String key, String value, ExecutionType type, String validation, Boolean skipIfNotFound) {
        ExecutionMojo execution = spy(new ExecutionMojo());
        doReturn(token).when(execution).getToken();
        doReturn(key).when(execution).getKey();
        doReturn(value).when(execution).getValue();
        doReturn(type).when(execution).getType();
        doReturn(validation).when(execution).getValidation();
        doReturn(!Objects.isNull(skipIfNotFound)).when(execution).isSkipIfNotFoundElement();
        return execution;
    }

}