
package hu.rxd.kevin.slack;

import java.io.Serializable;

//@AllArgsConstructor
//@Builder(builderClassName = "Builder")
//@Getter
//@Setter
public class SlackMessage implements Serializable {

  private String username;
  private String text;
  private String icon_emoji;

  static class Builder {
    SlackMessage instance;

    public Builder text(String string) {
      instance.text = string;
      return this;
    }

    public SlackMessage build() {
      return instance;
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
