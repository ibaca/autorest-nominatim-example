package com.intendia.gwt.example.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.intendia.gwt.autorest.client.AutoRestGwt;
import com.intendia.gwt.autorest.client.RequestResourceBuilder;
import com.intendia.gwt.autorest.client.ResourceFactory;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import rx.Observable;

public class ExampleEntryPoint implements EntryPoint {

    @AutoRestGwt @Path("search") interface Nominatim {
        @GET Observable<SearchResult> search(@QueryParam("q") String query, @QueryParam("format") String format);
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    public static class SearchResult {
        public String display_name; //ex: "Málaga, Provincia de Málaga, Andalusia, Spain",
        public String lat; //ex: "36.7210805",
        public String lon; //ex: "-4.4210409",
        public double importance; //ex: 0.73359836669253,
    }

    public void onModuleLoad() {
        ResourceFactory root = ResourceFactory.create(() -> new RequestResourceBuilder()
                .path("http://nominatim.openstreetmap.org/"));
        Nominatim nominatim = new Nominatim_RestServiceProxy(root);
        nominatim.search("Málaga,España", "json").subscribe(n -> {
            append("[" + (int) (n.importance * 10.) + "] " + n.display_name + " (" + n.lon + "," + n.lat + ")");
        });
    }

    private static void append(String text) {
        RootPanel.get().add(new Label(text));
    }
}
