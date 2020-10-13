package io.jenkins.plugins.adobe.cloudmanager.client;

import io.jenkins.plugins.adobe.cloudmanager.AdobeioConfig;
import io.jenkins.plugins.adobe.cloudmanager.swagger.api.PipelinesApi;
import io.jenkins.plugins.adobe.cloudmanager.swagger.invoker.ApiException;
import io.jenkins.plugins.adobe.cloudmanager.swagger.model.Pipeline;
import io.jenkins.plugins.adobe.cloudmanager.swagger.model.PipelineList;

public class PipelinesService extends AbstractService<PipelinesApi> {

  public PipelinesService(AdobeioConfig config)  {
    super(config, new PipelinesApi());
  }

  public Pipeline getPipeline(String programId, String pipelineId) throws ApiException {
    return this.api.getPipeline(programId, pipelineId, organizationId, authorization, getApiKey());
  }

  public PipelineList getPipelines(String programId) throws ApiException {
    return this.api.getPipelines(programId, organizationId, authorization, getApiKey());
  }
}
