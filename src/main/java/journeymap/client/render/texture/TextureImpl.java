/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.render.texture;

import com.google.common.base.MoreObjects;
import journeymap.client.task.main.ExpireTextureTask;
import journeymap.client.task.multi.MapPlayerTask;
import journeymap.common.Journeymap;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class TextureImpl extends AbstractTexture
{
    protected final ReentrantLock bufferLock = new ReentrantLock();
    protected BufferedImage image;
    protected boolean retainImage;
    protected int width;
    protected int height;
    protected float alpha;
    protected long lastImageUpdate;
    protected long lastBound;
    protected String description;
    protected ResourceLocation resourceLocation;
    protected List<WeakReference<Listener>> listeners = new ArrayList<>(0);

    protected ByteBuffer buffer;
    protected boolean bindNeeded;

    /**
     * Must be called on thread with OpenGL Context.  Texture is immediately bound.
     */
    public TextureImpl(ResourceLocation resourceLocation)
    {
        this(null, TextureCache.resolveImage(resourceLocation), false, false);
        this.resourceLocation = resourceLocation;
        setDescription(resourceLocation.getResourcePath());
    }

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

        this.retainImage = retainImage;
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
        if (bufferedImage == null)
        {
            return;
        }

        try
        {
            bufferLock.lock();

            this.retainImage = retainImage;
            if (retainImage)
            {
                this.image = bufferedImage;
            }

            this.width = bufferedImage.getWidth();
            this.height = bufferedImage.getHeight();
            int bufferSize = width * height * 4; // RGBA

            if (buffer == null || (buffer.capacity() != bufferSize))
            {
                buffer = ByteBuffer.allocateDirect(bufferSize);
            }

            loadByteBuffer(bufferedImage, buffer);
            bindNeeded = true;
        }
        finally
        {
            bufferLock.unlock();
        }
        this.lastImageUpdate = System.currentTimeMillis();
        notifyListeners();
    }

    public static void loadByteBuffer(BufferedImage bufferedImage, ByteBuffer buffer)
    {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
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
    }

    /**
     * Must be called on same thread as OpenGL Context
     *
     * @return
     */
    public void bindTexture()
    {
        if (!bindNeeded)
        {
            return;
        }

        if (bufferLock.tryLock())
        {
            if (glTextureId > -1)
            {
                MapPlayerTask.addTempDebugMessage("tex" + glTextureId, "Updating: " + getDescription());
            }

            try
            {
                GlStateManager.bindTexture(super.getGlTextureId());

                // Setup wrap mode
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

                //Setup texture scaling filtering
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

                // Texture
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

                bindNeeded = false;

                int glErr;
                while ((glErr = GL11.glGetError()) != GL11.GL_NO_ERROR)
                {
                    Journeymap.getLogger().warn("GL Error in TextureImpl after glTexImage2D: " + glErr + " in " + this);
                    bindNeeded = true;
                }

                if (!bindNeeded)
                {
                    lastBound = System.currentTimeMillis();
                }
            }
            catch (Throwable t)
            {
                Journeymap.getLogger().warn("Can't bind texture: " + t);
                buffer = null;
            }
            finally
            {
                bufferLock.unlock();
            }
        }
    }

    public boolean isBindNeeded()
    {
        return bindNeeded;
    }

    public boolean isBound()
    {
        return glTextureId != -1;
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

    public boolean hasImage()
    {
        return image != null;
    }

    public BufferedImage getImage()
    {
        if (image != null)
        {
            return image;
        }
        if (resourceLocation != null)
        {
            return TextureCache.resolveImage(resourceLocation);
        }
        return null;
    }

    public boolean isDefunct()
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

    public int getGlTextureId(boolean forceBind)
    {
        if (forceBind || glTextureId == -1)
        {
            return getGlTextureId();
        }
        else
        {
            return glTextureId;
        }
    }

    /**
     * Does not delete GLID - use with caution
     */
    public void clear()
    {
        bufferLock.lock();
        this.buffer = null;
        bufferLock.unlock();
        this.image = null;
        this.bindNeeded = false;
        this.lastImageUpdate = 0;
        this.lastBound = 0;
        this.glTextureId = -1;
    }

    /**
     * Must be called on same thread as OpenGL Context
     */
    public void queueForDeletion()
    {
        ExpireTextureTask.queue(this);
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
        if (resourceLocation != null)
        {
        }
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("glid", glTextureId)
                .add("description", description)
                .add("lastImageUpdate", lastImageUpdate)
                .add("lastBound", lastBound)
                .toString();
    }

    public void finalize()
    {
        if (isBound())
        {
            Journeymap.getLogger().warn("TextureImpl disposed without deleting texture glID: " + this);
            ExpireTextureTask.queue(this.glTextureId);
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

    public void addListener(Listener addedListener)
    {
        Iterator<WeakReference<Listener>> iter = listeners.iterator();
        while (iter.hasNext())
        {
            WeakReference<Listener> ref = iter.next();
            Listener listener = ref.get();
            if (listener == null)
            {
                iter.remove();
            }
            else
            {
                if (addedListener == listener)
                {
                    return;
                }
            }
        }
        this.listeners.add(new WeakReference<>(addedListener));
    }

    protected void notifyListeners()
    {
        Iterator<WeakReference<Listener>> iter = listeners.iterator();
        while (iter.hasNext())
        {
            WeakReference<Listener> ref = iter.next();
            Listener listener = ref.get();
            if (listener == null)
            {
                iter.remove();
            }
            else
            {
                listener.textureImageUpdated(this);
            }
        }
    }

    public static interface Listener<T extends TextureImpl>
    {
        void textureImageUpdated(T textureImpl);
    }
}
