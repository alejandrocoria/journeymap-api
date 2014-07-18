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

public class RenderFacade extends Render
{

    final Render render;

    public RenderFacade(Render render)
    {
        this.render = render;
    }

    public static ResourceLocation getEntityTexture(Render render, Entity entity)
    {
        return render.getEntityTexture(entity);
    }

    @Override
    public void doRender(Entity entity, double var2, double var4, double var6, float var8, float var9)
    {
        this.doRender(entity, var2, var4, var6, var8, var9);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return this.render.getEntityTexture(entity);
    }

}
