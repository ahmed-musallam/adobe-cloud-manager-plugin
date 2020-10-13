package io.jenkins.plugins.adobe.cloudmanager.client;

import io.jenkins.plugins.adobe.cloudmanager.AdobeioConfig;
import io.jenkins.plugins.adobe.cloudmanager.swagger.api.ProgramsApi;
import io.jenkins.plugins.adobe.cloudmanager.swagger.invoker.ApiException;
import io.jenkins.plugins.adobe.cloudmanager.swagger.model.Program;
import io.jenkins.plugins.adobe.cloudmanager.swagger.model.ProgramList;

public class ProgramsService extends AbstractService<ProgramsApi> {

  public ProgramsService(AdobeioConfig config) {
    super(config, new ProgramsApi());
  }

  public Program getProgram(String programId) throws ApiException {
    return api.getProgram(programId, organizationId, authorization, getApiKey());
  }

  public ProgramList getPrograms() throws ApiException {
    return api.getPrograms(organizationId, authorization, getApiKey());
  }
}
