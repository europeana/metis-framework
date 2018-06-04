package eu.europeana.metis.transformation.service;

/**
 * Created by pwozniak on 3/21/18
 */
public class TransformationException extends Exception {

  private static final long serialVersionUID = -3627626649245559228L;

  /**
   * Constructor.
   * 
   * @param cause The cause of this exception.
   */
  public TransformationException(Throwable cause) {
    super(cause);
  }
}
