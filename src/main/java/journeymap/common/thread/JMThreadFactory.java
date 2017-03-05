/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Factory to produce threads labeled as belonging to JourneyMap
 *
 * @author techbrew
 */
public class JMThreadFactory implements ThreadFactory
{
    /**
     * The constant threadNumber.
     */
    final static AtomicInteger threadNumber = new AtomicInteger(1);
    /**
     * The constant namePrefix.
     */
    final static String namePrefix = "JM-"; //$NON-NLS-1$
    /**
     * The Group.
     */
    final ThreadGroup group;
    /**
     * The Name.
     */
    final String name;

    /**
     * Instantiates a new Jm thread factory.
     *
     * @param name the name
     */
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
