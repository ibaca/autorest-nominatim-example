package com.intendia.gwt.nominatim;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.intendia.gwt.autorest.client.ResourceVisitor;
import com.intendia.gwt.example.client.Nominatim;
import com.intendia.gwt.example.client.Nominatim.SearchResult;
import com.intendia.gwt.example.client.Nominatim_RestServiceModel;

import rx.functions.Action1;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.newThread;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final TextView out = (TextView) findViewById(R.id.text); out.setText("");
        Nominatim nominatim = new Nominatim_RestServiceModel(new ResourceVisitor.Supplier() {
            @Override public ResourceVisitor get() { return osm(); }
        });
        nominatim.search("Málaga,España", "json").subscribeOn(newThread()).observeOn(mainThread())
                .forEach(new Action1<SearchResult>() {
                    @Override public void call(SearchResult n) {
                        out.append("[" + (int) (n.importance * 10.) + "] " + n.display_name + " (" + n.lon + "," + n.lat + ")");
                    }
                });
    }

    static ResourceVisitor osm() {
        return new AndroidResourceBuilder().path("http://nominatim.openstreetmap.org/");
    }
}
