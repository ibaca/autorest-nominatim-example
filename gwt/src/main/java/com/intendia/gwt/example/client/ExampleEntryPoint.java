package com.intendia.gwt.example.client;

import static com.intendia.gwt.example.client.Nominatim.NOMINATIM_OPENSTREETMAP;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.intendia.gwt.autorest.client.RequestResourceBuilder;
import com.intendia.gwt.autorest.client.ResourceVisitor;
import rx.Observable;
import rx.subjects.PublishSubject;

public class ExampleEntryPoint implements EntryPoint {

    public void onModuleLoad() {
        PublishSubject<String> query = PublishSubject.create();

        TextBox text = new TextBox(); RootPanel.get().add(text);
        text.addValueChangeHandler(e -> query.onNext(e.getValue()));

        Button search = new Button("search"); RootPanel.get().add(search);
        search.addClickHandler(e -> query.onNext(text.getValue()));

        ListBox url = new ListBox(); RootPanel.get().add(url);
        url.addItem(NOMINATIM_OPENSTREETMAP);
        url.addItem("http://localhost:8080/");
        url.addChangeHandler(e -> query.onNext(text.getValue()));

        FlowPanel results = new FlowPanel(); RootPanel.get().add(results);

        // remember last selected server
        Storage storage = Storage.getLocalStorageIfSupported();
        if (storage != null) {
            try {
                url.setSelectedIndex(Integer.valueOf(storage.getItem("nominatim.url")));
            } catch (Exception ignore) {}
            url.addChangeHandler(c -> storage.setItem("nominatim.url", Integer.toString(url.getSelectedIndex())));
        }

        // on each tick, re-configure root resource and fire new request
        query.switchMap(q -> {
            results.clear();
            if (q == null || q.isEmpty()) return Observable.empty();
            Nominatim nominatim = new Nominatim_RestServiceModel(() -> osm(url.getSelectedItemText()));
            return nominatim.search(q, "json").doOnNext(n -> results.add(new Label(
                    "[" + (int) (n.importance * 10.) + "] " + n.display_name + " (" + n.lon + "," + n.lat + ")")));
        }).retry((cnt, err) -> {
            GWT.log("request error: " + err, err); return true;
        }).subscribe();

         // fires initial search
        text.setValue("Málaga,España", true);
    }

    static ResourceVisitor osm(String path) { return new RequestResourceBuilder().path(path); }
}
