
package hu.rxd.kevin.kodi;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.filter.LoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.rxd.kevin.Settings;
import hu.rxd.kevin.TemporalyFailure;

public class KodiApiClient {

  private static KodiApiClient instance;

  Logger LOG = LoggerFactory.getLogger(KodiApiClient.class);

  private final WebTarget webTarget;

  public KodiApiClient(String host, boolean debug) {
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


  public enum KodiCmds {
    // @formatter:off
    UP("Input.Up"),
    DOWN("Input.Down"),
    LEFT("Input.Left"),
    RIGHT("Input.Right"),
    SELECT("Input.Select"),
    BACK("Input.Back"),
    PLAY("Input.ExecuteAction", "play"),
    PLAYPAUSE("Input.ExecuteAction", "playpause"),
    STOP("Input.ExecuteAction", "stop"), 
    SCROLLUP("Input.ExecuteAction", "scrollup"), 
    SCROLLDOWN("Input.ExecuteAction", "scrolldown"), 
    ENTER("Input.ExecuteAction", "enter"), 
    ;
    // @formatter:on
    public final String method;
    private HashMap<String, String> param;

    private KodiCmds(String method) {
      this.method = method;
      this.param = new HashMap<String, String>();
    }

    private KodiCmds(String method, String actionParam) {
      this.method = method;
      this.param = new HashMap<>();
      param.put("action", actionParam);
    }

  }

  public static KodiApiClient getInstance() {
    if (instance == null) {
      instance = new KodiApiClient(Settings.instance().getKodiAddress(), false);
    }
    return instance;
  }

  public static void main(String[] args) throws TemporalyFailure {
    KodiApiClient c = getInstance();
    //    while (true) {
    //      Thread.sleep(1000);
    c.send(KodiCmds.DOWN);
    //    }

  }

  public static class KodiRequest {
    public int id = 1;
    public String jsonrpc = "2.0";
    public String method;
    public Map<String, String> params;

    //    Map<String, Object> params;
    KodiRequest(KodiCmds cmd) {
      method = cmd.method;
      params = cmd.param;
    }
  }

  public void send(KodiCmds cmd) {
    System.out.println("send:" + cmd);
    Object r = new KodiRequest(cmd);
    Response response = webTarget.path("jsonrpc").request(MediaType.APPLICATION_JSON)

        //        .queryParam("query", query.replaceAll("\\{", "%7B").replaceAll("\\}", "%7D"))
        .post(Entity.entity(r, MediaType.APPLICATION_JSON));

    System.out.println(response.getStatus());
    System.out.println(response.readEntity(String.class));
  }

}
