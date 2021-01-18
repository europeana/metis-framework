package eu.europeana.metis.authentication.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EmailParameterTest {

  @Test
  void testEmptyEmailParameterConstructor() {
    final EmailParameter emailParameter = new EmailParameter();
    assertNull(emailParameter.getEmail());
  }

  @Test
  void testEmailParameterConstructor() {
    final String exampleEmail1 = "example1@email.com";
    final String exampleEmail2 = "example2@email.com";
    final EmailParameter emailParameter = new EmailParameter(exampleEmail1);
    assertEquals(exampleEmail1, emailParameter.getEmail());
    emailParameter.setEmail(exampleEmail2);
    assertEquals(exampleEmail2, emailParameter.getEmail());
  }

}