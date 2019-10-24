
package hu.rxd.kevin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import hu.rxd.kevin.mirobo.MiroboClient;
import hu.rxd.kevin.mqtt.KMqttService;
import hu.rxd.kevin.prometheus.PromMetricsServer;
import hu.rxd.kevin.prometheus.PrometheusApiClient;
import hu.rxd.kevin.prometheus.PrometheusApiClient.PMetric;
import hu.rxd.kevin.slack.SlackUtils;
import picocli.CommandLine;
import picocli.CommandLine.Option;

//FIXME rename
public class Mx implements Callable<Void> {

  @Option(names = "-c", description = "runs some checks")
  boolean runChecks;

  @Option(names = "-r", description = "runContinously")
  boolean runContinously;

  @Option(names = "-pp", description = "prometheus port", defaultValue = "16701")
  int prometheusPort;

  @Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
  private boolean helpRequested;

  private KMqttService mqttService;

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
        MiroboClient.mirobo("find");
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
    PromMetricsServer promMetricsServer = new PromMetricsServer(prometheusPort);
    try {
      SlackUtils.sendMessage("kevin launched..");
      while (true) {
        Thread.sleep(10000);
        run();
        PromMetricsServer.loops.inc();
      }
    } catch (Exception e) {
      SlackUtils.sendMessage("<!channel> Encountered exception: " + exceptionStacktraceToString(e));
    } finally {
      SlackUtils.sendMessage("<!here> shutting down?!");
    }
  }

  public static String exceptionStacktraceToString(Exception e) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    e.printStackTrace(ps);
    ps.close();
    return baos.toString();
  }

  private void run() throws Exception {
    try {
      if (needClean()) {
        SlackUtils.sendMessage("Initiating cleanup...");
        startMirobo();
      }
      if (interruptClean()) {
        SlackUtils.sendMessage("Cleanup interrupted; going home");
        sendMiroboHome();
      }
    } catch (TemporalyFailure e) {
      LOG.info("temporaly problem", e);
    }
  }

  private boolean needClean() throws TemporalyFailure {
    return conditionMet(isDayTime(), "dayTime") && conditionMet(cleanedSomeTimeAgo(), "lastClean")
        && conditionMet(everyoneIsAway(), "away") && true;
  }

  private boolean interruptClean() throws TemporalyFailure {
    return conditionMet(cleanStartInLastHour(), "cleanLastHour") &&
        conditionMet(presenceTransitionToAtHome(), "presenceAtHome");
  }

  private boolean cleanStartInLastHour() throws TemporalyFailure {
    Date cd = lastCleanDate();
    long tNow = new Date().getTime();
    long tClean = cd.getTime();
    long dSec = (tNow - tClean) / 1000;

    return dSec < 3600;

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
    //  FIXME:  MiroboClient.mirobo("fanspeed", "70");
    int rc = MiroboClient.mirobo("start");
    if (rc != 0) {
      LOG.error("mirobo return code: " + rc);
    }
  }

  private void sendMiroboHome() throws MqttException, IOException, InterruptedException {
    LOG.info("home");
    mqttService.publishCleanTime(new Date().getTime() - Settings.instance().getCleanInterval() * 1000);
    int rc = MiroboClient.mirobo("home");
    if (rc != 0) {
      LOG.error("mirobo return code: " + rc);
    }
  }

  private boolean everyoneIsAway() throws TemporalyFailure {
    Settings s = Settings.instance();
    PrometheusApiClient promClient = new PrometheusApiClient(s.getPrometheusAddress(), false);

    String interestingMacsPattern = "mac=~\"(" + Joiner.on("|").join(s.getPhoneMacs()) + ")\"";

    //    String query = "absent(wifi_station_signal_dbm{MACS} offset 10m) and absent(wifi_station_signal_dbm{MACS})";
    String query = "absent(count_over_time(wifi_station_signal_dbm{MACS}[3m])) "
        + "and absent(absent(count_over_time(wifi_station_signal_dbm{MACS}[10m])))";

    query = query.replaceAll("MACS", interestingMacsPattern);

    List<PMetric> res = promClient.doQuery(query);
    return res.size() > 0;
  }

  private boolean presenceTransitionToAtHome() throws TemporalyFailure {
    Settings s = Settings.instance();
    PrometheusApiClient promClient = new PrometheusApiClient(s.getPrometheusAddress(), false);

    String interestingMacsPattern = "mac=~\"(" + Joiner.on("|").join(s.getPhoneMacs()) + ")\"";

    //    String query = "absent(wifi_station_signal_dbm{MACS} offset 10m) and absent(wifi_station_signal_dbm{MACS})";
    String query = "absent(count_over_time(wifi_station_signal_dbm{MACS}[1m] offset 2m)) "
        + "and absent(absent(count_over_time(wifi_station_signal_dbm{MACS}[2m])))";

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
