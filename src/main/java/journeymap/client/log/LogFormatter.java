/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.log;

import journeymap.client.Constants;
import journeymap.client.JourneyMap;

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
                    ChatLog.announceI18N("jm.common.memory_warning", thrown.toString());
                    thrown.printStackTrace(System.err);
                    break;
                }
                else
                {
                    if (thrown instanceof LinkageError)
                    {
                        LinkageErrorWarnings++;
                        String error = Constants.getString("jm.error.compatability", JourneyMap.MOD_NAME, JourneyMap.FORGE_VERSION);
                        thrown.printStackTrace(System.err);
                        ChatLog.announceError(error);
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
}
