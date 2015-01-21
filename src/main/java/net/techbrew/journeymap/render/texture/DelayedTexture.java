/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.render.texture;


import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

/**
 * Created by mwoodman on 12/22/13.
 */
public class DelayedTexture extends TextureImpl
{
    private final Object lock = new Object();
    private ByteBuffer buffer = ByteBuffer.allocateDirect(512 * 512 * 4);

    private boolean rebindNeeded;

    /**
     * Can be safely called without the OpenGL Context.
     */
    public DelayedTexture()
    {
    }

    /**
     * Can be safely called without the OpenGL Context.
     */
    public DelayedTexture(Integer glId, BufferedImage image)
    {
        super(glId, image, true);
        setImage(image);
    }

    /**
     * Can be safely called without the OpenGL Context.
     */
    public void setImage(BufferedImage image)
    {
        synchronized (lock)
        {
            this.image = image;
            width = image.getWidth();
            height = image.getHeight();
            if (buffer.capacity() < (width * height * 4))
            {
                buffer = ByteBuffer.allocateDirect(width * height * 4);
            }
            buffer.clear();

            int[] pixels = new int[width * height];
            image.getRGB(0, 0, width, height, pixels, 0, width);
            int pixel;
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    pixel = pixels[y * width + x];
                    buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                    buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                    buffer.put((byte) (pixel & 0xFF));             // Blue component
                    buffer.put((byte) ((pixel >> 24) & 0xFF));     // Alpha component
                }
            }
            buffer.flip();
            buffer.rewind();
            rebindNeeded = true;
        }
    }

    @Override
    public boolean isBound()
    {
        return glTextureId != -1;
    }

    public boolean isRebindNeeded()
    {
        return rebindNeeded;
    }

    /**
     * Must be called on same thread as OpenGL Context
     *
     * @return
     */
    public void bindTexture()
    {
        synchronized (lock)
        {
            if (rebindNeeded)
            {
                try
                {
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, getGlTextureId());

                    //Send texel data to OpenGL

                    // Setup wrap mode
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

                    //Setup texture scaling filtering
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

                    int glErr = GL11.glGetError();
                    if (glErr != GL11.GL_NO_ERROR)
                    {
                        JourneyMap.getLogger().warn("GL Error in DelayedTexture after glTexImage2D: " + glErr);
                    }
                    else
                    {
                        rebindNeeded = false;
                    }
                }
                catch (Throwable t)
                {
                    JourneyMap.getLogger().warn("Can't bind texture: " + LogFormatter.toString(t));
                }
            }
        }
    }


}
