package com.huo.connect.net.impl;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.huo.connect.net.HttpClientFactory;

@Component
public class HttpClientFactoryImpl implements HttpClientFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientFactoryImpl.class);
	private PoolingHttpClientConnectionManager connectionManager;

	@Value("${http.connection.timeout}")
	private int connectionTimeout;

	@Value("${http.connection.request.timeout}")
	private int connectionRequestTimeout;

	@Value("${http.socket.timeout}")
	private int socketTimeout;

	@Value("${http.proxyHost}")
	private String proxyHost;

	@Value("${http.proxyPort}")
	private String proxyPort;

	private RequestConfig.Builder getRequestConfigBuilder() {
		return RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectionTimeout)
				.setConnectionRequestTimeout(connectionRequestTimeout);
	}

	private PoolingHttpClientConnectionManager initConnectionManager(SSLContext sslContext) {
		SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(sslContext,
				SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
				.register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", sslFactory)
				.build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(registry);
		connManager.setDefaultMaxPerRoute(200);
		connManager.setMaxTotal(1000);
		return connManager;
	}

	private CloseableHttpClient createHttpClient(PoolingHttpClientConnectionManager connManager, HttpHost proxy) {
		HttpClientBuilder hcBuilder = HttpClients.custom();
		RequestConfig.Builder configBuilder = this.getRequestConfigBuilder();
		if (proxy != null) {
			configBuilder.setProxy(proxy);
		}
		hcBuilder.setConnectionManager(connManager).setDefaultRequestConfig(configBuilder.build())
				.setRetryHandler(new DefaultHttpRequestRetryHandler());
		return hcBuilder.build();
	}

	private HttpHost buildHttpProxy(String host, String port) {
		if (StringUtils.isBlank(host)) {
			return null;
		}

		if (StringUtils.isBlank(port)) {
			return null;
		}

		int proxyPort = Integer.parseInt(port);
		LOGGER.debug("Proxy is {}:{}", host, proxyPort);
		return new HttpHost(host, proxyPort);
	}

	@Override
	public CloseableHttpClient getHttpClient() {
		try {
			HttpHost proxy = this.buildHttpProxy(proxyHost, proxyPort);
			PoolingHttpClientConnectionManager connManager = this.getConnectionManager();
			return this.createHttpClient(connManager, proxy);
		} catch (Exception e) {
			LOGGER.error("Create new http client failed", e);
			throw new RuntimeException("Create new http client failed", e);
		}
	}

	private KeyStore loadTrustedStore() {
		FileInputStream stream = null;
		try {
			char[] trustPassphrase = "changeit".toCharArray();
			KeyStore tks = KeyStore.getInstance("JKS");
			String ksPath = System.getProperty("java.home") + "/lib/security/cacerts";
			LOGGER.info("Read certs from jdk, {}", ksPath);
			stream = new FileInputStream(ksPath);
			tks.load(stream, trustPassphrase);
			return tks;

		} catch (Exception ex) {
			LOGGER.warn("Create http client failed, {}", ex);
			throw new RuntimeException("New ssl context", ex);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception ex) {
					LOGGER.warn("Close stream failed, {}", ex);
				}
			}
		}
	}

	private PoolingHttpClientConnectionManager getConnectionManager() throws KeyStoreException {
		if (this.connectionManager == null) {
			this.connectionManager = this.initConnectionManager(this.buildSSLContext());
		}

		return this.connectionManager;
	}

	private SSLContext buildSSLContext() {
		try {
			return new SSLContextBuilder().loadTrustMaterial(this.loadTrustedStore(), TrustSelfSignedStrategy.INSTANCE)
					.build();
		} catch (Exception ex) {
			LOGGER.warn("Create http client failed, {}", ex);
			throw new RuntimeException("New ssl context", ex);
		}
	}
}
