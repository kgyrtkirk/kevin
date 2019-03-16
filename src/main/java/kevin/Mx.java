
package kevin;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import kevin.PrometheusApiClient.PMetric;
import picocli.CommandLine;
import picocli.CommandLine.Option;

public class Mx implements Callable<Void> {

  @Option(names = "-c", description = "runs some checks")
  boolean runChecks;

  @Option(names = "-r", description = "runContinously")
  boolean runContinously;

  @Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
  private boolean helpRequested;

  private KMqttService mqttService;

  private long MIROBO_TIMEOUT = 10000;
  static Logger LOG = LoggerFactory.getLogger(Mx.class);

  public static void main(String[] args) throws Exception {
    CommandLine.call(new Mx(), args);
  }

  @Override
  public Void call() throws Exception {

    try (KMqttService mqttService1 = new KMqttService()) {
      //FIXME
      mqttService = mqttService1;

      if (runChecks) {
        mirobo("find");
        return null;
      }

      if (runContinously) {
        runLoop();
      } else {
        Thread.sleep(100);
        run();
      }
    }
    return null;
  }

  private void runLoop() throws Exception {
    while (true) {
      Thread.sleep(10000);
      run();
    }
  }

  private void run() throws Exception {
    try {
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

  @Deprecated
  private void startMirobo() throws Exception {
    LOG.info("startClean");
    mqttService.publishCleanTime(new Date().getTime());
    int rc = mirobo("start");
    if (rc != 0) {
      LOG.error("mirobo return code: " + rc);
    }
  }

  private int mirobo(String string) throws IOException, InterruptedException {
    try {
      return CmdExecutor.executeCommandLine(new String[] { "mirobo", string }, MIROBO_TIMEOUT);
    } catch (TimeoutException te) {
      LOG.error("timeout", te);
      new TemporalyFailure("mirobo timed out");
    }
    throw new RuntimeException("???");
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
