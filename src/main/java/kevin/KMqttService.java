
package kevin;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KMqttService implements AutoCloseable {

  Logger LOG = LoggerFactory.getLogger(KMqttService.class);

  private MqttClient mqtt;

  public KMqttService() throws Exception {
    Settings s = Settings.instance();
    String id = UUID.randomUUID().toString();
    mqtt = new MqttClient("tcp://demeter:1883", id);

    MqttConnectOptions options = new MqttConnectOptions();
    options.setAutomaticReconnect(true);
    options.setCleanSession(true);
    options.setConnectionTimeout(10);
    mqtt.connect(options);
    mqtt.setTimeToWait(1000);

    mqtt.subscribe("mirobo/#", new Listener());


    //    mqtt.disconnect(10000);
    //    mqtt.subscribeWithResponse(topicFilter)
  }

  static class MqttState {
    MiRoboState mirobo = new MiRoboState();
  }

  static class MiRoboState {
    Long lastCleanTime = 0l;
  }

  MqttState state = new MqttState();

  class Listener implements IMqttMessageListener {

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

      debug(new String(message.getPayload()));
      switch (topic) {
      case "mirobo/lastClean":
        try {
          long ts = Long.parseLong(new String(message.getPayload()));
          state.mirobo.lastCleanTime = ts;
        } catch (NumberFormatException nfe) {
          error("malformed mirobo/lastClean value");
        }
        break;
      default:
        debug("unhandled: " + topic);
        break;
      }
    }

    private void debug(String string) {
      System.out.println(string);
      LOG.debug(string);
    }

    private void error(String string) {
      System.out.println(string);
      LOG.error(string);
    }

  }

  public static void main(String[] args) throws Exception {
    KMqttService s = new KMqttService();
  }

  @Override
  public void close() throws Exception {
    mqtt.disconnect(1000);
  }

  public void publishCleanTime(long time) throws MqttException {
    MqttMessage msg = new MqttMessage(Long.toString(time).getBytes());
    msg.setRetained(true);
    msg.setQos(1);
    mqtt.publish("mirobo/lastClean", msg);
  }

}
