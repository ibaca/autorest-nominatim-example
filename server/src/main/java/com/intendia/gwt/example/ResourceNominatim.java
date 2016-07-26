package com.intendia.gwt.example;

import com.intendia.gwt.example.client.Nominatim;
import rx.Observable;

public class ResourceNominatim implements Nominatim {

    @Override public Observable<SearchResult> search(String query, String format) {
        return Observable.range(1, 10).map(n -> {
            SearchResult out = new SearchResult();
            out.display_name = query + "-" + n;
            out.lon = n + "0.1";
            out.lat = n + "0.2";
            out.importance = n;
            return out;
        });
    }
}
