package org.wso2.carbon.uuf.debug.appender;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.SerializedLayout;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.gson.Gson;

/*import org.slf4j.Logger;
import org.slf4j.LoggerFactory;*/
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * An Appender that delivers events over socket connections. Supports HTTP.
 */
@Plugin(name = "HTTP", category = "Core", elementType = "appender", printObject = true)
public class HTTPAppender extends AbstractAppender {

/*    private String host = "localhost";
    private int port = 8080;*/

    private static final int MAX_CAPACITY = 1000;
   // private static final Logger log = LoggerFactory.getLogger(HTTPAppender.class);
    private static Queue<DebugMessage> messages;
    private static Gson gson;

    protected HTTPAppender(String name,
                           org.apache.logging.log4j.core.Filter filter, Layout<? extends Serializable> layout, final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
        this.messages = new ConcurrentLinkedQueue<>();
        this.gson = new Gson();

       // log.debug("This is a debug");
    }

    /**
     * The append method is where the appender does the work.
     * This sends the logevent to a http url with the given host and port.
     * @param event : log event that has logging information
     */
    @Override
    public void append(LogEvent event) {
        String loggername = event.getLoggerName().toString();
        if (loggername.contains("org.wso2.carbon.uuf")) {
            messages.add(new DebugMessage(loggername, event));
            if (messages.size() > MAX_CAPACITY) {
                messages.poll();
            }
        }

       // System.out.println(" Event appending... " + event);
    }

    public static String getMessagesAsJson() {
        return gson.toJson(messages);
    }


    private static class DebugMessage {

        private final String requestId;
        private final LogEvent event;

        public DebugMessage(String requestId, LogEvent event) {
            this.requestId = requestId;
            this.event = event;
        }
    }

    @PluginFactory
    public static HTTPAppender createAppender(
            // @formatter:off
            @PluginAttribute("name") final String name,
            @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) final boolean ignoreExceptions,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final org.apache.logging.log4j.core.Filter filter) {
        // @formatter:on

        if (layout == null) {
            layout = SerializedLayout.createLayout();
        }

        if (name == null) {
            LOGGER.error("No name provided for HTTPAppender");
            return null;
        }

        return new HTTPAppender(name, filter, layout, ignoreExceptions);
    }

}
