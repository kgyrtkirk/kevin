package alexa;

import java.util.Optional;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import static com.amazon.ask.request.Predicates.intentName;

public class HelloWorldIntentHandler implements RequestHandler {

  public boolean canHandle(HandlerInput input) {
    return input.matches(intentName("Navigate"));
  }

  public Optional<Response> handle(HandlerInput input) {
    String speechText = "I am alive Hello World";

    return input.getResponseBuilder().withShouldEndSession(false).withSpeech(speechText)
        .build();
  }
}
