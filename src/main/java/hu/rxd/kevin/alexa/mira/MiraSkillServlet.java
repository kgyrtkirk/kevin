package hu.rxd.kevin.alexa.mira;

import static com.amazon.ask.request.Predicates.intentName;
import static com.amazon.ask.request.Predicates.requestType;

import java.util.Optional;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.SessionEndedRequest;
import com.amazon.ask.servlet.SkillServlet;

import kevin.MiroboClient;

public class MiraSkillServlet extends SkillServlet {

  private static final long serialVersionUID = 1L;

  public MiraSkillServlet() {
    super(getSkill());
  }

  private static Skill getSkill() {
    return Skills.standard()
        .addRequestHandlers(
            new InspectIntentHandler(),
            new CancelandStopIntentHandler(),
            new MiraIntentHandler(),
            new HelpIntentHandler(),
            new LaunchRequestHandler(),
            new SessionEndedRequestHandler())
        // Add your skill id below
        //.withSkillId("")
        .build();
  }

  public static class InspectIntentHandler implements RequestHandler {

    @Override
    public boolean canHandle(HandlerInput input) {
      Request r = input.getRequest();
      if (input.getRequest() instanceof IntentRequest) {
        Request request = input.getRequestEnvelope().getRequest();
        String n = (((IntentRequest) request).getIntent().getName());
        System.out.println("intent::::" + n);
      } else {
        System.out.println("???" + r.getClass().getAnnotatedInterfaces());
        System.out.println("???" + r);
      }
      return false;
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
      throw new RuntimeException();
    }

  }

  public static class CancelandStopIntentHandler implements RequestHandler {

    @Override
    public boolean canHandle(HandlerInput input) {
      return input.matches(intentName("AMAZON.StopIntent")
          .or(intentName("AMAZON.CancelIntent")));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
      String speechText = "Bye Bye";
      return input.getResponseBuilder()
          .withSpeech(speechText)
          .withSimpleCard("HelloWorld", speechText)
          .build();
    }
  }

  public static class HelpIntentHandler implements RequestHandler {

    @Override
    public boolean canHandle(HandlerInput input) {
      return input.matches(intentName("AMAZON.HelpIntent"))
          || input.matches(intentName("AMAZON.FallbackIntent"));

    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
      String speechText = "I am here to say Hello World to You";
      return input.getResponseBuilder()
          .withSpeech(speechText)
          .withSimpleCard("HelloWorld", speechText)
          .withReprompt(speechText)
          .build();
    }
  }

  public static class LaunchRequestHandler implements RequestHandler {

    @Override
    public boolean canHandle(HandlerInput input) {
      return input.matches(requestType(LaunchRequest.class));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
      String speechText = "I speak on behalf of Mira";
      MiroboClient.asyncWake();
      return input.getResponseBuilder()
          .withSpeech(speechText)
          .withReprompt(speechText)
          .build();
    }
  }

  public static class SessionEndedRequestHandler implements RequestHandler {

    @Override
    public boolean canHandle(HandlerInput input) {
      return input.matches(requestType(SessionEndedRequest.class));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
      return input.getResponseBuilder()
          .withSpeech("ended but why?")
          .build();
    }
  }

}