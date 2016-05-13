package com.intendia.gwt.example;

import static java.lang.System.out;

import com.intendia.gwt.autorest.client.AutoRestGwt;
import com.intendia.gwt.autorest.client.ResourceVisitor;
import java.util.function.Consumer;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

public class Main {
    @AutoRestGwt @Path("search") public interface Nominatim {
        @GET Async<SearchResult> search(@QueryParam("q") String query, @QueryParam("format") String format);
    }

    public static class SearchResult {
        public String display_name; //ex: "Málaga, Provincia de Málaga, Andalusia, Spain",
        public String lat; //ex: "36.7210805",
        public String lon; //ex: "-4.4210409",
        public double importance; //ex: 0.73359836669253,
    }

    public static void main(String[] args) throws Exception {
        Nominatim nominatim = new Nominatim_RestServiceModel(() -> osm());
        nominatim.search("Málaga,España", "json").forEach(n -> {
            out.println("[" + (int) (n.importance * 10.) + "] " + n.display_name + " (" + n.lon + "," + n.lat + ")");
        });
    }

    static ResourceVisitor osm() { return new JreResourceBuilder().path("http://nominatim.openstreetmap.org/"); }

    public interface Async<T> {
        void forEach(Consumer<T> fn);
    }
}
