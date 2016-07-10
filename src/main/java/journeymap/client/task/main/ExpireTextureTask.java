/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.task.main;

import journeymap.client.JourneymapClient;
import journeymap.client.render.texture.TextureImpl;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Ensure a texture is expired on the thread with OpenGL
 */
public class ExpireTextureTask implements IMainThreadTask
{
    private static final int MAX_FAILS = 5;
    private static String NAME = "Tick." + MappingMonitorTask.class.getSimpleName();
    private static Logger LOGGER = Journeymap.getLogger();
    private final List<TextureImpl> textures;
    private final int textureId;
    private volatile int fails;

    private ExpireTextureTask(int textureId)
    {
        textures = null;
        this.textureId = textureId;
    }

    private ExpireTextureTask(TextureImpl texture)
    {
        textures = new ArrayList<TextureImpl>();
        textures.add(texture);
        this.textureId = -1;
    }

    private ExpireTextureTask(Collection<TextureImpl> textureCollection)
    {
        this.textures = new ArrayList<TextureImpl>(textureCollection);
        this.textureId = -1;
    }

    public static void queue(int textureId)
    {
        if (textureId != -1)
        {
            Journeymap.getClient().queueMainThreadTask(new ExpireTextureTask(textureId));
        }
    }

    public static void queue(TextureImpl texture)
    {
        Journeymap.getClient().queueMainThreadTask(new ExpireTextureTask(texture));
    }

    public static void queue(Collection<TextureImpl> textureCollection)
    {
        Journeymap.getClient().queueMainThreadTask(new ExpireTextureTask(textureCollection));
    }

    @Override
    public IMainThreadTask perform(Minecraft mc, JourneymapClient jm)
    {
        boolean success = deleteTextures();
        if (!success && textures != null && !textures.isEmpty())
        {
            fails++;
            LOGGER.warn("ExpireTextureTask.perform() couldn't delete textures: " + textures + ", fails: " + fails);
            if (fails <= MAX_FAILS)
            {
                return this;
            }
        }
        return null;
    }

    /**
     * Must be called on same thread as OpenGL Context
     */
    private boolean deleteTextures()
    {
        if (textureId != -1)
        {
            return deleteTexture(textureId);
        }
        else
        {
            Iterator<TextureImpl> iter = textures.listIterator();
            while (iter.hasNext())
            {
                TextureImpl texture = iter.next();
                if (texture == null)
                {
                    iter.remove();
                    continue;
                }
                if (deleteTexture(texture))
                {
                    iter.remove();
                }
                else
                {
                    break;
                }
            }
            return textures.isEmpty();
        }
    }

    private boolean deleteTexture(TextureImpl texture)
    {
        boolean success = false;
        if (texture.isBound())
        {
            try
            {
                if (Display.isCurrent())
                {
                    GlStateManager.deleteTexture(texture.getGlTextureId());
                    //LOGGER.info("Successfully deleted " + texture);
                    texture.clear();
                    success = true;
                }
            }
            catch (LWJGLException t)
            {
                LOGGER.warn("Couldn't delete texture " + texture + ": " + t);
                success = false;
            }
        }
        else
        {
            texture.clear();
            success = true;
        }

        return success;
    }

    private boolean deleteTexture(int textureId)
    {
        try
        {
            if (Display.isCurrent())
            {
                GlStateManager.deleteTexture(textureId);
                return true;
            }
        }
        catch (LWJGLException t)
        {
            LOGGER.warn("Couldn't delete textureId " + textureId + ": " + t);
        }
        return false;
    }

    @Override
    public String getName()
    {
        return NAME;
    }
}
