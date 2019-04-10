package journeymap.client.ui.serveroption;

import com.google.gson.JsonObject;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.IntSliderButton;
import journeymap.client.ui.component.ListPropertyButton;
import journeymap.common.properties.Category;
import journeymap.common.properties.config.EnumField;
import journeymap.common.properties.config.IntegerField;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.EnumSet;

import static journeymap.client.ui.serveroption.ServerOption.Option.NONE;
import static journeymap.common.Constants.ENABLED;
import static journeymap.common.Constants.OP_TRACKING;
import static journeymap.common.Constants.TELEPORT;
import static journeymap.common.Constants.TRACKING;
import static journeymap.common.Constants.TRACKING_TIME;
import static journeymap.common.Constants.USE_WORLD_ID;

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
            list.add(checkBox("Enable", "", ENABLED, properties));
            list.add(checkBox("Teleport", "", TELEPORT, properties));
        }
        else
        {
            list.add(checkBox("Teleport", "", TELEPORT, properties));
            if (!FMLClientHandler.instance().getClient().isSingleplayer())
            {
                list.add(checkBox("Use World ID", "", USE_WORLD_ID, properties));

                //add tracking slider
                IntegerField sliderFieldValue = new IntegerField(Category.Hidden, "", 100, 20000, 1000);
                sliderFieldValue.set(properties.get(TRACKING_TIME).getAsInt());
                IntSliderButton trackingUpdateSlider = new IntSliderButton(sliderFieldValue, "Update Time: ", " ms");
                trackingUpdateSlider.visible = false;
                // Add tracking button
                ServerOption option = new ServerOption(TRACKING, properties);
                ListPropertyButton<ServerOption.Option> tracking;
                tracking = new ListPropertyButton<ServerOption.Option>(
                        EnumSet.allOf(ServerOption.Option.class),
                        "Server Radar:",
                        new EnumField<>(Category.Hidden, "", option.getOption()));
                tracking.addClickListener(button -> {
                    option.setOption(tracking.getField().get());
                    updateToggleProperty(option, this.properties, TRACKING, OP_TRACKING);
                    resetTrackingSlider(tracking.getField().get(), tracking, trackingUpdateSlider);
                    return true;
                });
                trackingUpdateSlider.setWidth(fontRenderer.getStringWidth("Update Time 20000 ms") + 10);
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
            tracking.setWidth(150);
            trackingUpdateSlider.setVisible(false);
        }
        else
        {
            tracking.setWidth(120);
            trackingUpdateSlider.setVisible(true);
        }
    }

    @Override
    public void draw(int startX, int startY, int gap)
    {
        buttons.layoutCenteredHorizontal(startX, startY + 5, true, gap - 2, true);
    }
}
