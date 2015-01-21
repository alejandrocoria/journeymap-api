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
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.StatTimer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class TextureImpl extends AbstractTexture
{

    //private static final IntBuffer dataBuffer = GLAllocation.createDirectIntBuffer(4194304);

    protected int width;
    protected int height;
    protected boolean retainImage;
    protected float alpha;
    /**
     * optionally-retained image *
     */
    protected BufferedImage image;
    protected volatile boolean unbound;

    private long lastUpdated;
    private String description;

    protected TextureImpl()
    {
    }

    public TextureImpl(BufferedImage image)
    {
        this(image, false);
    }

    public TextureImpl(BufferedImage image, boolean retainImage)
    {
        this.retainImage = retainImage;
        this.width = image.getWidth();
        this.height = image.getHeight();
        updateTexture(image, true);
    }

    TextureImpl(Integer glId, BufferedImage image, boolean retainImage)
    {
        if (glId != null)
        {
            this.glTextureId = glId;
        }
        this.retainImage = retainImage;
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    private static void uploadTextureImageSubImpl(BufferedImage image, int par1, int par2, boolean par3, boolean par4)
    {
        int var5 = image.getWidth();
        int var6 = image.getHeight();
        int var7 = 4194304 / var5;
        int[] var8 = new int[var7 * var5];


        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4); //4 for RGBA, 3 for RGB

        for (int y = 0; y < image.getHeight(); y++)
        {
            for (int x = 0; x < image.getWidth(); x++)
            {
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                buffer.put((byte) (pixel & 0xFF));               // Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
            }
        }

        buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS

        setTextureBlurred(par3);
        setTextureClamped(par4);
        StatTimer timer = StatTimer.get("TextureImpl.updateTexture.bind");
        timer.start();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        timer.stop();
    }

    private static void setTextureClamped(boolean par0)
    {
        if (par0)
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        }
        else
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        }
    }

    private static void setTextureBlurred(boolean par0)
    {
        if (par0)
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        }
        else
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
    }

//    private static void copyToBuffer(int[] par0ArrayOfInteger, int par1)
//    {
//        copyToBufferPos(par0ArrayOfInteger, 0, par1);
//    }
//
//    private static void copyToBufferPos(int[] par0ArrayOfInteger, int par1, int par2)
//    {
//        int[] var3 = par0ArrayOfInteger;
//
//        dataBuffer.clear();
//        dataBuffer.put(var3, par1, par2);
//        dataBuffer.position(0).limit(par2);
//    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    protected void updateTexture(BufferedImage updatedImage, boolean allocateMemory)
    {
        if (updatedImage.getWidth() != width || updatedImage.getHeight() != height)
        {
            throw new IllegalArgumentException("Image dimensions don't match");
        }
        if (retainImage)
        {
            this.image = updatedImage;
        }
        try
        {
            int glId = getGlTextureId();
            if (allocateMemory)
            {
                TextureUtil.uploadTextureImage(glId, updatedImage);
            }
            else
            {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, glId);
                uploadTextureImageSubImpl(updatedImage, 0, 0, false, false);
            }
            lastUpdated = System.currentTimeMillis();
        }
        catch (RuntimeException e)
        {
            if (e.getMessage().startsWith("No OpenGL context"))
            {
                this.unbound = true;
            }
            else
            {
                JourneyMap.getLogger().error("Failed to upload/bind texture: " + e.getMessage());
            }
        }
    }

    public void updateTexture(BufferedImage image)
    {
        updateTexture(image, retainImage);
    }

    @Override
    public int getGlTextureId()
    {
        int glId = super.getGlTextureId();
        if (unbound && image != null)
        {
            try
            {
                TextureUtil.uploadTextureImage(glId, image);
                unbound = false;
            }
            catch (Exception e)
            {
                JourneyMap.getLogger().error("Couldn't use deferred binding: " + e.getMessage());
            }
        }
        return glId;
    }

    public boolean isBound()
    {
        return !unbound;
    }

    public boolean hasImage()
    {
        return image != null;
    }

    public BufferedImage getImage()
    {
        return image;
    }

    public void clear()
    {
        this.image = null;
        this.unbound = true;
        this.glTextureId = -1;
    }

    public boolean deleteTexture()
    {
        if (this.glTextureId != -1)
        {
            try
            {
                GL11.glDeleteTextures(this.getGlTextureId());
                this.glTextureId = -1;
            }
            catch (Throwable t)
            {
                JourneyMap.getLogger().warn("Couldn't delete texture: " + t);
                return false;
            }
        }
        if (this.image != null)
        {
            this.image = null;
        }
        return true;
    }

    public long getLastUpdated()
    {
        return lastUpdated;
    }

    public void setLastUpdated(long time)
    {
        lastUpdated = time;
    }

    @Override
    public void loadTexture(IResourceManager par1ResourceManager)
    {
    }


    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("description", description)
                .add("lastUpdated", lastUpdated)
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

    /**
     * keep image with object
     */
    public boolean isRetainImage()
    {
        return retainImage;
    }

    public void setRetainImage(boolean retainImage)
    {
        this.retainImage = retainImage;
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
