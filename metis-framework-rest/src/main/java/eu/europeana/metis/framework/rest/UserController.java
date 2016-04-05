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
 * Created by ymamakis on 4/5/16.
 */
@Controller
@Api("/")
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping(value = "/user/{email}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ApiOperation(value = "Retrieve a user from Zoho by email", response = Contact.class)
    public Contact getUserByEmail(@ApiParam("email") @PathVariable("email")String email) throws IOException, ParseException {
        return userService.getUserByEmail(email);
    }
}
