package journeymap.client.render.map;

import journeymap.client.api.display.Context;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.impl.ClientAPI;
import journeymap.client.api.model.MapPolygon;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.model.TextProperties;
import journeymap.client.io.nbt.RegionLoader;
import journeymap.client.model.RegionCoord;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;
import java.util.Stack;

/**
 * Test class for displaying regions and the region locations.
 * It's kind of buggy, the map needs to be moved a bit for this to be activated.
 * <p>
 * Move map a bit then activate, it will display. Do not move the map and activate, it will not display.
 */
public class RegionRenderer
{
    private static RegionRenderer instance;
    public static boolean TOGGLED = false;

    private RegionRenderer()
    {
    }

    public static void render(boolean toggled)
    {
        if (instance == null)
        {
            instance = new RegionRenderer();
        }
        TOGGLED = toggled;
        if (toggled)
        {
            ClientAPI.INSTANCE.flagOverlaysForRerender();
            for (RegionCoord rc : instance.getRegions())
            {
                PolygonOverlay overlay = instance.createOverlay(rc);
                ClientAPI.INSTANCE.show(overlay);
            }
        }
        else
        {
            ClientAPI.INSTANCE.removeAll(Journeymap.MOD_ID, DisplayType.Polygon);
        }
    }

    private Stack<RegionCoord> getRegions()
    {
        try
        {
            Minecraft minecraft = Minecraft.getMinecraft();
            RegionLoader regionLoader = new RegionLoader(minecraft, Fullscreen.state().getMapType(), true);
            return regionLoader.getRegions();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to load regions", e);
        }
    }

    protected PolygonOverlay createOverlay(RegionCoord rCoord)
    {
        String displayId = "Region Display" + rCoord;
        String groupName = "Region";
        String label = "x:" + rCoord.regionX + ", z:" + rCoord.regionZ;

        // Style the polygon
        ShapeProperties shapeProps = new ShapeProperties()
                .setStrokeWidth(2)
                .setStrokeColor(0xff0000)
                .setStrokeOpacity(.7f)
                .setFillOpacity(.2f);

        // Style the text
        TextProperties textProps = new TextProperties()
                .setBackgroundColor(0x000022)
                .setBackgroundOpacity(.5f)
                .setColor(0x00ff00)
                .setOpacity(1f)
                .setFontShadow(true);

        // Define the shape
        int x = rCoord.getMinChunkX() << 4;
        int y = 70;
        int z = rCoord.getMinChunkZ() << 4;
        int maxX = (rCoord.getMaxChunkX() << 4) + 15;
        int maxZ = (rCoord.getMaxChunkZ() << 4) + 15;
        BlockPos sw = new BlockPos(x + 1, y, maxZ + 1);
        BlockPos se = new BlockPos(maxX + 1, y, maxZ + 1);
        BlockPos ne = new BlockPos(maxX + 1, y, z + 1);
        BlockPos nw = new BlockPos(x + 1, y, z + 1);
        MapPolygon regionPolygon = new MapPolygon(sw, se, ne, nw);

        // Create the overlay
        PolygonOverlay overlay = new PolygonOverlay(Journeymap.MOD_ID, displayId, rCoord.dimension, shapeProps, regionPolygon);

        // Set the text
        overlay.setOverlayGroupName(groupName)
                .setLabel(label)
                .setTextProperties(textProps)
                .setActiveUIs(EnumSet.of(Context.UI.Fullscreen, Context.UI.Webmap))
                .setActiveMapTypes(EnumSet.of(Context.MapType.Any));

        return overlay;
    }
}
