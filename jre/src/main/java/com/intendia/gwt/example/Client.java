package com.intendia.gwt.example;

import static com.intendia.gwt.example.client.Nominatim.NOMINATIM_OPENSTREETMAP;
import static java.lang.System.err;
import static java.lang.System.out;

import com.intendia.gwt.autorest.client.JreResourceBuilder;
import com.intendia.gwt.example.client.Nominatim;
import com.intendia.gwt.example.client.Nominatim_RestServiceModel;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class Client {

    public static void main(String[] args) {
        Nominatim nominatim = osm();
        nominatim.search("Málaga, España", "json").subscribe(new Observer<>() {
            @Override public void onSubscribe(Disposable d) { }
            @Override public void onNext(Nominatim.SearchResult n) {
                out.printf("[%.0f] %s (%s,%s)%n", n.importance * 10., n.display_name, n.lon, n.lat);
            }
            @Override public void onError(Throwable e) { err.println("request error: " + e); }
            @Override public void onComplete() { out.println("done!"); }
        });
    }

    public static Nominatim_RestServiceModel osm() {
        return new Nominatim_RestServiceModel(() -> new JreResourceBuilder(NOMINATIM_OPENSTREETMAP));
    }
}
