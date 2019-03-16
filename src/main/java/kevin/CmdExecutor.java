
package kevin;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class CmdExecutor {

  public static int executeCommandLine(final String[] commandLine, final long timeout)
      throws IOException, InterruptedException, TimeoutException {

    ProcessBuilder processBuilder = new ProcessBuilder(commandLine).inheritIO();
    processBuilder.environment().putAll(Settings.instance().getExecEnvironment());
    Process process = processBuilder.start();
    Worker worker = new Worker(process);
    worker.start();
    try {
      worker.join(timeout);
      if (worker.exit != null) {
        return worker.exit;
      } else {
        throw new TimeoutException();
      }
    } catch (InterruptedException ex) {
      worker.interrupt();
      Thread.currentThread().interrupt();
      throw ex;
    } finally {
      process.destroyForcibly();
    }
  }

  private static class Worker extends Thread {
    private final Process process;
    private Integer exit;

    private Worker(Process process) {
      this.process = process;
    }

    @Override
    public void run() {
      try {
        exit = process.waitFor();
      } catch (InterruptedException ignore) {
        return;
      }
    }
  }

}
