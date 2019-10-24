
package hu.rxd.kevin.prometheus;

import java.io.IOException;

import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;

public class PromMetricsServer {
  public static final Counter loops =
      Counter.build().name("loops_total").help("Total number of main loops.").register();
  final HTTPServer server;

  public PromMetricsServer(int port) throws IOException {
    server = new HTTPServer(port, true);
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    PromMetricsServer ps = new PromMetricsServer(1234);
    Thread.sleep(10000);

  }

}
