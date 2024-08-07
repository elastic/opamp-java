package co.elastic.opamp.sample;

import co.elastic.opamp.client.CentralConfigurationManager;
import co.elastic.opamp.client.CentralConfigurationProcessor;
import java.time.Duration;
import java.util.logging.Logger;

public class Main {
  private static final Logger logger = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) {
    String serviceName = "some-service";
    if (args.length > 0) {
      serviceName = args[0];
    }
    logger.info("============= Starting client for: " + serviceName);
    CentralConfigurationManager centralConfigurationManager =
        CentralConfigurationManager.builder()
            .setServiceName(serviceName)
            .setServiceVersion("1.0.0")
            .setPollingInterval(Duration.ofSeconds(5))
            .build();

    centralConfigurationManager.start(
        configuration -> {
          logger.info("Received configuration: " + configuration);
          return CentralConfigurationProcessor.Result.SUCCESS;
        });

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  logger.info("=========== Shutting down");
                  centralConfigurationManager.stop();
                }));
  }
}
