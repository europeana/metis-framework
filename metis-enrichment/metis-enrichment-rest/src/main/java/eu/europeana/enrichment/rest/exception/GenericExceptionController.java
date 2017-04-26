package eu.europeana.enrichment.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Class that holds the implementation of the toResponse methods of each
 * exception mapper
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 * 
 * 
 */
@Controller
public class GenericExceptionController {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleException(Exception e){
        ModelAndView mav = new ModelAndView();
        mav.addObject("error", e.getMessage());
        return mav;
    }
}
