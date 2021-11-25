
package hu.rxd.kevin.prometheus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import hu.rxd.kevin.mirobo.MiroboClient.MiRoboStatus;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;

public class PromMetricsServer {

  enum PCounter {
    LOOPS("loops_total","Total number of main loops.");

    public final String name;
    public final String desc;

    private PCounter(String name, String desc) {
      this.name = name;
      this.desc = desc;
    }
  }

  final HTTPServer server;
  private CollectorRegistry registry;
  private Map<PCounter, Counter> counters;

  public PromMetricsServer(int port) throws IOException {
    registry = new CollectorRegistry(true);
    server = new HTTPServer(new InetSocketAddress(port), registry, true);

    counters=new HashMap<>();

    for (PCounter c: PCounter.values()) {

      Counter cc = Counter.build().name(c.name).help(c.desc).register(registry);
      counters.put(c,cc);

    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    PromMetricsServer ps = new PromMetricsServer(1234);
    Thread.sleep(10000);
  }

  public void pushValues(MiRoboStatus status) {
    throw new RuntimeException("Unimplemented!");

  }

}
