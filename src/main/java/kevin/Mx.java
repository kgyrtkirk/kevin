
package kevin;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import kevin.PrometheusApiClient.PMetric;

public class Mx implements AutoCloseable {

  static Logger LOG = LoggerFactory.getLogger(Mx.class);
  final KMqttService mqttService;

  public static void main(String[] args) throws Exception {
    try (Mx mx = new Mx()) {
      mx.run();
    }

  }

  @Override
  public void close() throws Exception {
    mqttService.close();
  }

  private void run() throws Exception {
    try {
      Thread.sleep(100);
      if (needClean()) {
        startMirobo();
      }
    } catch (TemporalyFailure e) {
      LOG.info("temporaly problem", e);
    }
  }

  private boolean needClean() throws TemporalyFailure {
    return conditionMet(isDayTime(), "dayTime") && conditionMet(cleanedSomeTimeAgo(), "lastClean")
        && conditionMet(everyoneIsAway(), "away") && true;
  }

  private boolean cleanedSomeTimeAgo() throws TemporalyFailure {
    Date cd = lastCleanDate();
    long tNow = new Date().getTime();
    long tClean = cd.getTime();

    long dSec = (tNow - tClean) / 1000;

    long c = Settings.instance().getCleanInterval();
    LOG.debug("last cleaned: {} seconds ago", dSec);

    return dSec > c;
  }

  private boolean conditionMet(boolean c, String string) {
    if (!c) {
      LOG.debug("condition not met for " + string);
    }
    return c;
  }

  private boolean isDayTime() {
    Date d = new Date();
    int h = d.getHours();
    return (h >= 8 && h < 20);
  }

  private void startMirobo() throws Exception {
    LOG.info("startClean");
    mqttService.publishCleanTime(new Date().getTime());
    try {
      CmdExecutor.executeCommandLine(new String[] { "mirobo", "find" }, 1000);
      //      CmdExecutor.executeCommandLine(new String[] { "mirobo", "clean" }, 1000);
    } catch (TimeoutException te) {
      new TemporalyFailure("mirobo timed out");
    }
  }

  public Mx() throws Exception {
    mqttService = new KMqttService();
  }

  private boolean everyoneIsAway() throws TemporalyFailure {
    Settings s = Settings.instance();
    PrometheusApiClient promClient = new PrometheusApiClient(s.getPrometheusAddress(), false);

    String interestingMacsPattern = "mac=~\"(" + Joiner.on("|").join(s.getPhoneMacs()) + ")\"";

    String query = "absent(wifi_station_signal_dbm{MACS} offset 10m) and absent(wifi_station_signal_dbm{MACS})";
    query = query.replaceAll("MACS", interestingMacsPattern);

    List<PMetric> res = promClient.doQuery(query);
    return res.size() > 0;
  }

  private Date lastCleanDate() throws TemporalyFailure {
    Long ts = mqttService.state.mirobo.lastCleanTime;
    if (ts == null) {
      throw new TemporalyFailure("lastCleanTime is not available");
    }
    return new Date(ts);

  }

}
