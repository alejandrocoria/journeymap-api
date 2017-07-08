/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.service;

import journeymap.client.JourneymapClient;
import journeymap.client.cartography.color.ColorManager;
import journeymap.client.cartography.color.ColorPalette;
import journeymap.client.io.FileHandler;
import journeymap.client.io.ThemeFileHandler;
import journeymap.client.log.JMLogger;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.theme.ThemePresets;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
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
 * @author techbrew
 */
public class FileService extends BaseService
{

    private static final long serialVersionUID = 2L;
    /**
     * The Resource path.
     */
    protected final String resourcePath;
    /**
     * The Color palette json.
     */
    final String COLOR_PALETTE_JSON = "/" + ColorPalette.JSON_FILENAME;
    /**
     * The Color palette html.
     */
    final String COLOR_PALETTE_HTML = "/" + ColorPalette.HTML_FILENAME;
    /**
     * The Entity icon prefix.
     */
    final String ENTITY_ICON_PREFIX = "/entity_icon";
    /**
     * The Icon theme path prefix.
     */
    final String ICON_THEME_PATH_PREFIX = "/theme/";
    /**
     * The Skin prefix.
     */
    final String SKIN_PREFIX = "/skin/";
    private boolean useZipEntry;
    private File zipFile;

    /**
     * Default constructor
     */
    public FileService()
    {

        URL resourceDir = JourneymapClient.class.getResource(FileHandler.ASSETS_JOURNEYMAP_UI); //$NON-NLS-1$

        String testPath = null;

        if (resourceDir == null)
        {
            Journeymap.getLogger().error("Can't determine path to webroot!");
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

        if (!useZipEntry && Journeymap.FORGE_VERSION.contains("@"))
        {
            try
            {
                testPath = new File("../src/main/resources" + FileHandler.ASSETS_JOURNEYMAP_UI).getCanonicalPath();
                Journeymap.getLogger().info("Dev environment detected, serving source files from " + testPath);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
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
                ColorPalette colorPalette = ColorManager.INSTANCE.getCurrentPalette();
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
                ColorPalette colorPalette = ColorManager.INSTANCE.getCurrentPalette();
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
            else if (path.startsWith(ENTITY_ICON_PREFIX))
            {
                String location = event.query().parameters().split("location=")[1];
                BufferedImage image = TextureCache.resolveImage(new ResourceLocation(location));
                if (image == null)
                {
                    JMLogger.logOnce("Path not found: " + path, null);
                    throwEventException(404, "Unknown: " + path, event, true);
                }
                else
                {
                    ResponseHeader.on(event).contentType(ContentType.png).noCache();
                    serveImage(event, image);
                }
            }
            // Handle themed icon request
            else if (path.startsWith(ICON_THEME_PATH_PREFIX))
            {
                String themeIconPath = path.split(ICON_THEME_PATH_PREFIX)[1].replace('/', File.separatorChar);
                File themeDir = new File(ThemeFileHandler.getThemeIconDir(), ThemePresets.THEME_VICTORIAN.directory);
                File iconFile = new File(themeDir, themeIconPath);
                if (!iconFile.exists())
                {
                    // Fallback to jar asset
                    String setName = themeIconPath.split("\\" + File.separator)[0];
                    String iconPath = themeIconPath.substring(themeIconPath.indexOf(File.separatorChar) + 1);

                    if (event != null)
                    {
                        ResponseHeader.on(event).contentType(ContentType.png);
                    }

                    String resourcePath = String.format("theme/%s/%s", setName, iconPath);
                    try
                    {
                        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
                        IResource resource = resourceManager.getResource(new ResourceLocation(Journeymap.MOD_ID, resourcePath));
                        fileStream = resource.getInputStream();
                    }
                    catch (FileNotFoundException e)
                    {
                        JMLogger.logOnce("Resource not found: " + resourcePath, null);
                        throwEventException(404, "Unknown: " + path, event, true);
                    }
                    catch (Exception e)
                    {
                        JMLogger.logOnce("Resource not usable: " + resourcePath, e);
                        throwEventException(415, "Not an image: " + path, event, true);
                    }
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
                JMLogger.logOnce("Path not found: " + path, null);
                throwEventException(404, "Unknown: " + path, event, true);
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
            Journeymap.getLogger().error(LogFormatter.toString(t));
            throwEventException(500, "Error: " + path, event, true);
        }
    }

    /**
     * Gets stream.
     *
     * @param path  the path
     * @param event the event
     * @return the stream
     */
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
            Journeymap.getLogger().error(LogFormatter.toString(t));
        }

        return in;
    }

    /**
     * Serve skin.
     *
     * @param username the username
     * @param event    the event
     * @throws Exception the exception
     */
    public void serveSkin(String username, Event event) throws Exception
    {

        ResponseHeader.on(event).contentType(ContentType.png);

        TextureImpl tex = TextureCache.getPlayerSkin(username);
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
     * @param sourceFile the source file
     * @param event      the event
     * @throws Event       the event
     * @throws IOException the io exception
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
     * @param input the input
     * @param event the event
     * @throws Event       the event
     * @throws IOException the io exception
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
            Journeymap.getLogger().error(LogFormatter.toString(e));
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
            Journeymap.getLogger().warn("Failed to gzip encode: " + data);
            return null;
        }
    }

}
