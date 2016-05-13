package com.intendia.gwt.example;

import static java.util.stream.StreamSupport.stream;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intendia.gwt.autorest.client.CollectorResourceVisitor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import javax.ws.rs.core.MediaType;

public class JreResourceBuilder extends CollectorResourceVisitor {

    private String encode(String key) {
        try { return URLEncoder.encode(key, "UTF-8"); } catch (Exception e) { throw new RuntimeException(e); }
    }

    protected String query() {
        String q = "";
        for (Param p : params) q += (q.isEmpty() ? "" : "&") + encode(p.key) + "=" + encode(p.value);
        return q.isEmpty() ? "" : "?" + q;
    }

    protected String uri() {
        String uri = "";
        for (String path : paths) uri += path;
        return uri + query();
    }

    @Override public <T> T as(Class<? super T> container, Class<?> type) {
        if (!Main.Async.class.equals(container)) {
            throw new IllegalArgumentException("unsupported type " + container);
        }

        HttpURLConnection req;
        try {
            req = (HttpURLConnection) new URL(uri()).openConnection();
            req.setRequestMethod(method);
            req.setRequestProperty(ACCEPT, MediaType.APPLICATION_JSON);
            for (Map.Entry<String, String> e : headers.entrySet()) {
                req.setRequestProperty(e.getKey(), e.getValue());
            }
            // currently data is ignored
        } catch (IOException e) {
            throw new RuntimeException("open connection error: " + e, e);
        }

        try (InputStream inputStream = req.getInputStream()) {
            int rc = req.getResponseCode();
            if (rc != 200) throw new RuntimeException("unexpected response code " + rc);
            return getAsync(new JsonParser().parse(new InputStreamReader(inputStream)), type);
        } catch (IOException e) {
            throw new RuntimeException("receiving response error: " + e, e);
        }
    }

    @SuppressWarnings("unchecked") private <T> T getAsync(JsonElement json, Class<?> type) {
        Gson gson = new Gson();
        return (T) (Main.Async) fn -> {
            if (json.isJsonObject()) fn.accept(gson.fromJson(json, type));
            else stream(json.getAsJsonArray().spliterator(), false).map(e -> gson.fromJson(e, type)).forEach(fn);
        };
    }
}
