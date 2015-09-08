/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package net.minecraft.client.renderer.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/**
 * Workaround to get the entity texture via the protected Render.getEntityTexture() method.
 */
public class RenderFacade extends Render
{
    // 1.7
    // public RenderFacade(Render unused)
    // 1.8
    public RenderFacade(RenderManager unused)
    {
        super();
    }

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
