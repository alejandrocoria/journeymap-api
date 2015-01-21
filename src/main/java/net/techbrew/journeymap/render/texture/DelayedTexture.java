/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.render.texture;


import java.awt.image.BufferedImage;

/**
 * Supports deferred bindings.
 */
public class DelayedTexture extends TextureImpl
{
    /**
     * Safe to call on non-GL Context thread, but bindTexture() must be called later.
     */
    public DelayedTexture()
    {
        super(null, null, false, false);
    }

    /**
     * Safe to call on non-GL Context thread, but bindTexture() must be called later.
     */
    public DelayedTexture(BufferedImage image)
    {
        super(null, image, false, false);
    }

    /**
     * Safe to call on non-GL Context thread, but bindTexture() must be called later.
     */
    public DelayedTexture(Integer glID, BufferedImage image)
    {
        super(glID, image, false, false);
    }
}
