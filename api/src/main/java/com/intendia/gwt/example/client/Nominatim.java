package com.intendia.gwt.example.client;

import com.intendia.gwt.autorest.client.AutoRestGwt;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import rx.Observable;

@AutoRestGwt @Path("search")
public interface Nominatim {

    @GET Observable<SearchResult> search(@QueryParam("q") String query, @QueryParam("format") String format);

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object") //
    class SearchResult {
        public String display_name; //ex: "Málaga, Provincia de Málaga, Andalusia, Spain",
        public String lat; //ex: "36.7210805",
        public String lon; //ex: "-4.4210409",
        public double importance; //ex: 0.73359836669253,
    }
}

