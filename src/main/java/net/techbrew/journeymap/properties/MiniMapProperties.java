/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.properties;

import net.techbrew.journeymap.ui.minimap.DisplayVars;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Properties for the minimap in-game.
 */
public class MiniMapProperties extends InGameMapProperties
{
    protected transient static final int CODE_REVISION = 4;
    public final AtomicBoolean enabled = new AtomicBoolean(true);
    public final AtomicReference<DisplayVars.Shape> shape = new AtomicReference<DisplayVars.Shape>(DisplayVars.Shape.SmallSquare);
    public final AtomicReference<DisplayVars.Position> position = new AtomicReference<DisplayVars.Position>(DisplayVars.Position.TopRight);
    public final AtomicBoolean showFps = new AtomicBoolean(false);
    public final AtomicBoolean enableHotkeys = new AtomicBoolean(true);
    public final AtomicBoolean showWaypointLabels = new AtomicBoolean(true);
    public final AtomicReference<String> entityIconSetName = new AtomicReference<String>("2D");
    public final AtomicInteger customSize = new AtomicInteger(0);
    public final AtomicInteger frameAlpha = new AtomicInteger(255);
    public final AtomicBoolean rotateHeading = new AtomicBoolean(true);
    protected transient final String name = "minimap";

    public MiniMapProperties()
    {
    }

    @Override
    public AtomicReference<String> getEntityIconSetName()
    {
        return entityIconSetName;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int getCodeRevision()
    {
        return CODE_REVISION;
    }


    @Override
    protected boolean validate()
    {
        boolean saveNeeded = super.validate();

        if (frameAlpha.get() < 0)
        {
            frameAlpha.set(0);
            saveNeeded = true;
        }
        else if (frameAlpha.get() > 255)
        {
            frameAlpha.set(255);
            saveNeeded = true;
        }

        if (customSize.get() == 0)
        {
            int inferredSize;
            switch(this.shape.get())
            {
                case SmallSquare:
                    inferredSize = 128;
                    break;
                case MediumSquare:
                    inferredSize = 256;
                    break;
                case LargeSquare:
                    inferredSize = 512;
                    break;
                case SmallCircle:
                    inferredSize = 128;
                    break;
                case LargeCircle:
                    inferredSize = 512;
                    break;
                default:
                    inferredSize = 256;
            }
            this.customSize.set(inferredSize);
            saveNeeded = true;
        }

        return saveNeeded;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        MiniMapProperties that = (MiniMapProperties) o;
        return 0 == this.compareTo(that);
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + fileRevision;
        result = 31 * result + enabled.hashCode();
        result = 31 * result + shape.hashCode();
        result = 31 * result + position.hashCode();
        result = 31 * result + showFps.hashCode();
        result = 31 * result + enableHotkeys.hashCode();
        result = 31 * result + showWaypointLabels.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "MiniMapProperties: " +
                "fileRevision=" + fileRevision +
                ", showSelf=" + showSelf +
                ", showMobs=" + showMobs +
                ", showAnimals=" + showAnimals +
                ", showVillagers=" + showVillagers +
                ", showPets=" + showPets +
                ", showPlayers=" + showPlayers +
                ", showWaypoints=" + showWaypoints +
                ", managerEnabled=" + enabled +
                ", shape=" + shape +
                ", position=" + position +
                ", showFps=" + showFps +
                ", enableHotkeys=" + enableHotkeys +
                ", showWaypointLabels=" + showWaypointLabels +
                ", forceUnicode=" + forceUnicode +
                ", fontSmall=" + fontSmall +
                ", textureSmall=" + textureSmall +
                ", terrainAlpha=" + terrainAlpha +
                ", entityIconSetName=" + entityIconSetName;
    }
}
