package com.huo.connect.net;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GenericHttpResponse {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenericHttpResponse.class);

	private final ObjectMapper objectMapper = new ObjectMapper();
	private Map<String, String> headers = new HashMap<>();
	private String body;
	private int statusCode;
	private Exception innerException;

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public <T> T entity(Class<T> klass) {
		if (StringUtils.isEmpty(this.body)) {
			return null;
		}

		try {
			return this.objectMapper.readValue(this.body, klass);
		} catch (Exception e) {
			LOGGER.warn("Parse response body to class {} failed", klass);
			throw new RuntimeException("Parse response body to class failed", e);
		}
	}

	public Exception getInnerException() {
		return innerException;
	}

	public void setInnerException(Exception innerException) {
		this.innerException = innerException;
	}

}
