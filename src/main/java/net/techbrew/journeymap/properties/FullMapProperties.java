package net.techbrew.journeymap.properties;

/**
 * Properties for the full map in-game.
 */
public class FullMapProperties extends MapProperties
{
    protected transient static final int CURRENT_REVISION = 1;
    protected final String name = "fullmap";
    protected int revision = CURRENT_REVISION;

    protected boolean forceUnicode = false; // PREF_FORCEUNICODE(Boolean.class,"preference_forceunicode", false), //$NON-NLS-1$
    protected double fontScale = 1; // PREF_FONTSCALE(Double.class,"preference_fontscale", 1.0), //$NON-NLS-1$

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

        FullMapProperties that = (FullMapProperties) o;
        return 0 == this.compareTo(that);
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        long temp;
        result = 31 * result + name.hashCode();
        result = 31 * result + revision;
        result = 31 * result + (forceUnicode ? 1 : 0);
        temp = Double.doubleToLongBits(fontScale);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
