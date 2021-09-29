
package hu.rxd.kevin.mirobo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import hu.rxd.kevin.Settings;

public class CmdExecutor {

  static class StreamGobbler implements Runnable, Consumer<String> {
    private InputStream inputStream;
    private Consumer<String> consumeInputLine;
    private List<String> lines;

    public StreamGobbler(InputStream inputStream, Consumer<String> consumeInputLine) {
      this.inputStream = inputStream;
      this.consumeInputLine = consumeInputLine;
    }

    @Override
    public void run() {
      new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(this);
    }

    @Override
    public void accept(String t) {
      lines.add(t);
      consumeInputLine.accept(t);
    }
  }

  public static int executeCommandLine(final String[] commandLine, final long timeout)
      throws IOException, InterruptedException, TimeoutException {
    return executeCommandLine2(commandLine, timeout).exit;
  }

  public static ExecResult executeCommandLine2(final String[] commandLine, final long timeout)
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
        return new ExecResult(worker.exit, outputGobbler.lines, errorGobbler.lines);
      } else {
        throw new TimeoutException("After " + timeout + "ms command: " + Joiner.on(" ").join(commandLine));
      }
    } catch (InterruptedException ex) {
      worker.interrupt();
      Thread.currentThread().interrupt();
      throw ex;
    } finally {
      process.destroyForcibly();
    }
  }

  public static class ExecResult {

    private int exit;
    private List<String> stdout;
    private List<String> stderr;

    public ExecResult(int exit, List<String> stdout, List<String> stderr) {
      this.exit = exit;
      this.stdout = stdout;
      this.stderr = stderr;

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
