package net.techbrew.journeymap.ui.waypoint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.command.CommandServerTp;
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

	private enum ButtonEnum {Add, Find, SortName, SortDistance, Dimensions, Close};

    protected int rowHeight = 16;
    protected int colWaypoint = 0;
    protected int colLocation = 20;
    protected int colName = 60;

    protected Boolean canUserTeleport;

    private SortButton buttonSortName, buttonSortDistance;
    private DimensionsButton buttonDimensions;
    private Button buttonClose, buttonAdd;

    private ButtonList bottomButtons;

    private ArrayList<WaypointManagerItem> items = new ArrayList<WaypointManagerItem>();

    private ScrollPane itemScrollPane;

    private WaypointManagerItem.Sort currentSort;

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
                    CommandServerTp command = new CommandServerTp();
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
                    colName = Math.max(colName, colLocation + buttonSortDistance.getWidth() + 5);
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

                buttonClose = new Button(ButtonEnum.Close, Constants.getString("MapOverlay.close"));
                //buttonClose.fitWidth(getFontRenderer());
                buttonList.add(buttonClose);

                bottomButtons = new ButtonList(buttonDimensions, buttonAdd, buttonClose);
            }

            if (this.items.isEmpty())
            {
                updateItems();
                updateSort(buttonSortName);
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

        //initGui();

        // Header buttons
        int pad = 3;
        final int headerY = headerHeight + pad;
        final int headerX = itemScrollPane.getX();
        buttonSortName.setPosition(headerX + colName, headerY);
        buttonSortDistance.setPosition(headerX + colLocation, headerY);

        // Scroll pane
        int hgap = 4;
        int vgap = 5;
        int bottomButtonsHeight = buttonClose.getHeight() + (vgap*2);
        final int startY = headerHeight + vgap + getFontRenderer().FONT_HEIGHT;
        int scrollWidth = this.width-6;
        itemScrollPane.position(scrollWidth, this.height, startY, bottomButtonsHeight, 0, 0);

        // Bottom buttons
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
                    if(currentSort==sortButton.sort)
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

    protected class DimensionsButton extends Button
    {
        final Integer[] dimensions;
        Integer currentDim;

        public DimensionsButton(int id)
        {
            super(id, 0, 0, "");
            dimensions = WorldData.getDimensions();
            currentDim = Minecraft.getMinecraft().thePlayer.dimension;
            updateLabel();

            // Determine width
            int maxWidth = 0;
            for(Integer dim : dimensions)
            {
                String name = Constants.getString("Waypoint.dimension", WorldData.getDimensionName(dim));
                maxWidth = Math.max(maxWidth, getFontRenderer().getStringWidth(name));
            }
            this.width = maxWidth+12;
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