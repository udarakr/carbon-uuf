package org.wso2.carbon.uuf.core;

import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private final String context;
    private final List<Page> pages;
    private final Map<String, Fragment> fragments;
    private final Map<String, Renderable> bindings;

    public App(String context, List<Page> pages, Map<String, Fragment> fragments, Map<String, Renderable> bindings) {
        if (!context.startsWith("/")) {
            throw new IllegalArgumentException("app context must start with a '/'");
        }
        this.context = context;

        // We sort uri so that more wildcard-ed ones go to the bottom.
        Collections.sort(pages, (o1, o2) -> o1.getUriPatten().compareTo(o2.getUriPatten()));
        this.pages = pages;

        this.fragments = fragments;
        this.bindings = bindings;
    }

    public List<Page> getPages() {
        return pages;
    }

    public Map<String, Fragment> getFragments() {
        return fragments;
    }

    public Map<String, Renderable> getBindings() {
        return bindings;
    }

    public String serve(HttpRequest request) {
        String pageUri = request.getUri().substring(context.length());
        Optional<Page> servingPage = getPage(pageUri);
        if (!servingPage.isPresent()) {
            throw new UUFException("Requested page '" + pageUri + "' does not exists.", Response.Status.NOT_FOUND);
        }

        Page page = servingPage.get();
        if (log.isDebugEnabled()) {
            log.debug("Page '" + page.toString() + "' is serving.");
        }

        Map<String, Object> model = new HashMap<>();
        model.put("pageUri", pageUri);

        return page.serve(model, bindings, fragments);
    }

    public Optional<Page> getPage(String pageUri) {
        for (Page p : pages) {
            if (p.getUriPatten().match(pageUri)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "{\"context\": \"" + context + "\"}";
    }
}
