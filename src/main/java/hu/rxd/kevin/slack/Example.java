package hu.rxd.kevin.slack;

public class Example {
  public static void main(String[] args) {
    SlackMessage slackMessage = SlackMessage.builder().text("<!here> just testing").build();
    SlackUtils.sendMessage(slackMessage);
    System.out.println("asd");
  }
}
