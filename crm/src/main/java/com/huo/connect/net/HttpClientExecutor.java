package com.huo.connect.net;

import java.util.List;

import org.apache.http.NameValuePair;

public interface HttpClientExecutor {

	/**
	 * see {@link #get(String, QueryParam)}
	 * 
	 * @param path
	 * @return
	 */
	GenericHttpResponse get(String path);

	/**
	 * This method is get data from Partner by sending http get request.
	 * 
	 * 
	 * @param path
	 * @param queryParam
	 * @return @GenericHttpResponse
	 */
	GenericHttpResponse get(String path, QueryParam queryParam);

	/**
	 * This method using to post data to Partner by sending http post request
	 * request
	 * 
	 * 
	 * @param path
	 * @param payload
	 * @return @GenericHttpResponse
	 */
	GenericHttpResponse post(String path, Object payload);

	GenericHttpResponse postForm(String path, List<NameValuePair> params);

	/**
	 * This method using to delete resource from Partner by sending http delete
	 * request
	 * 
	 * @param path
	 * @return @GenericHttpResponse
	 */
	GenericHttpResponse delete(String path);

	/**
	 * This method using to update resource to Partner by sending http patch
	 * request
	 * 
	 * 
	 * @param path
	 * @param payload
	 * @return
	 */
	GenericHttpResponse patch(String path, Object payload);

	/**
	 * This method using to update resource to Partner by sending http patch
	 * request
	 * 
	 * 
	 * @param path
	 * @param payload
	 * @return
	 */
	GenericHttpResponse put(String path, Object payload);

	/**
	 * This method using to connect to Partner by sending http head request
	 * request
	 * 
	 * 
	 * @param path
	 * @param payload
	 * @return
	 */
	GenericHttpResponse head(String path);
}
