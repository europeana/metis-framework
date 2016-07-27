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
package eu.europeana.metis.framework.rest;

import eu.europeana.metis.framework.common.Contact;
import eu.europeana.metis.framework.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.text.ParseException;

/**
 * User management from Zoho
 * Created by ymamakis on 4/5/16.
 */
@Controller
@Api("/")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * Get a user by email
     * @param email The email of the user
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @RequestMapping(value = "/user/{email}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ApiOperation(value = "Retrieve a user from Zoho by email", response = Contact.class)
    public Contact getUserByEmail(@ApiParam("email") @PathVariable("email")String email) throws IOException, ParseException {
        return userService.getUserByEmail(email);
    }
}
