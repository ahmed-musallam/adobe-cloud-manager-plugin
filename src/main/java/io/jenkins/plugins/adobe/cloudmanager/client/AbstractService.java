package io.jenkins.plugins.adobe.cloudmanager.client;

import hudson.util.Secret;
import io.jenkins.plugins.adobe.cloudmanager.AdobeIOException;
import io.jenkins.plugins.adobe.cloudmanager.AdobeioConfig;
import io.jenkins.plugins.adobe.cloudmanager.AdobeioConstants;
import io.jenkins.plugins.adobe.cloudmanager.CloudManagerAuthUtil;

public abstract class AbstractService<T> {

  protected String organizationId, authorization;
  protected Secret apiKey;
  protected T api;

  public AbstractService(AdobeioConfig config, T api) {
    this.organizationId = config.getOrganizationID();
    try {
      this.authorization = AdobeioConstants.BEARER + config.getAccessToken();
    } catch (AdobeIOException e) {
      throw new IllegalStateException("Could not get access token", e);
    }

    this.apiKey = config.getApiKey();
    this.api = api;
  }

  protected String getApiKey() {
    return CloudManagerAuthUtil.safeGetPlainText(apiKey);
  }
}
