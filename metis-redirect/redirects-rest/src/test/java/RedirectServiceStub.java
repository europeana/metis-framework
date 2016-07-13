import eu.europeana.redirects.model.RedirectRequest;
import eu.europeana.redirects.model.RedirectRequestList;
import eu.europeana.redirects.model.RedirectResponse;
import eu.europeana.redirects.model.RedirectResponseList;
import eu.europeana.redirects.service.RedirectService;
import org.junit.Ignore;

/**
 * Created by ymamakis on 1/20/16.
 */
@Ignore
public class RedirectServiceStub implements RedirectService {
    @Override
    public RedirectResponse createRedirect(RedirectRequest request) {
       return null;
    }

    @Override
    public RedirectResponseList createRedirects(RedirectRequestList requests) {
        return null;
    }
}
