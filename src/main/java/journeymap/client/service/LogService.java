/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.service;

import journeymap.client.log.JMLogger;
import se.rupy.http.Event;

import java.io.File;


/**
 * Serve the journeyMap.log file from disk
 *
 * @author mwoodman
 */
public class LogService extends FileService
{

    private static final long serialVersionUID = 1L;

    private static final String CLASSPATH_ROOT = "/";
    private static final String CLASSPATH_WEBROOT = "web";
    private static final String IDE_TEST = "eclipse/Client/bin/";
    private static final String IDE_SOURCEPATH = "../../../src/minecraft/net/techbrew/journeymap/web";

    /**
     * Default constructor
     */
    public LogService()
    {
    }

    @Override
    public String path()
    {
        return "/log"; //$NON-NLS-1$
    }

    /**
     * Serve the journeyMap.log file
     */
    @Override
    public void filter(Event event) throws Event, Exception
    {

        final File logFile = JMLogger.getLogFile();

        if (logFile.exists())
        {
            ResponseHeader.on(event).contentType(ContentType.txt);
            ResponseHeader.on(event).inlineFilename("journeymap.log");
            serveFile(logFile, event);
        }
        else
        {
            throwEventException(404, "Not found: " + logFile.getPath(), event, true);
        }

    }
}