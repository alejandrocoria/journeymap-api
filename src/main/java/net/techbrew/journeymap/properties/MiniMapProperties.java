package net.techbrew.journeymap.properties;

import net.techbrew.journeymap.ui.minimap.DisplayVars;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Properties for the minimap in-game.
 */
public class MiniMapProperties extends MapProperties
{
    protected transient static final int CURRENT_REVISION = 1;
    protected final String name = "minimap";
    protected int revision = CURRENT_REVISION;

    public final AtomicBoolean enabled = new AtomicBoolean(true);
    public final AtomicReference<DisplayVars.Shape> shape = new AtomicReference<DisplayVars.Shape>(DisplayVars.Shape.SmallSquare);
    public final AtomicReference<DisplayVars.Position> position = new AtomicReference<DisplayVars.Position>(DisplayVars.Position.TopRight);
    public final AtomicBoolean showFps = new AtomicBoolean(false);
    public final AtomicBoolean enableHotkeys = new AtomicBoolean(true);
    public final AtomicBoolean showWaypointLabels = new AtomicBoolean(true);
    public final AtomicBoolean forceUnicode = new AtomicBoolean(false);
    public final AtomicBoolean fontSmall = new AtomicBoolean(true);

    @Override
    protected String getName()
    {
        return name;
    }

    @Override
    public int getCurrentRevision()
    {
        return CURRENT_REVISION;
    }

    @Override
    public int getRevision()
    {
        return revision;
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
        result = 31 * result + revision;
        result = 31 * result + enabled.hashCode();
        result = 31 * result + shape.hashCode();
        result = 31 * result + position.hashCode();
        result = 31 * result + showFps.hashCode();
        result = 31 * result + enableHotkeys.hashCode();
        result = 31 * result + showWaypointLabels.hashCode();
        result = 31 * result + forceUnicode.hashCode();
        result = 31 * result + fontSmall.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "MiniMapProperties: " +
                "revision=" + revision +
                ", showSelf=" + showSelf +
                ", showCaves=" + showCaves +
                ", showMobs=" + showMobs +
                ", showAnimals=" + showAnimals +
                ", showVillagers=" + showVillagers +
                ", showPets=" + showPets +
                ", showPlayers=" + showPlayers +
                ", showWaypoints=" + showWaypoints +
                ", showGrid=" + showGrid +
                ", enabled=" + enabled +
                ", shape=" + shape +
                ", position=" + position +
                ", showFps=" + showFps +
                ", enableHotkeys=" + enableHotkeys +
                ", showWaypointLabels=" + showWaypointLabels +
                ", forceUnicode=" + forceUnicode +
                ", fontSmall=" + fontSmall;
    }
}
