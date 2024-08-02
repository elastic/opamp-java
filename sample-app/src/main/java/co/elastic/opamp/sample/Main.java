package co.elastic.opamp.sample;

import co.elastic.opamp.client.CentralConfigurationManager;
import co.elastic.opamp.client.CentralConfigurationProcessor;
import java.util.logging.Logger;

public class Main {
  private static final Logger logger = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) {
    CentralConfigurationManager centralConfigurationManager =
        CentralConfigurationManager.builder()
            .setServiceName("my-service")
            .setServiceVersion("1.0.0")
            .build();

    centralConfigurationManager.start(
        configuration -> {
          logger.info("Received configuration: " + configuration);
          return CentralConfigurationProcessor.Result.SUCCESS;
        });

    Runtime.getRuntime().addShutdownHook(new Thread(centralConfigurationManager::stop));
  }
}
