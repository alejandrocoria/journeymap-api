package net.techbrew.journeymap.ui.waypoint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.ScrollPane;

/**
 * Created by Mark on 3/15/14.
 */
public class WaypointManagerItem implements ScrollPane.Scrollable {

    int x;
    int y;
    int width;
    int height;

    final Waypoint waypoint;
    final FontRenderer fontRenderer;

    Button buttonEnable;
    Button buttonRemove;
    Button buttonEdit;

    public WaypointManagerItem(Waypoint waypoint, FontRenderer fontRenderer)
    {
        this.waypoint = waypoint;
        this.fontRenderer = fontRenderer;

        String on = Constants.getString("MapOverlay.on");
        String off = Constants.getString("MapOverlay.off");
        String enableOn = Constants.getString("Waypoint.enable", on);
        String enableOff = Constants.getString("Waypoint.enable", off);

        buttonEnable = new Button(0,0,0, enableOn, enableOff, true); //$NON-NLS-1$
        buttonEnable.setToggled(waypoint.getEnable());
        buttonEnable.fitWidth(fontRenderer);

        buttonRemove = new Button(1,0,0, Constants.getString("Waypoint.remove")); //$NON-NLS-1$
        buttonRemove.fitWidth(fontRenderer);
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getFitWidth(FontRenderer fr) {
        // TODO
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void drawScrollable(Minecraft mc, int mouseX, int mouseY) {


    }

    @Override
    public void drawPartialScrollable(Minecraft mc, int x, int y, int width, int height) {

    }

    @Override
    public void clickScrollable(Minecraft mc, int mouseX, int mouseY) {

    }
}
