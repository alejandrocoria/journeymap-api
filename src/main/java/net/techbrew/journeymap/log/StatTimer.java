package net.techbrew.journeymap.log;

import com.google.common.util.concurrent.AtomicDouble;
import net.techbrew.journeymap.JourneyMap;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for timing whatever needs to be timed.
 */
public class StatTimer {

    private static Map<String, StatTimer> timers = Collections.synchronizedMap( new HashMap<String, StatTimer>());
    private static final int WARMUP_COUNT_DEFAULT=10;
    private static final int MAX_COUNT=1000000;
    private static final double NS = 1000000D;

    private final Logger logger = JourneyMap.getLogger();
    private final int warmupCount;
    private final AtomicLong counter = new AtomicLong();
    private final AtomicLong cancelCounter = new AtomicLong();
    private final AtomicDouble totalTime = new AtomicDouble();
    private final String name;
    private final boolean disposable;

    private boolean warmup = true;
    private boolean maxed = false;
    private Long started;
    private double max=0;
    private double min=Double.MAX_VALUE;

    /**
     * Get a timer by name.  If it hasn't been created, it will have WARMUP_COUNT_DEFAULT.
     * @param name
     * @return
     */
    public synchronized static StatTimer get(String name) {
        return get(name, WARMUP_COUNT_DEFAULT);
    }

    /**
     * Get a timer by name.  If it hasn't been created, it will have the warmupCount value provided.
     * @param name
     * @param warmupCount
     * @return
     */
    public synchronized static StatTimer get(String name, int warmupCount) {
        if(name==null) throw new IllegalArgumentException("StatTimer name required");
        StatTimer timer = timers.get(name);
        if(timer==null){
            timer = new StatTimer(name, warmupCount, false);
            timers.put(name, timer);
        }
        return timer;
    }

    /**
     * Create a disposable timer with a warmupCount of 0.
     * @param name
     * @return
     */
    public static StatTimer getDisposable(String name)
    {
        return new StatTimer(name, 0, true);
    }

    /**
     * Reset all timers.
     */
    public synchronized static void resetAll() {
        for(StatTimer timer : timers.values()){
            timer.reset();
        }
    }

    /**
     * Report all timers via log file.
     */
    public synchronized static String getReport() {
        List<StatTimer> list = new ArrayList<StatTimer>(timers.values());
        Collections.sort(list, new Comparator<StatTimer>(){
            @Override
            public int compare(StatTimer o1, StatTimer o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        StringBuffer sb = new StringBuffer();
        for(StatTimer timer : list){
            if(timer.counter.get()>0){
                sb.append(LogFormatter.LINEBREAK).append(timer.getReportString());
            }
        }
        return sb.toString();
    }

    /**
     * Private constructor.
     * @param name
     * @param warmupCount
     */
    private StatTimer(String name, int warmupCount, boolean disposable) {
        this.name = name;
        this.warmupCount = warmupCount;
        this.disposable = disposable;
        if(warmupCount<=0)
        {
            warmup = false;
        }
    }

    /**
     * Start the timer.
     * @return
     */
    public StatTimer start() {
        synchronized (counter) {
            if(maxed) return this;

            if(started !=null) {
                logger.warning(name + " is already running, cancelling first");
                this.cancel();
            }

            if(counter.get()==MAX_COUNT) {
                maxed=true;
                logger.info(name + " hit max count, " + MAX_COUNT);
                return this;
            }

            if(warmup && counter.get()>warmupCount){
                warmup = false;
                max = 0;
                min = 0;
                counter.set(0);
                cancelCounter.set(0);
                totalTime.set(0);
                if(logger.isLoggable(Level.FINE)){
                    logger.fine(name + " warmup done, " + warmupCount);
                }
            }

            started = System.nanoTime();
            return this;
        }
    }

    /**
     * Stop the timer, returns elapsed time in milliseconds.
     */
    public double stop() {
        synchronized (counter) {
            if(maxed) return 0;

            if(started == null) {
                logger.warning(name + " is not running.");
                return 0;
            }

            try {
                final double elapsedMs = (System.nanoTime() - started)/NS;
                totalTime.getAndAdd(elapsedMs);
                counter.getAndIncrement();
                if(elapsedMs<min) min=elapsedMs;
                if(elapsedMs>max) max=elapsedMs;
                started = null;
                return elapsedMs;
            } catch(Throwable t) {
                logger.severe("Timer error: " + LogFormatter.toString(t));
                reset();
                return 0;
            }
        }
    }

    /**
     * Stop the timer, return simple report of results.
     * @return
     */
    public String stopAndReport()
    {
        stop();
        return getSimpleReportString();
    }

    /**
     * Cancel a started timer.
     */
    public void cancel() {
        synchronized (counter) {
            started = null;
            cancelCounter.incrementAndGet();
        }
    }

    /**
     * Reset the timer.
     */
    public void reset() {
        synchronized (counter) {
            warmup = true;
            maxed = false;
            started = null;
            counter.set(0);
            cancelCounter.set(0);
            totalTime.set(0);
        }
    }

    /**
     * Log the timer's stats.
     */
    public void report() {
        logger.info(getReportString());
    }

    /**
     * Get the timer's stats as a string.
     * @return
     */
    public String getReportString() {
        final DecimalFormat df = new DecimalFormat("###.##");
        synchronized(counter) {
            final long count = counter.get();
            final double total = totalTime.get();
            final double avg = total/count;
            final long cancels = cancelCounter.get();

            String report = String.format("<b>%50s:</b>   Avg: %8sms, Min: %8sms, Max: %10sms, Total: %10s sec, Count: %8s, Canceled: %8s,",
                    name, df.format(avg), df.format(min), df.format(max), TimeUnit.MILLISECONDS.toSeconds((long) total), count, cancels);

            if(warmup) report+= String.format("* Warmup of %s not met", warmupCount);
            if(maxed) report+= "(MAXED)";

            return report;
        }
    }

    /**
     * Gets a simplified report of the timer stats.
     * @return
     */
    public String getSimpleReportString()
    {
        try
        {
            final DecimalFormat df = new DecimalFormat("###.##");
            synchronized (counter)
            {
                final long count = counter.get();
                final double total = totalTime.get();
                final double avg = total / count;

                final StringBuilder sb = new StringBuilder(name).append(" ");
                if (count > 1)
                {
                    sb.append("count: ").append(count);
                }
                sb.append("time: ").append(df.format(total) + "ms");
                if (count > 1)
                {
                    sb.append("min: ").append(df.format(min) + "ms");
                    sb.append("max: ").append(df.format(max) + "ms");
                    sb.append("avg: ").append(df.format(avg) + "ms");
                }
                if (maxed) sb.append("(MAXED)");
                return sb.toString();
            }
        }
        catch(Throwable t)
        {
            return String.format("StatTimer '%s' encountered an error getting its simple report: %s", name, t);
        }
    }

    /**
     * Pad string s with up to n spaces.
     * @param s
     * @param n
     * @return
     */
    private static String pad(Object s, int n) {
        return String.format("%1$-" + n + "s", s);
    }
}
