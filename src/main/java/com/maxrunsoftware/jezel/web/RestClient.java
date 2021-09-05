/*
 * Copyright (c) 2021 Max Run Software (dev@maxrunsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.maxrunsoftware.jezel.web;

import static com.google.common.base.Preconditions.*;
import static com.maxrunsoftware.jezel.Util.*;

import javax.json.JsonObject;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContextBuilder;

import com.google.common.collect.ImmutableList;
import com.maxrunsoftware.jezel.SettingService;
import com.maxrunsoftware.jezel.model.SchedulerJob;

public class RestClient {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RestClient.class);

	private final String host;
	private final String username;
	private final String password;
	private String bearer;

	public RestClient(SettingService settings) {
		var h = settings.getRestUrl();
		if (!h.endsWith("/")) h = h + "/";
		host = h;
		username = settings.getRestUsername();
		password = settings.getRestPassword();
	}

	public ImmutableList<SchedulerJob> getSchedulerJobs() throws Exception {
		if (bearer == null) login();
		var response = get(Verb.GET, host + "job");
		var o = response.jsonObject;
		var array = o.getJsonArray("schedulerJobs");
		var lb = ImmutableList.<SchedulerJob>builder();
		for (var val : array) {
			var oo = val.asJsonObject();
			var sj = new SchedulerJob();
			sj.fromJson(oo);
			lb.add(sj);
		}
		return lb.build();
	}

	private void login() throws Exception {
		var response = get(Verb.POST, host + "session", username, password);
		var o = response.jsonObject;
		this.bearer = trimOrNull(o.getString("bearer"));
	}

	public static record Response(int code, String json, JsonObject jsonObject) {}

	private Response get(Verb verb, String host, String username, String password) throws Exception {
		return get(verb, host, username, password, null);
	}

	private Response get(Verb verb, String host, String bearer) throws Exception {
		return get(verb, host, null, null, bearer);
	}

	private Response get(Verb verb, String host) throws Exception {
		if (bearer == null) login();
		var response = get(verb, host, bearer);
		if (response.code == 401) {
			// Old bearer token, get a new one
			login();
		}
		response = get(verb, host, bearer);
		if (response.code == 401) {
			// Bad username or password
			var msg = "Received 401 attempting to login";
			if (response.jsonObject != null) {
				var jmsg = trimOrNull(response.jsonObject.getString("message"));
				if (jmsg != null) msg = msg + ": " + jmsg;
			}
			throw new Exception(msg);
		}

		return response;
	}

	private static enum Verb {
		GET, POST, PUT, DELETE
	}

	private Response get(Verb verb, String host, String username, String password, String bearer) throws Exception {
		checkNotNull(host);
		HttpUriRequestBase action;
		if (verb.equals(Verb.DELETE)) action = new HttpDelete(host);
		else if (verb.equals(Verb.POST)) action = new HttpPost(host);
		else if (verb.equals(Verb.PUT)) action = new HttpPut(host);
		else action = new HttpGet(host);

		if (username != null) {
			LOG.debug("Username supplied so adding Authorization header");
			action.addHeader("Authorization", httpAuthorizationEncode(username, password));
		}
		if (bearer != null) {
			LOG.debug("Bearer supplied so adding Authorization header");
			action.addHeader("Authorization", "Bearer " + bearer);
		}

		int code = -1;
		JsonObject o = null;
		String json = null;

		try (CloseableHttpClient httpclient = createClient()) {
			LOG.trace("HttpClient created: " + httpclient.getClass().getName());
			try (CloseableHttpResponse response = httpclient.execute(action)) {
				LOG.trace("Received response: " + response.getClass().getName());
				code = response.getCode();
				HttpEntity httpEntity = response.getEntity();
				json = EntityUtils.toString(httpEntity);
				LOG.debug(json);

				o = fromJsonString(json);

			}
		}
		return new Response(code, json, o);

	}

	private CloseableHttpClient createClient() throws Exception {

		SSLContextBuilder sshbuilder = new SSLContextBuilder();

		sshbuilder.loadTrustMaterial(null, (chain, authType) -> true);

		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sshbuilder.build(), (hostname, session) -> true);

		var pcm = PoolingHttpClientConnectionManagerBuilder.create().setSSLSocketFactory(sslsf).build();

		//@formatter:off
		CloseableHttpClient httpclient = HttpClients.custom()
				.setDefaultCredentialsProvider(null)
				.setConnectionManager(pcm)
				.setDefaultCookieStore(null)
				.build();
		//@formatter:on

		return httpclient;
	}
}
