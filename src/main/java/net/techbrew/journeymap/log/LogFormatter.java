package net.techbrew.journeymap.log;

import modinfo.ModInfo;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter
{
    private static final MessageFormat messageFormat = new MessageFormat("{0,time,HH:mm:ss} {1} [{2}] [{3}.{4}] {5}\n"); //$NON-NLS-1$
    private static final String MINECRAFT_THREADNAME = "Minecraft main thread";

    private static int OutOfMemoryWarnings = 0;
    private static int LinkageErrorWarnings = 0;

    public LogFormatter()
    {
        super();
    }

    @Override
    public String format(LogRecord record)
    {
        final String className = record.getSourceClassName();
        final String shortClassName = className == null ? "?" : className.substring(className.lastIndexOf('.') + 1);
        final Thread thread = Thread.currentThread();
        String threadName = thread.getName();
        if (MINECRAFT_THREADNAME.equals(threadName))
        {
            threadName = "MC";
        }

        Object[] arguments = new Object[6];
        int i = 0;
        arguments[i++] = new Date(record.getMillis());
        arguments[i++] = record.getLevel();
        arguments[i++] = threadName;
        arguments[i++] = shortClassName;
        arguments[i++] = record.getSourceMethodName();
        arguments[i++] = record.getMessage();

        if (record.getLevel() == Level.SEVERE)
        {
            ModInfo modInfo = JourneyMap.getInstance().getModInfo();
            if (modInfo != null)
            {
                String action = shortClassName + "." + record.getSourceMethodName();
                modInfo.reportEvent("Log: " + record.getLevel(), action, record.getMessage());
            }
        }

        checkErrors(record.getThrown());

        return messageFormat.format(arguments);
    }

    public static String toString(Throwable thrown)
    {
        checkErrors(thrown);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        thrown.printStackTrace(ps);
        ps.flush();
        return baos.toString();
    }

    private static void checkErrors(Throwable thrown)
    {
        if (thrown != null && OutOfMemoryWarnings < 5)
        {
            while (thrown != null)
            {
                if (thrown instanceof OutOfMemoryError)
                {
                    OutOfMemoryWarnings++;
                    ChatLog.announceI18N("JourneyMap.memory_warning", thrown.toString());
                    break;
                }
                else
                {
                    if (thrown instanceof LinkageError)
                    {
                        LinkageErrorWarnings++;
                        String error = Constants.getString("JourneyMap.fatal_error", JourneyMap.MOD_NAME, JourneyMap.MC_VERSION);
                        ChatLog.announceError(error);
                        break;
                    }
                    else
                    {
                        if (thrown instanceof Exception)
                        {
                            thrown = ((Exception) thrown).getCause();
                        }
                    }
                }
            }
        }
    }
}
