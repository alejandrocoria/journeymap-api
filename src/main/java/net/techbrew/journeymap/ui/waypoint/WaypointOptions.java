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
    Button buttonEnable, buttonHide, buttonClose;
    TextField maxDistanceField;
    ButtonList listLeftButtons, listRightButtons;
    String labelMaxDistance = Constants.getString("Waypoint.max_distance");
    WaypointProperties props = JourneyMap.getInstance().waypointProperties;

    public WaypointOptions(Class<? extends JmUI> returnClass)
    {
        super(Constants.getString("Waypoint.options"), returnClass);
    }

    @Override
    public void initGui()
    {
        buttonList.clear();
        int id = 0;

        buttonEnable = BooleanPropertyButton.create(id++, "Waypoint.enable_beacons", props, props.beaconEnabled);
        buttonList.add(buttonEnable);

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

        buttonHide = new Button(id++, Constants.getString("JourneyMap.show_buttons"), Constants.getString("JourneyMap.hide_buttons"), false);
        buttonList.add(buttonHide);

        buttonClose = new Button(id++, Constants.getString("MapOverlay.close"));
        buttonClose.setWidth(buttonEnable.getWidth());
        buttonList.add(buttonClose);

        new ButtonList(buttonList).equalizeWidths(getFontRenderer());

        listLeftButtons.setNoDisableText(true);
        listRightButtons.setNoDisableText(true);

        updateButtons();
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
        int by = Math.max(30, (this.height-(8*24))/2);

        buttonEnable.centerHorizontalOn(bx).setY(by);
        by = buttonEnable.getBottomY() + vgap;

        int leftOffset = (hgap / 2) + listLeftButtons.get(0).getWidth();

        listLeftButtons.layoutVertical(bx - leftOffset, by, true, vgap);
        listRightButtons.layoutVertical(bx + (hgap / 2), by, true, vgap);

        // Max Distance Field
        maxDistanceField.setX(bx + hgap);
        maxDistanceField.setY(listLeftButtons.getBottomY() + vgap);
        maxDistanceField.setWidth(buttonEnable.getWidth() - hgap);
        maxDistanceField.drawTextBox();

        // Mask behind the label for max distance
        DrawUtil.drawRectangle(listLeftButtons.getLeftX(), maxDistanceField.getY(), listLeftButtons.get(0).getWidth(), maxDistanceField.getHeight(), Color.black, 150);

        // Label
        DrawUtil.drawLabel(labelMaxDistance, bx - hgap, maxDistanceField.getMiddleY(), DrawUtil.HAlign.Left, DrawUtil.VAlign.Middle, Color.BLACK, 0, Color.cyan, 255, 1, true);

        // Close
        buttonHide.setPosition(bx - leftOffset, maxDistanceField.getBottomY() + (vgap*4));
        buttonClose.alignTo(buttonHide, DrawUtil.HAlign.Right, hgap, DrawUtil.VAlign.Middle, vgap);
    }

    @Override
    public void drawDefaultBackground()
    {
        if(buttonHide.getToggled())
        {
            // Reinforce header since normal mask isn't used
            DrawUtil.drawRectangle(0, 0, this.width, headerHeight, Color.black, 100);

            // Add footer to mask the bottom toolbar
            DrawUtil.drawRectangle(0, this.height - headerHeight, this.width, headerHeight, Color.black, 150);
        }
        else
        {
            super.drawDefaultBackground();
        }

        // Show FPS
        String fps = mc.debug;
        final int idx = fps != null ? fps.indexOf(',') : -1;
        if (idx > 0)
        {
            fps = fps.substring(0, idx);
            DrawUtil.drawLabel(fps, width-5, 5, DrawUtil.HAlign.Left, DrawUtil.VAlign.Below, Color.BLACK, 0, Color.cyan, 255, 1, true);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button instanceof BooleanPropertyButton)
        {
            ((BooleanPropertyButton) button).toggle();
        }

        if(button.id == buttonEnable.id)
        {
            updateButtons();
            return;
        }

        if(button.id == buttonHide.id)
        {
            buttonHide.toggle();
            updateButtons();
            return;
        }

        if (button.id == buttonClose.id)
        {
            closeAndReturn();
        }
    }

    protected void updateButtons()
    {
        boolean enable = buttonEnable.getToggled();
        boolean show = !buttonHide.getToggled();

        listLeftButtons.setDefaultStyle(show);
        listLeftButtons.setOptions(enable, show, show);
        listRightButtons.setDefaultStyle(show);
        listRightButtons.setOptions(enable, show, show);

        maxDistanceField.setEnabled(enable);

        buttonEnable.setEnabled(true);
        buttonEnable.setDefaultStyle(show);
        buttonEnable.setDrawBackground(show);
        buttonEnable.setDrawFrame(show);
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

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        maxDistanceField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void close()
    {
        JourneyMap.getInstance().waypointProperties.save();
    }
}
