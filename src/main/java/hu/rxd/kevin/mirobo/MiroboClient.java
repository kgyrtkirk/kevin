
package hu.rxd.kevin.mirobo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.rxd.kevin.TemporalyFailure;
import hu.rxd.kevin.mirobo.CmdExecutor.ExecResult;
import hu.rxd.kevin.slack.SlackUtils;

public class MiroboClient {

  static Logger LOG = LoggerFactory.getLogger(MiroboClient.class);

  private static final long MIROBO_TIMEOUT = 15000;

  public static int mirobo(String... string) throws IOException, InterruptedException {
    try {
      wakeCommand();
      ArrayList<String> args = new ArrayList<>();
      args.add("mirobo");
      args.addAll(Arrays.asList(string));

      int exitCode = CmdExecutor.executeCommandLine(args.toArray(new String[0]), MIROBO_TIMEOUT);
      if (exitCode != 0) {
        SlackUtils.sendMessage("mirobo " + Arrays.toString(string) + " exitcode:" + exitCode);
      }
      return exitCode;
    } catch (TimeoutException te) {
      LOG.error("timeout", te);
      new TemporalyFailure("mirobo timed out");
    }
    throw new RuntimeException("???");
  }

  public static MiRoboStatus status() throws IOException, InterruptedException {
    try {
      wakeCommand();
      ArrayList<String> args = new ArrayList<>();
      args.add("mirobo");
      args.add("status");

      ExecResult res = CmdExecutor.executeCommandLine2(args.toArray(new String[0]), MIROBO_TIMEOUT);
      if (res.exitCode != 0) {
        SlackUtils.sendMessage((args) + " exitcode:" + res.exitCode);
      }
      return new MiRoboStatus(res.stdout);
    } catch (TimeoutException te) {
      LOG.error("timeout", te);
      new TemporalyFailure("mirobo timed out");
    }
    throw new RuntimeException("???");
  }

  static enum StateKey {
    // @formatter:off
    State("State"),
    Battery("Battery"),
    Fanspeed("Fanspeed"),
    CleaningSince("Cleaning since"),
    CleanedArea("Cleaned area"),
    Waterbox("Water box attached"),
    Mop("Mop attached");
    // @formatter:on


    private String str;

    StateKey(String str) {
      this.str = str;

    }

    public static StateKey fromString(String str) {
      for (StateKey e : values()) {
        if (e.str.equals(str)) {
          return e;
        }
      }
      throw new IllegalArgumentException("unknown:" + str);
    }
  }

  public static class MiRoboStatus {

    private Map<StateKey, String> vals;

    // [State: Charging, Battery: 80 %,
    // Fanspeed: 102 %,
    // Cleaning since: 0:42:40,
    // Cleaned area: 36.855 m²,
    // Water box attached: True,
    // Mop attached: True]

    public MiRoboStatus(List<String> stdout) {
      Pattern pat = Pattern.compile("([^:]+): +([^ ]+).*");
      vals = new HashMap<>();
      for (String line : stdout) {
        Matcher m = pat.matcher(line);
        if (!m.matches()) {
          throw new RuntimeException("Not able to process statusline: " + line);
        }
        StateKey l = StateKey.fromString(m.group(1));
        vals.put(l, m.group(2));
      }
    }
  }

  private static void wakeCommand() throws IOException, InterruptedException {
    try {
      CmdExecutor.executeCommandLine(new String[] { "mirobo", "consumables" }, MIROBO_TIMEOUT);
    } catch (TimeoutException te) {
      // ignore
    }
  }

  public static void asyncWake() {
    // fire and forget :D
    new Thread() {
      @Override
      public void run() {
        try {
          wakeCommand();
        } catch (IOException | InterruptedException e) {
        }
      }
    }.start();
  }

}
