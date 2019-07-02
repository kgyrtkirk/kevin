package hu.rxd.kevin.alexa.kodi;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.ask.model.ui.PlayBehavior;
import com.amazon.ask.request.RequestHelper;

import kevin.KodiApiClient;
import kevin.KodiApiClient.KodiCmds;

import static com.amazon.ask.request.Predicates.intentName;

public class KodiIntentHandler implements RequestHandler {

  private Map<String, Runnable> actions = new HashMap<>();

  public KodiIntentHandler() {
    actions.put("Navigate_Back", new SimpleKodiCmd(KodiCmds.BACK));
    actions.put("Navigate_Select", new SimpleKodiCmd(KodiCmds.SELECT));
    actions.put("Navigate_Up", new SimpleKodiCmd(KodiCmds.UP));
    actions.put("Navigate_Down", new SimpleKodiCmd(KodiCmds.DOWN));
    actions.put("Navigate_Left", new SimpleKodiCmd(KodiCmds.LEFT));
    actions.put("Navigate_Right", new SimpleKodiCmd(KodiCmds.RIGHT));
    actions.put("Navigate_Enter", new SimpleKodiCmd(KodiCmds.ENTER));
    actions.put("Navigate_Play", new SimpleKodiCmd(KodiCmds.PLAY));
    actions.put("Navigate_Stop", new SimpleKodiCmd(KodiCmds.STOP));
    actions.put("Navigate_Pause", new SimpleKodiCmd(KodiCmds.PLAYPAUSE));
    actions.put("Navigate_Scrollup", new SimpleKodiCmd(KodiCmds.SCROLLUP));
    actions.put("Navigate_Scrolldown", new SimpleKodiCmd(KodiCmds.SCROLLDOWN));
  }

  static class SimpleKodiCmd implements Runnable {
    private KodiCmds cmd;

    SimpleKodiCmd(KodiCmds cmd1) {
      cmd = cmd1;
    }

    @Override
    public void run() {
      KodiApiClient.getInstance().send(cmd);
    }

  }

  public boolean canHandle(HandlerInput input) {
    for (Entry<String, Runnable> e : actions.entrySet()) {
      if (input.matches(intentName(e.getKey())))
        return true;
    }
    return false;
  }

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
      }catch (Exception e) {
        //FIXME LOG
        if (e instanceof TimeoutException)
          speechText = "A timeout exception occured";
        else
          speechText = "Some exception occured";
        System.err.println(e);
      }
    }

    return input.getResponseBuilder()
        .withShouldEndSession(false)
        .withSpeech(speechText)
        .build();
  }
}
