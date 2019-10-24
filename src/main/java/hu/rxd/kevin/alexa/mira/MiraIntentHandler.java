package hu.rxd.kevin.alexa.mira;

import static com.amazon.ask.request.Predicates.intentName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.RequestHelper;

import kevin.MiroboClient;
import kevin.Settings;
import kevin.Settings.CleanZone;

public class MiraIntentHandler implements RequestHandler {

  MiraCommand miraCmd = new MiraCommand();

  public MiraIntentHandler() {

  }

  @Override
  public boolean canHandle(HandlerInput input) {

    for (String a : miraCmd.getActions()) {
      if (input.matches(intentName(a))) {
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
    if (miraCmd.getActions().contains(intentName)) {
      speechText = "intent " + intentName + " is unknown";
    } else {
      try {
        miraCmd.execute(intentName);
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
    // visitor would be better..but meh..
    return miraCmd.getHelp();
  }
}
