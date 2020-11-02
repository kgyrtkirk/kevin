
package hu.rxd.kevin.mqtt;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.rxd.kevin.Settings;

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

    Listener messageListener = new Listener();
    mqtt.subscribe("mirobo/#", messageListener);
    mqtt.subscribe("kevin/#", messageListener);


    //    mqtt.disconnect(10000);
    //    mqtt.subscribeWithResponse(topicFilter)
  }

  
  public static class MqttState {
    public MiRoboState mirobo = new MiRoboState();
    public KevinState kevin = new KevinState();
  }

  public static class KevinState {
    public boolean nightShift = true;
    public boolean dayShift = true;
  }

  public static class MiRoboState {
    public Long lastCleanTime = 0l;
    public Long lastNightCleanTime = 0l;
  }

  public MqttState state = new MqttState();

  class Listener implements IMqttMessageListener {

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

      debug(new String(message.getPayload()));
      switch (topic) {
      case "kevin/nightShift":
        try {
          long ts = Long.parseLong(new String(message.getPayload()));
          state.kevin.nightShift = ts != 0;
          LOG.info("nightShift: {}", state.kevin.nightShift);
        } catch (NumberFormatException nfe) {
          error("malformed " + topic + " value");
        }
        break;
      case "kevin/dayShift":
        try {
          long ts = Long.parseLong(new String(message.getPayload()));
          state.kevin.dayShift = ts != 0;
          LOG.info("dayShift: {}", state.kevin.nightShift);
        } catch (NumberFormatException nfe) {
          error("malformed " + topic + " value");
        }
        break;
      case "mirobo/lastClean":
        try {
          long ts = Long.parseLong(new String(message.getPayload()));
          state.mirobo.lastCleanTime = ts;
        } catch (NumberFormatException nfe) {
          error("malformed mirobo/lastClean value");
        }
        break;
      case "mirobo/lastNightClean":
        try {
          long ts = Long.parseLong(new String(message.getPayload()));
          state.mirobo.lastNightCleanTime = ts;
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

  public void publishNightCleanTime(long time) throws MqttException {
    MqttMessage msg = new MqttMessage(Long.toString(time).getBytes());
    msg.setRetained(true);
    msg.setQos(1);
    mqtt.publish("mirobo/lastNightClean", msg);
  }

}
