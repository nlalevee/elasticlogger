package org.hibnet.elasticlogger.http.index;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.elasticsearch.action.admin.indices.status.IndicesStatusRequest;
import org.elasticsearch.action.admin.indices.status.IndicesStatusResponse;
import org.elasticsearch.client.Client;
import org.hibnet.elasticlogger.http.TemplateRenderer;

public class IndexHandler extends AbstractHandler {

    private final TemplateRenderer templateRenderer;

    private final Client client;

    public IndexHandler(Client client, TemplateRenderer templateRenderer) {
        this.client = client;
        this.templateRenderer = templateRenderer;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        IndicesStatusRequest indicesStatusRequest = new IndicesStatusRequest();
        IndicesStatusResponse indicesStatusResponse = client.admin().indices().status(indicesStatusRequest).actionGet();

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("indicesStatusResponse", indicesStatusResponse);

        templateRenderer.render(baseRequest, response, "index/index.html", vars);
    }
}
