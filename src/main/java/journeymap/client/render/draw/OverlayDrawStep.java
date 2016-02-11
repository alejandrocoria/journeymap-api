package journeymap.client.render.draw;

import journeymap.client.api.display.Overlay;
import journeymap.client.render.map.GridRenderer;

import javax.annotation.Nullable;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * DrawStep specific to an overlay.
 */
public interface OverlayDrawStep extends DrawStep
{
    Overlay getOverlay();

    Rectangle2D.Double getBounds();

    boolean isOnScreen(double xOffset, double yOffset, GridRenderer gridRenderer);

    void setTitlePosition(@Nullable Point2D.Double titlePosition);
}
