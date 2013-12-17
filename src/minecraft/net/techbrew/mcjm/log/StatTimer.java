package net.techbrew.mcjm.log;

import com.google.common.util.concurrent.AtomicDouble;
import net.techbrew.mcjm.JourneyMap;
import sun.rmi.runtime.Log;

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
    private static final int WARMUP_COUNT_DEFAULT=10;
    private static final int MAX_COUNT=1000000;
    private static final double NS = 1000000D;

    private final Logger logger = JourneyMap.getLogger();
    private final int warmupCount;
    private boolean warmup = true;
    private boolean maxed = false;
    private final AtomicLong counter = new AtomicLong();
    private final AtomicDouble totalTime = new AtomicDouble();
    private final String name;

    private Long started;

    public synchronized static StatTimer get(String name) {
        return get(name, WARMUP_COUNT_DEFAULT);
    }

    public synchronized static StatTimer get(String name, int warmupCount) {
        if(name==null) throw new IllegalArgumentException("StatTimer name required");
        StatTimer timer = timers.get(name);
        if(timer==null){
            timer = new StatTimer(name, warmupCount);
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
        StringBuffer sb = new StringBuffer();
        for(StatTimer timer : list){
            sb.append("\n\t").append(timer.getReportString());
        }
        JourneyMap.getLogger().info(sb.toString());
    }

    private StatTimer(String name, int warmupCount) {
        this.name = name;
        this.warmupCount = warmupCount;
    }

    public StatTimer start() {
        synchronized (counter) {
            if(maxed) return this;

            if(started !=null) {
                logger.warning(name + " is already running.");
                return this;
            }

            if(counter.get()==MAX_COUNT) {
                maxed=true;
                logger.info(name + " hit max count, " + MAX_COUNT);
                return this;
            }

            if(warmup && counter.get()>warmupCount){
                warmup = false;
                counter.set(0);
                totalTime.set(0);
                if(logger.isLoggable(Level.INFO)){
                    logger.info(name + " warmup done, " + warmupCount);
                }
            }

            started = System.nanoTime();
            return this;
        }
    }

    public void pause() {
        synchronized (counter) {
            if(maxed) return;

            if(started == null) {
                logger.warning(name + " is not running.");
                return;
            }

            try {
                final double elapsedMs = (System.nanoTime() - started)/NS;
                totalTime.getAndAdd(elapsedMs);
                counter.getAndIncrement();
                started = null;
            } catch(Throwable t) {
                logger.warning("Timer error: " + LogFormatter.toString(t));
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
            warmup = true;
            maxed = false;
            started = null;
            counter.set(0);
            totalTime.set(0);
        }
    }

    public void report() {
        logger.info(getReportString());
    }

    public String getReportString() {
        final DecimalFormat df = new DecimalFormat("###.##");
        synchronized(counter) {
            final long count = counter.get();
            final double total = totalTime.get();
            final double avg = total/count;

            final StringBuffer sb = new StringBuffer(pad(name,50)).append(": ");
            sb.append("Count: ").append(pad(count,8));
            sb.append("Time: ").append(pad(df.format(total)+"ms", 15));
            sb.append("Avg: ").append(pad(df.format(avg)+"ms",10));
            if(warmup) sb.append("(WARMUP NOT MET: ").append(warmupCount).append(")");
            if(maxed) sb.append("(MAXED)");
            return sb.toString();
        }
    }

    private static String pad(Object s, int n) {
        return String.format("%1$-" + n + "s", s);
    }
}
