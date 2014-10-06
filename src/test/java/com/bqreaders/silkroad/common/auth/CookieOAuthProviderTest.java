package com.bqreaders.silkroad.common.auth;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.dropwizard.auth.Authenticator;

/**
 * @author Rubén Carrasco
 *
 */
public class CookieOAuthProviderTest {

	private static final String TOKEN = "token";

	CookieOAuthProvider<TestClass> cookieOAuthProvider;

	private Authenticator<String, TestClass> authenticator;

	private final String realm = "asdf";

	private Cookie cookie;

	private HttpContext context;

	private HttpRequestContext request;

	private Auth a;

	@Before
	public void setUp() throws Exception {
		authenticator = mock(Authenticator.class);
		Optional<TestClass> value = Optional.of(new TestClass());
		when(authenticator.authenticate(TOKEN)).thenReturn(value);
		cookieOAuthProvider = new CookieOAuthProvider<>(authenticator, realm);

		a = mock(Auth.class);
		when(a.required()).thenReturn(true);

		cookie = mock(Cookie.class);
		when(cookie.getValue()).thenReturn(TOKEN);

		context = mock(HttpContext.class);
		request = mock(HttpRequestContext.class);
		when(context.getRequest()).thenReturn(request);
		Map<String, Cookie> cookies = new HashMap<String, Cookie>() {
			{
				put(HttpHeaders.AUTHORIZATION, cookie);
			}
		};
		when(request.getCookies()).thenReturn(cookies);
	}

	@Test
	public void test() {
		ComponentContext ic = null;
		Parameter c = null;
		Injectable<?> injectable = cookieOAuthProvider.getInjectable(ic, a, c);
		Object value = ((AbstractHttpContextInjectable) injectable).getValue(context);
		assertThat(value instanceof TestClass).isTrue();
	}

	@Test(expected = WebApplicationException.class)
	public void testNoCookie() {
		ComponentContext ic = null;
		Parameter c = null;
		Injectable<?> injectable = cookieOAuthProvider.getInjectable(ic, a, c);

		when(request.getCookies()).thenReturn(null);

		Object value = ((AbstractHttpContextInjectable) injectable).getValue(context);
	}

	private class TestClass {

	}

}
