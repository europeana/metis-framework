package eu.europeana.metis.mediaprocessing.temp;

@FunctionalInterface
public interface HttpClientCallback<I, O> {

  void accept(I input, O output, String status);

}
