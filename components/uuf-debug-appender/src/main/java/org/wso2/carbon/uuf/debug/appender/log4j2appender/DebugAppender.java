/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.uuf.debug.appender.log4j2appender;

import com.google.gson.Gson;

import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.appender.*;

import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Plugin(name="DebugAppender", category="Core", elementType="appender", printObject=true)
public class DebugAppender extends AbstractAppender {

    private static final int MAX_CAPACITY = 1000;

    private static Queue<DebugMessage> messages;
    private static Gson gson;

    protected DebugAppender(String name, Filter filter,
                             Layout<? extends Serializable> layout, final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
        this.messages = new ConcurrentLinkedQueue<>();
        this.gson = new Gson();
    }

    @Override
    public void append(LogEvent event) {
        //TODO get MDC support
        String requestId = event.getContextMap().get("uuf-request");
        if (requestId != null) {
            messages.add(new DebugMessage(requestId, event));
            if (messages.size() > MAX_CAPACITY) {
                messages.poll();
            }
        }
    }


    public void attach() {
 //       setThreshold(Level.DEBUG);
 //       Logger logger = Logger.getLogger("org.wso2.carbon.uuf");
        //TODO: fix this
//        logger.setLevel(Level.DEBUG);
//        Logger.getRootLogger().addAppender(this);
    }


    public void detach() {
//        Logger.getRootLogger().removeAppender(this);
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
    public static DebugAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter,
            @PluginAttribute("otherAttribute") String otherAttribute) {
        if (name == null) {
            LOGGER.error("No name provided for MyCustomAppenderImpl");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new DebugAppender(name, filter, layout, true);
    }
}
