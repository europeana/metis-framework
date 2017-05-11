/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.metis.mapping.rest.controllers;

import eu.europeana.metis.mapping.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * A generic exception Hadnler
 * Created by ymamakis on 6/16/16.
 */
@Controller
public class GenericExceptionHandler {
    /**
     * Return a 500
     * @param req The request that generated the error
     * @param exception The exception generated
     * @return a HTTP 500
     */
    @ExceptionHandler({SaveMappingFailedException.class, MappingToXSLException.class,
            TemplateGenerationFailedException.class, TransformationFailedException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleException(HttpServletRequest req, Exception exception) {
        return constructMAV(req, exception);
    }

    /**
     * Return a 404
     * @param req The request that generated the error
     * @param exception The exception generated
     * @return a HTTP 404
     */
    @ExceptionHandler({MappingNotFoundException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFoundException(HttpServletRequest req, MappingNotFoundException exception) {
        return constructMAV(req, exception);
    }

    private ModelAndView constructMAV(HttpServletRequest req, Exception e) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", e);
        mav.addObject("url", req.getRequestURL());
        mav.setViewName("Error");
        return mav;
    }
}
