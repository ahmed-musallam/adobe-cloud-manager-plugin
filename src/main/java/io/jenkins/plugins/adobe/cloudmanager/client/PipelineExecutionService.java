package io.jenkins.plugins.adobe.cloudmanager.client;

import io.jenkins.plugins.adobe.cloudmanager.AdobeioConfig;
import io.jenkins.plugins.adobe.cloudmanager.swagger.api.PipelineExecutionApi;
import io.jenkins.plugins.adobe.cloudmanager.swagger.invoker.ApiException;

public class PipelineExecutionService extends AbstractService<PipelineExecutionApi> {

    public PipelineExecutionService(AdobeioConfig config) {
        super(config, new PipelineExecutionApi());
    }

    public void startPipeline(String programId, String pipelineId) throws ApiException {
        api.startPipeline(
                programId, pipelineId, organizationId, authorization, getApiKey(), "application/json");
    }
}
