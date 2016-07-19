package eu.europeana.validation.rest.exceptions.exceptionmappers;

import eu.europeana.validation.rest.exceptions.BatchValidationException;
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
public class BatchValidationExceptionController {
    @ExceptionHandler(BatchValidationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ModelAndView handleException(BatchValidationException e) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("Batch validation error");
        mav.addObject(e.getList());
        mav.addObject(e.getMessage());
        return  mav;
    }
}
