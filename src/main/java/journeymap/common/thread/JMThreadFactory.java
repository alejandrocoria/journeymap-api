/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.common.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Factory to produce threads labeled as belonging to JourneyMap
 *
 * @author mwoodman
 */
public class JMThreadFactory implements ThreadFactory
{
    final static AtomicInteger threadNumber = new AtomicInteger(1);
    final static String namePrefix = "JM-"; //$NON-NLS-1$
    final ThreadGroup group;
    final String name;

    public JMThreadFactory(String name)
    {
        this.name = namePrefix + name;
        SecurityManager securitymanager = System.getSecurityManager();
        group = securitymanager == null ? Thread.currentThread().getThreadGroup() : securitymanager.getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable runnable)
    {
        String fullName = name + "-" + threadNumber.getAndIncrement(); //$NON-NLS-1$
        Thread thread = new Thread(group, runnable, fullName);
        if (thread.isDaemon())
        {
            thread.setDaemon(false);
        }
        if (thread.getPriority() != 5)
        {
            thread.setPriority(5);
        }
        return thread;
    }

}
