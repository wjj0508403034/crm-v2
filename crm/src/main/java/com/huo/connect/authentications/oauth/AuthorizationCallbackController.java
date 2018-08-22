package com.huo.connect.authentications.oauth;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.huo.connect.net.GenericHttpResponse;
import com.huo.connect.net.HttpClientExecutor;

@Controller
@RequestMapping(value = "/oauth2/")
public class AuthorizationCallbackController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationCallbackController.class);

	@Value("${oauth.onedrive.client_id}")
	private String clientId;

	@Value("${oauth.onedrive.client_secret}")
	private String clientSecret;

	@Value("${oauth.onedrive.baseUrl}")
	private String baseUrl;

	@Autowired
	private HttpClientExecutor httpClientExecutor;

	@RequestMapping(value = "authorizedCallback", method = RequestMethod.GET)
	@ResponseBody
	public GenericHttpResponse authorizationCallback(HttpServletRequest request, HttpServletResponse response) {
		String code = request.getParameter("code");
		String state = request.getParameter("state");
		GenericHttpResponse res = httpClientExecutor.postForm(this.buildTokenUrl(), this.buildTokenParams(code));
		return res;
	}

	private String buildTokenUrl() {
		StringBuilder sb = new StringBuilder();
		sb.append(baseUrl + "token");
		return sb.toString();
	}

	private List<NameValuePair> buildTokenParams(String code) {
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("client_id", this.clientId));
		params.add(new BasicNameValuePair("scope", "openid offline_access profile"));
		params.add(new BasicNameValuePair("code", code));
		params.add(new BasicNameValuePair("redirect_uri", "http://localhost:8080/oauth2/authorizedCallback"));
		params.add(new BasicNameValuePair("grant_type", "authorization_code"));
		params.add(new BasicNameValuePair("client_secret", clientSecret));
		return params;
	}

}
