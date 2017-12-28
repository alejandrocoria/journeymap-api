/*
 * JourneyMap API (http://journeymap.info)
 * http://bitbucket.org/TeamJM/journeymap-api
 *
 * Copyright (c) 2011-2016 Techbrew.  All Rights Reserved.
 * The following limited rights are granted to you:
 *
 * You MAY:
 *  + Write your own code that uses the API source code in journeymap.* packages as a dependency.
 *  + Write and distribute your own code that uses, modifies, or extends the example source code in example.* packages
 *  + Fork and modify any source code for the purpose of submitting Pull Requests to the TeamJM/journeymap-api repository.
 *    Submitting new or modified code to the repository means that you are granting Techbrew all rights to the submitted code.
 *
 * You MAY NOT:
 *  - Distribute source code or classes (whether modified or not) from journeymap.* packages.
 *  - Submit any code to the TeamJM/journeymap-api repository with a different license than this one.
 *  - Use code or artifacts from the repository in any way not explicitly granted by this license.
 *
 */

package journeymap.client.api.model;

import com.google.common.base.Objects;
import journeymap.client.api.util.UIState;
import journeymap.common.api.feature.Feature;

import java.util.EnumSet;

/**
 * Extends MapText to indicate which UIs and MapTypes it may appear in.
 * <p>
 * Setters use the Builder pattern so they can be chained.
 */
public class TextProperties extends MapText<TextProperties>
{
    protected EnumSet<Feature.Display> activeUIs = EnumSet.allOf(Feature.Display.class);
    protected EnumSet<Feature.MapType> activeMapTypes = EnumSet.allOf(Feature.MapType.class);

    /**
     * Default constructor.
     */
    public TextProperties()
    {

    }

    /**
     * Constructor to copy another instance.
     * @param other text properties
     */
    public TextProperties(TextProperties other)
    {
        super(other);
        this.activeUIs = EnumSet.copyOf(other.activeUIs);
        this.activeMapTypes = EnumSet.copyOf(other.activeMapTypes);
    }

    /**
     * Returns a set of enums indicating which JourneyMap UIs (Fullscreen, Minimap, Webmap)
     * the text should be displayed in.  This is only checked if the overlay containing these
     * text properties is already active.
     * <p>
     * For example, this can be specified to have labels only displayed in the fullscreen map, but not the minimap.
     * @return enumset
     */
    public EnumSet<Feature.Display> getActiveUIs()
    {
        return activeUIs;
    }

    /**
     * Set of enums indicating which JourneyMap UIs (Fullscreen, Minimap, Webmap) the text should be displayed in.
     * This is only checked if the overlay containing these text properties is already active.
     * <p>
     * For example, this can be specified to have labels only displayed in the fullscreen map, but not the minimap.
     * @param activeUIs active UIs
     * @return this
     */
    public TextProperties setActiveUIs(EnumSet<Feature.Display> activeUIs)
    {
        this.activeUIs = EnumSet.noneOf(Feature.Display.class);
        this.activeUIs.addAll(activeUIs);
        return this;
    }

    /**
     * Returns a set of enums indicating which map types (Day, Night) the text should be active in.
     *
     * @return enumset
     */
    public EnumSet<Feature.MapType> getActiveMapTypes()
    {
        return activeMapTypes;
    }

    /**
     * Set of enums indicating which JourneyMap map types (Day, Night) the text should be active in.
     *
     * @param activeMapTypes active types
     * @return this
     */
    public TextProperties setActiveMapTypes(EnumSet<Feature.MapType> activeMapTypes)
    {
        this.activeMapTypes = EnumSet.noneOf(Feature.MapType.class);
        this.activeMapTypes.addAll(activeMapTypes);
        return this;
    }

    /**
     * Whether the overlay should be active for the given contexts.
     *
     * @param uiState UIState
     * @return true if the overlay should be active
     */
    public boolean isActiveIn(UIState uiState)
    {
        return uiState.active
                && (activeUIs.contains(uiState.ui))
                && (activeMapTypes.contains(uiState.mapType))
                && (this.minZoom <= uiState.zoom && this.maxZoom >= uiState.zoom);
    }

    /**
     * The minimum zoom level (0 is lowest) where the polygon should be visible.
     *
     * @return the min zoom
     */
    public int getMinZoom()
    {
        return minZoom;
    }

    /**
     * Sets the minimum zoom level (0 is lowest) where text should be visible.
     *
     * @param minZoom the min zoom
     * @return this
     */
    public TextProperties setMinZoom(int minZoom)
    {
        this.minZoom = Math.max(0, minZoom);
        return this;
    }

    /**
     * The maximum zoom level (8 is highest) where text should be visible.
     *
     * @return the max zoom
     */
    public int getMaxZoom()
    {
        return maxZoom;
    }

    /**
     * Sets the maximum zoom level (8 is highest) where the polygon should be visible.
     *
     * @param maxZoom the max zoom
     * @return this
     */
    public TextProperties setMaxZoom(int maxZoom)
    {
        this.maxZoom = Math.min(8, maxZoom);
        return this;
    }

    /**
     * Gets how many horizontal pixels to shift the center of the label from the center of the overlay.
     * (For MarkerOverlays, the "center" is directly over MarkerOverlay.getPoint(), regardless of how
     * it's icon is placed.)
     *
     * @return pixels to offset
     */
    public int getOffsetX()
    {
        return offsetX;
    }

    /**
     * Sets how many horizontal pixels to shift the center of the label from the center of the overlay.
     * (For MarkerOverlays, the "center" is directly over MarkerOverlay.getPoint(), regardless of how
     * it's icon is placed.)
     *
     * @param offsetX pixels
     * @return this
     */
    public TextProperties setOffsetX(int offsetX)
    {
        this.offsetX = offsetX;
        return this;
    }

    /**
     * Gets how many vertical pixels to shift the center of the label from the center of the overlay.
     * (For MarkerOverlays, the "center" is directly over MarkerOverlay.getPoint(), regardless of how
     * it's icon is placed.)
     *
     * @return pixels to offset
     */
    public int getOffsetY()
    {
        return offsetY;
    }

    /**
     * Sets how many vertical pixels to shift the center of the label from the center of the overlay.
     * (For MarkerOverlays, the "center" is directly over MarkerOverlay.getPoint(), regardless of how
     * it's icon is placed.)
     *
     * @param offsetY pixels
     * @return this
     */
    public TextProperties setOffsetY(int offsetY)
    {
        this.offsetY = offsetY;
        return this;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("activeMapTypes", activeMapTypes)
                .add("activeUIs", activeUIs)
                .add("backgroundColor", backgroundColor)
                .add("backgroundOpacity", backgroundOpacity)
                .add("color", color)
                .add("opacity", opacity)
                .add("fontShadow", fontShadow)
                .add("maxZoom", maxZoom)
                .add("minZoom", minZoom)
                .add("offsetX", offsetX)
                .add("offsetY", offsetY)
                .add("scale", scale)
                .toString();
    }
}
