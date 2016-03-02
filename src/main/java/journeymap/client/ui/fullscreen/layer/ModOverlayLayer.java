/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.fullscreen.layer;

import journeymap.client.api.display.IOverlayListener;
import journeymap.client.api.display.Overlay;
import journeymap.client.api.impl.ClientAPI;
import journeymap.client.api.util.UIState;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.draw.OverlayDrawStep;
import journeymap.client.render.map.GridRenderer;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Handles events for mod overlays.
 */
public class ModOverlayLayer implements LayerDelegate.Layer
{
    protected List<OverlayDrawStep> allDrawSteps = new ArrayList<OverlayDrawStep>();
    protected List<OverlayDrawStep> visibleSteps = new ArrayList<OverlayDrawStep>();
    protected List<OverlayDrawStep> touchedSteps = new ArrayList<OverlayDrawStep>();
    protected BlockPos lastCoord;
    protected Point2D.Double lastMousePosition;
    protected UIState lastUiState;
    protected boolean propagateClick;

    /**
     * Compares state of args to prior args and refreshes the relevant drawsteps
     * from mods, triggering events as needed.
     */
    private void ensureCurrent(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition, BlockPos blockCoord)
    {
        UIState currentUiState = gridRenderer.getUIState();
        boolean uiStateChange = !Objects.equals(lastUiState, currentUiState);

        if (uiStateChange || !Objects.equals(blockCoord, lastCoord) || lastMousePosition == null)
        {
            lastCoord = blockCoord;
            lastUiState = currentUiState;
            lastMousePosition = mousePosition;

            allDrawSteps.clear();
            ClientAPI.INSTANCE.getDrawSteps(allDrawSteps, currentUiState);

            updateOverlayState(gridRenderer, mousePosition, blockCoord, uiStateChange);
        }
    }

    @Override
    public List<DrawStep> onMouseMove(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition, BlockPos blockCoord, float fontScale)
    {
        ensureCurrent(mc, gridRenderer, mousePosition, blockCoord);

        Overlay overlay;
        IOverlayListener listener;

        if (!touchedSteps.isEmpty())
        {
            for (OverlayDrawStep overlayDrawStep : touchedSteps)
            {
                try
                {
                    overlay = overlayDrawStep.getOverlay();
                    listener = overlay.getOverlayListener();
                    fireOnMouseMove(listener, mousePosition, blockCoord);
                    overlayDrawStep.setTitlePosition(mousePosition);
                }
                catch (Throwable t)
                {
                    Journeymap.getLogger().error(t.getMessage(), t);
                }
            }
        }
        return Collections.emptyList();
    }


    @Override
    public List<DrawStep> onMouseClick(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition, BlockPos blockCoord, int button, boolean doubleClick, float fontScale)
    {
        ensureCurrent(mc, gridRenderer, mousePosition, blockCoord);

        Overlay overlay;
        IOverlayListener listener;

        propagateClick = true;
        if (!touchedSteps.isEmpty())
        {
            for (OverlayDrawStep overlayDrawStep : touchedSteps)
            {
                try
                {
                    overlay = overlayDrawStep.getOverlay();
                    listener = overlay.getOverlayListener();
                    if (listener != null)
                    {
                        boolean continueClick = fireOnMouseClick(listener, mousePosition, blockCoord, button, doubleClick);
                        overlayDrawStep.setTitlePosition(mousePosition);
                        if (!continueClick)
                        {
                            propagateClick = false;
                            break;
                        }
                    }
                }
                catch (Throwable t)
                {
                    Journeymap.getLogger().error(t.getMessage(), t);
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public boolean propagateClick()
    {
        return propagateClick;
    }

    /**
     * Organizes overlays by whether they're displayed and/or "touched" under the mouse.
     * Fires events on IOverlayListeners: onActivate, onMouseOut, onDeactivate
     */
    private void updateOverlayState(GridRenderer gridRenderer, Point2D.Double mousePosition, BlockPos blockCoord, boolean uiStateChange)
    {
        Rectangle2D.Double bounds;
        for (OverlayDrawStep overlayDrawStep : allDrawSteps)
        {
            Overlay overlay = overlayDrawStep.getOverlay();
            IOverlayListener listener = overlay.getOverlayListener();

            boolean currentlyActive = visibleSteps.contains(overlayDrawStep);
            boolean currentlyTouched = touchedSteps.contains(overlayDrawStep);

            if (overlayDrawStep.isOnScreen(0,0,gridRenderer, 0))
            {
                if (!currentlyActive)
                {
                    visibleSteps.add(overlayDrawStep);
                    fireActivate(listener);
                }
                else if (uiStateChange)
                {
                    fireActivate(listener);
                }

                bounds = overlayDrawStep.getBounds();
                if (bounds != null && bounds.contains(mousePosition))
                {
                    if (!currentlyTouched)
                    {
                        touchedSteps.add(overlayDrawStep);
                    }
                }
                else
                {
                    if (currentlyTouched)
                    {
                        touchedSteps.remove(overlayDrawStep);
                        overlayDrawStep.setTitlePosition(null);
                        fireOnMouseOut(listener, mousePosition, blockCoord);
                    }
                }
            }
            else
            {
                if (currentlyTouched)
                {
                    touchedSteps.remove(overlayDrawStep);
                    overlayDrawStep.setTitlePosition(null);
                    fireOnMouseOut(listener, mousePosition, blockCoord);
                }

                if (currentlyActive)
                {
                    visibleSteps.remove(overlayDrawStep);
                    overlayDrawStep.setTitlePosition(null);
                    fireDeActivate(listener);
                }
            }

        }
    }

    private void fireActivate(IOverlayListener listener)
    {
        if (listener != null)
        {
            try
            {
                listener.onActivate(lastUiState);
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
    }

    private void fireDeActivate(IOverlayListener listener)
    {
        if (listener != null)
        {
            try
            {
                listener.onDeactivate(lastUiState);
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
    }

    private void fireOnMouseMove(IOverlayListener listener, Point2D.Double mousePosition, BlockPos blockCoord)
    {
        if (listener != null)
        {
            try
            {
                listener.onMouseMove(lastUiState, mousePosition, blockCoord);
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
    }

    private boolean fireOnMouseClick(IOverlayListener listener, Point2D.Double mousePosition, BlockPos blockCoord, int button, boolean doubleClick)
    {
        if (listener != null)
        {
            try
            {
                return listener.onMouseClick(lastUiState, mousePosition, blockCoord, button, doubleClick);
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
        return true;
    }

    private void fireOnMouseOut(IOverlayListener listener, Point2D.Double mousePosition, BlockPos blockCoord)
    {
        if (listener != null)
        {
            try
            {
                listener.onMouseOut(lastUiState, mousePosition, blockCoord);
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
    }
}
