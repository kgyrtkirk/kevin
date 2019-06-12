package alexa;

import com.amazon.ask.Skill;
import com.amazon.ask.SkillStreamHandler;
import com.amazon.ask.Skills;

public class HelloWorldStreamHandler extends SkillStreamHandler {

  private static Skill getSkill() {
    return Skills.standard()
        .addRequestHandlers(
            new CancelandStopIntentHandler(),
            new KodiIntentHandler(),
            new HelpIntentHandler(),
            new LaunchRequestHandler(),
            new SessionEndedRequestHandler())
        .withSkillId("Try2")
        .build();
  }

  public HelloWorldStreamHandler() {
    super(getSkill());
  }
}