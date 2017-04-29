package com.intendia.gwt.example;

import static com.intendia.gwt.example.client.Nominatim.NOMINATIM_OPENSTREETMAP;
import static java.lang.System.out;

import com.intendia.gwt.example.client.Nominatim;
import com.intendia.gwt.example.client.Nominatim_RestServiceModel;

public class Client {

    public static void main(String[] args) throws Exception {
        Nominatim nominatim = osm();
        nominatim.search("Málaga,España", "json").forEach(n -> {
            out.println("[" + (int) (n.importance * 10.) + "] " + n.display_name + " (" + n.lon + "," + n.lat + ")");
        });
    }

    public static Nominatim_RestServiceModel osm() {
        return new Nominatim_RestServiceModel(() -> new JreResourceBuilder(NOMINATIM_OPENSTREETMAP));
    }
}
