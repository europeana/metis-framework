package eu.europeana.metis.dereference.wrappers;

import eu.europeana.metis.dereference.RdfRetriever;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.ResponseInfo;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BodyHandlerWrapper implements BodyHandler<String> {

  private static final Logger LOG = LoggerFactory.getLogger(BodyHandlerWrapper.class);

  private final CountDownLatch latch = new CountDownLatch(1);
  private final BodyHandler<String> handler;
  private SubscriberWrapper subscriberWrapper;

  public BodyHandlerWrapper(BodyHandler<String> handler) {
    this.handler = handler;
  }

  @Override
  public BodySubscriber<String> apply(ResponseInfo responseInfo) {
    subscriberWrapper = new SubscriberWrapper(handler.apply(responseInfo), latch);
    return subscriberWrapper;
  }

  public void cancel() {
    CompletableFuture.runAsync(() -> {
      try {
        latch.await();
        subscriberWrapper.cancel();
      } catch (InterruptedException e) {
        LOG.info("There was some problem interrupting the connection");
      }
    });
  }
}