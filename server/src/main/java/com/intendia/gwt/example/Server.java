package com.intendia.gwt.example;

import static org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_HEADERS_PARAM;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import rx.Observable;
import rx.Single;

public class Server {
    public static void main(String[] args) throws Exception {
        ResourceConfig config = new DefaultResourceConfig(ResourceNominatim.class, ObjectMapperContextResolver.class);
        org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(8080);
        ServletContextHandler context = new ServletContextHandler(server, "/");
        FilterHolder cof = new FilterHolder(new CrossOriginFilter()); cof.setInitParameter(ALLOWED_HEADERS_PARAM, "*");
        context.addFilter(cof, "/*", EnumSet.allOf(DispatcherType.class));
        context.addServlet(new ServletHolder(new ServletContainer(config)), "/*");
        server.start();
        server.join();
    }

    @Provider
    public static class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
        final ObjectMapper objectMapper = new ObjectMapper();

        {
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            objectMapper.registerModule(new ApplicationJacksonModule());
        }

        @Override public ObjectMapper getContext(Class<?> type) {
            return objectMapper;
        }
    }

    public static class ApplicationJacksonModule extends Module {
        @Override public String getModuleName() { return "Application Module"; }
        @Override public Version version() { return Version.unknownVersion(); }
        @Override public void setupModule(SetupContext c) { c.addSerializers(new RxJavaJacksonSerializers()); }
    }

    public static class RxJavaJacksonSerializers extends Serializers.Base {
        @Override public JsonSerializer<?> findSerializer(SerializationConfig sc, JavaType jt, BeanDescription bd) {
            Class<?> raw = jt.getRawClass();
            if (Observable.class.isAssignableFrom(raw)) {
                JavaType vt = jt.containedType(0);
                if (vt == null) vt = TypeFactory.unknownType();
                JavaType delegate = sc.getTypeFactory().constructParametrizedType(Iterable.class, Iterable.class, vt);
                return new StdDelegatingSerializer(ObservableConverter.instance, delegate, null);
            }
            if (Single.class.isAssignableFrom(raw)) {
                JavaType delegate = jt.containedType(0);
                if (delegate == null) delegate = TypeFactory.unknownType();
                return new StdDelegatingSerializer(SingleConverter.instance, delegate, null);
            }
            return super.findSerializer(sc, jt, bd);
        }

        static class ObservableConverter extends StdConverter<Object, Iterable<?>> {
            static final Converter<Object, Iterable<?>> instance = new ObservableConverter();
            @Override public Iterable<?> convert(Object v) { return ((Observable<?>) v).toBlocking().toIterable(); }
        }

        static class SingleConverter extends StdConverter<Object, Object> {
            static final Converter<Object, Object> instance = new SingleConverter();
            @Override public Object convert(Object v) { return ((Single<?>) v).toObservable().toBlocking().single(); }
        }
    }
}
