package journeymap.client.api.impl;

import com.google.common.base.Objects;
import com.google.common.collect.HashBasedTable;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.Displayable;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates objects passed to the ClientAPI for a specific mod.
 */
public class ModObjects
{
    private final String modId;

    private final HashBasedTable<DisplayType, String, DisplayablePair> table = HashBasedTable.create(DisplayType.values().length, 4);

    public ModObjects(String modId)
    {
        this.modId = modId;
    }

    public DisplayablePair add(Displayable displayable)
    {
        return ensure(displayable).setInternal(null);
    }

    private DisplayablePair ensure(Displayable displayable)
    {
        String displayId = displayable.getDisplayId();
        DisplayType displayType = DisplayType.of(displayable.getClass());

        DisplayablePair displayablePair = null;
        synchronized (table)
        {
            displayablePair = table.get(displayType, displayId);
            if (displayablePair == null)
            {
                displayablePair = new DisplayablePair(displayable);
                table.put(displayType, displayId, displayablePair);
            }
        }
        return displayablePair;
    }

    public DisplayablePair get(DisplayType displayType, String displayId)
    {
        return table.get(displayType, displayId);
    }

    public void remove(Displayable displayable)
    {
        table.remove(DisplayType.of(displayable.getClass()), displayable.getDisplayId());
    }

    public void remove(DisplayType displayType, String displayId)
    {
        table.remove(displayType, displayId);
    }

    public void removeAll(DisplayType displayType)
    {
        table.row(displayType).clear();
    }

    public boolean exists(DisplayType displayType, String displayId)
    {
        return table.row(displayType).containsKey(displayId);
    }

    public List<DisplayablePair> getDisplayablePairs(DisplayType displayType)
    {
        return new ArrayList<DisplayablePair>(table.row(displayType).values());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ModObjects))
        {
            return false;
        }
        ModObjects that = (ModObjects) o;
        return Objects.equal(modId, that.modId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(modId);
    }
}
