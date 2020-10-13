package io.jenkins.plugins.adobe.cloudmanager;

import java.io.IOException;

public class AdobeIOException extends IOException {

  public AdobeIOException(String errorMessage, Throwable err) {
    super(errorMessage, err);
  }
  public AdobeIOException(String errorMessage) {
    super(errorMessage);
  }
}
