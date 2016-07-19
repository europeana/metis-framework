package eu.europeana.validation.rest.exceptions.exceptionmappers;

import eu.europeana.validation.rest.exceptions.ValidationException;
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
public class ValidationExceptionController {
    @ExceptionHandler(ValidationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ModelAndView handleException(ValidationException e) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("Validation error");
        mav.addObject(e.getId());
        mav.addObject(e.getMessage());
        return  mav;
    }
}
