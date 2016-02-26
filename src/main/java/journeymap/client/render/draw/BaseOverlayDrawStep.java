package journeymap.client.render.draw;

import com.google.common.base.Strings;
import journeymap.client.api.display.Overlay;
import journeymap.client.api.model.TextProperties;
import journeymap.client.api.util.UIState;
import journeymap.client.render.map.GridRenderer;

import javax.annotation.Nullable;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

/**
 * Reusable functionality for OverlayDrawStep implementations
 */
public abstract class BaseOverlayDrawStep<T extends Overlay> implements OverlayDrawStep
{
    public final T overlay;

    protected Rectangle2D.Double screenBounds = new Rectangle2D.Double();
    protected Point2D.Double titlePosition = new Point2D.Double();
    protected Point2D.Double labelPosition = new Point2D.Double();
    protected UIState lastUiState = null;
    protected boolean dragging = false;
    protected boolean enabled = true;

    protected BaseOverlayDrawStep(T overlay)
    {
        this.overlay = overlay;
    }

    /**
     * Update positions of screenBounds, labelPosition, and other points as needed.
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

        if (textProperties.isActiveIn(gridRenderer.getUIState()))
        {
            String labelText = overlay.getLabel();
            if (labelPosition != null && !Strings.isNullOrEmpty(labelText))
            {
                DrawUtil.drawLabel(overlay.getLabel(),
                        labelPosition.x + xOffset,
                        labelPosition.y + yOffset,
                        DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle,
                        textProperties.getBackgroundColor(),
                        textProperties.getBackgroundOpacity(),
                        textProperties.getColor(),
                        textProperties.getOpacity(),
                        textProperties.getScale() * fontScale,
                        textProperties.hasFontShadow(),
                        rotation);
            }

            String titleText = overlay.getTitle();
            if (titlePosition != null && !Strings.isNullOrEmpty(titleText))
            {
                DrawUtil.drawLabel(titleText,
                        titlePosition.x + 5 + xOffset,
                        titlePosition.y + yOffset,
                        DrawUtil.HAlign.Right, DrawUtil.VAlign.Above,
                        textProperties.getBackgroundColor(),
                        textProperties.getBackgroundOpacity(),
                        textProperties.getColor(),
                        textProperties.getOpacity(),
                        textProperties.getScale() * fontScale,
                        textProperties.hasFontShadow(),
                        rotation);
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
        if (draggingDone || overlay.getNeedsRerender() || !Objects.equals(uiState, lastUiState))
        {
            // Update positions first
            lastUiState = uiState;
            updatePositions(gridRenderer);
            overlay.clearFlagForRerender();
        }

        // Verify screenbounds within grid
        if (screenBounds == null)
        {
            return false;
        }
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
