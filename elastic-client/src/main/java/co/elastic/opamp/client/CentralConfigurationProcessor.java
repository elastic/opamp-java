package co.elastic.opamp.client;

import java.util.Map;

public interface CentralConfigurationProcessor {

  Result process(Map<String, String> configuration);

  enum Result {
    APPLIED,
    ERROR
  }
}
