package com.intendia.gwt.example;

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
import rx.Observable;

public class JreResourceBuilder extends CollectorResourceVisitor {

    private String encode(String key) {
        try { return URLEncoder.encode(key, "UTF-8"); } catch (Exception e) { throw new RuntimeException(e); }
    }

    protected String query() {
        String q = "";
        for (Param p : queryParams) q += (q.isEmpty() ? "" : "&") + encode(p.key) + "=" + encode(p.value.toString());
        return q.isEmpty() ? "" : "?" + q;
    }

    protected String uri() {
        String uri = "";
        for (String path : paths) uri += path;
        return uri + query();
    }

    @Override public <T> T as(Class<? super T> container, Class<?> type) {
        if (!Observable.class.equals(container)) {
            throw new IllegalArgumentException("unsupported type " + container);
        }

        HttpURLConnection req;
        try {
            req = (HttpURLConnection) new URL(uri()).openConnection();
            req.setRequestMethod(method);
            req.setRequestProperty("Accept", "application/json");
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
            Gson gson = new Gson();
            JsonElement json = new JsonParser().parse(new InputStreamReader(inputStream));
            //noinspection unchecked
            return json.isJsonObject() ?
                    (T) Observable.just(gson.fromJson(json, type)) :
                    (T) Observable.from(json.getAsJsonArray()).map(e -> gson.fromJson(e, type));
        } catch (IOException e) {
            throw new RuntimeException("receiving response error: " + e, e);
        }
    }
}
