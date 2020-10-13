package io.jenkins.plugins.adobe.cloudmanager;

import hudson.util.Secret;

public interface AdobeioConfig {

  Secret getApiKey();

  String getOrganizationID();

  String getTechnicalAccountId();

  Secret getClientSecret();

  Secret getPrivateKey();

  String getAccessToken() throws AdobeIOException ;
}
