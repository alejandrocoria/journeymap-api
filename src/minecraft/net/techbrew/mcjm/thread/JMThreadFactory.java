package net.techbrew.mcjm.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class JMThreadFactory implements ThreadFactory {
	
	private static JMThreadFactory instance;

    final ThreadGroup group;
    final AtomicInteger threadNumber = new AtomicInteger(1);
    final String namePrefix;
    
    public static synchronized JMThreadFactory getInstance() {
    	if(instance==null) {
    		instance = new JMThreadFactory();
    	}
    	return instance;
    }
    
    private JMThreadFactory()
    {
        SecurityManager securitymanager = System.getSecurityManager();
        group = securitymanager == null ? Thread.currentThread().getThreadGroup() : securitymanager.getThreadGroup();
        namePrefix = (new StringBuilder()).append("JourneyMap-thread-").toString(); //$NON-NLS-1$
    }
    
	public Thread newThread(Runnable runnable)
    {
        Thread thread = new Thread(group, runnable, (new StringBuilder()).append(namePrefix).append(threadNumber.getAndIncrement()).toString(), 0L);
        if(thread.isDaemon())
            thread.setDaemon(false);
        if(thread.getPriority() != 5)
            thread.setPriority(5);
        return thread;
    }
	
}