package com.intendia.gwt.example;

import static java.lang.System.out;

import com.intendia.gwt.autorest.client.ResourceVisitor;
import com.intendia.gwt.example.client.Nominatim;
import com.intendia.gwt.example.client.Nominatim_RestServiceModel;

public class Main {

    public static void main(String[] args) throws Exception {
        Nominatim nominatim = new Nominatim_RestServiceModel(() -> osm());
        nominatim.search("Málaga,España", "json").forEach(n -> {
            out.println("[" + (int) (n.importance * 10.) + "] " + n.display_name + " (" + n.lon + "," + n.lat + ")");
        });
    }

    static ResourceVisitor osm() { return new JreResourceBuilder().path("http://nominatim.openstreetmap.org/"); }

}
