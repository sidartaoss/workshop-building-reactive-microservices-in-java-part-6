package io.vertx.sidartasilva.http;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.client.HttpRequest;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.codec.BodyCodec;
import io.vertx.rxjava.servicediscovery.ServiceDiscovery;
import io.vertx.rxjava.servicediscovery.types.HttpEndpoint;
import rx.Single;


public class HelloConsumerHttpVerticle extends AbstractVerticle {

    private WebClient hello;

    @Override
    public void start() {
        Router router = Router.router(vertx);
        router.get("/")
                .handler(this::invokeHelloMicroservice);

        // Create the service discovery instance
        ServiceDiscovery.create(vertx, discovery -> {
            // Look up for an Http endpoint named "project006"
            Single<WebClient> single = HttpEndpoint.rxGetWebClient(
                discovery, rec -> rec.getName().equalsIgnoreCase("project006"),
                new JsonObject().put("keepAlive", false)
            );

            single.subscribe(
                client -> {
                    // the configured hello to call our microservice
                    this.hello = client;
                    
                    // start the Http server
                    
                    vertx.createHttpServer()
                            .requestHandler(router::accept)
                            .listen(8081);

                },
                err -> System.out.println("Oh no, no service")
            );
        });

    }

    private void invokeHelloMicroservice(RoutingContext rc) {
        HttpRequest<JsonObject> request1 = hello.get("/Adam")
                .as(BodyCodec.jsonObject());

        HttpRequest<JsonObject> request2 = hello.get("/Eve")
                .as(BodyCodec.jsonObject());

        Single<HttpResponse<JsonObject>> s1 = request1.rxSend();
        Single<HttpResponse<JsonObject>> s2 = request2.rxSend();

        Single.zip(s1, s2, (adam, eve) -> {
            return new JsonObject()
                    .put("adam", adam.body().getString("message")
                        + " " + adam.body().getString("served-by")
                    )
                    .put("eve", eve.body().getString("message")
                        + " " + eve.body().getString("served-by")
                    );
        })
        .subscribe(
            x -> rc.response().end(x.encodePrettily()),
            t -> rc.response().end(new JsonObject().encodePrettily())
        );
    }

}
