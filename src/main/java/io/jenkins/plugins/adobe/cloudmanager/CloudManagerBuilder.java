package io.jenkins.plugins.adobe.cloudmanager;

import java.io.IOException;
import java.io.PrintStream;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.adobe.cloudmanager.client.PipelineExecutionService;
import io.jenkins.plugins.adobe.cloudmanager.client.PipelinesService;
import io.jenkins.plugins.adobe.cloudmanager.client.ProgramsService;
import io.jenkins.plugins.adobe.cloudmanager.swagger.invoker.ApiException;
import io.jenkins.plugins.adobe.cloudmanager.swagger.model.PipelineList;
import io.jenkins.plugins.adobe.cloudmanager.swagger.model.ProgramList;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudManagerBuilder extends Builder implements SimpleBuildStep {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudManagerBuilder.class);

    private String program;
    private String pipeline;

    @DataBoundConstructor
    public CloudManagerBuilder(String program, String pipeline) {
        this.program = program;
        this.pipeline = pipeline;
    }

    public String getProgram() {
        return program;
    }

    @DataBoundSetter
    public void setProgram(String program) {
        this.program = program;
    }

    public String getPipeline() {
        return pipeline;
    }

    @DataBoundSetter
    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();
        CloudManagerGlobalConfig config = ExtensionList.lookupSingleton(CloudManagerGlobalConfig.class);

        String accessToken = config.getAccessToken();

        if (StringUtils.isBlank(accessToken)) {
            throw new IllegalStateException(
                    "Could not get an acess token for the cloud manager API."
                            + "Check the plugin configuration in Jenkins system config and "
                            + "ensure the authentication values are correct");
        }

        if (StringUtils.isBlank(getProgram())) {
            throw new IllegalStateException("Program Value is not configured");
        } else if (StringUtils.isBlank(getPipeline())) {
            throw new IllegalStateException("Pipeline Value is not configured");
        }

        logger.println(
                "[INFO] Starting pipeline with programId: "
                        + getProgram()
                        + " and pipelineId: "
                        + getPipeline());

        PipelineExecutionService executionService = new PipelineExecutionService(config);
        try {
            executionService.startPipeline(getProgram(), getPipeline());
            logger.println(
                    "[SUCCESS] Pipeline was started successfully! You can monitor its progress in cloud manager.");
        } catch (ApiException ex) {
            throw new IllegalStateException(
                    "Pipeline was not started, service responded with status: "
                            + ex.getCode()
                            + "and error body: "
                            + ex.getMessage());
        }

    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Inject
        CloudManagerGlobalConfig config;

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Cloud Manager Build Step";
        }

        public ListBoxModel doFillProgramItems() throws IOException {
            ListBoxModel items = new ListBoxModel();
            items.add("Select Program", "");
            ProgramsService service = new ProgramsService(config);
            try {
                ProgramList response = service.getPrograms();
                response.getEmbedded().getPrograms().stream()
                        .forEach(p -> items.add(p.getName() + " (" + p.getId() + ")", p.getId()));

            } catch (ApiException ex) {
                LOGGER.error(
                        "Request to get programs was not successful. "
                                + "Response code: "
                                + ex.getCode()
                                + "Raw Response: "
                                + ex.getMessage());
                items.add("Could not get programs. Check Jenkins logs", "");
            }
            return items;
        }

        public ListBoxModel doFillPipelineItems(@QueryParameter String program) throws IOException {
            ListBoxModel items = new ListBoxModel();
            PipelinesService service = new PipelinesService(config);
            if (StringUtils.isBlank(program)) {
                return items;
            }
            try {
                PipelineList response = service.getPipelines(program);
                response.getEmbedded().getPipelines().forEach(
                        p -> {
                            items.add(p.getName() + " (" + p.getId() + ")", p.getId());
                        });
            } catch (ApiException ex) {
                LOGGER.error(
                        "Request to get pipelines was not successful. "
                                + "Response code: "
                                + ex.getCode()
                                + "Raw Response: "
                                + ex.getMessage());
                items.add("Could not get pipelines. Check Jenkins logs", "");
            }
            return items;
        }
    }
}
