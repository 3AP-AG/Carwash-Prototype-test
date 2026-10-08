package ch.aaap.prototype.platform.error;

/** Thrown by services when a requested resource does not exist → 404. */
public class NotFoundException extends RuntimeException {

  public NotFoundException(String message) {
    super(message);
  }
}
