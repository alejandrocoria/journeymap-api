package journeymap.client.api.impl;

import com.google.common.base.Objects;
import journeymap.client.api.display.Displayable;

/**
 * Pairs a Displayable (API) object with an internal object actually used for rendering.
 */
public class DisplayablePair<D extends Displayable, O>
{
    private D displayable;
    private O internal;

    public DisplayablePair(D displayable)
    {
        setDisplayable(displayable);
    }

    public D getDisplayable()
    {
        return displayable;
    }

    public DisplayablePair setDisplayable(D displayable)
    {
        this.displayable = displayable;
        return this;
    }

    public O getInternal()
    {
        return internal;
    }

    public DisplayablePair setInternal(O internal)
    {
        this.internal = internal;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof DisplayablePair))
        {
            return false;
        }
        DisplayablePair<?, ?> that = (DisplayablePair<?, ?>) o;
        return Objects.equal(displayable, that.displayable);
    }

    @Override
    public int hashCode()
    {
        return 31 * displayable.hashCode();
    }
}
