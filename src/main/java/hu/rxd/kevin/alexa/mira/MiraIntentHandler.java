package hu.rxd.kevin.alexa.mira;

import static com.amazon.ask.request.Predicates.intentName;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.RequestHelper;

public class MiraIntentHandler implements RequestHandler {

  MiraCommand miraCmd = new MiraCommand();

  public MiraIntentHandler() {

  }

  @Override
  public boolean canHandle(HandlerInput input) {

    System.out.println("canHandle: XXX");
    for (String a : miraCmd.getActions()) {
      System.out.println("canHandle: " + a);
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
