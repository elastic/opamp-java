package co.elastic.opamp.sample;

import co.elastic.opamp.client.OpampClient;
import java.util.Scanner;

public class Main {
  public static void main(String[] args) {
    OpampClient client =
        OpampClient.builder()
            .setServiceName("My Service")
            .setServiceNamespace("something")
            .setServiceVersion("1.0.0")
            .enableRemoteConfig()
            .build();
    LoggerCallback callback = new LoggerCallback();
    client.start(callback);
    Scanner scanner = new Scanner(System.in);
    while (true) {
      System.out.println("Enter a command: (exit)");
      String line = scanner.nextLine();
      if (line.equals("exit")) {
        client.stop();
        break;
      }
    }
    scanner.close();
  }
}
