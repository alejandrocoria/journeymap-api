package journeymap.client.render.draw;

import com.google.common.base.Strings;
import journeymap.client.api.display.Context;
import journeymap.client.api.display.Overlay;
import journeymap.client.api.model.TextProperties;
import journeymap.client.api.util.UIState;
import journeymap.client.cartography.RGB;
import journeymap.client.render.map.GridRenderer;

import javax.annotation.Nullable;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.EnumSet;
import java.util.Objects;

/**
 * Reusable functionality for OverlayDrawStep implementations
 */
public abstract class BaseOverlayDrawStep<T extends Overlay> implements OverlayDrawStep
{
    public final T overlay;

    protected Rectangle2D.Double screenBounds = new Rectangle2D.Double();
    protected Point2D.Double titlePosition = null;
    protected UIState lastUiState = null;
    protected boolean dragging = false;
    protected boolean enabled = true;

    protected BaseOverlayDrawStep(T overlay)
    {
        this.overlay = overlay;
    }

    /**
     * Update positions of screenBounds and other points as needed.
     *
     * @param gridRenderer
     */
    protected abstract void updatePositions(GridRenderer gridRenderer);

    /**
     * Draw label and/or title
     *
     * @param xOffset
     * @param yOffset
     * @param gridRenderer
     * @param drawScale
     * @param fontScale
     * @param rotation
     */
    protected void drawText(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale, double rotation)
    {
        TextProperties textProperties = overlay.getTextProperties();
        EnumSet<Context.UI> activeUIs = textProperties.getActiveUIs();
        int zoom = gridRenderer.getZoom();
        boolean showLabel = (textProperties.getActiveUIs().contains(Context.UI.Any) || activeUIs.contains(Context.UI.Fullscreen))
                && (zoom >= textProperties.getMinZoom() && zoom <= textProperties.getMaxZoom());
        if (showLabel)
        {
            DrawUtil.drawLabel(overlay.getLabel(),
                    screenBounds.getCenterX() + xOffset,
                    gridRenderer.getHeight() - screenBounds.getMinY() - (screenBounds.getHeight() / 2.0) + yOffset,
                    DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle,
                    textProperties.getBackgroundColor(),
                    RGB.toClampedInt(textProperties.getBackgroundOpacity()),
                    textProperties.getColor(),
                    RGB.toClampedInt(textProperties.getOpacity()),
                    textProperties.getScale() * fontScale,
                    textProperties.hasFontShadow(),
                    rotation);

            if (titlePosition != null)
            {
                String title = overlay.getTitle();
                if (!Strings.isNullOrEmpty(title))
                {
                    DrawUtil.drawLabel(title,
                            titlePosition.x + 5 + xOffset,
                            gridRenderer.getHeight() - titlePosition.y + yOffset,
                            DrawUtil.HAlign.Right, DrawUtil.VAlign.Above,
                            textProperties.getBackgroundColor(),
                            RGB.toClampedInt(textProperties.getBackgroundOpacity()),
                            textProperties.getColor(),
                            RGB.toClampedInt(textProperties.getOpacity()),
                            textProperties.getScale() * fontScale,
                            textProperties.hasFontShadow(),
                            rotation);
                }
            }
        }
    }

    /**
     * Determines whether this is onscreen, updating cached values as needed.
     *
     * @param gridRenderer
     * @return false if not rendered
     */
    public boolean isOnScreen(double xOffset, double yOffset, GridRenderer gridRenderer)
    {
        if (!enabled)
        {
            return false;
        }

        UIState uiState = gridRenderer.getUIState();

        // Check active
        if (!overlay.isActiveIn(uiState))
        {
            // Not displayed
            return false;
        }

        // Check dragging
        boolean draggingDone = false;
        if (xOffset != 0 || yOffset != 0)
        {
            dragging = true;
        }
        else
        {
            draggingDone = dragging;
            dragging = false;
        }

        // Update positions after drag or if the UIState changed
        if (draggingDone || !Objects.equals(uiState, lastUiState))
        {
            // Update positions first
            lastUiState = uiState;
            updatePositions(gridRenderer);
        }

        // Verify screenbounds within grid
        return gridRenderer.isOnScreen(screenBounds);
    }

    @Override
    public void setTitlePosition(@Nullable Point2D.Double titlePosition)
    {
        this.titlePosition = titlePosition;
    }

    @Override
    public int getDisplayOrder()
    {
        return overlay.getDisplayOrder();
    }

    @Override
    public String getModId()
    {
        return overlay.getModId();
    }

    @Override
    public Rectangle2D.Double getBounds()
    {
        return screenBounds;
    }

    @Override
    public Overlay getOverlay()
    {
        return overlay;
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
}
