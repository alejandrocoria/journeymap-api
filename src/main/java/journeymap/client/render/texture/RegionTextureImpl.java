/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.render.texture;

import journeymap.client.task.multi.MapPlayerTask;
import journeymap.common.Journeymap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.math.ChunkPos;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class RegionTextureImpl extends TextureImpl
{
    protected HashSet<ChunkPos> dirtyChunks = new HashSet<>();

    public RegionTextureImpl(BufferedImage image)
    {
        super(null, image, true, false);
    }

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

        if (dirtyChunks.isEmpty())
        {
            super.bindTexture();
            return;
        }

        if (bufferLock.tryLock())
        {
            if (glTextureId > -1)
            {
                MapPlayerTask.addTempDebugMessage("tex" + glTextureId, "Updating " + dirtyChunks.size() + " chunks within: " + getDescription());
            }

            try
            {
                for (ChunkPos pos : dirtyChunks)
                {
                    TextureUtil.uploadTextureImageSub(this.glTextureId, getImage().getSubimage(pos.chunkXPos, pos.chunkZPos, 16, 16),
                            pos.chunkXPos, pos.chunkZPos, false, true);
                    //System.out.println("Chunk");
                }

                dirtyChunks.clear();

                int glErr = GL11.glGetError();
                if (glErr != GL11.GL_NO_ERROR)
                {
                    bindNeeded = true;
                    Journeymap.getLogger().warn("GL Error in RegionTextureImpl after glTexSubImage2D: " + glErr + " in " + this);
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
     */
    public Set<ChunkPos> getDirtyAreas()
    {
        return dirtyChunks;
    }
}
