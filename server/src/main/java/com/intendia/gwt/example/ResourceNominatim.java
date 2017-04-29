package com.intendia.gwt.example;

import com.intendia.gwt.example.client.Nominatim;
import rx.Observable;

public class ResourceNominatim implements Nominatim {

    @Override public Observable<SearchResult> search(String query, String format) {
        if (!"json".equals(format)) throw new IllegalArgumentException("only 'json' format supported");
        return Client.osm().search(query, format);
    }
}
