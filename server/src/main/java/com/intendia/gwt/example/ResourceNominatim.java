package com.intendia.gwt.example;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import com.intendia.gwt.example.client.Nominatim;
import io.reactivex.Observable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

public class ResourceNominatim implements Nominatim {

    @Context HttpServletRequest request;

    @Override public Observable<SearchResult> search(String query, String format) {
        if (!TOKEN.equals(request.getHeader(X_API_KEY))) throw new WebApplicationException(
                Response.status(UNAUTHORIZED).entity("unauthorized; include 'X-Api-Key: secure' header").build());
        if (!"json".equals(format)) throw new IllegalArgumentException("only 'json' format supported");
        return Client.osm().search(query, format);
    }
}
