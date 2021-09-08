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
package com.maxrunsoftware.jezel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.hibernate.Session;

import jakarta.servlet.http.HttpServletRequest;

public final class Util {
	public static final String httpAuthorizationEncode(String username, String password) {
		final var up = username + ":" + password;
		final var encodedBytes = up.getBytes(StandardCharsets.UTF_8);
		final var str = Base64.getEncoder().encodeToString(encodedBytes);
		final var strFull = "Basic " + str;
		return strFull;
	}

	public static class HttpAuthorizationCredential {
		public final String username;
		public final String password;

		private HttpAuthorizationCredential(String username, String password) {
			this.username = username;
			this.password = password;
		}
	}

	public static final HttpAuthorizationCredential httpAuthorizationDecode(String authorization) {
		if (authorization == null) return null;
		if (!authorization.toLowerCase().startsWith("basic")) return null;

		final var base64Credentials = authorization.substring("basic".length()).trim();
		final var credDecoded = Base64.getDecoder().decode(base64Credentials);
		final var credentials = new String(credDecoded, StandardCharsets.UTF_8);
		final var values = credentials.split(":", 2);
		return new HttpAuthorizationCredential(values[0], values[1]);
	}

	public static final String httpAuthorizationDecodeBearer(String authorization) {
		if (authorization == null) return null;
		if (!authorization.toLowerCase().startsWith("bearer")) return null;

		final var bearerToken = authorization.substring("bearer".length()).trim();
		return bearerToken;
	}

	public static final byte[] copy(byte[] bytes) {
		if (bytes == null) return null;
		byte[] dest = new byte[bytes.length];
		System.arraycopy(bytes, 0, dest, 0, bytes.length);
		return dest;
	}

	public static final byte[] asBytes(String s) {
		return s.getBytes(StandardCharsets.UTF_8);
	}

	public static final String asString(byte[] bytes) {
		return new String(bytes, StandardCharsets.UTF_8);
	}

	protected static UUID asUUID(String val) {
		if (val == null) return null;
		if (val.length() == 32) val = val.subSequence(0, 8)
				+ "-" + val.subSequence(8, 12)
				+ "-" + val.subSequence(12, 16)
				+ "-" + val.subSequence(16, 20)
				+ "-" + val.subSequence(20, 32);
		if (val.length() != 36) return null;
		return UUID.fromString(val);
	}

	public static final String trimOrNull(String s) {
		if (s == null) return null;
		s = s.trim();
		if (s.length() == 0) return null;
		return s;
	}

	public static final boolean parseBoolean(String s) {
		s = s.toLowerCase();
		if (s.equals("true") || s.equals("t") || s.equals("yes") || s.equals("y") || s.equals("1") || s.equals("on")) return true;
		if (s.equals("false") || s.equals("f") || s.equals("no") || s.equals("n") || s.equals("0") || s.equals("off")) return false;
		throw new IllegalArgumentException("Could not parse '" + s + "' to boolean");
	}

	public static final int parseInt(String s) {
		return Integer.parseInt(s);
	}

	public static final <T> T as(Object o, Class<T> t) {
		// https://stackoverflow.com/a/1034300
		return t.isInstance(o) ? t.cast(o) : null;
	}

	public static final String[] split(String s, String separator) {
		// https://stackoverflow.com/a/6374137
		return s.split(Pattern.quote(separator));
	}

	@SafeVarargs
	public static final <T> T coalesce(T... values) {
		for (var val : values) {
			if (val != null) return val;
		}
		return null;
	}

	@SafeVarargs
	public static final <T> boolean equalsAny(T sourceObj, T... otherObjs) {
		//@formatter:off
		if (otherObjs == null || otherObjs.length == 0) return false;
		for (T otherObj : otherObjs) {
			if (sourceObj == null) {
				if (otherObj == null) return true;
			} else {
				if (otherObj != null) {
					if (sourceObj.equals(otherObj)) return true; 
				}
			}
		}
		//@formatter:on
		return false;
	}

	@SafeVarargs
	public static final boolean equalsAnyIgnoreCase(String sourceObj, String... otherObjs) {
		//@formatter:off
		if (otherObjs == null || otherObjs.length == 0) return false;
		for (String otherObj : otherObjs) {
			if (sourceObj == null) {
				if (otherObj == null) return true;
			} else {
				if (otherObj != null) {
					if (sourceObj.equalsIgnoreCase(otherObj)) return true; 
				}
			}
		}
		//@formatter:on
		return false;
	}

	public static final String readLine() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		try {
			return reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static final String getEnvironmentVariable(String name) {
		return Util.trimOrNull(System.getenv(name));
	}

	public static final String getEnvironmentVariable(String name, String defaultValue) {
		return coalesce(getEnvironmentVariable(name), defaultValue);
	}

	public static final int getEnvironmentVariable(String name, int defaultValue) {
		var val = getEnvironmentVariable(name, null);
		if (val == null) return defaultValue;
		return Integer.parseInt(val);
	}

	public static final boolean getEnvironmentVariable(String name, boolean defaultValue) {
		var val = getEnvironmentVariable(name, null);
		if (val == null) return defaultValue;
		return parseBoolean(val);
	}

	public static int randomInt(int minInclusive, int maxInclusive) {
		return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
	}

	public static boolean randomBoolean() {
		return randomInt(0, 2) == 1 ? true : false;
	}

	public static UUID randomUUID() {
		return UUID.randomUUID();
	}

	@SafeVarargs
	public static <T> T randomPick(T... objs) {
		return objs[randomInt(0, objs.length - 1)];
	}

	public static <T> T randomPick(List<T> objs) {
		return objs.get(randomInt(0, objs.size() - 1));
	}

	public static <T> T randomRemove(List<T> objs) {
		return objs.remove(randomInt(0, objs.size() - 1));
	}

	public static <V> Map<String, V> mapCaseInsensitive() {
		return new CaseInsensitiveMap<String, V>();
	}

	public static final <T> List<T> getAll(Class<T> type, Session session) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<T> criteria = builder.createQuery(type);
		criteria.from(type);
		List<T> data = session.createQuery(criteria).getResultList();
		return data;
	}

	public static final <T> T getById(Class<T> type, Session session, int id) {
		return session.get(type, id);
	}

	public static final JsonObjectBuilder createObjectBuilder() {
		return Json.createObjectBuilder();
	}

	public static final JsonArrayBuilder createArrayBuilder() {
		return Json.createArrayBuilder();
	}

	public static final <T extends JsonCodable> JsonArrayBuilder createArrayBuilder(Iterable<T> iterable) {
		var jb = Json.createArrayBuilder();
		for (var item : iterable) {
			jb.add(item.toJson());
		}
		return jb;
	}

	public static final JsonArrayBuilder createArrayBuilder(Map<String, String> map, String keyName, String valName) {
		var aBuilder = createArrayBuilder();
		for (var pKey : map.keySet()) {
			var pVal = map.get(pKey);
			if (pVal == null) continue;
			var pOB = createObjectBuilder();
			pOB.add(keyName, pKey);
			pOB.add(valName, pVal);
			aBuilder.add(pOB);
		}
		return aBuilder;
	}

	public static final String toJsonString(JsonObjectBuilder json, boolean formatted) {
		return toJsonString(json.build(), formatted);
	}

	public static final String toJsonString(JsonObject jsonObject, boolean formatted) {
		Map<String, Boolean> config = new HashMap<>();

		if (formatted) { config.put(JsonGenerator.PRETTY_PRINTING, true); }

		JsonWriterFactory writerFactory = Json.createWriterFactory(config);

		String jsonString = "";
		try (Writer writer = new StringWriter()) {
			writerFactory.createWriter(writer).write(jsonObject);
			jsonString = writer.toString();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		jsonString = jsonString.trim();
		return jsonString;
	}

	public static final JsonObject fromJsonString(String json) {
		return Json.createReader(new StringReader(json)).readObject();
	}

	public static final int save(Session session, Object obj) {
		var tx = session.beginTransaction();
		var result = (int) session.save(obj);
		tx.commit();
		return result;
	}

	public static final void delete(Session session, Object obj) {
		var tx = session.beginTransaction();
		session.delete(obj);
		tx.commit();
	}

	public static final boolean delete(Class<?> clazz, Session session, int id) {
		var tx = session.beginTransaction();
		var obj = session.get(clazz, id);
		if (obj == null) return false;
		session.delete(obj);
		tx.commit();
		return true;
	}

	public static final String getFullURL(HttpServletRequest request) {
		// https://stackoverflow.com/a/2222268
		StringBuilder requestURL = new StringBuilder(request.getRequestURL().toString());
		String queryString = request.getQueryString();
		if (queryString != null) { requestURL.append("?").append(queryString); }
		return requestURL.toString();
	}
}
