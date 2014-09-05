/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.minecraft.client.renderer.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/**
 * Workaround to get the entity texture via the protected Render.getEntityTexture() method.
 */
public class RenderFacade extends Render
{
    /**
     * It's a cheat, but it works.   RenderFacade.getEntityTexture(render,entity)
     */
    public static ResourceLocation getEntityTexture(Render render, Entity entity)
    {
        return render.getEntityTexture(entity);
    }

    /**
     * Unused.
     */
    public RenderFacade(Render render)
    {
    }

    /**
     * Unused.
     */
    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return null;
    }

    /**
     * Unused.
     */
    @Override
    public void doRender(Entity entity, double var2, double var4, double var6, float var8, float var9)
    {
    }

}
