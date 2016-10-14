/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.render.texture;

import com.google.common.base.Strings;
import journeymap.client.io.FileHandler;
import journeymap.client.io.IconSetFileHandler;
import journeymap.client.io.RegionImageHandler;
import journeymap.client.io.ThemeFileHandler;
import journeymap.client.model.MapType;
import journeymap.client.task.main.ExpireTextureTask;
import journeymap.client.ui.minimap.EntityDisplay;
import journeymap.client.ui.theme.Theme;
import journeymap.common.Journeymap;
import journeymap.common.thread.JMThreadFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Texture management with functionality for reloading them as needed.
 * @author mwoodman
 */
public enum TextureCache
{
    INSTANCE;

    // BufferedImages should be retained
    public static final ResourceLocation GridCheckers = uiImage("grid-checkers.png");
    public static final ResourceLocation GridDots = uiImage("grid-dots.png");
    public static final ResourceLocation GridSquares = uiImage("grid.png");
    public static final ResourceLocation ColorPicker = uiImage("colorpick.png");
    public static final ResourceLocation ColorPicker2 = uiImage("colorpick2.png");
    public static final ResourceLocation TileSampleDay = uiImage("tile-sample-day.png");
    public static final ResourceLocation TileSampleNight = uiImage("tile-sample-night.png");
    public static final ResourceLocation TileSampleUnderground = uiImage("tile-sample-underground.png");
    public static final ResourceLocation UnknownEntity = uiImage("unknown.png");

    // BufferedImages don't need be retained
    public static final ResourceLocation Brick = uiImage("brick.png");
    public static final ResourceLocation Deathpoint = uiImage("waypoint-death.png");
    public static final ResourceLocation MobDot = uiImage("marker-dot-16.png");
    public static final ResourceLocation MobDot_Large = uiImage("marker-dot-32.png");
    public static final ResourceLocation MobDotArrow = uiImage("marker-dot-arrow-16.png");
    public static final ResourceLocation MobDotArrow_Large = uiImage("marker-dot-arrow-32.png");
    public static final ResourceLocation MobDotChevron = uiImage("marker-chevron-16.png");
    public static final ResourceLocation MobDotChevron_Large = uiImage("marker-chevron-32.png");
    public static final ResourceLocation MobIconArrow = uiImage("marker-icon-arrow-16.png");
    public static final ResourceLocation MobIconArrow_Large = uiImage("marker-icon-arrow-32.png");
    public static final ResourceLocation PlayerArrow = uiImage("marker-player-16.png");
    public static final ResourceLocation PlayerArrowBG = uiImage("marker-player-bg-16.png");
    public static final ResourceLocation PlayerArrow_Large = uiImage("marker-player-32.png");
    public static final ResourceLocation PlayerArrowBG_Large = uiImage("marker-player-bg-32.png");
    public static final ResourceLocation Logo = uiImage("ico/journeymap60.png");
    public static final ResourceLocation MinimapSquare128 = uiImage("minimap/minimap-square-128.png");
    public static final ResourceLocation MinimapSquare256 = uiImage("minimap/minimap-square-256.png");
    public static final ResourceLocation MinimapSquare512 = uiImage("minimap/minimap-square-512.png");
    public static final ResourceLocation Patreon = uiImage("patreon.png");
    public static final ResourceLocation Waypoint = uiImage("waypoint.png");
    public static final ResourceLocation WaypointEdit = uiImage("waypoint-edit.png");
    public static final ResourceLocation WaypointOffscreen = uiImage("waypoint-offscreen.png");

    private static ResourceLocation uiImage(String fileName)
    {
        return new ResourceLocation(Journeymap.MOD_ID, "ui/img/" + fileName);
    }

    // Keeps the textures referenced here from being garbage collected, since ResourceLocationTexture's cache uses weak values.
    private final ArrayList<TextureImpl> preloaded = new ArrayList<>(32);
    private final Map<String, TextureImpl> playerSkins = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, TextureImpl> themeImages = Collections.synchronizedMap(new HashMap<>());

    private ThreadPoolExecutor texExec = new ThreadPoolExecutor(2, 4, 15L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(8), new JMThreadFactory("texture"),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    public Future<TextureImpl> scheduleTextureTask(Callable<TextureImpl> textureTask)
    {
        return texExec.submit(textureTask);
    }

    private void preloadRetained()
    {
        preloaded.addAll(Arrays.asList(ColorPicker, ColorPicker2, GridCheckers, GridDots, GridSquares, TileSampleDay,
                TileSampleNight, TileSampleUnderground, UnknownEntity).stream().map(ResourceLocationTexture::get)
                .collect(Collectors.toList()));
    }

    private void preloadNonRetained()
    {
        preloaded.addAll(Arrays.asList(Brick, Deathpoint, Logo, MinimapSquare128, MinimapSquare256, MinimapSquare512,
                MobDot, MobDot_Large, MobDotArrow, MobDotArrow_Large, MobDotChevron, MobDotChevron_Large,
                MobIconArrow_Large, PlayerArrow, PlayerArrowBG, PlayerArrow_Large, PlayerArrowBG, Patreon, Waypoint,
                WaypointEdit, WaypointOffscreen).stream().map(ResourceLocationTexture::get).collect(Collectors.toList()));
    }

    public void reset()
    {
        preloaded.clear();
        ResourceLocationTexture.purge();
        preloadRetained();
        preloadNonRetained();
    }

    public void purgeThemeImages()
    {
        synchronized (themeImages)
        {
            ExpireTextureTask.queue(themeImages.values());
            themeImages.clear();
        }
    }

    /**
     * *********************************************
     */

    public TextureImpl getMinimapCustomSquare(int size, float alpha)
    {
        size = Math.max(64, Math.min(size, 1024));
        alpha = Math.max(0, Math.min(alpha, 1));

        final BufferedImage frameImg;
        if (size <= 128)
        {
            frameImg = resolveImage(MinimapSquare128);
        }
        else if (size <= 256)
        {
            frameImg = resolveImage(MinimapSquare256);
        }
        else if (size <= 512)
        {
            frameImg = resolveImage(MinimapSquare512);
        }
        else
        {
            frameImg = resolveImage(MinimapSquare128);
        }

        BufferedImage resizedImg = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) resizedImg.getGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.drawImage(frameImg, 0, 0, size, size, null);
        g.dispose();

        // Just use 128 as the key
        ResourceLocationTexture.CACHE.invalidate(MinimapSquare128);
        ResourceLocationTexture tex = new ResourceLocationTexture(MinimapSquare128);
        tex.setImage(resizedImg, false);
        return tex;
    }

    public static TextureImpl getLowerEntityTexture(EntityDisplay entityDisplay, boolean showHeading)
    {
        ResourceLocation texLocation = null;
        switch (entityDisplay)
        {
            case LargeDots:
            {
                texLocation = showHeading ? MobDotArrow_Large : MobDot_Large;
                break;
            }
            case SmallDots:
            {
                texLocation = showHeading ? MobDotArrow : MobDot;
                break;
            }
            case LargeIcons:
            {
                texLocation = showHeading ? MobIconArrow_Large : null;
                break;
            }
            case SmallIcons:
            {
                texLocation = showHeading ? MobIconArrow : null;
                break;
            }
        }
        return ResourceLocationTexture.get(texLocation);
    }

    public static TextureImpl getUpperEntityTexture(EntityDisplay entityDisplay)
    {
        return getUpperEntityTexture(entityDisplay, (String) null);
    }

    public static TextureImpl getUpperEntityTexture(EntityDisplay entityDisplay, String playerName)
    {
        switch (entityDisplay)
        {
            case LargeDots:
            {
                return ResourceLocationTexture.get(MobDotChevron_Large);
            }
            case SmallDots:
            {
                return ResourceLocationTexture.get(MobDotChevron);
            }
        }

        if (!Strings.isNullOrEmpty(playerName))
        {
            return INSTANCE.getPlayerSkin(playerName);
        }

        return null;
    }

    public static TextureImpl getUpperEntityTexture(EntityDisplay entityDisplay, ResourceLocation iconLocation)
    {
        switch (entityDisplay)
        {
            case LargeDots:
            {
                return ResourceLocationTexture.get(MobDotChevron_Large);
            }
            case SmallDots:
            {
                return ResourceLocationTexture.get(MobDotChevron);
            }
        }

        if (iconLocation == null)
        {
            return null;
        }
        return ResourceLocationTexture.getRetained(iconLocation);
    }


    public TextureImpl getTileSample(MapType mapType)
    {
        if (mapType.isNight())
        {
            return ResourceLocationTexture.getRetained(TileSampleNight);
        }
        else if (mapType.isUnderground())
        {
            return ResourceLocationTexture.getRetained(TileSampleUnderground);
        }
        else
        {
            return ResourceLocationTexture.getRetained(TileSampleDay);
        }
    }

    /**
     * Gets a buffered image from the resource location
     *
     * @param location location
     * @return image
     */
    public static BufferedImage resolveImage(ResourceLocation location)
    {
        if (location.getResourceDomain().equals("fake"))
        {
            return null;
        }

        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
        try
        {
            IResource resource = resourceManager.getResource(location);
            InputStream is = resource.getInputStream();
            return TextureUtil.readBufferedImage(is);
        }
        catch (Exception e)
        {
            //Journeymap.getLogger().error("Resource not usable as image: " + location, LogFormatter.toPartialString(e));
            Journeymap.getLogger().error("Resource not usable as image: " + location);
            return null;
        }
    }

    public TextureImpl getThemeTexture(Theme theme, String iconPath)
    {
        return getSizedThemeTexture(theme, iconPath, 0, 0, false, 1f, false);
    }

    public TextureImpl getSizedThemeTexture(Theme theme, String iconPath, int width, int height, boolean resize, float alpha, boolean retainImage)
    {
        String texName = String.format("%s/%s", theme.directory, iconPath);
        synchronized (themeImages)
        {
            TextureImpl tex = themeImages.get(texName);
            if (tex == null || (tex.retainImage != retainImage) || (!tex.hasImage() && tex.retainImage) || (resize && (width != tex.width || height != tex.height)) || tex.alpha != alpha)
            {
                File parentDir = ThemeFileHandler.getThemeIconDir();
                BufferedImage img = FileHandler.getIconFromFile(parentDir, theme.directory, iconPath);
                if (img == null)
                {
                    String resourcePath = String.format("theme/%s/%s", theme.directory, iconPath);
                    img = resolveImage(new ResourceLocation(Journeymap.MOD_ID, resourcePath));
                }

                if (img != null)
                {
                    if (resize || alpha < 1f)
                    {
                        if (alpha < 1f || img.getWidth() != width || img.getHeight() != height)
                        {
                            BufferedImage tmp = new BufferedImage(width, height, img.getType());
                            Graphics2D g = tmp.createGraphics();
                            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g.drawImage(img, 0, 0, width, height, null);
                            g.dispose();
                            img = tmp;
                        }
                    }

                    if (tex != null)
                    {
                        tex.queueForDeletion();
                    }
                    tex = new TextureImpl(img, retainImage);
                    tex.alpha = alpha;
                    themeImages.put(texName, tex);
                }
                else
                {
                    Journeymap.getLogger().error("Unknown theme image: " + texName);
                    IconSetFileHandler.ensureEntityIconSet("Default");
                    return ResourceLocationTexture.getRetained(UnknownEntity);
                }
            }
            return tex;
        }
    }

    public TextureImpl getScaledCopy(String texName, TextureImpl original, int width, int height, float alpha)
    {
        synchronized (themeImages)
        {
            TextureImpl tex = themeImages.get(texName);
            if (tex == null || (!tex.hasImage() && tex.retainImage) || (width != tex.width || height != tex.height) || tex.alpha != alpha)
            {
                BufferedImage img = original.getImage();
                if (img != null)
                {
                    if (alpha < 1f || img.getWidth() != width || img.getHeight() != height)
                    {
                        BufferedImage tmp = new BufferedImage(width, height, img.getType());
                        Graphics2D g = tmp.createGraphics();
                        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.drawImage(img, 0, 0, width, height, null);
                        g.dispose();
                        img = tmp;
                    }

                    if (tex != null)
                    {
                        tex.queueForDeletion();
                    }
                    tex = new TextureImpl(img);
                    tex.alpha = alpha;
                    themeImages.put(texName, tex);
                }
                else
                {
                    Journeymap.getLogger().error("Unable to get scaled image: " + texName);
                    return ResourceLocationTexture.getRetained(UnknownEntity);
                }
            }
            return tex;
        }
    }

    /**
     * Get the head portion of a player's skin, scaled to 24x24 pixels.
     * TODO use skinmanager
     */
    public TextureImpl getPlayerSkin(final String username)
    {
        TextureImpl tex = null;
        synchronized (playerSkins)
        {
            tex = playerSkins.get(username);
            if (tex != null)
            {
                return tex;
            }
            else
            {
                // Create blank to return immediately
                BufferedImage blank = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
                tex = new TextureImpl(null, blank, true, false);
                playerSkins.put(username, tex);
            }
        }

        final TextureImpl playerSkinTex = tex;

        // Load it async
        texExec.submit(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                BufferedImage img = downloadSkin(username);
                if (img != null)
                {
                    final BufferedImage scaledImage = new BufferedImage(24, 24, img.getType());
                    final Graphics2D g = RegionImageHandler.initRenderingHints(scaledImage.createGraphics());
                    g.drawImage(img, 0, 0, 24, 24, null);
                    g.dispose();
                    playerSkinTex.setImage(scaledImage, true);
                }
                else
                {
                    Journeymap.getLogger().warn("Couldn't get a skin at all for " + username);
                }
                return null;
            }
        });

        return playerSkinTex;
    }

    /**
     * Blocks.  Use this in a thread.
     */
    protected BufferedImage downloadSkin(String username)
    {
        BufferedImage img = null;
        HttpURLConnection conn = null;
        try
        {
            String skinPath = String.format("http://skins.minecraft.net/MinecraftSkins/%s.png", StringUtils.stripControlCodes(username));
            img = downloadImage(new URL(skinPath));
            if (img == null)
            {
                img = downloadImage(new URL("http://skins.minecraft.net/MinecraftSkins/Herobrine.png"));
            }
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().warn("Error getting skin image for " + username + ": " + e.getMessage());
        }
        return img;
    }

    private BufferedImage downloadImage(URL imageURL)
    {
        BufferedImage img = null;
        HttpURLConnection conn = null;
        try
        {
            conn = (HttpURLConnection) imageURL.openConnection(Minecraft.getMinecraft().getProxy());
            HttpURLConnection.setFollowRedirects(true);
            conn.setInstanceFollowRedirects(true);
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.connect();
            if (conn.getResponseCode() / 100 == 2) // can't get input stream before response code available
            {
                img = ImageIO.read(conn.getInputStream()).getSubimage(8, 8, 8, 8);
            }
            else
            {
                Journeymap.getLogger().warn("Bad Response getting image: " + imageURL + " : " + conn.getResponseCode());
            }
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().warn("Error getting skin image: " + imageURL + " : " + e.getMessage());
        }
        finally
        {
            if (conn != null)
            {
                conn.disconnect();
            }
        }

        return img;
    }
}
