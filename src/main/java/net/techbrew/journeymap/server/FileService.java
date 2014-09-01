/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.server;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.cartography.ColorCache;
import net.techbrew.journeymap.cartography.ColorPalette;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.io.IconSetFileHandler;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import se.rupy.http.Event;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


/**
 * Serve files from disk.  Works for zip-archived files
 * when the mod is in normal use, also works for standard
 * file-system access when the mod is unzipped or when
 * running from Eclipse during development.
 *
 * @author mwoodman
 */
public class FileService extends BaseService
{

    private static final long serialVersionUID = 2L;
    protected final String resourcePath;
    final String COLOR_PALETTE_JSON = "/" + ColorPalette.JSON_FILENAME;
    final String COLOR_PALETTE_HTML = "/" + ColorPalette.HTML_FILENAME;
    final String ICON_ENTITY_PATH_PREFIX = "/icon/entity/";
    final String ICON_SKIN_PATH_PREFIX = "/icon/skin/";
    final String SKIN_PREFIX = "/skin/";
    private boolean useZipEntry;
    private File zipFile;

    /**
     * Default constructor
     */
    public FileService()
    {

        URL resourceDir = JourneyMap.class.getResource(FileHandler.ASSETS_JOURNEYMAP_WEB); //$NON-NLS-1$

        String testPath = null;

        if (resourceDir == null)
        {
            JourneyMap.getLogger().error("Can't determine path to webroot!");
        }
        else
        {

            // Format reusable resourcePath
            testPath = resourceDir.getPath();
            if (testPath.endsWith("/"))
            { //$NON-NLS-1$
                testPath = testPath.substring(0, testPath.length() - 1);
            }

            // Check whether operating out of a zip/jar
            useZipEntry = (resourceDir.getProtocol().equals("file") || resourceDir.getProtocol().equals("jar")) && testPath.contains("!/"); //$NON-NLS-1$	//$NON-NLS-2$
        }

        resourcePath = testPath;
    }

    @Override
    public String path()
    {
        return null; // Default handler
    }

    @Override
    public void filter(Event event) throws Event, Exception
    {

        String path = null;
        InputStream in = null;

        try
        {

            // Determine request path
            path = event.query().path(); //$NON-NLS-1$

            // Handle skin request
            if (path.startsWith(SKIN_PREFIX))
            {
                serveSkin(path.split(SKIN_PREFIX)[1], event);
                return;
            }

            InputStream fileStream = null;

            // Handle colorpalette reques
            if (path.startsWith(COLOR_PALETTE_JSON))
            {
                ColorPalette colorPalette = ColorCache.instance().getCurrentPalette();
                if (colorPalette != null)
                {
                    File jsonFile = colorPalette.getOrigin();
                    if (jsonFile.canRead())
                    {
                        ResponseHeader.on(event).contentType(ContentType.js);
                        fileStream = new FileInputStream(jsonFile);
                    }
                }
            }
            else if (path.startsWith(COLOR_PALETTE_HTML))
            {
                ColorPalette colorPalette = ColorCache.instance().getCurrentPalette();
                if (colorPalette != null)
                {
                    File htmlFile = colorPalette.getOriginHtml(true, false);
                    if (htmlFile.canRead())
                    {
                        ResponseHeader.on(event).contentType(ContentType.html);
                        fileStream = new FileInputStream(htmlFile);
                    }
                }
            }
            // Handle entity icon request
            else if (path.startsWith(ICON_ENTITY_PATH_PREFIX))
            {
                String entityIconPath = path.split(ICON_ENTITY_PATH_PREFIX)[1].replace('/', File.separatorChar);
                File iconFile = new File(IconSetFileHandler.getEntityIconDir(), entityIconPath);
                if (!iconFile.exists())
                {
                    // Fallback to jar asset
                    String setName = entityIconPath.split(File.separator)[0];
                    String iconPath = entityIconPath.substring(entityIconPath.indexOf(File.separatorChar) + 1);

                    if (event != null)
                    {
                        ResponseHeader.on(event).contentType(ContentType.png);
                    }
                    fileStream = IconSetFileHandler.getIconStream(IconSetFileHandler.ASSETS_JOURNEYMAP_ICON_ENTITY, setName, iconPath);
                    JourneyMap.getLogger().warn("Couldn't get file for " + path);
                }
                else
                {
                    if (event != null)
                    {
                        ResponseHeader.on(event).content(iconFile);
                    }
                    fileStream = new FileInputStream(iconFile);
                }
            }
            // Handle skin icon request
            else if (path.startsWith(ICON_SKIN_PATH_PREFIX))
            {
                String skinIconPath = path.split(ICON_SKIN_PATH_PREFIX)[1].replace('/', File.separatorChar);
                File iconFile = new File(IconSetFileHandler.getThemeIconDir(), skinIconPath);
                if (!iconFile.exists())
                {
                    // Fallback to jar asset
                    String setName = skinIconPath.split(File.separator)[0];
                    String iconPath = skinIconPath.substring(skinIconPath.indexOf(File.separatorChar) + 1);

                    if (event != null)
                    {
                        ResponseHeader.on(event).contentType(ContentType.png);
                    }
                    fileStream = IconSetFileHandler.getIconStream(IconSetFileHandler.ASSETS_JOURNEYMAP_ICON_THEME, setName, iconPath);
                    JourneyMap.getLogger().warn("Couldn't get theme file for " + path);
                }
                else
                {
                    if (event != null)
                    {
                        ResponseHeader.on(event).content(iconFile);
                    }
                    fileStream = new FileInputStream(iconFile);
                }
            }
            else
            {
                // Default file request
                fileStream = getStream(path, event);
            }

            if (fileStream == null)
            {
                JourneyMap.getLogger().debug("Path not found: " + path);
                throwEventException(404, Constants.getMessageJMERR13(path), event, true);
            }
            else
            {
                serveStream(fileStream, event);
            }


        }
        catch (Event eventEx)
        {
            throw eventEx;
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error(LogFormatter.toString(t));
            throwEventException(500, Constants.getMessageJMERR12(path), event, true);
        }
    }

    protected InputStream getStream(String path, Event event)
    {
        InputStream in = null;

        try
        {
            // Determine request path
            String requestPath = null;

            if ("/".equals(path))
            {
                // Default to index
                requestPath = resourcePath + "/index.html"; //$NON-NLS-1$
            }
            else
            {
                requestPath = resourcePath + path;
            }

            if (useZipEntry)
            {
                // Running out of a Zip archive or jar
                String[] tokens = requestPath.split("file:")[1].split("!/"); //$NON-NLS-1$ //$NON-NLS-2$

                // Lazy load the file
                if (zipFile == null)
                {
                    zipFile = new File(URI.create(tokens[0]).getPath());
                    if (!zipFile.canRead())
                    {
                        throw new RuntimeException("Can't read Zip file: " + zipFile + " (originally: " + tokens[0] + ")");
                    }
                }
                String innerName = tokens[1];

                FileInputStream fis = new FileInputStream(zipFile);
                ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
                ZipEntry zipEntry;

                boolean found = false;
                while ((zipEntry = zis.getNextEntry()) != null)
                {
                    if (innerName.equals(zipEntry.getName()))
                    {
                        // Set inputstream
                        in = new ZipFile(zipFile).getInputStream(zipEntry);
                        if (event != null)
                        {
                            ResponseHeader.on(event).content(zipEntry);
                        }
                        found = true;
                        break;
                    }
                }

                if (!found)
                {
                    zis.close();
                    fis.close();
                    // Didn't find it
                    in = null;
                }
            }
            else
            {
                // Running out of a directory
                File file = new File(requestPath);
                if (file.exists())
                {
                    if (event != null)
                    {
                        ResponseHeader.on(event).content(file);
                    }
                    in = new FileInputStream(file);
                }
                else
                {
                    in = null;
                }
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error(LogFormatter.toString(t));
        }

        return in;
    }

    public void serveSkin(String username, Event event) throws Exception
    {

        ResponseHeader.on(event).contentType(ContentType.png);

        TextureImpl tex = TextureCache.instance().getPlayerSkin(username);
        BufferedImage img = tex.getImage();
        if (img != null)
        {
            serveImage(event, img);
        }
        else
        {
            event.reply().code("404 Not Found");
        }

    }

    /**
     * Respond with the contents of a file.
     *
     * @param sourceFile
     * @param event
     * @throws Event
     * @throws IOException
     */
    public void serveFile(File sourceFile, Event event) throws Event, IOException
    {

        // Set content headers
        ResponseHeader.on(event).content(sourceFile);

        // Stream file
        serveStream(new FileInputStream(sourceFile), event);
    }

    /**
     * Respond with the contents of a file input stream.
     *
     * @param input
     * @param event
     * @throws Event
     * @throws IOException
     */
    public void serveStream(final InputStream input, Event event) throws Event, IOException
    {

        // Transfer inputstream to event outputstream
        ReadableByteChannel inputChannel = null;
        WritableByteChannel outputChannel = null;
        try
        {
            inputChannel = Channels.newChannel(input);

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            GZIPOutputStream output = new GZIPOutputStream(bout);

            outputChannel = Channels.newChannel(output);
            ByteBuffer buffer = ByteBuffer.allocate(65536);
            while (inputChannel.read(buffer) != -1)
            {
                buffer.flip();
                outputChannel.write(buffer);
                buffer.clear();
            }

            output.flush();
            output.close();
            bout.close();

            byte[] gzbytes = bout.toByteArray();

            ResponseHeader.on(event).contentLength(gzbytes.length).setHeader("Content-encoding", "gzip");    //$NON-NLS-1$ //$NON-NLS-2$
            event.output().write(gzbytes);

        }
        catch (IOException e)
        {
            JourneyMap.getLogger().error(LogFormatter.toString(e));
            throw event;
        }
        finally
        {
            if (input != null)
            {
                input.close();
            }
        }

    }

    /**
     * Gzip encode a string and return the byte array.
     *
     * @param data
     * @return
     */
    @Override
    protected byte[] gzip(String data)
    {
        ByteArrayOutputStream bout = null;
        try
        {
            bout = new ByteArrayOutputStream();
            GZIPOutputStream output = new GZIPOutputStream(bout);
            output.write(data.getBytes());
            output.flush();
            output.close();
            bout.close();
            return bout.toByteArray();
        }
        catch (Exception ex)
        {
            JourneyMap.getLogger().warn("Failed to gzip encode: " + data);
            return null;
        }
    }

}
