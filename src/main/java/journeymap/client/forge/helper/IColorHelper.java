/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.helper;

import journeymap.client.model.BlockMD;

import java.awt.*;

/**
 * Created by Mark on 7/12/2015.
 */
public interface IColorHelper
{
    boolean failedFor(BlockMD blockMD);

    Integer loadBlockColor(BlockMD blockMD);
}
