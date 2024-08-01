package co.elastic.opamp.sample;

import co.elastic.opamp.client.CentralConfigurationManager;
import co.elastic.opamp.client.CentralConfigurationProcessor;

public class Main {
  public static void main(String[] args) {
    CentralConfigurationManager centralConfigurationManager =
        CentralConfigurationManager.builder()
            .setServiceName("my-service")
            .setServiceVersion("1.0.0")
            .build();

    centralConfigurationManager.start(
        configuration -> CentralConfigurationProcessor.Result.SUCCESS);

    Runtime.getRuntime().addShutdownHook(new Thread(centralConfigurationManager::stop));
  }
}
