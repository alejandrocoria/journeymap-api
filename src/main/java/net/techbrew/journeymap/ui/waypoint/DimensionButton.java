package net.techbrew.journeymap.ui.waypoint;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.data.WorldData;
import net.techbrew.journeymap.ui.Button;

/**
 * Created by mwoodman on 3/5/14.
 */
public class DimensionButton extends Button {

    public final int dimension;
    public final String onString = Constants.getString("MapOverlay.on");
    public final String offString = Constants.getString("MapOverlay.off");

    DimensionButton(int id, final int dimension, boolean toggled)
    {
        this(id, dimension, WorldData.getDimensionName(dimension), toggled);
    }

    DimensionButton(int id, int dimension, String dimensionName, boolean toggled)
    {
        super(id, 0, 0, String.format("%s: %s", dimensionName, Constants.getString("MapOverlay.on")), String.format("%s: %s", dimensionName, Constants.getString("MapOverlay.off")), toggled);
        this.dimension = dimension;
        setToggled(toggled);
    }
}
