/*
 *
 * JourneyMap API
 * http://journeymap.info
 * http://bitbucket.org/TeamJM/journeymap-api
 *
 * Copyright (c) 2011-2015 Techbrew.  All Rights Reserved.
 * The following limited rights are granted to you:
 *
 * You MAY:
 *   + Write your own code that uses this API source code as a dependency.
 *   + Distribute compiled classes of unmodified API source code which your code depends upon.
 *   + Fork and modify API source code for the purpose of submitting Pull Requests to the
 *        TeamJM/journeymap-api repository.  Submitting new or modified code to the repository
 *        means that you are granting Techbrew all rights over the code.
 *
 * You MAY NOT:
 *   - Submit any code to the TeamJM/journeymap-api repository with a different license than this one.
 *   - Distribute modified versions of the API source code or compiled artifacts of  modified API
 *        source code.  In this context, "modified" means changes which have not been both approved
 *        and merged into the TeamJM/journeymap-api repository.
 *   - Use or distribute the API code in any way not explicitly granted by this license statement.
 *
 */

package journeymap.api.client.v1;

import com.google.common.base.Objects;
import com.google.common.base.Verify;

import java.util.List;

/**
 * Specification defining how a polygon map overlay should be displayed.
 */
public class PolygonSpec extends OverlayBase
{
    private String polygonId;
    private MapPolygon outerArea;
    private List<MapPolygon> holes;
    private float strokeWidth = 2;
    private int strokeColor = 0xffffff;
    private float strokeOpacity = 1;
    private int fillColor = 0x000000;
    private float fillOpacity = 0.5f;

    /**
     * Constructor.
     *
     * @param polygonId        A unique id for the polygon (scoped within your mod) which can be used to remove/update it.
     * @param overlayGroupName (Optional) A suggested group or category name used to organize map overlays.
     * @param outerArea        A polygon of the outer area to be displayed.
     */
    public PolygonSpec(String polygonId, String overlayGroupName, MapPolygon outerArea)
    {
        this(polygonId, overlayGroupName, null, null, outerArea, null);
    }

    /**
     * Constructor.
     *
     * @param polygonId        A unique id for the polygon (scoped within your mod) which can be used to remove/update it.
     * @param overlayGroupName (Optional) A suggested group or category name used to organize map overlays.
     * @param title            (Optional) Rollover text to be displayed when the mouse is over the overlay.
     * @param label            (Optional) Label text to be displayed on the polygon.
     * @param outerArea        A polygon of the outer area to be displayed.
     * @param holes            (Optional) A list of polygons treated as holes inside the outerArea
     */
    public PolygonSpec(String polygonId, String overlayGroupName, String title, String label, MapPolygon outerArea, List<MapPolygon> holes)
    {
        Verify.verifyNotNull(polygonId);
        this.polygonId = polygonId;
        Verify.verifyNotNull(polygonId);
        this.outerArea = outerArea;
        this.holes = holes;
        super.setOverlayGroupName(overlayGroupName);
        super.setTitle(title);
        super.setLabel(label);
    }

    /**
     * Sets the display characteristics of the polygon
     *
     * @param strokeWidth   Line thickness of the polygon edges.
     * @param strokeColor   Line color (rgb) of the polygon edges.
     * @param strokeOpacity Line opacity (between 0 and 1) of the polygon edges.
     * @param fillColor     Fill color (rgb) of the polygon.
     * @param fillOpacity   Fill opacity (between 0 and 1) of the polygon area.
     */
    public void setStyle(float strokeWidth, int strokeColor, float strokeOpacity, int fillColor, int fillOpacity)
    {
        this.strokeWidth = strokeWidth;
        this.strokeColor = strokeColor;
        this.strokeOpacity = strokeOpacity;
        this.fillColor = fillColor;
        this.fillOpacity = fillOpacity;
    }

    /**
     * A unique id for the polygon which can be used to remove/update it.
     *
     * @return the polygon id
     */
    public String getPolygonId()
    {
        return polygonId;
    }

    /**
     * A polygon of the outer area to be displayed.
     *
     * @return the outer area
     */
    public MapPolygon getOuterArea()
    {
        return outerArea;
    }

    /**
     * (optional) A list of polygons treated as holes inside the outerArea
     *
     * @return the holes
     */
    public List<MapPolygon> getHoles()
    {
        return holes;
    }

    /**
     * Line thickness of the polygon edges.
     *
     * @return the stroke width
     */
    public float getStrokeWidth()
    {
        return strokeWidth;
    }

    /**
     * Sets stroke width.
     *
     * @param strokeWidth the stroke width
     */
    public void setStrokeWidth(float strokeWidth)
    {
        this.strokeWidth = strokeWidth;
    }

    /**
     * Line color (rgb) of the polygon edges.
     *
     * @return the stroke color
     */
    public int getStrokeColor()
    {
        return strokeColor;
    }

    /**
     * Sets stroke color.
     *
     * @param strokeColor the stroke color
     */
    public void setStrokeColor(int strokeColor)
    {
        this.strokeColor = Math.max(0x000000, Math.min(strokeColor, 0xffffff));
    }

    /**
     * Line opacity (between 0 and 1) of the polygon edges.
     *
     * @return the stroke opacity
     */
    public float getStrokeOpacity()
    {
        return strokeOpacity;
    }

    /**
     * Sets stroke opacity.
     *
     * @param strokeOpacity the stroke opacity
     */
    public void setStrokeOpacity(float strokeOpacity)
    {
        this.strokeOpacity = Math.max(0, Math.min(strokeOpacity, 1));
        ;
    }

    /**
     * Fill color (rgb) of the polygon.
     *
     * @return the fill color
     */
    public int getFillColor()
    {
        return fillColor;
    }

    /**
     * Sets fill color.
     *
     * @param fillColor the fill color
     */
    public void setFillColor(int fillColor)
    {
        this.fillColor = Math.max(0x000000, Math.min(fillColor, 0xffffff));
    }

    /**
     * Fill opacity of the polygon (between 0 and 1).
     *
     * @return the fill opacity
     */
    public float getFillOpacity()
    {
        return fillOpacity;
    }

    /**
     * Sets fill opacity (between 0 and 1).
     *
     * @param fillOpacity the fill opacity
     */
    public void setFillOpacity(int fillOpacity)
    {
        this.fillOpacity = Math.max(0, Math.min(fillOpacity, 1));
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("polygonId", polygonId)
                .add("label", label)
                .add("title", title)
                .add("overlayGroupName", overlayGroupName)
                .add("outerArea", outerArea)
                .add("holes", holes)
                .add("fillColor", fillColor)
                .add("fillOpacity", fillOpacity)
                .add("strokeColor", strokeColor)
                .add("strokeOpacity", strokeOpacity)
                .add("strokeWidth", strokeWidth)
                .add("color", color)
                .add("inFullscreen", inFullscreen)
                .add("inMinimap", inMinimap)
                .add("inWebmap", inWebmap)
                .add("maxZoom", maxZoom)
                .add("minZoom", minZoom)
                .add("zIndex", zIndex)
                .toString();
    }
}
