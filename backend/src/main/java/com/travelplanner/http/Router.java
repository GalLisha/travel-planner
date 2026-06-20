package com.travelplanner.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.travelplanner.dto.response.ApiErrorDto;
import com.travelplanner.exception.BadRequestException;
import com.travelplanner.exception.NotFoundException;
import com.travelplanner.exception.UnauthorizedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal HTTP router built directly on the JDK's {@code com.sun.net.httpserver}
 * package. There is no servlet container and no web framework involved -
 * routes are matched by HTTP method + a path template (e.g. "/api/itinerary/{id}")
 * and dispatched to a {@link RouteHandler}.
 */
public class Router implements HttpHandler {

    private static final Pattern PARAM_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_]+)}");

    private final List<Route> routes = new ArrayList<>();

    public void get(String pathTemplate, RouteHandler handler) {
        register("GET", pathTemplate, handler);
    }

    public void post(String pathTemplate, RouteHandler handler) {
        register("POST", pathTemplate, handler);
    }

    public void put(String pathTemplate, RouteHandler handler) {
        register("PUT", pathTemplate, handler);
    }

    public void register(String method, String pathTemplate, RouteHandler handler) {
        List<String> paramNames = new ArrayList<>();
        Matcher paramMatcher = PARAM_PATTERN.matcher(pathTemplate);
        StringBuffer regex = new StringBuffer();
        while (paramMatcher.find()) {
            paramNames.add(paramMatcher.group(1));
            paramMatcher.appendReplacement(regex, "([^/]+)");
        }
        paramMatcher.appendTail(regex);
        routes.add(new Route(method, Pattern.compile("^" + regex + "$"), paramNames, handler));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            applyCorsHeaders(exchange);
            String method = exchange.getRequestMethod();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            for (Route route : routes) {
                if (!route.method.equalsIgnoreCase(method)) {
                    continue;
                }
                Matcher matcher = route.pattern.matcher(path);
                if (!matcher.matches()) {
                    continue;
                }
                Map<String, String> pathParams = new HashMap<>();
                for (int i = 0; i < route.paramNames.size(); i++) {
                    pathParams.put(route.paramNames.get(i), matcher.group(i + 1));
                }
                dispatch(route, exchange, pathParams);
                return;
            }
            new RequestContext(exchange, new HashMap<>())
                    .sendJson(404, new ApiErrorDto("NOT_FOUND", "No route for " + method + " " + path));
        } finally {
            exchange.close();
        }
    }

    private void dispatch(Route route, HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        RequestContext ctx = new RequestContext(exchange, pathParams);
        try {
            route.handler.handle(ctx);
        } catch (NotFoundException e) {
            ctx.sendJson(404, new ApiErrorDto("NOT_FOUND", e.getMessage()));
        } catch (BadRequestException e) {
            ctx.sendJson(400, new ApiErrorDto("BAD_REQUEST", e.getMessage()));
        } catch (UnauthorizedException e) {
            ctx.sendJson(401, new ApiErrorDto("UNAUTHORIZED", e.getMessage()));
        } catch (Throwable e) {
            e.printStackTrace();
            ctx.sendJson(500, new ApiErrorDto("INTERNAL_ERROR", "Something went wrong processing the request"));
        }
    }

    private void applyCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static class Route {
        final String method;
        final Pattern pattern;
        final List<String> paramNames;
        final RouteHandler handler;

        Route(String method, Pattern pattern, List<String> paramNames, RouteHandler handler) {
            this.method = method;
            this.pattern = pattern;
            this.paramNames = paramNames;
            this.handler = handler;
        }
    }
}
