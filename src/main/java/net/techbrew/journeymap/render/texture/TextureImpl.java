/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.render.texture;

import com.google.common.base.Objects;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.IResourceManager;
import net.techbrew.journeymap.JourneyMap;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class TextureImpl extends AbstractTexture
{
    private final Object bufferLock = new Object();
    protected BufferedImage image;
    protected boolean retainImage;
    protected int width;
    protected int height;
    protected float alpha;
    protected long lastImageUpdate;
    protected long lastBound;
    protected String description;

    protected ByteBuffer buffer;
    protected boolean bindNeeded;

    /**
     * Must be called on thread with OpenGL Context.  Texture is immediately bound.
     */
    public TextureImpl(BufferedImage image)
    {
        this(null, image, false, true);
    }

    /**
     * Must be called on thread with OpenGL Context.  Texture is immediately bound.
     */
    public TextureImpl(BufferedImage image, boolean retainImage)
    {
        this(null, image, retainImage, true);
    }

    /**
     * If bindImmediately, must be called on thread with OpenGL Context.
     */
    public TextureImpl(Integer glId, BufferedImage image, boolean retainImage)
    {
        this(glId, image, retainImage, true);
    }

    /**
     * If bindImmediately, must be called on thread with OpenGL Context.
     */
    public TextureImpl(Integer glId, BufferedImage image, boolean retainImage, boolean bindImmediately)
    {
        if (glId != null)
        {
            this.glTextureId = glId;
        }

        if (image != null)
        {
            setImage(image, retainImage);
        }

        if (bindImmediately)
        {
            bindTexture();
            buffer = null;
        }
    }

    /**
     * Can be safely called without the OpenGL Context.
     */
    public void setImage(BufferedImage bufferedImage, boolean retainImage)
    {
        this.retainImage = retainImage;
        if (retainImage)
        {
            this.image = bufferedImage;
        }

        this.width = bufferedImage.getWidth();
        this.height = bufferedImage.getHeight();
        int bufferSize = width * height * 4; // RGBA

        synchronized (bufferLock)
        {
            if (buffer == null || (buffer.capacity() != bufferSize))
            {
                buffer = ByteBuffer.allocateDirect(bufferSize);
            }
            buffer.clear();

            int[] pixels = new int[width * height];
            bufferedImage.getRGB(0, 0, width, height, pixels, 0, width);
            int pixel;
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    pixel = pixels[y * width + x];
                    buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red
                    buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green
                    buffer.put((byte) ((pixel & 0xFF)));           // Blue
                    buffer.put((byte) ((pixel >> 24) & 0xFF));     // Alpha
                }
            }
            buffer.flip();
            buffer.rewind();
            bindNeeded = true;
        }
        this.lastImageUpdate = System.currentTimeMillis();
    }

    /**
     * Must be called on same thread as OpenGL Context
     *
     * @return
     */
    public void bindTexture()
    {
        if (bindNeeded)
        {
            synchronized (bufferLock)
            {
                try
                {
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, super.getGlTextureId());

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
                        JourneyMap.getLogger().warn("GL Error in TextureImpl after glTexImage2D: " + glErr);
                    }
                    else
                    {
                        bindNeeded = false;
                        lastBound = System.currentTimeMillis();
                    }
                }
                catch (Throwable t)
                {
                    JourneyMap.getLogger().warn("Can't bind texture: " + t);
                    buffer = null;
                }
            }
        }
    }

    public boolean isBindNeeded()
    {
        return bindNeeded;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Must be called on same thread as OpenGL Context
     */
    public void updateAndBind(BufferedImage image)
    {
        updateAndBind(image, retainImage);
    }

    /**
     * Must be called on same thread as OpenGL Context
     */
    public void updateAndBind(BufferedImage image, boolean retainImage)
    {
        setImage(image, retainImage);
        bindTexture();

    }

//    @Override
//    public int getGlTextureId()
//    {
//        int glId = super.getGlTextureId();
//        if (bindNeeded)
//        {
//            bindTexture();
//        }
//        return glId;
//    }

    public boolean hasImage()
    {
        return image != null;
    }

    public BufferedImage getImage()
    {
        return image;
    }

    public boolean isUnused()
    {
        return this.glTextureId == -1 && image == null && buffer == null;
    }

    /**
     * Must be called with GL Context on current thread.
     */
    @Override
    public int getGlTextureId()
    {
        if (bindNeeded)
        {
            bindTexture();
        }
        return super.getGlTextureId();
    }

    /**
     * Does not delete GLID - use with caution
     */
    private void clear()
    {
        synchronized (bufferLock)
        {
            this.buffer = null;
        }
        this.image = null;
        this.bindNeeded = false;
        this.lastImageUpdate = 0;
        this.lastBound = 0;
        this.glTextureId = -1;
    }

    /**
     * Must be called on same thread as OpenGL Context
     */
    public boolean deleteTexture()
    {
        boolean success = false;

        if (this.glTextureId != -1)
        {
            try
            {
                if (Display.isCurrent())
                {
                    GL11.glDeleteTextures(this.getGlTextureId());
                    this.glTextureId = -1;
                    clear();
                    success = true;
                }
            }
            catch (Throwable t)
            {
                JourneyMap.getLogger().warn("Couldn't delete texture: " + t);
                success = false;
            }
        }
        else
        {
            clear();
            success = true;
        }

        return success;
    }

    public long getLastImageUpdate()
    {
        return lastImageUpdate;
    }

    public long getLastBound()
    {
        return lastBound;
    }

    @Override
    public void loadTexture(IResourceManager par1ResourceManager)
    {
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("glid", glTextureId)
                .add("description", description)
                .add("lastImageUpdate", lastImageUpdate)
                .add("lastBound", lastBound)
                .toString();
    }

    public void finalize()
    {
        if (this.glTextureId != -1)
        {
            if (!deleteTexture())
            {
                JourneyMap.getLogger().error("TextureImpl disposed without deleting texture glID: " + this.glTextureId);
            }
            else
            {
                JourneyMap.getLogger().warn("TextureImpl hadn't deleted texture glID before disposed of properly.");
            }
        }
    }

    /**
     * width of this icon in pixels
     */
    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    /**
     * height of this icon in pixels
     */
    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public float getAlpha()
    {
        return alpha;
    }

    public void setAlpha(float alpha)
    {
        this.alpha = alpha;
    }
}
