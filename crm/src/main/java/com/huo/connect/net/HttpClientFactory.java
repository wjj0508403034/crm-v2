package com.huo.connect.net;

import org.apache.http.impl.client.CloseableHttpClient;

public interface HttpClientFactory {

	CloseableHttpClient getHttpClient();
}
