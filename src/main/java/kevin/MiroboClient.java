
package kevin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        SlackUtils.sendMessage("mirobo " + string + " exitcode:" + exitCode);
      }
      return exitCode;
    } catch (TimeoutException te) {
      LOG.error("timeout", te);
      new TemporalyFailure("mirobo timed out");
    }
    throw new RuntimeException("???");
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