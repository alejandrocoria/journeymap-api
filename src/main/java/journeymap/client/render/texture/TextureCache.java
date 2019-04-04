/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.render.texture;

import journeymap.client.io.FileHandler;
import journeymap.client.io.IconSetFileHandler;
import journeymap.client.io.ThemeLoader;
import journeymap.client.task.main.ExpireTextureTask;
import journeymap.client.ui.theme.Theme;
import journeymap.common.Journeymap;
import journeymap.common.thread.JMThreadFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Texture management
 *
 * @author techbrew
 */
public class TextureCache
{
    /**
     * The constant GridCheckers.
     */
// BufferedImages should be retained
    public static final ResourceLocation GridCheckers = uiImage("grid-checkers.png");
    /**
     * The constant GridDots.
     */
    public static final ResourceLocation GridDots = uiImage("grid-dots.png");
    /**
     * The constant GridSquares.
     */
    public static final ResourceLocation GridSquares = uiImage("grid.png");
    /**
     * The constant GridSquaresWithRegionOutline.
     */
    public static final ResourceLocation GridRegionSquares = uiImage("grid-region.png");
    /**
     * The constant GridSquaresWithRegionOutline.
     */
    public static final ResourceLocation GridRegion = uiImage("region.png");
    /**
     * The constant ColorPicker.
     */
    public static final ResourceLocation ColorPicker = uiImage("colorpick.png");
    /**
     * The constant ColorPicker2.
     */
    public static final ResourceLocation ColorPicker2 = uiImage("colorpick2.png");
    /**
     * The constant TileSampleDay.
     */
    public static final ResourceLocation TileSampleDay = uiImage("tile-sample-day.png");
    /**
     * The constant TileSampleNight.
     */
    public static final ResourceLocation TileSampleNight = uiImage("tile-sample-night.png");
    /**
     * The constant TileSampleUnderground.
     */
    public static final ResourceLocation TileSampleUnderground = uiImage("tile-sample-underground.png");
    /**
     * The constant UnknownEntity.
     */
    public static final ResourceLocation UnknownEntity = uiImage("unknown.png");

    /**
     * The constant Deathpoint.
     */
    public static final ResourceLocation Deathpoint = uiImage("waypoint-death.png");
    /**
     * The constant MobDot.
     */
    public static final ResourceLocation MobDot = uiImage("marker-dot-16.png");
    /**
     * The constant MobDot_Large.
     */
    public static final ResourceLocation MobDot_Large = uiImage("marker-dot-32.png");
    /**
     * The constant MobDotArrow.
     */
    public static final ResourceLocation MobDotArrow = uiImage("marker-dot-arrow-16.png");
    /**
     * The constant MobDotArrow_Large.
     */
    public static final ResourceLocation MobDotArrow_Large = uiImage("marker-dot-arrow-32.png");
    /**
     * The constant MobDotChevron.
     */
    public static final ResourceLocation MobDotChevron = uiImage("marker-chevron-16.png");
    /**
     * The constant MobDotChevron_Large.
     */
    public static final ResourceLocation MobDotChevron_Large = uiImage("marker-chevron-32.png");
    /**
     * The constant MobIconArrow.
     */
    public static final ResourceLocation MobIconArrow = uiImage("marker-icon-arrow-16.png");
    /**
     * The constant MobIconArrow_Large.
     */
    public static final ResourceLocation MobIconArrow_Large = uiImage("marker-icon-arrow-32.png");
    /**
     * The constant PlayerArrow.
     */
    public static final ResourceLocation PlayerArrow = uiImage("marker-player-16.png");
    /**
     * The constant PlayerArrowBG.
     */
    public static final ResourceLocation PlayerArrowBG = uiImage("marker-player-bg-16.png");
    /**
     * The constant PlayerArrow_Large.
     */
    public static final ResourceLocation PlayerArrow_Large = uiImage("marker-player-32.png");
    /**
     * The constant PlayerArrowBG_Large.
     */
    public static final ResourceLocation PlayerArrowBG_Large = uiImage("marker-player-bg-32.png");
    /**
     * The constant Logo.
     */
    public static final ResourceLocation Logo = uiImage("ico/journeymap.png");
    /**
     * The constant MinimapSquare128.
     */
    public static final ResourceLocation MinimapSquare128 = uiImage("minimap/minimap-square-128.png");
    /**
     * The constant MinimapSquare256.
     */
    public static final ResourceLocation MinimapSquare256 = uiImage("minimap/minimap-square-256.png");
    /**
     * The constant MinimapSquare512.
     */
    public static final ResourceLocation MinimapSquare512 = uiImage("minimap/minimap-square-512.png");
    /**
     * The constant Patreon.
     */
    public static final ResourceLocation Patreon = uiImage("patreon.png");
    /**
     * The constant Patreon.
     */
    public static final ResourceLocation Discord = uiImage("discord.png");
    /**
     * The constant Waypoint.
     */
    public static final ResourceLocation Waypoint = uiImage("waypoint.png");
    /**
     * The constant WaypointEdit.
     */
    public static final ResourceLocation WaypointEdit = uiImage("waypoint-edit.png");
    /**
     * The constant WaypointOffscreen.
     */
    public static final ResourceLocation WaypointOffscreen = uiImage("waypoint-offscreen.png");

    private static ResourceLocation uiImage(String fileName)
    {
        return new ResourceLocation(Journeymap.MOD_ID, "ui/img/" + fileName);
    }

    /**
     * The constant playerSkins.
     */
// Keeps the textures referenced here from being garbage collected, since ResourceLocationTexture's cache uses weak values.
    public static final Map<String, TextureImpl> playerSkins = Collections.synchronizedMap(new HashMap<>());
    /**
     * The constant themeImages.
     */
    public static final Map<String, TextureImpl> themeImages = Collections.synchronizedMap(new HashMap<>());

    private static ThreadPoolExecutor texExec = new ThreadPoolExecutor(2, 4, 15L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(8), new JMThreadFactory("texture"),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    /**
     * Gets texture.
     *
     * @param location the location
     * @return the texture
     */
    public static TextureImpl getTexture(ResourceLocation location)
    {
        if (location == null)
        {
            return null;
        }
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        ITextureObject textureObject = textureManager.getTexture(location);
        if (textureObject == null || (!(textureObject instanceof TextureImpl)))
        {
            textureObject = new TextureImpl(location);
            boolean loaded = textureManager.loadTexture(location, textureObject);
            if (!loaded)
            {
                textureObject = null;
            }

        }
        return (TextureImpl) textureObject;
    }

    /**
     * Schedule texture task future.
     *
     * @param <T>         the type parameter
     * @param textureTask the texture task
     * @return the future
     */
    public static <T extends TextureImpl> Future<T> scheduleTextureTask(Callable<T> textureTask)
    {
        return texExec.submit(textureTask);
    }

    /**
     * Reset.
     */
    public static void reset()
    {
        playerSkins.clear();

        Arrays.asList(ColorPicker, ColorPicker2, Deathpoint, GridCheckers, GridDots, GridSquares, GridRegionSquares, GridRegion, Logo,
                MinimapSquare128, MinimapSquare256, MinimapSquare512, MobDot, MobDot_Large, MobDotArrow,
                MobDotArrow_Large, MobDotChevron, MobDotChevron_Large, MobIconArrow_Large, Patreon, PlayerArrow,
                PlayerArrow_Large, PlayerArrowBG, PlayerArrowBG, TileSampleDay, TileSampleNight, TileSampleUnderground,
                UnknownEntity, Waypoint, WaypointEdit, WaypointOffscreen).stream().map(TextureCache::getTexture);

        Arrays.asList(ColorPicker, ColorPicker2, GridCheckers, GridDots, GridSquares, GridRegion, GridRegionSquares, TileSampleDay,
                TileSampleNight, TileSampleUnderground, UnknownEntity).stream().map(TextureCache::getTexture);
    }

    /**
     * Purge theme images.
     *
     * @param themeImages the theme images
     */
    public static void purgeThemeImages(Map<String, TextureImpl> themeImages)
    {
        synchronized (themeImages)
        {
            ExpireTextureTask.queue(themeImages.values());
            themeImages.clear();
        }
    }

    /**
     * Gets a buffered image from the resource location
     *
     * @param location location
     * @return image buffered image
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
        catch (FileNotFoundException e)
        {
            if ("journeymap".equals(location.getResourceDomain()))
            {
                // Dev environment
                File imgFile = new File("../src/main/resources/assets/journeymap/" + location.getResourcePath());
                if (imgFile.exists())
                {
                    try
                    {
                        return ImageIO.read(imgFile);
                    }
                    catch (IOException e1)
                    {
                    }
                }
            }
            Journeymap.getLogger().warn("Image not found: " + e.getMessage());
            return null;
        }
        catch (Exception e)
        {

            //Journeymap.getLogger().error("Resource not usable as image: " + location, LogFormatter.toPartialString(e));
            Journeymap.getLogger().warn("Resource not readable with TextureUtil.readBufferedImage(): " + location);
            return null;
        }
    }

    /**
     * Gets theme texture.
     *
     * @param theme    the theme
     * @param iconPath the icon path
     * @return the theme texture
     */
    public static TextureImpl getThemeTexture(Theme theme, String iconPath)
    {
        return getSizedThemeTexture(theme, iconPath, 0, 0, false, 1f, false);
    }

    /**
     * Gets sized theme texture.
     *
     * @param theme       the theme
     * @param iconPath    the icon path
     * @param width       the width
     * @param height      the height
     * @param resize      the resize
     * @param alpha       the alpha
     * @param retainImage the retain image
     * @return the sized theme texture
     */
    public static TextureImpl getSizedThemeTexture(Theme theme, String iconPath, int width, int height, boolean resize, float alpha, boolean retainImage)
    {
        String texName = String.format("%s/%s", theme.directory, iconPath);
        synchronized (themeImages)
        {
            TextureImpl tex = themeImages.get(texName);
            if (tex == null || (tex.retainImage != retainImage) || (!tex.hasImage() && tex.retainImage) || (resize && (width != tex.width || height != tex.height)) || tex.alpha != alpha)
            {
                File parentDir = ThemeLoader.getThemeIconDir();
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
                    return getTexture(UnknownEntity);
                }
            }
            return tex;
        }
    }

    /**
     * Gets scaled copy.
     *
     * @param texName  the tex name
     * @param original the original
     * @param width    the width
     * @param height   the height
     * @param alpha    the alpha
     * @return the scaled copy
     */
    public static TextureImpl getScaledCopy(String texName, TextureImpl original, int width, int height, float alpha)
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
                    return getTexture(UnknownEntity);
                }
            }
            return tex;
        }
    }

    /**
     * Get the head portion of a player's skin, scaled to 24x24 pixels.
     *
     * @param username the username
     * @return the player skin
     */
    public static TextureImpl getPlayerSkin(final UUID playerId, final String username)
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
        texExec.submit((Callable<Void>) () -> {
            BufferedImage img = IgnSkin.getFaceImage(playerId, username);
            if (img != null)
            {
                playerSkinTex.setImage(img, true);
            }
            else
            {
                Journeymap.getLogger().warn("Couldn't get a skin at all for " + username);
            }
            return null;
        });

        return playerSkinTex;
    }
}
