package com.huo.connect.authentications.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/oauth2/")
public class AuthorizationController {

	@Value("${oauth.onedrive.client_id}")
	private String clientId;

	@Value("${oauth.onedrive.client_secret}")
	private String clientSecret;

	@Value("${oauth.onedrive.baseUrl}")
	private String baseUrl;

	@RequestMapping(value = "authorize", method = RequestMethod.GET)
	public String startAuthorization() {
		return "redirect:" + this.buildAuthorizeUrl();
	}

	private String buildAuthorizeUrl() {
		StringBuilder sb = new StringBuilder();
		sb.append(baseUrl + "authorize?");
		sb.append("client_id=" + clientId);
		sb.append("&response_type=code");
		sb.append("&redirect_uri=http://localhost:8080/oauth2/authorizedCallback");
		sb.append("&response_mode=query");
		sb.append("&scope=openid offline_access profile");
		sb.append("&state=12345");
		return sb.toString();
	}
}
