package ru.practicum.project.auth.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
	    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
	    classes = AuthorizationServerConfig.class,
	    properties = {
	        "server.port=0", 
	        "spring.security.oauth2.authorizationserver.issuer-url=http://issuer.test"
	    }
	)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.security.oauth2.authorizationserver.issuer-url=http://issuer.test"
})
class AuthorizationServerConfigTest {

  @Autowired MockMvc mvc;

  @Test
  void wellKnown_returnsIssuer() throws Exception {
    mvc.perform(get("/.well-known/oauth-authorization-server"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.issuer").value("http://issuer.test"));
  }

  @Test
  void jwks_exposed() throws Exception {
    mvc.perform(get("/oauth2/jwks"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.keys[0].kty").value("RSA"));
  }

  @Test
  void token_clientCredentials_ok() throws Exception {
    mvc.perform(post("/oauth2/token")
            .with(httpBasic("store-app", "secret"))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("grant_type", "client_credentials")
            .param("scope", "payment.read payment.write"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.access_token").exists())
        .andExpect(jsonPath("$.token_type").value("Bearer"))
        .andExpect(jsonPath("$.scope", containsString("payment.read")))
        .andExpect(jsonPath("$.scope", containsString("payment.write")));
  }

  @Test
  void token_clientCredentials_badSecret_unauthorized() throws Exception {
    mvc.perform(post("/oauth2/token")
            .with(httpBasic("store-app", "wrong"))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("grant_type", "client_credentials")
            .param("scope", "payment.read"))
        .andExpect(status().isUnauthorized());
  }
}