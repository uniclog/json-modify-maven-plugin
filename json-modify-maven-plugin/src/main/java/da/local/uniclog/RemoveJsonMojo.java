package da.local.uniclog;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPathException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Mojo(name = "remove", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class RemoveJsonMojo extends AbstractMojo {
    private Log log;
    @Parameter(alias = "json.in")
    private String jsonInputPath;
    @Parameter(alias = "json.out")
    private String jsonOutputPath;
    @Parameter(alias = "executions", required = true)
    private List<ModifyExecution> executions;

    @Override
    public void execute() throws MojoExecutionException {

        // log.info(":: pr: " + jsonInputPath);
        // log.info(":: pr: " + jsonOutputPath);
        // executions.forEach(ex -> log.info(":: pr: " + ex.getToken() + " : " + ex.getValue() + " : " + ex.getType()));
        SupportUtils utils = new SupportUtils();

        DocumentContext json = utils.readJsonObject(jsonInputPath);
        log.debug(":: in: " + json.jsonString());

        for (ModifyExecution ex : executions) {
            try {
                if (nonNull(ex.getValidation()) && validation(json, ex)) {
                    String err = String.format("Not valid element \"%s\" = %s", ex.getToken(), ex.getValidation());
                    log.error(err);
                    throw new MojoExecutionException(err);
                }
                json.delete(ex.getToken());
                log.info(String.format(":: rm -> %s", ex.getToken()));
            } catch (JsonPathException e) {
                String err = String.format("Not found json element \"%s\"", ex.getToken());
                log.error(err);
                if (!ex.isSkipIfNotFoundElement()) {
                    throw new MojoExecutionException(err, e);
                }
            }
        }

        jsonOutputPath = isNull(jsonOutputPath) ? jsonInputPath : jsonOutputPath;
        utils.writeJsonObject(json, jsonOutputPath);
        log.debug(":: out: " + json.jsonString());
    }

    @Override
    public Log getLog() {
        return log;
    }

    @Override
    public void setLog(Log log) {
        this.log = log;
    }

    private boolean validation(DocumentContext json, ModifyExecution ex) {
        Object object = json.read(ex.getToken());
        String node = object.toString();
        log.info(String.format(":: validation: %s == %s", ex.getValidation(), node));
        return !node.equals(ex.getValidation());
    }
}
