package journeymap.client.ui.serveroption;

import com.google.gson.JsonObject;
import journeymap.client.Constants;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.Label;
import journeymap.client.ui.component.ListPropertyButton;
import journeymap.common.properties.Category;
import journeymap.common.properties.config.EnumField;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;
import java.util.EnumSet;

import static journeymap.common.Constants.CAVE_MAP;
import static journeymap.common.Constants.OP_CAVE_MAP;
import static journeymap.common.Constants.OP_SURFACE_MAP;
import static journeymap.common.Constants.OP_TOPO_MAP;
import static journeymap.common.Constants.SURFACE_MAP;
import static journeymap.common.Constants.TOPO_MAP;

public class MappingOptions implements Draw
{
    private ButtonList buttons;
    private JsonObject properties;
    private FontRenderer fontRenderer;
    private Label label;
    private ButtonList mappingToggleButtons;

    public MappingOptions(JsonObject properties, FontRenderer fontRenderer)
    {
        this.fontRenderer = fontRenderer;
        this.properties = properties;
        this.buttons = buildButtons();
    }

    private ButtonList buildButtons()
    {
        ButtonList list = new ButtonList();
        // create label

        label = new Label(fontRenderer.getStringWidth(Constants.getString("jm.server.edit.mapping.label")) + 10, "jm.server.edit.mapping.label");
        label.setHAlign(DrawUtil.HAlign.Center);
        label.setWidth(label.getFitWidth(fontRenderer));

        ServerOption surfaceOption = new ServerOption(SURFACE_MAP, properties);
        ListPropertyButton<ServerOption.Option> surfaceOptionButton = new ListPropertyButton<ServerOption.Option>(
                EnumSet.allOf(ServerOption.Option.class),
                Constants.getString("jm.server.edit.mapping.toggle.surface.label"),
                new EnumField<>(Category.Hidden, "", surfaceOption.getOption()));
        surfaceOptionButton.addClickListener(button -> {
            surfaceOption.setOption(surfaceOptionButton.getField().get());
            updateToggleProperty(surfaceOption, this.properties, SURFACE_MAP, OP_SURFACE_MAP);
            return true;
        });

        ServerOption topoOption = new ServerOption(TOPO_MAP, properties);
        ListPropertyButton<ServerOption.Option> topoOptionButton = new ListPropertyButton<ServerOption.Option>(
                EnumSet.allOf(ServerOption.Option.class),
                Constants.getString("jm.server.edit.mapping.toggle.topo.label"),
                new EnumField<>(Category.Hidden, "", topoOption.getOption()));
        topoOptionButton.addClickListener(button -> {
            topoOption.setOption(topoOptionButton.getField().get());
            updateToggleProperty(topoOption, this.properties, TOPO_MAP, OP_TOPO_MAP);
            return true;
        });

        ServerOption caveOption = new ServerOption(CAVE_MAP, properties);
        ListPropertyButton<ServerOption.Option> caveOptionButton = new ListPropertyButton<ServerOption.Option>(
                EnumSet.allOf(ServerOption.Option.class),
                Constants.getString("jm.server.edit.mapping.toggle.cave.label"),
                new EnumField<>(Category.Hidden, "", caveOption.getOption()));
        caveOptionButton.addClickListener(button -> {
            caveOption.setOption(caveOptionButton.getField().get());
            updateToggleProperty(caveOption, this.properties, CAVE_MAP, OP_CAVE_MAP);
            return true;
        });
        surfaceOptionButton.setTooltip(300,
                getToggleTooltipBase(),
                Constants.getString("jm.server.edit.mapping.toggle.surface.tooltip")
        );
        topoOptionButton.setTooltip(300,
                getToggleTooltipBase(),
                Constants.getString("jm.server.edit.mapping.toggle.topo.tooltip")
        );
        caveOptionButton.setTooltip(300,
                getToggleTooltipBase(),
                Constants.getString("jm.server.edit.mapping.toggle.cave.tooltip")
        );
        mappingToggleButtons = new ButtonList(surfaceOptionButton, topoOptionButton, caveOptionButton);
        mappingToggleButtons.setWidths(120);
        list.add(label);
        list.addAll(mappingToggleButtons);

        return list;
    }

    @Override
    public void draw(int startX, int startY, int gap)
    {
        label.setX(startX - (label.getWidth() / 2));
        label.setY(startY + 5);

        DrawUtil.drawRectangle(label.getX(), label.getBottomY() - 4, label.getWidth(), 1, new Color(255, 255, 255).getRGB(), 1f);

        mappingToggleButtons.layoutCenteredHorizontal(startX, label.getBottomY(), true, gap);
    }

    @Override
    public ButtonList getButtons()
    {
        return this.buttons;
    }
}
