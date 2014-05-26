package net.techbrew.journeymap.ui.waypoint;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.properties.WaypointProperties;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.ui.*;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.TextField;

import java.awt.*;

/**
 * Waypoint options UI
 */
public class WaypointOptions extends JmUI
{
    Button buttonClose;
    TextField maxDistanceField;
    ButtonList listLeftButtons, listRightButtons;
    String labelMaxDistance = Constants.getString("Waypoint.max_distance");

    public WaypointOptions(Class<? extends JmUI> returnClass)
    {
        super(Constants.getString("Waypoint.options"), returnClass);
    }

    @Override
    public void initGui()
    {
        WaypointProperties props = JourneyMap.getInstance().waypointProperties;
        int id = 0;

        buttonClose = new Button(id++, Constants.getString("MapOverlay.close"));
        buttonList.add(buttonClose);

        listLeftButtons = new ButtonList(
                BooleanPropertyButton.create(id++, "Waypoint.auto_hide_label", props, props.autoHideLabel),
                BooleanPropertyButton.create(id++, "Waypoint.show_static_beam", props, props.showStaticBeam),
                BooleanPropertyButton.create(id++, "Waypoint.show_rotating_beam", props, props.showRotatingBeam),
                BooleanPropertyButton.create(id++, "Waypoint.show_texture", props, props.showTexture),
                BooleanPropertyButton.create(id++, BooleanPropertyButton.Type.SmallLarge, "Waypoint.texture_size", props, props.textureSmall)
        );
        buttonList.addAll(listLeftButtons);

        listRightButtons = new ButtonList(
                BooleanPropertyButton.create(id++, "Waypoint.show_name", props, props.showName),
                BooleanPropertyButton.create(id++, "Waypoint.show_distance", props, props.showDistance),
                BooleanPropertyButton.create(id++, "Waypoint.bold_label", props, props.boldLabel),
                BooleanPropertyButton.create(id++, "Waypoint.force_unicode", props, props.forceUnicode),
                BooleanPropertyButton.create(id++, BooleanPropertyButton.Type.SmallLarge, "Waypoint.font_size", props, props.fontSmall)
        );
        buttonList.addAll(listRightButtons);

        maxDistanceField = new TextField(props.maxDistance.toString(), getFontRenderer(), 100, 20, true, true);
        maxDistanceField.setClamp(-1, 10000);

        ButtonList.equalizeWidths(getFontRenderer(), buttonList);
    }

    @Override
    protected void layoutButtons()
    {
        if (buttonList.isEmpty())
        {
            initGui();
        }

        final int hgap = 4;
        final int vgap = 3;
        final int bx = this.width / 2;
        int by = Math.max(50, this.height / 6);

        int leftOffset = (hgap / 2) + listLeftButtons.get(0).getWidth();

        listLeftButtons.layoutVertical(bx - leftOffset, by, true, vgap);
        listRightButtons.layoutVertical(bx + (hgap / 2), by, true, vgap);

        DrawUtil.drawLabel(labelMaxDistance, bx - hgap, listLeftButtons.getBottomY() + 12, DrawUtil.HAlign.Left, DrawUtil.VAlign.Below, Color.BLACK, 0, Color.cyan, 255, 1, true);
        maxDistanceField.setX(bx + hgap);
        maxDistanceField.setY(listLeftButtons.getBottomY() + 10);
        maxDistanceField.setWidth(listRightButtons.get(0).getWidth() - hgap);
        maxDistanceField.drawTextBox();

        // Close
        buttonClose.centerHorizontalOn(width / 2).setY(maxDistanceField.getY() + maxDistanceField.getHeight() + vgap + vgap + vgap);
    }

    @Override
    public void drawDefaultBackground()
    {
        DrawUtil.drawRectangle(0, 0, this.width, headerHeight, Color.black, 100);

        DrawUtil.drawRectangle(0, this.height - headerHeight, this.width, headerHeight, Color.black, 150);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button instanceof BooleanPropertyButton)
        {
            ((BooleanPropertyButton) button).toggle();
        }

        if (button.id == buttonClose.id)
        {
            closeAndReturn();
        }
    }

    @Override
    protected void keyTyped(char par1, int par2)
    {
        if (maxDistanceField.textboxKeyTyped(par1, par2))
        {
            Integer maxDistance = maxDistanceField.clamp();
            if (maxDistance != null)
            {
                JourneyMap.getInstance().waypointProperties.maxDistance.set(maxDistance);
                // Don't save until close()
            }
            return;
        }

        super.keyTyped(par1, par2);
    }

    public void close()
    {
        JourneyMap.getInstance().waypointProperties.save();
    }
}
