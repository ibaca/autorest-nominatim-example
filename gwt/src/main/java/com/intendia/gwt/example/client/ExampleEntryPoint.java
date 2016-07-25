package com.intendia.gwt.example.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.intendia.gwt.autorest.client.RequestResourceVisitor;
import com.intendia.gwt.autorest.client.ResourceVisitor;

public class ExampleEntryPoint implements EntryPoint {

    public void onModuleLoad() {
        Nominatim nominatim = new Nominatim_RestServiceModel(() -> osm());
        nominatim.search("Málaga,España", "json").forEach(n -> RootPanel.get().add(new Label(
                "[" + (int) (n.importance * 10.) + "] " + n.display_name + " (" + n.lon + "," + n.lat + ")")));
    }

    static ResourceVisitor osm() { return new RequestResourceVisitor().path("http://nominatim.openstreetmap.org/"); }

}
