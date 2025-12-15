package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class StaticResourceConfig {

    private Mono<ServerResponse> serveHtml(String path) {
        Resource resource = new ClassPathResource(path);
        return ServerResponse
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .body(BodyInserters.fromResource(resource));
    }

    @Bean
    public RouterFunction<ServerResponse> staticResourceRouter() {
        return RouterFunctions
                .resources("/static/**", new ClassPathResource("static/"))
                .and(route(GET("/css/**"), request -> {
                    String path = request.path();
                    Resource resource = new ClassPathResource("static" + path);
                    if (resource.exists()) {
                        return ServerResponse.ok()
                                .contentType(MediaType.parseMediaType(getMediaType(path)))
                                .body(BodyInserters.fromResource(resource));
                    }
                    return ServerResponse.notFound().build();
                }))
                .and(route(GET("/js/**"), request -> {
                    String path = request.path();
                    Resource resource = new ClassPathResource("static" + path);
                    if (resource.exists()) {
                        return ServerResponse.ok()
                                .contentType(MediaType.parseMediaType(getMediaType(path)))
                                .body(BodyInserters.fromResource(resource));
                    }
                    return ServerResponse.notFound().build();
                }))
                .and(route(GET("/"), request -> serveHtml("templates/index.html")))
                .and(route(GET("/home"), request -> serveHtml("templates/home.html")))
                .and(route(GET("/login"), request -> serveHtml("templates/login.html")))
                .and(route(GET("/register"), request -> serveHtml("templates/register.html")))
                .and(route(GET("/reset-password"), request -> serveHtml("templates/reset-password.html")))
                .and(route(GET("/oauth2-callback"), request -> serveHtml("templates/oauth2-callback.html")))
                .and(route(GET("/products"), request -> serveHtml("templates/products.html")))
                .and(route(GET("/menu-list"), request -> serveHtml("templates/menu-list.html")))
                .and(route(GET("/menu-detail-modal"), request -> serveHtml("templates/menu-detail-modal.html")))
                .and(route(GET("/orders"), request -> serveHtml("templates/orders.html")))
                .and(route(GET("/orders1"), request -> serveHtml("templates/orders1.html")))
                .and(route(GET("/boards"), request -> serveHtml("templates/boards.html")))
                .and(route(GET("/boards/{id}"), request -> serveHtml("templates/board-detail.html")))
                .and(route(GET("/boards/edit/{id}"), request -> serveHtml("templates/board-edit.html")))
                .and(route(GET("/board-write"), request -> serveHtml("templates/board-write.html")))
                .and(route(GET("/board-manage"), request -> serveHtml("templates/board-manage.html")))
                .and(route(GET("/board-detail"), request -> serveHtml("templates/board-detail.html")))
                .and(route(GET("/board-edit"), request -> serveHtml("templates/board-edit.html")))
                .and(route(GET("/admin"), request -> serveHtml("templates/admin.html")))
                .and(route(GET("/admin/orders"), request -> serveHtml("templates/admin/order-list.html")))
                .and(route(GET("/admin/orders/{id}"), request -> serveHtml("templates/admin/order-detail.html")))
                .and(route(GET("/cust"), request -> serveHtml("templates/cust.html")))
                .and(route(GET("/rbmq1"), request -> serveHtml("templates/rbmq1.html")));
    }

    private String getMediaType(String path) {
        if (path.endsWith(".css")) {
            return "text/css";
        } else if (path.endsWith(".js")) {
            return "application/javascript";
        }
        return "application/octet-stream";
    }
}

