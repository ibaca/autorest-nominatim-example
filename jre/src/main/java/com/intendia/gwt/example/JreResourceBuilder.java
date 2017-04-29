package com.intendia.gwt.example;

import static com.intendia.gwt.autorest.client.CollectorResourceVisitor.Param.expand;
import static java.util.stream.Collectors.joining;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intendia.gwt.autorest.client.CollectorResourceVisitor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;
import rx.Observable;

public class JreResourceBuilder extends CollectorResourceVisitor {

    public JreResourceBuilder(String root) { path(root); }

    private String encode(String key) {
        try { return URLEncoder.encode(key, "UTF-8"); } catch (Exception e) { throw new RuntimeException(e); }
    }

    protected String query() {
        String q = "";
        for (Param p : expand(queryParams)) q += (q.isEmpty() ? "" : "&") + encode(p.k) + "=" + encode(p.v.toString());
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
            for (Param e : headerParams) req.setRequestProperty(e.k, Objects.toString(e.v));
            // currently data is ignored
        } catch (IOException e) {
            throw new RuntimeException("open connection error: " + e, e);
        }

        try (InputStream inputStream = req.getInputStream()) {
            int rc = req.getResponseCode();
            if (rc != 200) throw new RuntimeException("unexpected response code " + rc);
            Gson gson = new Gson();
            String text = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(joining("\n"));
            JsonElement json = new JsonParser().parse(text);
            //noinspection unchecked
            return json.isJsonObject() ?
                    (T) Observable.just(gson.fromJson(json, type)) :
                    (T) Observable.from(json.getAsJsonArray()).map(e -> gson.fromJson(e, type));
        } catch (IOException e) {
            throw new RuntimeException("receiving response error: " + e, e);
        }
    }
}
