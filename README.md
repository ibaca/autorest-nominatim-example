# AutoREST Nominatim Example

[AutoREST][AutoREST] generates utility code which should help you extract the request data
on each endpoint call and use it to populate a request. So in simplified example you define…
```java
@AutoRestGwt @Path("search") interface Nominatim {
  @GET Observable<SearchResult> search(@QueryParam("q") String query);
}
```
[AutoREST][AutoREST] generates a `Nominatim_RestServiceModel`, and you should implement a `ResourceVisitor` 
provided as factory to the model, this makes your services instantiations similar to… 
```java
Nominatim nominatim = new Nominatim_RestServiceModel(DummyResourceVisitor::new);
nominatim.search("málaga").subscribe(n -> out.println("received " + n));
```
…on each call to the model (like the previous `.search("málaga")`) a new visitor (like next one) is evaluated…
```java
class DummyResourceVisitor {
  public ResourceVisitor path(Object... paths) { out.println("path " + Arrays.toString(paths)); return this; }
  public ResourceVisitor param(String key, @Nullable Object value) { out.println("param " + key + "=" + value); return this; }
  public ResourceVisitor method(String method) { out.println("method " + method); return this; }
  public <T> T as(Class<? super T> container, Class<?> type) { return null; }
}
```
…producing this output `"method GET" ⏎ "path [search]" ⏎ "param q=málaga"`.

Ok, this dummy visitor makes nothing, just print Request configuration, do not even generate a request. Gathering
the request data, creating a request and handling the response into the expected type is now your responsibility.
**BUT** this project try to demonstrate how moving the responsibility out of the library and giving to
you the full control of the request building, request encoding and response decoding is not only much easier, simpler
and clear as you may expect, but also make it almost trivial to extend and customize your services.

[AutoREST][AutoREST] separates services handling in three responsibilities
* **schema** defines the REST service interface and models using [JAX-RS][jaxrs] standard 
* **request** the goal of client side services is to create a request to transfer the payload
* **codec** the url, headers and request payload should be encoded/decoded

> You should note that AutoREST should not build the request (althought include a basic implementation), 
do not implement codecs, and schema definition uses the standard JAX-RS, so not AutoREST involved neither.
For all this, AutoREST should be considered a blueprint which help you organize this responsabilities giving
a common template and best practices to keep service implementation clean and simple.

## Modules

Client modules of this project shows how to implement the **request** and **codec** responsibilities for
various environments, API module shows how to define the service **schema** in one place and share this schema
not only between the different client but also with the server implementation.

* [api module][api] contains the [Nominatim][Nominatim] scheme definition (models as inner classes),
and is shared between the other modules
* client implementations: uses the API to build a request
  * [jre module][jre] 
  * [gwt module][gwt]
* server implementations: uses the API to expose a service
  * [server module][server] implements the [Nominatim][Nominatim] service definition as 
  [ResourceNominatim][Resource], and exposes it using a jetty + jersey + jackson server. Configuration 
  of this libraries are all [here][Main] and you can run the server as an plain java using the `Main.main`.
  

[api]: https://github.com/ibaca/autorest-nominatim-example/tree/master/api
[jre]: https://github.com/ibaca/autorest-nominatim-example/tree/master/jre
[gwt]: https://github.com/ibaca/autorest-nominatim-example/tree/master/gwt
[server]: https://github.com/ibaca/autorest-nominatim-example/tree/master/server
[Nominatim]: https://github.com/ibaca/autorest-nominatim-example/blob/master/api/src/main/java/com/intendia/gwt/example/client/Nominatim.java
[Resource]: https://github.com/ibaca/autorest-nominatim-example/blob/master/server/src/main/java/com/intendia/gwt/example/ResourceNominatim.java
[Main]: https://github.com/ibaca/autorest-nominatim-example/blob/master/server/src/main/java/com/intendia/gwt/example/Main.java
[jaxrs]: https://jax-rs-spec.java.net/
[AutoREST]: https://github.com/intendia-oss/autorest
