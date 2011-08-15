package org.hibnet.elasticlogger.http;

import static org.hibnet.elasticlogger.http.URIMatcher.endsWith;
import static org.hibnet.elasticlogger.http.URIMatcher.eq;
import static org.hibnet.elasticlogger.http.URIMatcher.regexp;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandlerContainer;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.elasticsearch.action.admin.indices.status.TransportIndicesStatusAction;
import org.elasticsearch.action.search.TransportSearchAction;

public class DispatcherHandler extends AbstractHandlerContainer {

    private Map<URIMatcher, Handler> handlers = new LinkedHashMap<URIMatcher, Handler>();

    public DispatcherHandler(TransportSearchAction transportSearchAction, TransportIndicesStatusAction transportIndicesStatusAction) {
        TemplateRenderer templateRenderer = new TemplateRenderer();

        IndexHandler indexHandler = new IndexHandler(transportIndicesStatusAction,  templateRenderer);
        handlers.put(eq("/"), indexHandler);
        handlers.put(eq("/index.html"), indexHandler);

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setBaseResource(Resource.newClassPathResource("/org/hibnet/elasticlogger/http/resources/"));
        handlers.put(endsWith(".css"), resourceHandler);
        handlers.put(endsWith(".png"), resourceHandler);

        SearchHandler searchHandler = new SearchHandler(transportSearchAction, templateRenderer);
        handlers.put(regexp("/([a-zA-Z][a-zA-Z0-9]*)/"), searchHandler);
        handlers.put(regexp("/([a-zA-Z][a-zA-Z0-9]*)/index\\.html"), searchHandler);
    }

    @Override
    public Handler[] getHandlers() {
        return handlers.values().toArray(new Handler[0]);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        for (Entry<URIMatcher, Handler> entry : handlers.entrySet()) {
            if (entry.getKey().match(target, request)) {
                entry.getValue().handle(target, baseRequest, request, response);
                return;
            }
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        baseRequest.setHandled(true);
    }

}
