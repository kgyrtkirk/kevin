package hu.rxd.kevin.alexa.mira;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import kevin.MiroboClient;
import kevin.Settings;
import kevin.Settings.CleanZone;

public class MiraCommand {

  private Map<String, AlexaCmd> actions = new LinkedHashMap<>();

  public MiraCommand() {

    actions.put("mirobo_start", new SimpleMiroboCmd("start"));
    actions.put("mirobo_pause", new SimpleMiroboCmd("pause"));
    actions.put("mirobo_home", new SimpleMiroboCmd("home"));
    actions.put("mirobo_find", new SimpleMiroboCmd("find"));
    Map<String, CleanZone> z = Settings.instance().getCleanZones();

    for (Entry<String, CleanZone> e : z.entrySet()) {
      actions.put(e.getKey(),
          new ZoneCleanMiroboCmd(e.getValue()));
    }
  }

  interface AlexaCmd extends Runnable {
    String getHelp();
  }

  static class SimpleMiroboCmd implements AlexaCmd {
    private String cmd;

    SimpleMiroboCmd(String cmd1) {
      cmd = cmd1;
    }

    @Override
    public void run() {
      try {
        MiroboClient.mirobo(cmd);
      } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public String getHelp() {
      return cmd;
    }

  }

  static class ZoneCleanMiroboCmd implements AlexaCmd {
    private CleanZone cz;

    public ZoneCleanMiroboCmd(CleanZone cleanZone) {
      this.cz = cleanZone;
    }

    @Override
    public void run() {
      try {
        MiroboClient.mirobo("zoned-clean", cz.zone);
      } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public String getHelp() {
      return cz.name;
    }

  }

  public Set<String> getActions() {
    return actions.keySet();
  }

  public void execute(String intentName) {
    AlexaCmd a = actions.get(intentName);
    if (a == null) {
      throw new IllegalArgumentException("not a valid action: " + intentName);
    }
    a.run();
  }

  public List<String> getHelp() {
    List<String> ret = new ArrayList<>();
    for (AlexaCmd a : actions.values()) {
      ret.add(a.getHelp());
    }
    return ret;
  }

}
