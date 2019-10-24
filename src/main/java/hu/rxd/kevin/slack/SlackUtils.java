package hu.rxd.kevin.slack;

import java.io.IOException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.ObjectMapper;

import hu.rxd.kevin.Settings;

// nice tutorial at: https://www.woolha.com/tutorials/java-sending-message-to-slack-webhook

public class SlackUtils {
  private static String slackWebhookUrl = Settings.instance().getSlackHookUrl();

  public static void sendMessage(String message) {
    SlackMessage slackMessage = SlackMessage.builder().text(message).build();
    SlackUtils.sendMessage(slackMessage);
  }

  public static void sendMessage(SlackMessage message) {
    CloseableHttpClient client = HttpClients.createDefault();
    HttpPost httpPost = new HttpPost(slackWebhookUrl);

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      String json = objectMapper.writeValueAsString(message);

      StringEntity entity = new StringEntity(json);
      httpPost.setEntity(entity);
      httpPost.setHeader("Accept", "application/json");
      httpPost.setHeader("Content-type", "application/json");

      client.execute(httpPost);
      client.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}