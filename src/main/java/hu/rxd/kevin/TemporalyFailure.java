
package hu.rxd.kevin;

public class TemporalyFailure extends Exception {

  public TemporalyFailure(String string, Throwable t) {
    super(string, t);
  }
  public TemporalyFailure(String string) {
    super(string);
  }

}
