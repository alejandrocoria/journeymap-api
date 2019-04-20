package journeymap.client.ui.serveroption;

import com.google.gson.JsonObject;
import journeymap.client.Constants;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.CheckBox;
import journeymap.client.ui.component.Label;
import journeymap.client.ui.component.ListPropertyButton;
import journeymap.common.properties.Category;
import journeymap.common.properties.config.EnumField;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;
import java.util.EnumSet;

import static journeymap.client.ui.serveroption.ServerOption.Option.ALL;
import static journeymap.client.ui.serveroption.ServerOptionsManager.formattedToolTipHeader;
import static journeymap.common.network.Constants.ANIMAL_RADAR;
import static journeymap.common.network.Constants.MOB_RADAR;
import static journeymap.common.network.Constants.OP_RADAR;
import static journeymap.common.network.Constants.PLAYER_RADAR;
import static journeymap.common.network.Constants.RADAR;
import static journeymap.common.network.Constants.VILLAGER_RADAR;

public class RadarOptions implements Draw
{
    private ButtonList buttons;
    private JsonObject properties;
    private FontRenderer fontRenderer;
    private Label label;
    private ListPropertyButton<ServerOption.Option> radarPropertyButton;
    private ButtonList checkBoxList;

    public RadarOptions(JsonObject properties, FontRenderer fontRenderer)
    {
        this.fontRenderer = fontRenderer;
        this.properties = properties;
        this.buttons = createRadarButtons();
    }

    private ButtonList createRadarButtons()
    {
        ButtonList list = new ButtonList();
        // create label
        label = new Label(fontRenderer.getStringWidth(Constants.getString("jm.server.edit.radar.label")) + 10, "jm.server.edit.radar.label");
        label.setHAlign(DrawUtil.HAlign.Center);
        label.setWidth(label.getFitWidth(fontRenderer));

        CheckBox playerChkBx = checkBox("jm.server.edit.radar.chkbox.player", PLAYER_RADAR, properties);
        CheckBox villagerChkBx = checkBox("jm.server.edit.radar.chkbox.villager", VILLAGER_RADAR, properties);
        CheckBox animalChkBx = checkBox("jm.server.edit.radar.chkbox.animal", ANIMAL_RADAR, properties);
        CheckBox mobChkBx = checkBox("jm.server.edit.radar.chkbox.mob", MOB_RADAR, properties);
        checkBoxList = new ButtonList(playerChkBx, villagerChkBx, animalChkBx, mobChkBx);

        ServerOption option = new ServerOption(RADAR, properties);
        radarPropertyButton = new ListPropertyButton<ServerOption.Option>(
                EnumSet.allOf(ServerOption.Option.class),
                Constants.getString("jm.server.edit.radar.toggle.label"),
                new EnumField<>(Category.Hidden, "", option.getOption()));
        radarPropertyButton.addClickListener(button -> {
            option.setOption(radarPropertyButton.getField().get());
            updateToggleProperty(option, this.properties, RADAR, OP_RADAR);
            updateCheckBoxes(radarPropertyButton.getField().get());
            return true;
        });
        radarPropertyButton.setWidth(fontRenderer.getStringWidth(label.displayString) + 40);
        radarPropertyButton.setTooltip(300,
                formattedToolTipHeader("jm.server.edit.radar.toggle.label") +
                        getToggleTooltipBase(),
                Constants.getString("jm.server.edit.radar.toggle.tooltip1"),
                Constants.getString("jm.server.edit.radar.toggle.tooltip2")
        );
        updateCheckBoxes(radarPropertyButton.getField().get());
        list.add(label);
        list.add(radarPropertyButton);
        list.addAll(checkBoxList);
        return list;
    }

    private void updateCheckBoxes(ServerOption.Option options)
    {
        if (ALL.equals(options))
        {
            checkBoxList.setVisible(true);
        }
        else
        {
            checkBoxList.setVisible(false);
        }
    }

    @Override
    public void draw(int startX, int startY, int gap)
    {
        label.setX(startX - (label.getWidth() / 2));
        label.setY(startY + 5);

        DrawUtil.drawRectangle(label.getX(), label.getBottomY() - 4, label.getWidth(), 1, new Color(255, 255, 255).getRGB(), 1f);
        radarPropertyButton.setX(startX - (radarPropertyButton.getWidth() / 2));
        radarPropertyButton.setY(label.getBottomY());

        checkBoxList.layoutCenteredHorizontal(startX, radarPropertyButton.getBottomY() + gap, true, gap);

    }

    @Override
    public ButtonList getButtons()
    {
        return this.buttons;
    }
}
