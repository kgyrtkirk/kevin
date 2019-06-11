package alexa;

import java.util.Optional;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import static com.amazon.ask.request.Predicates.intentName;

import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;

public class CancelandStopIntentHandler implements RequestHandler {

  public boolean canHandle(HandlerInput input) {

    if (input.getRequest() instanceof IntentRequest) {
      String n = (((IntentRequest) input.getRequestEnvelope().getRequest()).getIntent().getName());
      System.out.println("intent::::" + n);

    } else {
      System.out.println("???" + input);

    }
    return input
        .matches(intentName("AMAZON.StopIntent")
            .or(intentName("AMAZON.CancelIntent")));
  }

  public Optional<Response> handle(HandlerInput input) {
    String speechText = "Bye Bye";
    return input.getResponseBuilder()
        .withSpeech(speechText)
        .withSimpleCard("HelloWorld", speechText)
        .build();
  }
}