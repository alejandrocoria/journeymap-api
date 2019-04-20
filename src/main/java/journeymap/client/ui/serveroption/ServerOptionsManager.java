package journeymap.client.ui.serveroption;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import journeymap.client.Constants;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.component.Button;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.JmUI;
import journeymap.client.ui.component.Label;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.network.GetAllConfigs;
import journeymap.common.network.UpdateAllConfigs;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static journeymap.common.network.Constants.DEFAULT_DIM;
import static journeymap.common.network.Constants.DIMENSIONS;
import static journeymap.common.network.Constants.DIM_ID;
import static journeymap.common.network.Constants.DIM_NAME;
import static journeymap.common.network.Constants.GLOBAL;
import static journeymap.common.network.Constants.WORLD_ID;

public class ServerOptionsManager extends JmUI
{
    private int index = 0;
    private JsonObject global;
    private JsonObject defaultDimension;
    private Map<Integer, JsonObject> dimensionMap;
    private List<String> dimIndexList;
    private JsonObject activeProperty;
    private ConfigDisplay configDisplay;


    private Button buttonNext;
    private Button buttonPrevious;
    private ButtonList topButtons;
    private Label labelSelector;
    private Label labelWorldId;


    private Button buttonClose;
    private Button buttonSave;
    private ButtonList bottomButtons;

    private final int hgap = 6;
    private final int vgap = 6;
    private int startY;
    private int centerX;
    private int topRowLeft;
    private int tileY;

    public ServerOptionsManager(JmUI returnDisplay)
    {
        super(Constants.getString("jm.server.edit.label.admin.edit"), returnDisplay);
        this.dimIndexList = Lists.newArrayList();
        Keyboard.enableRepeatEvents(true);
        getData();
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    private void getData()
    {
        try
        {
            new GetAllConfigs().send(result -> {
                if (result.getAsJson().get(GLOBAL) != null)
                {
                    this.dimIndexList.add(GLOBAL);
                    this.global = result.getAsJson().get(GLOBAL).getAsJsonObject();
                    this.activeProperty = global;
                    this.labelSelector = new Label(150, "jm.server.edit.label.selection.global");
                    labelSelector.setTooltip(formattedToolTipHeader("jm.server.edit.label.selection.global") + Constants.getString("jm.server.edit.label.selection.global.tooltip"));
                    this.labelSelector.setHAlign(DrawUtil.HAlign.Center);
                }

                if (result.getAsJson().get(DEFAULT_DIM) != null)
                {
                    this.dimIndexList.add(DEFAULT_DIM);
                    this.defaultDimension = result.getAsJson().get(DEFAULT_DIM).getAsJsonObject();
                }

                if (result.getAsJson().get(DIMENSIONS) != null)
                {
                    this.dimensionMap = buildDimensionMap(result.getAsJson().getAsJsonArray(DIMENSIONS));
                }
            });
//        }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error("Error getting data", e);
        }
    }

    private Map<Integer, JsonObject> buildDimensionMap(JsonArray dims)
    {
        Map<Integer, JsonObject> dimMap = new HashMap<>();

        for (JsonElement dim : dims)
        {
            JsonObject json = dim.getAsJsonObject();
            if (json.get(DIM_ID) != null)
            {
                int dimId = json.get(DIM_ID).getAsInt();
                this.dimIndexList.add(String.valueOf(dimId));
                dimMap.put(dimId, json);
            }
        }

        return dimMap;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        try
        {
            if (global != null)
            {
                buttonList.clear();
                buttonNext = new Button(Constants.getString("jm.server.edit.label.button.next"));
                buttonNext.setTooltip(formattedToolTipHeader("jm.server.edit.label.button.next.tooltip") + Constants.getString("jm.server.edit.label.button.next.tooltip"));
                buttonNext.setWidth(40);
                buttonPrevious = new Button(Constants.getString("jm.server.edit.label.button.previous"));
                buttonPrevious.setTooltip(formattedToolTipHeader("jm.server.edit.label.button.previous.tooltip") + Constants.getString("jm.server.edit.label.button.previous.tooltip"));
                buttonPrevious.setWidth(buttonNext.getWidth());

                if (global.get(WORLD_ID) != null)
                {
                    labelWorldId = new Label(304, "jm.server.edit.label.worldId", global.get(WORLD_ID).getAsString());
                    labelWorldId.setTooltip(formattedToolTipHeader("jm.server.edit.chkbox.world.id") + Constants.getString("jm.server.edit.label.worldId.tooltip"));
                }
                else
                {
                    labelWorldId = new Label(40, "jm.server.edit.label.worldId.singleplayer");
                    labelWorldId.setTooltip(formattedToolTipHeader("jm.server.edit.label.worldId.singleplayer") + Constants.getString("jm.server.edit.label.worldId.singleplayer.tooltip"));
                }
                labelWorldId.setHAlign(DrawUtil.HAlign.Center);
                labelWorldId.setWidth(labelWorldId.getFitWidth(getFontRenderer()));

                buttonSave = new Button(Constants.getString("jm.waypoint.save"));
                buttonClose = new Button(Constants.getString("jm.server.edit.button.close"));
                bottomButtons = new ButtonList(buttonClose, buttonSave);
                bottomButtons.equalizeWidths(getFontRenderer());

                configDisplay = new ConfigDisplay(activeProperty, fontRenderer);

                topButtons = new ButtonList(buttonPrevious, labelSelector, buttonNext);
                buttonList.add(labelWorldId);
                buttonList.addAll(topButtons);
                buttonList.addAll(configDisplay.getButtons());
                buttonList.addAll(bottomButtons);
            }
        }

        catch (Throwable t)
        {
            Journeymap.getLogger().error(LogFormatter.toString(t));
            UIManager.INSTANCE.closeAll();
        }
    }


    @Override
    protected void layoutButtons()
    {
        this.startY = Math.max(40, (this.height - 230) / 2);
        this.centerX = this.width / 2;
        this.topRowLeft = centerX - (100 / 2);
        this.tileY = startY + (vgap * 2);
        // Buttons
        if (buttonList.isEmpty() && global != null)
        {
            initGui();
        }

        if (global != null && !buttonList.isEmpty() && topButtons != null && !topButtons.isEmpty())
        {
            // WorldId label
            labelWorldId.setX(centerX - (labelWorldId.getWidth() / 2));
            labelWorldId.setY(startY - 10);

            // Top Buttons
            try
            {
                topButtons.layoutCenteredHorizontal(centerX, labelWorldId.getBottomY(), true, hgap);
            }
            catch (Exception e)
            {
                System.out.println(topButtons.size());
            }
            // Draw config
            configDisplay.draw(centerX, topButtons.getBottomY(), hgap);

            // Bottom Buttons
            int bottomY = Math.min(tileY + 128 + (vgap * 2), height - 10 - buttonClose.getHeight());
            bottomButtons.equalizeWidths(getFontRenderer(), hgap, centerX - topRowLeft);
            bottomButtons.layoutCenteredHorizontal(centerX, bottomY + 20, true, hgap);
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        try
        {
            if (guibutton == buttonSave)
            {
                //save
                save();
                closeAndReturn();
                return;
            }
            if (guibutton == buttonClose)
            {
                closeAndReturn();
                return;
            }
            if (guibutton == buttonNext)
            {
                index++;
                nextProperty();
                return;
            }

            if (guibutton == buttonPrevious)
            {
                index--;
                nextProperty();
                return;
            }
        }
        catch (Throwable t)
        {
            logger.error("Error in SeverEditor.actionPerformed: " + LogFormatter.toString(t));
        }
    }

    private void save()
    {
        JsonObject updatedProperties = new JsonObject();
        JsonArray dims = new JsonArray();
        updatedProperties.add(GLOBAL, global);

        for (JsonObject dim : dimensionMap.values())
        {
            dims.add(dim);
        }
        updatedProperties.add(DIMENSIONS, dims);
        updatedProperties.add(DEFAULT_DIM, defaultDimension);
        new UpdateAllConfigs().send(updatedProperties);
    }

    private void nextProperty()
    {
        if (index < 0)
        {
            index = dimIndexList.size() - 1;
        }
        else if (index > dimIndexList.size() - 1)
        {
            index = 0;
        }
        if (index == 0)
        {
            labelSelector.displayString = Constants.getString("jm.server.edit.label.selection.global");
            labelSelector.setTooltip(formattedToolTipHeader("jm.server.edit.label.selection.global") + Constants.getString("jm.server.edit.label.selection.global.tooltip"));
            this.activeProperty = global;
        }
        else if (index == 1)
        {
            labelSelector.displayString = Constants.getString("jm.server.edit.label.selection.default");
            labelSelector.setTooltip(formattedToolTipHeader("jm.server.edit.label.selection.default") + Constants.getString("jm.server.edit.label.selection.default.tooltip"));
            this.activeProperty = defaultDimension;
        }
        else
        {
            String dimName = dimensionMap.get(Integer.valueOf(dimIndexList.get(index))).get(DIM_NAME).getAsString();
            labelSelector.displayString = Constants.getString("jm.server.edit.label.selection.dimension", dimName, dimIndexList.get(index));
            labelSelector.setTooltip(formattedToolTipHeader("jm.theme.labelsource.dimension") + Constants.getString("jm.server.edit.label.selection.dimension.tooltip"));
            this.activeProperty = dimensionMap.get(Integer.valueOf(dimIndexList.get(index)));
        }

        this.initGui();
    }

    @Override
    public void drawScreen(int x, int y, float par3)
    {
        try
        {
            super.drawScreen(x, y, par3);
        }
        catch (Throwable t)
        {
            logger.error("Error in SeverEditor.drawScreen: " + LogFormatter.toString(t));
        }
    }

    @Override
    protected void closeAndReturn()
    {
        buttonList.clear();
        if (returnDisplay == null)
        {
            UIManager.INSTANCE.closeAll();
        }
        else
        {
            UIManager.INSTANCE.open(returnDisplay);
        }
    }

    static String formattedToolTipHeader(String key)
    {
        return "§b[" + Constants.getString(key) + "]§f" + "\n";
    }
}
