package journeymap.client.ui.serveroption;

import com.google.gson.JsonObject;
import journeymap.client.Constants;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.CheckBox;
import journeymap.client.ui.component.IntSliderButton;
import journeymap.client.ui.component.ListPropertyButton;
import journeymap.common.properties.Category;
import journeymap.common.properties.config.EnumField;
import journeymap.common.properties.config.IntegerField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.EnumSet;

import static journeymap.client.ui.serveroption.ServerOption.Option.NONE;
import static journeymap.client.ui.serveroption.ServerOptionsManager.formattedToolTipHeader;
import static journeymap.common.network.Constants.ENABLED;
import static journeymap.common.network.Constants.OP_TRACKING;
import static journeymap.common.network.Constants.TELEPORT;
import static journeymap.common.network.Constants.TRACKING;
import static journeymap.common.network.Constants.TRACKING_DEFUALT;
import static journeymap.common.network.Constants.TRACKING_MAX;
import static journeymap.common.network.Constants.TRACKING_MIN;
import static journeymap.common.network.Constants.TRACKING_UPDATE_TIME;
import static journeymap.common.network.Constants.USE_WORLD_ID;

public class TopBoxOptions implements Draw
{

    private ButtonList buttons;
    private JsonObject properties;
    private FontRenderer fontRenderer;

    public TopBoxOptions(JsonObject properties, FontRenderer fontRenderer)
    {
        this.fontRenderer = fontRenderer;
        this.properties = properties;
        this.buttons = buildButtons();
    }

    public ButtonList getButtons()
    {
        return this.buttons;
    }

    private ButtonList buildButtons()
    {
        ButtonList list = new ButtonList();
        // this is a check to see if we have a dim, or a global property. Global does not have an enable field.
        if (this.properties.get(ENABLED) != null)
        {
            list.add(checkBox("jm.server.edit.chkbox.enable", ENABLED, properties));
            CheckBox dimTeleportCheckBox = checkBox("jm.server.edit.chkbox.teleport", TELEPORT, properties);
            dimTeleportCheckBox.setTooltip(formattedToolTipHeader("jm.server.edit.chkbox.teleport") + Constants.getString("jm.server.edit.chkbox.teleport.dimension.tooltip") + "\n\n" + Constants.getString("jm.server.edit.chkbox.teleport.dimension.tooltip2"));
            list.add(dimTeleportCheckBox);
        }
        else
        {
            list.add(checkBox("jm.server.edit.chkbox.teleport", TELEPORT, properties));
            if (!FMLClientHandler.instance().getClient().isSingleplayer() || (Minecraft.getMinecraft().getIntegratedServer() != null && Minecraft.getMinecraft().getIntegratedServer().getPublic()))
            {
                if (!FMLClientHandler.instance().getClient().isSingleplayer())
                {
                    CheckBox worldIdCheckBox = checkBox("jm.server.edit.chkbox.world.id", USE_WORLD_ID, properties);
                    worldIdCheckBox.setTooltip(formattedToolTipHeader("jm.server.edit.chkbox.world.id") + Constants.getString("jm.server.edit.chkbox.world.id.tooltip") + "\n\n" + Constants.getString("jm.server.edit.chkbox.world.id.tooltip2") + "\n\n" + Constants.getString("jm.server.edit.chkbox.world.id.tooltip3"));
                    list.add(worldIdCheckBox);
                }
//                list.add(checkBox("jm.server.edit.chkbox.world.id" + "\n" + Constants.getString("jm.server.edit.chkbox.world.id.tooltip2") + "\n\n" + Constants.getString("jm.server.edit.chkbox.world.id.tooltip3"), USE_WORLD_ID, properties));

                //add tracking slider
                IntegerField sliderFieldValue = new IntegerField(Category.Hidden, "", TRACKING_MIN, TRACKING_MAX, TRACKING_DEFUALT);
                sliderFieldValue.set(properties.get(TRACKING_UPDATE_TIME).getAsInt());
                IntSliderButton trackingUpdateSlider = new IntSliderButton(sliderFieldValue, Constants.getString("jm.server.edit.slider.update.pre"), Constants.getString("jm.server.edit.slider.update.post"));
                trackingUpdateSlider.setTooltip(formattedToolTipHeader("jm.server.edit.tracking.update.label") + Constants.getString("jm.server.edit.slider.update.tooltip"));
                trackingUpdateSlider.visible = false;
                // Add tracking button
                ServerOption option = new ServerOption(TRACKING, properties);
                ListPropertyButton<ServerOption.Option> tracking;
                tracking = new ListPropertyButton<ServerOption.Option>(
                        EnumSet.allOf(ServerOption.Option.class),
                        Constants.getString("jm.server.edit.tracking.label"),
                        new EnumField<>(Category.Hidden, "", option.getOption()));
                tracking.addClickListener(button -> {
                    option.setOption(tracking.getField().get());
                    updateToggleProperty(option, this.properties, TRACKING, OP_TRACKING);
                    resetTrackingSlider(tracking.getField().get(), tracking, trackingUpdateSlider);
                    return true;
                });
                tracking.setTooltip(300,
                        formattedToolTipHeader("jm.server.edit.tracking.label") +
                                getToggleTooltipBase(),
                        Constants.getString("jm.server.edit.tracking.tooltip1"),
                        "",
                        Constants.getString("jm.server.edit.tracking.tooltip2"),
                        "",
                        Constants.getString("jm.server.edit.tracking.tooltip3"),
                        "",
                        Constants.getString("jm.server.edit.tracking.tooltip4")
                );

                trackingUpdateSlider.addClickListener(button -> {
                    this.properties.remove(TRACKING_UPDATE_TIME);
                    this.properties.addProperty(TRACKING_UPDATE_TIME, trackingUpdateSlider.getValue());
                    return true;
                });


                trackingUpdateSlider.setWidth(fontRenderer.getStringWidth(Constants.getString("jm.server.edit.slider.update.pre") + TRACKING_MAX + Constants.getString("jm.server.edit.slider.update.post")) + 10);
                resetTrackingSlider(tracking.getField().get(), tracking, trackingUpdateSlider);
                list.add(tracking);
                list.add(trackingUpdateSlider);
            }
        }
        return list;
    }

    private void resetTrackingSlider(ServerOption.Option options, ListPropertyButton tracking, IntSliderButton trackingUpdateSlider)
    {
        if (NONE.equals(options))
        {
            trackingUpdateSlider.setVisible(false);
        }
        else
        {
            trackingUpdateSlider.setVisible(true);
        }
        tracking.setWidth(fontRenderer.getStringWidth(Constants.getString("jm.server.edit.tracking.label") + " : * " + options.displayName() + " * ") + 10);
    }

    @Override
    public void draw(int startX, int startY, int gap)
    {
        buttons.layoutCenteredHorizontal(startX, startY + 5, true, gap - 2, true);
    }
}
