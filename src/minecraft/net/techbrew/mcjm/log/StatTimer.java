package net.techbrew.mcjm.log;

import com.google.common.util.concurrent.AtomicDouble;
import net.techbrew.mcjm.JourneyMap;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mwoodman on 12/13/13.
 */
public class StatTimer {

    private static Map<String, StatTimer> timers = Collections.synchronizedMap( new HashMap<String, StatTimer>());

    private final Logger logger = JourneyMap.getLogger();

    private final AtomicLong counter = new AtomicLong();
    private final AtomicDouble totalTime = new AtomicDouble();
    private final String name;

    private Long started;

    public synchronized static StatTimer get(String name) {
        if(name==null) throw new IllegalArgumentException("StatTimer name required");
        StatTimer timer = timers.get(name);
        if(timer==null){
            timer = new StatTimer(name);
            timers.put(name, timer);
        }
        return timer;
    }

    public synchronized static void resetAll() {
        for(StatTimer timer : timers.values()){
            timer.reset();
        }
    }

    public synchronized static void reportAll() {
        List<StatTimer> list = new ArrayList<StatTimer>(timers.values());
        Collections.sort(list, new Comparator<StatTimer>(){
            @Override
            public int compare(StatTimer o1, StatTimer o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        for(StatTimer timer : list){
            timer.report();
        }
    }

    private StatTimer(String name) {
        this.name = name;
    }

    public StatTimer start() {
        synchronized (counter) {
            if(started !=null) {
                logger.warning(name + " is already running.");
                return this;
            }
            started = System.nanoTime();
            return this;
        }
    }

    public void pause() {
        synchronized (counter) {
            if(started ==null) {
                logger.warning(name + " was not running.");
                return;
            }
            final long paused = System.nanoTime();
            final double millis = (paused- started)/1000000D;
            try {
                totalTime.getAndAdd(millis);
                counter.getAndIncrement();
                started = null;
            } catch(Error e) {
                logger.warning("Timer error: " + e);
                reset();
            }
        }
    }

    public void cancel() {
        synchronized (counter) {
            started = null;
        }
    }

    public void reset() {
        synchronized (counter) {
            started = null;
            counter.set(0);
            totalTime.set(0);
        }
    }

    public void report() {
        report(Level.INFO);
    }

    public void report(Level logLevel) {
        if(logger.isLoggable(logLevel)) {
            DecimalFormat df = new DecimalFormat("###.##");
            synchronized(counter) {
                final long count = counter.get();
                final double total = totalTime.get();
                final double avg = total/count;
                logger.log(logLevel, name + " count: " + count + ", total: " + df.format(total) + "ms, avg: " + df.format(avg) + "ms");
            }
        }
    }
}
