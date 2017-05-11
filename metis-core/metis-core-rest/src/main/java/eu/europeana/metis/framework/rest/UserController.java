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

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.framework.common.Contact;
import eu.europeana.metis.framework.exceptions.UserNotFoundException;
import eu.europeana.metis.framework.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * User management from Zoho
 * Created by ymamakis on 4/5/16.
 */
@Controller
@Api("/")
public class UserController {

  @Autowired
  private UserService userService;

  @RequestMapping(value = RestEndpoints.USER, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response", response = Contact.class),
      @ApiResponse(code = 404, message = "User not found")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "email", value = "User's email", dataType = "string", paramType = "query"),
  })
  @ApiOperation(value = "Get a user from Zoho by email")
  public Contact getUserByEmail(@QueryParam("email") String email)
      throws IOException, UserNotFoundException {
    return userService.getUserByEmail(email);
  }
}
