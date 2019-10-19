package hu.rxd.kevin.alexa.mira;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import org.ietf.jgss.Oid;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.RequestHelper;

import kevin.MiroboClient;

import static com.amazon.ask.request.Predicates.intentName;

public class MiraIntentHandler implements RequestHandler {

  private Map<String, AlexaCmd> actions = new HashMap<>();

  public MiraIntentHandler() {

    actions.put("mirobo_start", new SimpleMiroboCmd("start"));
    actions.put("mirobo_pause", new SimpleMiroboCmd("pause"));
    actions.put("mirobo_home", new SimpleMiroboCmd("home"));
    actions.put("mirobo_find", new SimpleMiroboCmd("find"));
  }

  

  // eloszoba:  [[26000,24000,28000,27000,1]]
  // nappali - sonyeg [[27500,20000,30000,23200,1]]
  // nappali -  [[27500,19000,31000,24000,1]]
  // konyha:    [[26000,19000,27500,22500,1]]

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

  @Override
  public boolean canHandle(HandlerInput input) {
    for (Entry<String, AlexaCmd> e : actions.entrySet()) {
      if (input.matches(intentName(e.getKey()))) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Optional<Response> handle(HandlerInput input) {
    String speechText = " \n";
    RequestHelper h = RequestHelper.forHandlerInput(input);
    String intentName = h.getIntentName();
    Runnable action = actions.get(intentName);
    if (action == null) {
      speechText = "intent " + intentName + " is unknown";
    } else {
      try {
        action.run();
      } catch (Exception e) {
        if (e instanceof TimeoutException) {
          speechText = "A timeout exception occured";
        } else {
          speechText = "Some exception occured";
        }
        System.err.println(e);
      }
    }

    return input.getResponseBuilder()
        .withShouldEndSession(false)
        .withSpeech(speechText)
        .build();
  }

  public List<String> getHelp() {
    List<String> ret = new ArrayList<>();
    for (AlexaCmd a : actions.values()) {
      ret.add(a.getHelp());
    }
    return ret;
  }
}
