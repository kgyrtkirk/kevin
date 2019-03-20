
package kevin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class CmdExecutor {

  static class StreamGobbler implements Runnable {
    private InputStream inputStream;
    private Consumer<String> consumeInputLine;

    public StreamGobbler(InputStream inputStream, Consumer<String> consumeInputLine) {
      this.inputStream = inputStream;
      this.consumeInputLine = consumeInputLine;
    }

    @Override
    public void run() {
      new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumeInputLine);
    }
  }

  public static int executeCommandLine(final String[] commandLine, final long timeout)
      throws IOException, InterruptedException, TimeoutException {


    Logger logger = LoggerFactory.getLogger(CmdExecutor.class);
    logger.info("execute:" + Joiner.on(" ").join(commandLine));

    ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
    processBuilder.environment().putAll(Settings.instance().getExecEnvironment());
    Process process = processBuilder.start();
    StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), logger::info);
    StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), logger::error);

    new Thread(outputGobbler).start();
    new Thread(errorGobbler).start();
    Worker worker = new Worker(process);
    worker.start();
    try {
      worker.join(timeout);
      if (worker.exit != null) {
        logger.info("exit code: " + worker.exit);
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
