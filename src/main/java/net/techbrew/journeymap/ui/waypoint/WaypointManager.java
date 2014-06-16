package net.techbrew.journeymap.ui.waypoint;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.command.server.CommandTeleport;
import net.minecraft.world.WorldProvider;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.WorldData;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.ui.*;
import net.techbrew.journeymap.ui.map.MapOverlay;
import net.techbrew.journeymap.waypoint.WaypointStore;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class WaypointManager extends JmUI
{

    final static String ASCEND = Constants.getString("JourneyMap.char_uparrow");
    final static String DESCEND = Constants.getString("JourneyMap.char_downarrow");
    final static int COLWAYPOINT = 0;
    final static int COLLOCATION = 20;
    final static int COLNAME = 60;
    final static int DEFAULT_ITEMWIDTH = 460;

    private static WaypointManagerItem.Sort currentSort;

    private enum ButtonEnum
    {
        Add, Find, SortName, SortDistance, Dimensions, ToggleAll, Help, Options, Close
    }

    final String on = Constants.getString("MapOverlay.on");
    final String off = Constants.getString("MapOverlay.off");

    protected int rowHeight = 16;
    protected int colWaypoint = COLWAYPOINT;
    protected int colLocation = COLLOCATION;
    protected int colName = COLNAME;
    protected int itemWidth = DEFAULT_ITEMWIDTH;

    protected Boolean canUserTeleport;

    private SortButton buttonSortName, buttonSortDistance;
    private DimensionsButton buttonDimensions;
    private Button buttonClose, buttonAdd, buttonHelp, buttonOptions, buttonToggleAll;

    private ButtonList bottomButtons;

    private ArrayList<WaypointManagerItem> items = new ArrayList<WaypointManagerItem>();

    private ScrollPane itemScrollPane;

    private Waypoint focusWaypoint;

    public WaypointManager()
    {
        this(null, null);
    }

    public WaypointManager(Waypoint focusWaypoint, Class<? extends JmUI> returnClass)
    {
        super(Constants.getString("Waypoint.manage_title"), returnClass);
        this.focusWaypoint = focusWaypoint;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        try
        {
            if (canUserTeleport == null)
            {
                if (mc.thePlayer.capabilities.isCreativeMode || mc.theWorld.getWorldInfo().areCommandsAllowed())
                {
                    canUserTeleport = true;
                }
                else
                {
                    CommandTeleport command = new CommandTeleport();
                    canUserTeleport = command.canCommandSenderUseCommand(mc.thePlayer);
                }
            }

            if (this.buttonList.isEmpty())
            {
                FontRenderer fr = getFontRenderer();

                if (buttonSortDistance == null)
                {
                    WaypointManagerItem.Sort distanceSort = new WaypointManagerItem.DistanceComparator(FMLClientHandler.instance().getClient().thePlayer, true);
                    String distanceLabel = Constants.getString("Waypoint.distance");
                    buttonSortDistance = new SortButton(ButtonEnum.SortDistance, distanceLabel, distanceSort);
                    buttonSortDistance.setTextOnly(fr);
                }
                buttonList.add(buttonSortDistance);

                if (buttonSortName == null)
                {
                    WaypointManagerItem.Sort nameSort = new WaypointManagerItem.NameComparator(true);
                    buttonSortName = new SortButton(ButtonEnum.SortName, Constants.getString("Waypoint.name"), nameSort);
                    buttonSortName.setTextOnly(fr);
                }
                buttonList.add(buttonSortName);

                if (buttonToggleAll == null)
                {
                    String enableOn = Constants.getString("Waypoint.enable_all", "", on);
                    String enableOff = Constants.getString("Waypoint.enable_all", "", off);
                    buttonToggleAll = new Button(ButtonEnum.ToggleAll.ordinal(), 0, 0, enableOff, enableOn, true);
                    buttonToggleAll.setTextOnly(getFontRenderer());
                }
                buttonList.add(buttonToggleAll);

                // Bottom buttons
                if (buttonDimensions == null)
                {
                    buttonDimensions = new DimensionsButton(ButtonEnum.Dimensions.ordinal());
                }

                if (buttonAdd == null)
                {
                    buttonAdd = new Button(ButtonEnum.Add, Constants.getString("Waypoint.new"));
                    buttonAdd.fitWidth(getFontRenderer());
                    buttonAdd.setWidth(buttonAdd.getWidth() * 2);
                }

                if (buttonHelp == null)
                {
                    buttonHelp = new Button(ButtonEnum.Help, Constants.getString("MapOverlay.help"));
                    buttonHelp.fitWidth(getFontRenderer());
                }

                if (buttonOptions == null)
                {
                    buttonOptions = new Button(ButtonEnum.Options, Constants.getString("MapOverlay.options_button"));
                    buttonOptions.fitWidth(getFontRenderer());
                }

                buttonClose = new Button(ButtonEnum.Close, Constants.getString("MapOverlay.close"));

                bottomButtons = new ButtonList(buttonOptions, buttonHelp, buttonAdd, buttonDimensions, buttonClose);
                buttonList.addAll(bottomButtons);
            }

            if (this.items.isEmpty())
            {
                updateItems();
                if (currentSort == null)
                {
                    updateSort(buttonSortDistance);
                }
                else
                {
                    if (buttonSortDistance.sort.equals(currentSort))
                    {
                        buttonSortDistance.sort.ascending = currentSort.ascending;
                        buttonSortDistance.setActive(true);
                        buttonSortName.setActive(false);
                    }
                    if (buttonSortName.sort.equals(currentSort))
                    {
                        buttonSortName.sort.ascending = currentSort.ascending;
                        buttonSortName.setActive(true);
                        buttonSortDistance.setActive(false);
                    }
                }
            }

            if (itemScrollPane == null)
            {
                itemScrollPane = new ScrollPane(mc, 0, 0, items, rowHeight, 2);
                itemScrollPane.setShowFrame(false);
                itemScrollPane.setShowSelectionBox(false);
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().severe(LogFormatter.toString(t));
            UIManager.getInstance().closeAll();
        }
    }

    /**
     * Center buttons in UI.
     */
    @Override
    protected void layoutButtons()
    {

        // Header buttons
        int pad = 3;
        int headerY = headerHeight + pad;
        if (items.size() > 0)
        {
            if (items.get(0).y > headerY + 16)
            {
                headerY = items.get(0).y - 16;
            }

            buttonToggleAll.setY(headerY);
            buttonToggleAll.centerHorizontalOn(items.get(0).getButtonEnableCenterX());
        }
        buttonToggleAll.setDrawButton(!items.isEmpty());

        int margin = getMargin();
        int headerX = itemScrollPane.getX() + margin;
        buttonSortDistance.setPosition(headerX + colWaypoint, headerY);
        colName = buttonSortDistance.getX() + buttonSortDistance.getWidth() + 5 - headerX;
        buttonSortName.setPosition(headerX + colName, headerY);

        buttonSortDistance.setDrawButton(!items.isEmpty());
        buttonSortName.setDrawButton(!items.isEmpty());

        // Scroll pane
        int hgap = 4;
        int vgap = 5;
        int bottomButtonsHeight = buttonClose.getHeight() + (vgap * 2);
        final int startY = headerHeight + vgap + getFontRenderer().FONT_HEIGHT;
        int scrollWidth = this.width - 6;
        itemScrollPane.position(scrollWidth, this.height, startY, bottomButtonsHeight, 0, 0);

        // Bottom buttons
        bottomButtons.equalizeWidths(getFontRenderer());
        int bottomButtonWidth = Math.min(bottomButtons.getWidth(hgap)+25, scrollWidth);
        bottomButtons.equalizeWidths(getFontRenderer(), hgap, bottomButtonWidth);
        bottomButtons.layoutCenteredHorizontal(this.width / 2, this.height - bottomButtonsHeight + vgap, true, hgap);
    }

    @Override
    public void drawScreen(int x, int y, float par3)
    {
        drawBackground(0);
        layoutButtons();

        itemScrollPane.drawScreen(x, y, par3);

        // Check for focused waypoint, scroll if needed for next pass
        if (focusWaypoint != null && !items.isEmpty() && itemScrollPane != null)
        {
            int index = -1;
            for (WaypointManagerItem item : items)
            {
                if (item.waypoint.equals(focusWaypoint))
                {
                    itemScrollPane.select(item);
                    index = items.indexOf(item);
                    break;
                }
            }
            int offset = Math.max(1, itemScrollPane.getLastVisibleIndex() - 2);
            if (index > -1 && index > offset)
            {
                int delta = index - offset;
                itemScrollPane.scrollBy(delta * itemScrollPane.getSlotHeight());
            }
            focusWaypoint = null;
        }

        for (int k = 0; k < this.buttonList.size(); ++k)
        {
            GuiButton guibutton = (GuiButton) this.buttonList.get(k);
            guibutton.drawButton(this.mc, x, y);
        }
        buttonToggleAll.drawUnderline();

        drawTitle();
        drawLogo();
    }

    protected int getMargin()
    {
        return width > itemWidth + 2 ? (width - itemWidth) / 2 : 0;
    }

    protected void keyTyped(char par1, int par2)
    {
        switch (par2)
        {
            case Keyboard.KEY_ESCAPE:
                refreshAndClose();
                return;
            // TODO: Arrow keys
            default:
                break;
        }
    }

    protected boolean isSelected(WaypointManagerItem item)
    {
        return itemScrollPane.isSelected(item);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (buttonToggleAll.mouseOver(mouseX, mouseY))
        {
            return;
        }
        itemScrollPane.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        switch (ButtonEnum.values()[guibutton.id])
        {
            case Close:
            {
                refreshAndClose();
                return;
            }
            case SortName:
            {
                updateSort(buttonSortName);
                return;
            }
            case SortDistance:
            {
                updateSort(buttonSortDistance);
                return;
            }
            case Dimensions:
            {
                buttonDimensions.toggle();
                updateItems();
                return;
            }
            case Add:
            {
                Waypoint waypoint = Waypoint.of(mc.thePlayer);
                UIManager.getInstance().openWaypointEditor(waypoint, true, WaypointManager.class);
                return;
            }
            case Help:
            {
                UIManager.getInstance().openWaypointHelp(getClass());
                return;
            }
            case ToggleAll:
            {
                boolean state = buttonToggleAll.getToggled();
                state = toggleItems(state);
                buttonToggleAll.setToggled(state);
                return;
            }
            case Options:
            {
                UIManager.getInstance().openWaypointOptions(getClass());
                return;
            }
        }
    }

    public void removeWaypoint(WaypointManagerItem item)
    {
        WaypointStore.instance().remove(item.waypoint);
        this.items.remove(item);
    }

    protected boolean toggleItems(boolean enable)
    {
        for (WaypointManagerItem item : items)
        {
            if (enable == item.waypoint.isEnable())
            {
                enable = !enable;
                break;
            }
        }
        for (WaypointManagerItem item : items)
        {
            if (item.waypoint.isEnable() != enable)
            {
                item.enableWaypoint(enable);
            }
        }
        return !enable;
    }

    protected void updateItems()
    {
        items.clear();
        Integer currentDim = buttonDimensions.currentWorldProvider == null ? null : buttonDimensions.currentWorldProvider.dimensionId;
        FontRenderer fr = getFontRenderer();
        itemWidth = 0;

        Collection<Waypoint> waypoints = WaypointStore.instance().getAll();
        boolean allOn = true;
        for (Waypoint waypoint : waypoints)
        {
            WaypointManagerItem item = new WaypointManagerItem(waypoint, fr, this);
            item.getDistanceTo(mc.thePlayer);
            if (currentDim == null || item.waypoint.getDimensions().contains(currentDim))
            {
                items.add(item);
                if (allOn)
                {
                    allOn = waypoint.isEnable();
                }
            }
        }

        if (items.isEmpty())
        {
            itemWidth = DEFAULT_ITEMWIDTH;
        }
        else
        {
            itemWidth = items.get(0).internalWidth;
        }

        buttonToggleAll.setToggled(!allOn);
        updateCount();

        if (currentSort != null)
        {
            Collections.sort(items, currentSort);
        }
    }

    protected void updateSort(SortButton sortButton)
    {
        for (Button button : (List<Button>) buttonList)
        {
            if (button instanceof SortButton)
            {
                if (button == sortButton)
                {
                    if (sortButton.sort.equals(currentSort))
                    {
                        sortButton.toggle();
                    }
                    else
                    {
                        sortButton.setActive(true);
                    }
                    currentSort = sortButton.sort;
                }
                else
                {
                    ((SortButton) button).setActive(false);
                }
            }
        }

        if (currentSort != null)
        {
            Collections.sort(items, currentSort);
        }
    }

    protected void updateCount()
    {
        String itemCount = items.isEmpty() ? "" : Integer.toString(items.size());
        String enableOn = Constants.getString("Waypoint.enable_all", itemCount, on);
        String enableOff = Constants.getString("Waypoint.enable_all", itemCount, off);
        buttonToggleAll.setLabels(enableOff, enableOn);
    }

    protected void refreshAndClose()
    {
        bottomButtons.setEnabled(false);
        WaypointStore.instance().bulkSave();
        //DataCache.instance().getWaypoints(true);
        MapOverlay.state().requireRefresh();
        closeAndReturn();
    }

    protected void closeAndReturn()
    {
        if (returnClass == null)
        {
            UIManager.getInstance().closeAll();
        }
        else
        {
            UIManager.getInstance().open(returnClass);
        }
    }

    protected class SortButton extends Button
    {
        final WaypointManagerItem.Sort sort;
        final String labelInactive;

        public SortButton(Enum enumValue, String label, WaypointManagerItem.Sort sort)
        {
            super(enumValue.ordinal(), 0, 0, String.format("%s %s", label, ASCEND), String.format("%s %s", label, DESCEND), sort.ascending);
            this.labelInactive = label;
            this.sort = sort;
        }

        @Override
        public void toggle()
        {
            sort.ascending = !sort.ascending;
            setActive(true);
        }

        @Override
        public void drawButton(Minecraft minecraft, int mouseX, int mouseY)
        {
            super.drawButton(minecraft, mouseX, mouseY);
            super.drawUnderline();
        }

        public void setActive(boolean active)
        {
            if (active)
            {
                setToggled(sort.ascending);
            }
            else
            {
                displayString = String.format("%s %s", labelInactive, " ");
            }
        }
    }

    protected static class DimensionsButton extends Button
    {
        final List<WorldProvider> worldProviders = WorldData.getDimensionProviders(WaypointStore.instance().getLoadedDimensions());

        static boolean needInit = true;
        static WorldProvider currentWorldProvider;

        public DimensionsButton(int id)
        {
            super(id, 0, 0, "");

            if(needInit || currentWorldProvider !=null)
            {
                currentWorldProvider = FMLClientHandler.instance().getClient().thePlayer.worldObj.provider;
                needInit = false;
            }
            updateLabel();

            // Determine width
            fitWidth(FMLClientHandler.instance().getClient().fontRenderer);
        }

        protected void updateLabel()
        {
            String dimName;

            if (currentWorldProvider != null)
            {
                dimName = currentWorldProvider.getDimensionName();
            }
            else
            {
                dimName = Constants.getString("Waypoint.dimension_all");
            }
            displayString = Constants.getString("Waypoint.dimension", dimName);
        }

        @Override
        public int getFitWidth(FontRenderer fr)
        {
            int maxWidth = 0;
            for (WorldProvider worldProvider : worldProviders)
            {
                String name = Constants.getString("Waypoint.dimension", worldProvider.getDimensionName());
                maxWidth = Math.max(maxWidth, FMLClientHandler.instance().getClient().fontRenderer.getStringWidth(name));
            }
            return maxWidth + 12;
        }

        @Override
        public void toggle()
        {
            int index;

            if (currentWorldProvider == null)
            {
                index = 0;
            }
            else
            {
                index = -1;

                for (WorldProvider worldProvider : worldProviders)
                {
                    if(worldProvider.dimensionId== currentWorldProvider.dimensionId)
                    {
                        index = worldProviders.indexOf(worldProvider) + 1;
                        break;
                    }
                }
            }

            if (index >= worldProviders.size() || index < 0)
            {
                currentWorldProvider = null; // "All"
            }
            else
            {
                currentWorldProvider = worldProviders.get(index);
            }

            updateLabel();
        }
    }
}
