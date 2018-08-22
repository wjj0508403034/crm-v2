package com.huo.connect.net.impl;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huo.connect.net.GenericHttpResponse;
import com.huo.connect.net.HttpClientExecutor;
import com.huo.connect.net.HttpClientFactory;
import com.huo.connect.net.HttpMethods;
import com.huo.connect.net.QueryParam;

@Component
public class HttpClientExecutorImpl implements HttpClientExecutor {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientExecutorImpl.class);

	@Value("${http.connection.timeout}")
	protected int connectionTimeout;

	@Value("${http.connection.request.timeout}")
	protected int connectionRequestTimeout;

	@Value("${http.socket.timeout}")
	protected int socketTimeout;


	private final String HTTP_HEADS_ACCEPT = "accept";
	private final String HTTP_HEADS_CONTENTTYPE = "content-type";

	@Autowired
	private HttpClientFactory httpClientFactory;

	private CloseableHttpClient getHttpClient() {
		return this.httpClientFactory.getHttpClient();
	}

	@Override
	public GenericHttpResponse get(String path) {
		return this.get(path, null);
	}

	@Override
	public GenericHttpResponse get(String path, QueryParam queryParam) {
		String basePath = this.buildRequestUrl(path);
		String url = this.addQueryParam(basePath, queryParam);
		return this.execute(url, HttpMethods.GET);
	}

	@Override
	public GenericHttpResponse post(String path, Object payload) {
		return this.execute(this.buildRequestUrl(path), HttpMethods.POST, this.mapperToEntity(payload));
	}
	
	@Override
	public GenericHttpResponse postForm(String path, List<NameValuePair> params) {
		try {
			return this.execute(this.buildRequestUrl(path), HttpMethods.POST, new UrlEncodedFormEntity(params));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public GenericHttpResponse delete(String path) {
		return this.execute(this.buildRequestUrl(path), HttpMethods.DELETE);
	}

	@Override
	public GenericHttpResponse patch(String path, Object payload) {
		return this.execute(this.buildRequestUrl(path), HttpMethods.PATCH, this.mapperToEntity(payload));
	}

	@Override
	public GenericHttpResponse put(String path, Object payload) {
		return this.execute(this.buildRequestUrl(path), HttpMethods.PUT, this.mapperToEntity(payload));
	}

	@Override
	public GenericHttpResponse head(String path) {
		return this.execute(this.buildRequestUrl(path), HttpMethods.HEAD);
	}

	private RequestConfig getRequestConfig() {
		return RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectionTimeout)
				.setConnectionRequestTimeout(connectionRequestTimeout).build();
	}

	/**
	 * Create http request by http methods
	 * 
	 * @param url
	 * @param method
	 * @return @HttpRequestBase
	 */
	private HttpRequestBase createHttpRequestByMethod(String url, String method) {
		if (StringUtils.equalsIgnoreCase(method, HttpMethods.GET)) {
			return new HttpGet(url);
		}

		if (StringUtils.equalsIgnoreCase(method, HttpMethods.POST)) {
			return new HttpPost(url);
		}

		if (StringUtils.equalsIgnoreCase(method, HttpMethods.DELETE)) {
			return new HttpDelete(url);
		}

		if (StringUtils.equalsIgnoreCase(method, HttpMethods.PATCH)) {
			return new HttpPatch(url);
		}

		if (StringUtils.equalsIgnoreCase(method, HttpMethods.PUT)) {
			return new HttpPut(url);
		}

		if (StringUtils.equalsIgnoreCase(method, HttpMethods.HEAD)) {
			return new HttpHead(url);
		}

		throw new RuntimeException("Not support method:" + method);
	}

	/**
	 * Convert any object to @HttpEntity
	 * 
	 * 
	 * @param payload
	 * @return @HttpEntity
	 */
	private HttpEntity mapperToEntity(Object payload) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String content = objectMapper.writeValueAsString(payload);
			LOGGER.info("payload is : {}", content);
			return new StringEntity(content);
		} catch (Exception e) {
			LOGGER.warn("Convert payload failed", e);
			throw new RuntimeException("Convert payload failed", e);
		}
	}

	/**
	 * See {@link #execute(String, String, Object)}
	 * 
	 * @param url
	 * @param method
	 * @return @GenericHttpResponse
	 */
	private GenericHttpResponse execute(String url, String method) {
		return this.execute(url, method, null);
	}

	/**
	 * This method using to execute http request and return to response
	 * 
	 * 
	 * @param url
	 *            is request url
	 * @param method
	 *            is HttpMethod, see @HttpMethods
	 * @return @GenericHttpResponse
	 */
	protected GenericHttpResponse execute(String url, String method, HttpEntity payload) {
		final CloseableHttpClient httpClient = this.getHttpClient();
		GenericHttpResponse response = new GenericHttpResponse();
		HttpRequestBase httpEntity = this.createHttpRequestByMethod(url, method);
		httpEntity.setConfig(this.getRequestConfig());
		httpEntity.addHeader(HTTP_HEADS_ACCEPT, "application/json");
		httpEntity.addHeader(HTTP_HEADS_CONTENTTYPE, "application/x-www-form-urlencoded");

		if (payload != null && httpEntity instanceof HttpEntityEnclosingRequest) {
			((HttpEntityEnclosingRequest) httpEntity).setEntity(payload);
		}

		InputStream inputStream = null;
		try {
			LOGGER.info("Execute the http request [{}] {}", method, url);
			HttpResponse httpResponse = httpClient.execute(httpEntity);
			if (httpResponse != null && httpResponse.getStatusLine() != null) {
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				response.setStatusCode(statusCode);
				LOGGER.info("Http response code: {}", statusCode);
				HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {
					inputStream = entity.getContent();
					String content = IOUtils.toString(inputStream, "UTF-8");
					LOGGER.info("Http response content:{}", content);
					response.setBody(content);
				}
				for (Header header : httpResponse.getAllHeaders()) {
					response.getHeaders().put(header.getName(), header.getValue());
				}
				return response;
			}

		} catch (Exception e) {
			LOGGER.warn("Execute request {} failed", url, e);
			response.setStatusCode(500);
			response.setInnerException(e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					LOGGER.warn("Close stream exception {}", e);
				}
			}

			if (httpEntity != null) {
				try {
					httpEntity.releaseConnection();
				} catch (Exception e) {
					LOGGER.warn("Release connection failed, {}", e);
				}
			}
		}

		return response;
	}

	/**
	 * Put query parameters at end of the url
	 * 
	 * @param path
	 * @param queryParam
	 * @return the full url
	 */
	private String addQueryParam(String path, QueryParam queryParam) {
		if (queryParam == null) {
			return path;
		}
		String queryStr = queryParam.buildQuery();
		if (StringUtils.isEmpty(queryStr)) {
			return path;
		}

		if (StringUtils.endsWithIgnoreCase(path, "?")) {
			return path + queryStr;
		}

		return path + "?" + queryStr;
	}

	/**
	 * Combine the url path
	 * 
	 * @param path1
	 * @param path2
	 * @return combined path
	 */
	private String combineUrlPath(String path1, String path2) {
		if (StringUtils.isEmpty(path2)) {
			return path1;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(path1);
		if (!StringUtils.endsWithIgnoreCase(path1, "/")) {
			sb.append("/");
		}

		if (StringUtils.startsWithIgnoreCase(path2, "/")) {
			sb.append(path2.substring(1));
		} else {
			sb.append(path2);
		}

		return sb.toString();
	}

	private String buildRequestUrl(String apiPath) {
//		String rootPath = this.getRootPath();
//		String baseUrl = this.combineUrlPath(rootPath, this.subPath);
//		return this.combineUrlPath(baseUrl, apiPath);
		return apiPath;
	}


}
