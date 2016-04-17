/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.common.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class LogFormatter
{
    public static final String LINEBREAK = System.getProperty("line.separator");

    private static int OutOfMemoryWarnings = 0;
    private static int LinkageErrorWarnings = 0;

    public LogFormatter()
    {
        super();
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
        int maxRecursion = 5;
        if (thrown != null && OutOfMemoryWarnings < 5 && LinkageErrorWarnings < 5)
        {
            while (thrown != null && maxRecursion > 0)
            {
                if (thrown instanceof StackOverflowError)
                {
                    return;
                }
                else if (thrown instanceof OutOfMemoryError)
                {
                    OutOfMemoryWarnings++;
                    thrown.printStackTrace(System.err);
                    break;
                }
                else
                {
                    if (thrown instanceof LinkageError)
                    {
                        LinkageErrorWarnings++;
                        thrown.printStackTrace(System.err);
                        break;
                    }
                    else
                    {
                        if (thrown instanceof Exception)
                        {
                            thrown = ((Exception) thrown).getCause();
                            maxRecursion--;
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a String of the stacktrace only up to the same method which calls this one.
     *
     * @param t
     * @return
     */
    public static String toPartialString(Throwable t)
    {
        StringBuilder sb = new StringBuilder(t.toString());
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        for (StackTraceElement ste : t.getStackTrace())
        {
            sb.append("\n\tat " + ste);
            if (ste.getClassName().equals(caller.getClassName()) && ste.getMethodName().equals(caller.getMethodName()))
            {
                break;
            }
        }
        return sb.toString();
    }
}
