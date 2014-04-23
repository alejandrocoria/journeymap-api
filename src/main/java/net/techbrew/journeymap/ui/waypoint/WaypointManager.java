package net.techbrew.journeymap.ui.waypoint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.command.server.CommandTeleport;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.data.WaypointsData;
import net.techbrew.journeymap.data.WorldData;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.ui.*;
import net.techbrew.journeymap.ui.map.MapOverlay;
import net.techbrew.journeymap.waypoint.WaypointStore;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class WaypointManager extends JmUI {

    final static String ASCEND = Constants.getString("JourneyMap.char_uparrow");
    final static String DESCEND = Constants.getString("JourneyMap.char_downarrow");
    final static int COLWAYPOINT = 0;
    final static int COLLOCATION = 20;
    final static int COLNAME = 60;
    final static int DEFAULT_ITEMWIDTH = 460;

    private static WaypointManagerItem.Sort currentSort;

	private enum ButtonEnum {Add, Find, SortName, SortDistance, Dimensions, Help, Close};

    protected int rowHeight = 16;
    protected int colWaypoint = COLWAYPOINT;
    protected int colLocation = COLLOCATION;
    protected int colName = COLNAME;
    protected int itemWidth = DEFAULT_ITEMWIDTH;

    protected Boolean canUserTeleport;

    private SortButton buttonSortName, buttonSortDistance;
    private DimensionsButton buttonDimensions;
    private Button buttonClose, buttonAdd, buttonHelp;

    private ButtonList bottomButtons;

    private ArrayList<WaypointManagerItem> items = new ArrayList<WaypointManagerItem>();

    private ScrollPane itemScrollPane;

	public WaypointManager()
    {
		super(Constants.getString("Waypoint.manage_title"));
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
                if (mc.thePlayer.capabilities.isCreativeMode)
                {
                    canUserTeleport = true;
                } else
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
                    WaypointManagerItem.Sort distanceSort = new WaypointManagerItem.DistanceComparator(mc.thePlayer, true);
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

                if (buttonDimensions == null)
                {
                    buttonDimensions = new DimensionsButton(ButtonEnum.Dimensions.ordinal());
                }
                buttonList.add(buttonDimensions);

                if (buttonAdd == null)
                {
                    buttonAdd = new Button(ButtonEnum.Add, Constants.getString("Waypoint.new"));
                    buttonAdd.fitWidth(getFontRenderer());
                    buttonAdd.setWidth(buttonAdd.getWidth() * 2);
                }
                buttonList.add(buttonAdd);

                if(buttonHelp == null)
                {
                    buttonHelp = new Button(ButtonEnum.Help, Constants.getString("MapOverlay.help"));
                    buttonHelp.fitWidth(getFontRenderer());
                }
                buttonList.add(buttonHelp);

                buttonClose = new Button(ButtonEnum.Close, Constants.getString("MapOverlay.close"));
                //buttonClose.fitWidth(getFontRenderer());
                buttonList.add(buttonClose);

                bottomButtons = new ButtonList(buttonHelp, buttonDimensions, buttonAdd, buttonClose);
            }

            if (this.items.isEmpty())
            {
                updateItems();
                if(currentSort==null)
                {
                    updateSort(buttonSortDistance);
                }
                else
                {
                    if(buttonSortDistance.sort.equals(currentSort))
                    {
                        buttonSortDistance.sort.ascending = currentSort.ascending;
                        buttonSortDistance.setActive(true);
                        buttonSortName.setActive(false);
                    }
                    if(buttonSortName.sort.equals(currentSort))
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
            }
        }
        catch(Throwable t)
        {
            JourneyMap.getLogger().severe(LogFormatter.toString(t));
            UIManager.getInstance().closeAll();
        }
    }
    
    /**
	 * Center buttons in UI.
	 */
    @Override
    protected void layoutButtons() {
		// Buttons

        // Header buttons
        int pad = 3;
        int headerY = headerHeight + pad;
        if(items.size()>0)
        {
            if(items.get(0).y>headerY + 16)
            {
                headerY = items.get(0).y - 16;
            }
        }

        int margin = getMargin();
        int headerX = itemScrollPane.getX() + margin;
        buttonSortDistance.setPosition(headerX + colWaypoint, headerY);
        colName = buttonSortDistance.getX() + buttonSortDistance.getWidth() + 5 - headerX;
        buttonSortName.setPosition(headerX + colName, headerY);

        buttonSortDistance.drawButton = !items.isEmpty();
        buttonSortName.drawButton = !items.isEmpty();

        // Scroll pane
        int hgap = 4;
        int vgap = 5;
        int bottomButtonsHeight = buttonClose.getHeight() + (vgap*2);
        final int startY = headerHeight + vgap + getFontRenderer().FONT_HEIGHT;
        int scrollWidth = this.width-6;
        itemScrollPane.position(scrollWidth, this.height, startY, bottomButtonsHeight, 0, 0);

        // Bottom buttons
        ButtonList.equalizeWidths(mc.fontRenderer, bottomButtons);
        if(bottomButtons.getWidth(hgap) > this.width)
        {
            ButtonList.equalizeWidths(mc.fontRenderer, new ButtonList(buttonAdd, buttonHelp));
            buttonAdd.setWidth(buttonAdd.getWidth() + 10);
            buttonHelp.setWidth(buttonHelp.getWidth() + 10);
        }
        bottomButtons.layoutCenteredHorizontal(this.width/2, this.height - bottomButtonsHeight + vgap, true, hgap);
	}

    @Override
    public void drawScreen(int x, int y, float par3)
    {
        drawBackground(0);
        layoutButtons();

        itemScrollPane.drawScreen(x, y, par3);

        for (int k = 0; k < this.buttonList.size(); ++k)
        {
            GuiButton guibutton = (GuiButton)this.buttonList.get(k);
            guibutton.drawButton(this.mc, x, y);
        }

        drawTitle();
        drawLogo();
    }

    protected int getMargin()
    {
        return width>itemWidth+2 ? (width-itemWidth)/2 : 0;
    }

    protected void keyTyped(char par1, int par2)
    {
        switch(par2)
        {
            case Keyboard.KEY_ESCAPE :
                refreshAndClose();
                return;
            // TODO: Arrow keys
            default:
                break;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        itemScrollPane.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        switch(ButtonEnum.values()[guibutton.id])
        {
            case Close :
            {
                refreshAndClose();
                return;
            }
            case SortName :
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
                UIManager.getInstance().openWaypointHelp();
                return;
            }
        }
	}

    public void removeWaypoint(WaypointManagerItem item)
    {
        WaypointStore.instance().remove(item.waypoint);
        this.items.remove(item);
    }

    protected void updateItems()
    {
        items.clear();
        Integer currentDim = buttonDimensions.currentDim;
        FontRenderer fr = getFontRenderer();
        itemWidth = 0;

        Collection<Waypoint> waypoints = WaypointStore.instance().getAll();
        for(Waypoint waypoint : waypoints)
        {
            WaypointManagerItem item = new WaypointManagerItem(waypoint, fr, this);
            item.getDistanceSqToEntity(mc.thePlayer);
            if(currentDim==null || item.waypoint.getDimensions().contains(currentDim))
            {
                items.add(item);
            }
        }

        if(items.isEmpty())
        {
            itemWidth = DEFAULT_ITEMWIDTH;
        }
        else
        {
            itemWidth = items.get(0).internalWidth;
        }

        if(currentSort!=null)
        {
            Collections.sort(items, currentSort);
        }
    }

    protected void updateSort(SortButton sortButton)
    {
        for(Button button : (List<Button>) buttonList)
        {
            if(button instanceof SortButton)
            {
                if(button == sortButton)
                {
                    if(sortButton.sort.equals(currentSort))
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

        if(currentSort!=null)
        {
            Collections.sort(items, currentSort);
        }
    }

    protected void refreshAndClose() {
        DataCache.instance().forceRefresh(WaypointsData.class);
        MapOverlay.state().requireRefresh();
        UIManager.getInstance().openMap();
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
            if (!drawButton)
            {
                return;
            }
            DrawUtil.drawRectangle(xPosition,yPosition+height,width,1, Button.smallFrameColorDark, 255);
        }

        public void setActive(boolean active)
        {
            if(active)
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
        final Integer[] dimensions;
        static Integer currentDim = 0;

        public DimensionsButton(int id)
        {
            super(id, 0, 0, "");
            dimensions = WorldData.getDimensions();
            if(currentDim!=null)
            {
                currentDim = Minecraft.getMinecraft().thePlayer.dimension;
            }
            updateLabel();

            // Determine width
            fitWidth(Minecraft.getMinecraft().fontRenderer);
        }

        protected void updateLabel()
        {
            String dimName;

            if(currentDim!=null)
            {
                dimName = WorldData.getDimensionName(currentDim);
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
            for(Integer dim : dimensions)
            {
                String name = Constants.getString("Waypoint.dimension", WorldData.getDimensionName(dim));
                maxWidth = Math.max(maxWidth, Minecraft.getMinecraft().fontRenderer.getStringWidth(name));
            }
            return maxWidth+12;
        }

        @Override
        public void toggle()
        {
            int index;

            if(currentDim==null)
            {
                index = 0;
            }
            else
            {
                index = Arrays.binarySearch(dimensions, currentDim) + 1;
            }

            if(index==dimensions.length || index==-1)
            {
                currentDim = null;
            }
            else
            {
                currentDim = dimensions[index];
            }

            updateLabel();
        }
    }
}
