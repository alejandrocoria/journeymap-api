package net.techbrew.journeymap.properties;

import net.techbrew.journeymap.ui.minimap.DisplayVars;

/**
 * Properties for the minimap in-game.
 */
public class MiniMapProperties extends MapProperties
{
    protected transient static final int CURRENT_REVISION = 1;
    protected final String name = "minimap";
    protected int revision = CURRENT_REVISION;

    protected boolean enabled = true;
    protected String shape = DisplayVars.Shape.SmallSquare.name();
    protected String position = DisplayVars.Position.TopRight.name();
    protected boolean showFps = false;
    protected boolean enableHotkeys = true;

    protected boolean forceUnicode = false; // PREF_FORCEUNICODE(Boolean.class,"preference_forceunicode", false), //$NON-NLS-1$
    protected double fontScale = 1; // PREF_FONTSCALE(Double.class,"preference_fontscale", 1.0), //$NON-NLS-1$



    protected boolean showWaypointLabels;

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

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }


    public boolean isForceUnicode()
    {
        return forceUnicode;
    }

    public void setForceUnicode(boolean forceUnicode)
    {
        this.forceUnicode = forceUnicode;
        save();
    }

    public boolean toggleForceUnicode()
    {
        setForceUnicode(!forceUnicode);
        return forceUnicode;
    }

    public double getFontScale()
    {
        return fontScale;
    }

    public void setFontScale(double fontScale)
    {
        this.fontScale = fontScale;
        save();
    }

    public String getShape()
    {
        return shape;
    }

    public void setShape(String shape)
    {
        this.shape = shape;
    }

    public String getPosition()
    {
        return position;
    }

    public void setPosition(String position)
    {
        this.position = position;
    }

    public boolean isShowFps()
    {
        return showFps;
    }

    public void setShowFps(boolean showFps)
    {
        this.showFps = showFps;
    }

    public boolean isEnableHotkeys()
    {
        return enableHotkeys;
    }

    public void setEnableHotkeys(boolean enableHotkeys)
    {
        this.enableHotkeys = enableHotkeys;
    }

    public boolean isShowWaypointLabels()
    {
        return showWaypointLabels;
    }

    public void setShowWaypointLabels(boolean showWaypointLabels)
    {
        this.showWaypointLabels = showWaypointLabels;
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
        long temp;
        result = 31 * result + name.hashCode();
        result = 31 * result + revision;
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + shape.hashCode();
        result = 31 * result + position.hashCode();
        result = 31 * result + (showFps ? 1 : 0);
        result = 31 * result + (enableHotkeys ? 1 : 0);
        result = 31 * result + (forceUnicode ? 1 : 0);
        temp = Double.doubleToLongBits(fontScale);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (showWaypointLabels ? 1 : 0);
        return result;
    }
}
