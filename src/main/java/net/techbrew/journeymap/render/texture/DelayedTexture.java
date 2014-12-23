/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.render.texture;


import net.minecraft.client.renderer.texture.TextureUtil;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.RegionImageHandler;
import net.techbrew.journeymap.log.StatTimer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

/**
 * Created by mwoodman on 12/22/13.
 */
public class DelayedTexture
{
    static StatTimer timer = StatTimer.get("DelayedTexture.setImage");
    private final Object lock = new Object();
    int width;
    int height;
    ByteBuffer buffer;
    Integer glId;
    BufferedImage image;
    TextureImpl result;

    /**
     * Can be safely called without the OpenGL Context.
     */
    public DelayedTexture(Integer glId, BufferedImage image, String debugString)
    {
        this.glId = glId;
        setImage(image);

        if (debugString != null)
        {
            Graphics2D g = RegionImageHandler.initRenderingHints(image.createGraphics());
            g.setPaint(Color.WHITE);
            g.setStroke(new BasicStroke(3));
            g.drawRect(0, 0, width, height);
            final Font labelFont = new Font("Arial", Font.BOLD, 16);
            g.setFont(labelFont); //$NON-NLS-1$
            g.drawString(debugString, 16, 16);
            g.dispose();
        }
    }

    /**
     * Can be safely called without the OpenGL Context.
     */
    public void setImage(BufferedImage image)
    {
        synchronized (lock)
        {
            timer.start();
            this.image = image;
            width = image.getWidth();
            height = image.getHeight();

            buffer = ByteBuffer.allocateDirect(width * height * 4);
            byte data[] = (byte[]) image.getRaster().getDataElements(0, 0, width, height, null);
            buffer.clear();
            buffer.put(data);
            buffer.rewind();

            timer.stop();
        }
    }

    /**
     * Must be called on same thread as OpenGL Context
     *
     * @return
     */
    public TextureImpl bindTexture(BufferedImage image)
    {
        setImage(image);
        synchronized (lock)
        {
            if (result != null)
            {
                result.updateTexture(image);
                return result;
            }
            else
            {
                return bindTexture(false);
            }
        }
    }

    /**
     * Must be called on same thread as OpenGL Context
     *
     * @return
     */
    public synchronized TextureImpl bindTexture(boolean retainImage)
    {
        if (result == null)
        {
            if (glId == null)
            {
                glId = TextureUtil.glGenTextures();
            }

            try
            {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, glId);

                // TODO: Use render settings?
                // Setup wrap mode
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

                //Setup texture scaling filtering
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

                //Send texel data to OpenGL
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

                result = new TextureImpl(glId, image, retainImage);
            }
            catch (Throwable t)
            {
                JourneyMap.getLogger().warn("Can't bind texture: " + t);
            }
        }

        return result;
    }
}
