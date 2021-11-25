
package hu.rxd.kevin.prometheus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import hu.rxd.kevin.mirobo.MiroboClient.MiRoboStatus;
import hu.rxd.kevin.mirobo.MiroboClient.StateKey;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;

public class PromMetricsServer {

  final HTTPServer server;
  private CollectorRegistry registry;
  public Counter loops;
  Gauge errorMetric;
  private Gauge battery;
  private Gauge waterLow;
  private Gauge state;

  public PromMetricsServer(int port) throws IOException {
    registry = new CollectorRegistry(true);
    server = new HTTPServer(new InetSocketAddress(port), registry, true);

    loops = Counter.build().name("loops_total").help("Total number of main loops.").register(registry);

    errorMetric = Gauge.build().name("mirobo_error").labelNames("error").help("Mirobo error state/message.").register(registry);
    battery = Gauge.build().name("mirobo_battery").help("battery").register(registry);
    waterLow = Gauge.build().name("mirobo_waterlow").help("waterlow flag").register(registry);
    state = Gauge.build().name("mirobo_state").labelNames("value").help("state value").register(registry);


  }

  public static void main(String[] args) throws IOException, InterruptedException {
    PromMetricsServer ps = new PromMetricsServer(1234);
    Thread.sleep(10000);
  }

  public void pushValues(MiRoboStatus status) {

    Map<StateKey, String> vals = status.getVals();

    setLabelMetric(errorMetric, vals.get(StateKey.Error));
    battery.set(Double.valueOf(vals.get(StateKey.Battery)));
    waterLow.set(Double.valueOf(vals.get(StateKey.WATER_LOW)));
    setLabelMetric(state, vals.get(StateKey.State));

  }


  private void setLabelMetric(Gauge gauge, String value) {
    gauge.clear();
    if (value == null) {
      gauge.labels("").set(0);
    } else {
      gauge.labels(value).set(1);
    }
  }

}
