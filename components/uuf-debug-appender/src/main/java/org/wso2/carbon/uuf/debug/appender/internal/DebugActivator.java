package org.wso2.carbon.uuf.debug.appender.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.wso2.carbon.uuf.debug.appender.service.DebuggerService;
import org.wso2.carbon.uuf.debug.appender.service.DebuggerServiceImpl;

import org.wso2.carbon.uuf.debug.appender.log4j2appender.DebugAppender;


public class DebugActivator implements BundleActivator {
    private ServiceRegistration registration;
    private static final Logger logger = LogManager.getLogger(DebugActivator.class.getName());

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        registration = bundleContext.registerService(
                DebuggerService.class.getName(),
                new DebuggerServiceImpl(),
                null);

        while(true) {
            logger.info("DebugAppender.getMessagesAsJson() " + DebugAppender.getMessagesAsJson());
            Thread.sleep(5000);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        registration.unregister();
    }
}