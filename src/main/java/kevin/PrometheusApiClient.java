
package kevin;

import java.util.List;
import java.util.Map;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.filter.LoggingFilter;

public class PrometheusApiClient {
  private final WebTarget webTarget;

  public PrometheusApiClient(String host) {
    ClientConfig clientConfig = new ClientConfig()
        .property(ClientProperties.READ_TIMEOUT, 3000)
        .property(ClientProperties.CONNECT_TIMEOUT, 5000);

    webTarget = ClientBuilder
        .newClient(clientConfig)
        .register(new LoggingFilter())
        .target(host);
  }

  public static void main(String[] args) {
    PrometheusApiClient client = new PrometheusApiClient("http://demeter:9090");
    String query = "(wifi_station_signal_dbm)";

    client.doQuery(query);
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

  public List<PMetric> doQuery(String query) {
    PEnvelope response = webTarget
        .path("/api/v1/query")
        .queryParam("query", query.replaceAll("\\{", "%7B").replaceAll("\\}", "%7D"))
        .request()
        .get()
        .readEntity(PEnvelope.class);

    System.out.println(response.status);
    System.out.println(response.errorType);
    System.out.println(response.error);
    System.out.println(response.data.resultType);
    System.out.println(response.data.result);
    return response.data.result;
  }
}
