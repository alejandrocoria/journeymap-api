/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.render.texture;

import journeymap.client.task.multi.MapPlayerTask;
import journeymap.common.Journeymap;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.math.ChunkPos;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

/**
 * The type Region texture.
 */
public class RegionTextureImpl extends TextureImpl
{
    /**
     * The Dirty chunks.
     */
    protected HashSet<ChunkPos> dirtyChunks = new HashSet<>();

    /**
     * Instantiates a new Region texture.
     *
     * @param image the image
     */
    public RegionTextureImpl(BufferedImage image)
    {
        super(null, image, true, false);
    }

    /**
     * Sets image.
     *
     * @param bufferedImage the buffered image
     * @param retainImage   the retain image
     * @param updatedChunks the updated chunks
     */
    public void setImage(BufferedImage bufferedImage, boolean retainImage, HashSet<ChunkPos> updatedChunks)
    {
        if (updatedChunks.size() > 15)
        {
            super.setImage(bufferedImage, retainImage);
        }
        else
        {
            this.dirtyChunks.addAll(updatedChunks);
            bindNeeded = true;
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
            }
            finally
            {
                bufferLock.unlock();
            }

        }
        this.lastImageUpdate = System.currentTimeMillis();
        notifyListeners();
    }

    /**
     * Must be called on same thread as OpenGL Context
     */
    @Override
    public void bindTexture()
    {
        if (!bindNeeded)
        {
            return;
        }

        if (this.glTextureId == -1)
        {
            this.glTextureId = TextureUtil.glGenTextures();
        }

        if (lastBound == 0 || dirtyChunks.isEmpty())
        {
            super.bindTexture();
            return;
        }

        if (bufferLock.tryLock())
        {
            MapPlayerTask.addTempDebugMessage("tex" + glTextureId, "Updating " + dirtyChunks.size() + " chunks within: " + getDescription());

            GlStateManager.bindTexture(this.glTextureId);

            // Setup wrap mode
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

            //Setup texture scaling filtering
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

            try
            {
                boolean glErrors = false;
                ByteBuffer chunkBuffer = ByteBuffer.allocateDirect(1024); // 16x16xRGBA
                for (ChunkPos pos : dirtyChunks)
                {
                    BufferedImage chunkImage = getImage().getSubimage(pos.chunkXPos, pos.chunkZPos, 16, 16);
                    loadByteBuffer(chunkImage, chunkBuffer);

                    GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, pos.chunkXPos, pos.chunkZPos, 16, 16, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, chunkBuffer);

                    int err;
                    while ((err = GL11.glGetError()) != GL11.GL_NO_ERROR)
                    {
                        glErrors = true;
                        Journeymap.getLogger().warn("GL Error in RegionTextureImpl after glTexSubImage2D: " + err + " for " + pos + " in " + this);
                    }

                    if (glErrors)
                    {
                        break;
                    }
                }

                dirtyChunks.clear();

                if (glErrors)
                {
                    bindNeeded = true;
                }
                else
                {
                    bindNeeded = false;
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
                //MapPlayerTask.removeTempDebugMessage(msg);
            }
        }
        else
        {
            //System.out.println("Missed binding, will try later");
        }
    }

    /**
     * WARNING:  These aren't actual ChunkPos coordinates, they're simply x,z pixel offsets within the image.
     * Each one does line up with a chunk, however.
     *
     * @return the dirty areas
     */
    public Set<ChunkPos> getDirtyAreas()
    {
        return dirtyChunks;
    }
}
