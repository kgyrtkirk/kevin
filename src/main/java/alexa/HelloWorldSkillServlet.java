package alexa;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;
import com.amazon.ask.servlet.SkillServlet;

public class HelloWorldSkillServlet extends SkillServlet {

  public HelloWorldSkillServlet() {
    super(getSkill());
  }

  private static Skill getSkill() {
    return Skills.standard()
        .addRequestHandlers(
            new CancelandStopIntentHandler(),
            new HelloWorldIntentHandler(),
            new HelpIntentHandler(),
            new LaunchRequestHandler(),
            new SessionEndedRequestHandler())
        // Add your skill id below
        //.withSkillId("")
        .build();
  }
}