package eu.europeana.metis.mediaprocessing.http.wrappers;

import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.ResponseInfo;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CancelableBodyWrapper<T> implements BodyHandler<T> {

  private static final Logger LOG = LoggerFactory.getLogger(CancelableBodyWrapper.class);

  private final CountDownLatch latch = new CountDownLatch(1);
  private final BodyHandler<String> handler;
  private CancelableSubscriber cancelableSubscriber;

  public CancelableBodyWrapper(BodyHandler<String> handler) {
    this.handler = handler;
  }

  @Override
  public BodySubscriber<T> apply(ResponseInfo responseInfo) {
    cancelableSubscriber = new CancelableSubscriber(handler.apply(responseInfo), latch);
    return cancelableSubscriber;
  }

  public void cancel() {
    CompletableFuture.runAsync(() -> {
      try {
        latch.await();
        cancelableSubscriber.cancel();
      } catch (InterruptedException e) {
        LOG.info("There was some problem interrupting the connection");
      }
    });
  }
}