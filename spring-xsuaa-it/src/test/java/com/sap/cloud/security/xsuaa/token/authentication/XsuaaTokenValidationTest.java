package com.sap.cloud.security.xsuaa.token.authentication;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.sap.cloud.security.xsuaa.mock.JWTUtil;

import testservice.api.XsuaaITApplication;
import testservice.api.v1.TestController;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"xsuaa.xsappname=java-hello-world",
		"xsuaa.clientid=sb-java-hello-world" }, classes = { XsuaaITApplication.class,
				testservice.api.v1.SecurityConfiguration.class, TestController.class })
@AutoConfigureMockMvc
@ActiveProfiles("test.api.v1")
public class XsuaaTokenValidationTest {

	@Autowired
	MockMvc mvc;

	@Test
	public void testToken_testdomain() throws Exception {
		this.mvc.perform(get("/user").with(bearerToken(JWTUtil.createJWT("/saml.txt", "testdomain"))))
				.andExpect(status().isOk()).andExpect(content().string(containsString("user:Mustermann")));
		this.mvc.perform(get("/user").with(bearerToken(JWTUtil.createJWT("/saml.txt", "testdomain"))))
				.andExpect(status().isOk()).andExpect(content().string(containsString("user:Mustermann")));
	}

	@Test
	public void testToken_otherdomain() throws Exception {
		this.mvc.perform(get("/user").with(bearerToken(JWTUtil.createJWT("/saml.txt", "otherdomain"))))
				.andExpect(status().isOk()).andExpect(content().string(containsString("user:Mustermann")));
	}

	@Test
	public void test_Scope() throws Exception {
		this.mvc.perform(get("/scope").with(bearerToken(JWTUtil.createJWT("/saml.txt", "otherdomain"))))
				.andExpect(status().isOk());
	}

	@Test
	@Deprecated
	public void test_requesttoken() throws Exception {
		this.mvc.perform(
				get("/requesttoken").with(bearerToken(JWTUtil.createJWT("/saml.txt", "uaa", "legacy-token-key"))))
				.andExpect(status().isOk()).andExpect(
						content().string(containsString(".ewogICJqdGkiOiAiOGU3YjNiMDAtNzc1MS00YjQ2LTliMWEtNWE0NmEyY")));
	}

	@Test
	@Deprecated
	public void test_clientcredentialstoken() throws Exception {
		this.mvc.perform(
				get("/clientCredentialsToken")
						.with(bearerToken(JWTUtil.createJWT("/saml.txt", "uaa", "legacy-token-key"))))
				.andExpect(status().isOk()).andExpect(
						content().string(containsString(".ewogICJqdGkiOiAiOGU3YjNiMDAtNzc1MS00YjQ2LTliMWEtNWE0NmEyY")));
	}

	private static class BearerTokenRequestPostProcessor implements RequestPostProcessor {
		private String token;

		public BearerTokenRequestPostProcessor(String token) {
			this.token = token;
		}

		@Override
		public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
			request.addHeader("Authorization", "Bearer " + this.token);
			return request;
		}
	}

	private static BearerTokenRequestPostProcessor bearerToken(String token) {
		return new BearerTokenRequestPostProcessor(token);
	}
}
