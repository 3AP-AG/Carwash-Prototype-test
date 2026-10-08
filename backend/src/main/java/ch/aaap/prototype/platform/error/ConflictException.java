package ch.aaap.prototype.platform.error;

/** Thrown by services when a request conflicts with the current state → 409. */
public class ConflictException extends RuntimeException {

  public ConflictException(String message) {
    super(message);
  }
}
