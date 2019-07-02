package hu.rxd.kevin.alexa.mira;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.RequestHelper;

import kevin.MiroboClient;

import static com.amazon.ask.request.Predicates.intentName;

public class MiraIntentHandler implements RequestHandler {

  private Map<String, Runnable> actions = new HashMap<>();

  public MiraIntentHandler() {
    actions.put("mirobo_start", new SimpleMiroboCmd("start"));
    actions.put("mirobo_pause", new SimpleMiroboCmd("pause"));
    actions.put("mirobo_home", new SimpleMiroboCmd("home"));
    actions.put("mirobo_find", new SimpleMiroboCmd("find"));
  }

  static class SimpleMiroboCmd implements Runnable {
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

  }

  @Override
  public boolean canHandle(HandlerInput input) {
    for (Entry<String, Runnable> e : actions.entrySet()) {
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
}
