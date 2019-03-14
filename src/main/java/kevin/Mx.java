
package kevin;

import java.util.List;

import com.google.common.base.Joiner;

import kevin.PrometheusApiClient.PMetric;

public class Mx {

  public static void main(String[] args) {
    System.out.println(everyoneIsAway());
  }

  private static boolean everyoneIsAway() {
    Settings s = Settings.instance();
    PrometheusApiClient promClient = new PrometheusApiClient(s.getPrometheusAddress());

    System.out.println(s.getPhoneMacs());

    String interestingMacsPattern = "mac=~\"(" + Joiner.on("|").join(s.getPhoneMacs()) + ")\"";

    //    String query = "absent(wifi_station_signal_dbm{MACS} offset 10m)";
    String query = "absent(wifi_station_signal_dbm{MACS} offset 10m) and absent(wifi_station_signal_dbm{MACS})";
    query = query.replaceAll("MACS", interestingMacsPattern);

    System.out.println(query);
    List<PMetric> res = promClient.doQuery(query);
    System.out.println(res.size());
    return res.size() > 0;
  }



}
