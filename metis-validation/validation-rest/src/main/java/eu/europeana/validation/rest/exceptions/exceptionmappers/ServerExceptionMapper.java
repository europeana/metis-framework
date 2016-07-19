package eu.europeana.validation.rest.exceptions.exceptionmappers;

import eu.europeana.validation.rest.exceptions.ServerException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by ymamakis on 2/24/16.
 */
@Controller
public class ServerExceptionMapper{
    @ExceptionHandler(ServerException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleException(ServerException e) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("Server error");
        mav.addObject(e.getMessage());
        return  mav;
    }
}
