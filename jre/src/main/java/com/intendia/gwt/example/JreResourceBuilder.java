package com.intendia.gwt.example;

import static com.intendia.gwt.autorest.client.CollectorResourceVisitor.Param.expand;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intendia.gwt.autorest.client.CollectorResourceVisitor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;
import javax.annotation.Nullable;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

public class JreResourceBuilder extends CollectorResourceVisitor {
    private static final Gson JSON = new Gson();

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

    @SuppressWarnings("unchecked")
    @Override public <T> T as(Class<? super T> container, Class<?> type) {
        Func1<JsonElement, ?> parser = n -> JSON.fromJson(n, type);
        if (Completable.class.equals(container)) return (T) request().toCompletable();
        if (Single.class.equals(container)) return (T) request().map(parser);
        if (Observable.class.equals(container)) return (T) request()
                .flatMapObservable(n -> Observable.from(n.getAsJsonArray()).map(parser));
        throw new IllegalArgumentException("unsupported type " + container);
    }

    private Single<JsonElement> request() {
        return Single.defer(() -> {
            HttpURLConnection req;
            try {
                req = (HttpURLConnection) new URL(uri()).openConnection();
                req.setRequestMethod(method);
                req.setRequestProperty("Accept", "application/json");
                for (Param e : headerParams) req.setRequestProperty(e.k, Objects.toString(e.v));
            } catch (IOException e) {
                throw new RuntimeException("open connection error: " + e, e);
            }

            if (data != null) try (OutputStreamWriter out = new OutputStreamWriter(req.getOutputStream())) {
                JSON.toJson(data, out);
            } catch (Exception e) {
                throw new RuntimeException("writing output stream error: " + e, e);
            }

            @Nullable JsonElement json;
            try (InputStream inputStream = req.getInputStream()) {
                int rc = req.getResponseCode();
                if (rc != 200) throw new RuntimeException("unexpected response code " + rc);
                json = new JsonParser().parse(new InputStreamReader(inputStream));
            } catch (IOException e) {
                throw new RuntimeException("receiving response error: " + e, e);
            }
            return Single.just(json);
        });
    }
}
