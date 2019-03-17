
package kevin;

import java.util.List;
import java.util.Map;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.filter.LoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class PrometheusApiClient {

  Logger LOG = LoggerFactory.getLogger(PrometheusApiClient.class);

  private final WebTarget webTarget;

  public PrometheusApiClient(String host, boolean debug) {
    ClientConfig clientConfig = new ClientConfig()
        .property(ClientProperties.READ_TIMEOUT, 3000)
        .property(ClientProperties.CONNECT_TIMEOUT, 5000);

    WebTarget webTarget0 = ClientBuilder
        .newClient(clientConfig)
        .target(host);
    if (debug) {
      webTarget0 = webTarget0.register(new LoggingFilter());
    }
    webTarget = webTarget0;
  }

  public static void main(String[] args) throws TemporalyFailure {
    //    PrometheusApiClient client = new PrometheusApiClient("http://demeter:9090", true);
    //    String query = "(wifi_station_signal_dbm)";
    //
    //    client.doQuery(query);
    Settings s = Settings.instance();
    PrometheusApiClient promClient = new PrometheusApiClient(s.getPrometheusAddress(), false);

    String interestingMacsPattern = "mac=~\"(" + Joiner.on("|").join(s.getPhoneMacs()) + ")\"";

    String query = "absent(count_over_time(wifi_station_signal_dbm{MACS}[3m] offset 385m)) "
        + "and absent(absent(count_over_time(wifi_station_signal_dbm{MACS}[10m] offset 385m) ))";

    //    String query = "absent(wifi_station_signal_dbm{MACS} offset 10m) and absent(wifi_station_signal_dbm{MACS})";
    query = query.replaceAll("MACS", interestingMacsPattern);


    List<PMetric> res = promClient.doQuery(query);
    System.out.println(res);

  }

  static class PEnvelope {
    public String status;
    public PData data;

    public String errorType;
    public String error;
    public String warnings;

  }

  static class PData {
    public String resultType;
    public List<PMetric> result;
  }

  public static class PMetric {
    public Map<String, String> metric;
    public List<Object> value;
    public List<Object[]> values;
  }

  public List<PMetric> doQuery(String query) throws TemporalyFailure {
    LOG.debug("query: {}", query);
    PEnvelope response = webTarget
        .path("/api/v1/query")
        .queryParam("query", query.replaceAll("\\{", "%7B").replaceAll("\\}", "%7D"))
        .request()
        .get()
        .readEntity(PEnvelope.class);

    LOG.debug("status: {}", response.status);
    if (!response.status.equals("success")) {
      throw new TemporalyFailure("prom-status:" + response.status);
    }
    //    System.out.println(response.status);
    //    System.out.println(response.errorType);
    //    System.out.println(response.error);
    //    System.out.println(response.data.resultType);
    //    System.out.println(response.data.result);
    return response.data.result;
  }
}
