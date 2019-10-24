package hu.rxd.kevin.alexa.kodi;

import static com.amazon.ask.request.Predicates.intentName;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.RequestHelper;

import hu.rxd.kevin.kodi.KodiApiClient;
import hu.rxd.kevin.kodi.KodiApiClient.KodiCmds;

public class KodiIntentHandler implements RequestHandler {

  private Map<String, KodiAction> actions = new HashMap<>();

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

  interface KodiAction {
    void run(int count);
  }

  static class SimpleKodiCmd implements KodiAction {
    private KodiCmds cmd;

    SimpleKodiCmd(KodiCmds cmd1) {
      cmd = cmd1;
    }

    @Override
    public void run(int count) {
      for (int i = 0; i < count; i++) {
        KodiApiClient.getInstance().send(cmd);
      }
    }

  }

  public boolean canHandle(HandlerInput input) {
    for (Entry<String, KodiAction> e : actions.entrySet()) {
      if (input.matches(intentName(e.getKey())))
        return true;
    }
    return false;
  }

  //  tell kodi to go three down
  public Optional<Response> handle(HandlerInput input) {
    String speechText = " \n";
    RequestHelper h = RequestHelper.forHandlerInput(input);
    String intentName = h.getIntentName();
    KodiAction action = actions.get(intentName);
    Optional<String> countSlot = h.getSlotValue("count");
    int count = countSlot.isPresent() ? Integer.valueOf(countSlot.get()) : 1;
    if(count < 0 || count > 10) {
      return input.getResponseBuilder()
          .withShouldEndSession(false)
          .withSpeech("I doubt that you  really meaned: " + count + " times...")
          .build();
    }

    if (action == null) {
      speechText = "intent " + intentName + " is unknown";
    } else {
      try {
        action.run(count);
        speechText = "<audio src=\"soundbank://soundlibrary/computers/beeps_tones/beeps_tones_06\"/>";
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
