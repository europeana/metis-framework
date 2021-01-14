package eu.europeana.metis.authentication.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CredentialsTest {

  @Test
  void testCredentialsConstructor() {
    final String exampleEmail1 = "example1@email.com";
    final String examplePassword1 = "password1";
    final String exampleEmail2 = "example2@email.com";
    final String examplePassword2 = "password2";

    final Credentials credentials = new Credentials(exampleEmail1, examplePassword1);
    assertEquals(exampleEmail1, credentials.getEmail());
    assertEquals(examplePassword1, credentials.getPassword());

    credentials.setEmail(exampleEmail2);
    credentials.setPassword(examplePassword2);
    assertEquals(exampleEmail2, credentials.getEmail());
    assertEquals(examplePassword2, credentials.getPassword());
  }

}