package kevin;

import java.io.File;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Settings {

  private static class DataClass {
    public String prometheusAddress;
    public Set<String> phoneMacs;
    public Long cleanInterval;
    public Map<String, String> execEnvironment;
    public String slackHookUrl;

    //    public JenkinsSettings jenkins = new JenkinsSettings();
    //    public JiraSettings jira = new JiraSettings();
  }

  private static Settings i;
  private DataClass dataClass;
  private File configFile;

  private Settings(File configFile) {
    this.configFile = configFile;
    ObjectMapper om = new ObjectMapper(new YAMLFactory());
    try {
      dataClass = om.readValue(configFile, DataClass.class);
    } catch (Exception e) {
      throw new RuntimeException("failed to open/or read configuration: " + configFile, e);
    }
  }

  public static Settings instance() {
    if (i == null) {
      File configFile = new File(System.getProperty("user.home"), ".config/kevin.yml");

      i = new Settings(configFile);
    }
    return i;
  }

  public String getPrometheusAddress() {
    return ensureNotNull(dataClass.prometheusAddress);
  }

  private <T> T ensureNotNull(T prometheusAddress) {
    if (prometheusAddress == null) {
      throw new RuntimeException("setting is null");
    }
    return prometheusAddress;
  }

  public Set<String> getPhoneMacs() {
    return ensureNotNull(dataClass.phoneMacs);
  }

  public Map<? extends String, ? extends String> getExecEnvironment() {
    return dataClass.execEnvironment;
  }

  public long getCleanInterval() {
    return ensureNotNull(dataClass.cleanInterval);
  }

  public String getSlackHookUrl() {
    return ensureNotNull(dataClass.slackHookUrl);
  }

}