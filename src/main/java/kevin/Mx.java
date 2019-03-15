
package kevin;

import java.util.List;

import com.google.common.base.Joiner;

import kevin.PrometheusApiClient.PMetric;

public class Mx {

  public static void main(String[] args) throws Exception {
    System.out.println(everyoneIsAway());
    new KMqttService();

  }

  private static boolean everyoneIsAway() {
    Settings s = Settings.instance();
    PrometheusApiClient promClient = new PrometheusApiClient(s.getPrometheusAddress(), false);

    String interestingMacsPattern = "mac=~\"(" + Joiner.on("|").join(s.getPhoneMacs()) + ")\"";

    String query = "absent(wifi_station_signal_dbm{MACS} offset 10m) and absent(wifi_station_signal_dbm{MACS})";
    query = query.replaceAll("MACS", interestingMacsPattern);

    List<PMetric> res = promClient.doQuery(query);
    return res.size() > 0;
  }



}
