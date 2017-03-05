/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.waypoint;

import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.cartography.RGB;
import journeymap.client.command.CmdTeleportWaypoint;
import journeymap.client.model.Waypoint;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.component.Button;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.OnOffButton;
import journeymap.client.ui.component.ScrollListPane;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.option.SlotMetadata;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.awt.*;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author techbrew 3/15/14.
 */
public class WaypointManagerItem implements ScrollListPane.ISlot
{

    /**
     * The Background.
     */
    static Integer background = new Color(20, 20, 20).getRGB();
    /**
     * The Background hover.
     */
    static Integer backgroundHover = new Color(40, 40, 40).getRGB();
    /**
     * The Font renderer.
     */
    final FontRenderer fontRenderer;
    /**
     * The Manager.
     */
    final WaypointManager manager;
    /**
     * The X.
     */
    int x;
    /**
     * The Y.
     */
    int y;
    /**
     * The Width.
     */
    int width;
    /**
     * The Internal width.
     */
    int internalWidth;
    /**
     * The Distance.
     */
    Integer distance;
    /**
     * The Waypoint.
     */
    Waypoint waypoint;
    /**
     * The Button enable.
     */
    OnOffButton buttonEnable;
    /**
     * The Button remove.
     */
    Button buttonRemove;
    /**
     * The Button edit.
     */
    Button buttonEdit;
    /**
     * The Button find.
     */
    Button buttonFind;
    /**
     * The Button teleport.
     */
    Button buttonTeleport;
    /**
     * The Button chat.
     */
    Button buttonChat;
    /**
     * The Hgap.
     */
    int hgap = 4;
    /**
     * The Button list left.
     */
    ButtonList buttonListLeft;
    /**
     * The Button list right.
     */
    ButtonList buttonListRight;
    /**
     * The Slot index.
     */
    int slotIndex; // TODO
    /**
     * The Slot metadata.
     */
    SlotMetadata<Waypoint> slotMetadata;

    /**
     * Instantiates a new Waypoint manager item.
     *
     * @param waypoint     the waypoint
     * @param fontRenderer the font renderer
     * @param manager      the manager
     */
    public WaypointManagerItem(Waypoint waypoint, FontRenderer fontRenderer, WaypointManager manager)
    {
        int id = 0;
        this.waypoint = waypoint;
        this.fontRenderer = fontRenderer;
        this.manager = manager;

        SlotMetadata<Waypoint> slotMetadata = new SlotMetadata<Waypoint>(null, null, null, false); // TODO


        String on = Constants.getString("jm.common.on");
        String off = Constants.getString("jm.common.off");

        buttonEnable = new OnOffButton(on, off, true);
        buttonEnable.setToggled(waypoint.isEnable());
        buttonFind = new Button(Constants.getString("jm.waypoint.find"));

        buttonTeleport = new Button(Constants.getString("jm.waypoint.teleport"));
        JourneymapClient jm = Journeymap.getClient();

        if (jm.isServerEnabled())
        {
            buttonTeleport.setDrawButton(jm.isServerTeleportEnabled());
            buttonTeleport.setEnabled(jm.isServerTeleportEnabled());
        }
        else
        {
            buttonTeleport.setDrawButton(manager.canUserTeleport);
            buttonTeleport.setEnabled(manager.canUserTeleport);
        }

        buttonListLeft = new ButtonList(buttonEnable, buttonFind, buttonTeleport);
        buttonListLeft.setHeights(manager.rowHeight);
        buttonListLeft.fitWidths(fontRenderer);

        buttonEdit = new Button(Constants.getString("jm.waypoint.edit"));
        buttonRemove = new Button(Constants.getString("jm.waypoint.remove"));
        buttonChat = new Button(Constants.getString("jm.waypoint.chat"));
        buttonChat.setTooltip(Constants.getString("jm.waypoint.chat.tooltip"));

        buttonListRight = new ButtonList(buttonChat, buttonEdit, buttonRemove);
        buttonListRight.setHeights(manager.rowHeight);
        buttonListRight.fitWidths(fontRenderer);

        this.internalWidth = fontRenderer.getCharWidth('X') * 32; // Label width
        internalWidth += Math.max(manager.colLocation, manager.colName); // Add other columns
        internalWidth += buttonListLeft.getWidth(hgap); // Add buttons
        internalWidth += buttonListRight.getWidth(hgap); // Add buttons
        internalWidth += 10; // Pad that action
    }

    /**
     * Gets slot index.
     *
     * @return the slot index
     */
    public int getSlotIndex()
    {
        return slotIndex;
    }

    /**
     * Sets slot index.
     *
     * @param slotIndex the slot index
     */
    public void setSlotIndex(int slotIndex)
    {
        this.slotIndex = slotIndex;
    }

    /**
     * Sets position.
     *
     * @param x the x
     * @param y the y
     */
//@Override
    public void setPosition(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets x.
     *
     * @return the x
     */
//@Override
    public int getX()
    {
        return x;
    }

    /**
     * Gets y.
     *
     * @return the y
     */
//@Override
    public int getY()
    {
        return y;
    }

    /**
     * Gets width.
     *
     * @return the width
     */
//@Override
    public int getWidth()
    {
        return width;
    }

    /**
     * Sets width.
     *
     * @param width the width
     */
//@Override
    public void setWidth(int width)
    {
        this.width = width;
    }

    /**
     * Gets fit width.
     *
     * @param fr the fr
     * @return the fit width
     */
//@Override
    public int getFitWidth(FontRenderer fr)
    {
        return width;
    }

    /**
     * Gets height.
     *
     * @return the height
     */
//@Override
    public int getHeight()
    {
        return manager.rowHeight;
    }

    /**
     * Draw partial scrollable.
     *
     * @param mc     the mc
     * @param x      the x
     * @param y      the y
     * @param width  the width
     * @param height the height
     */
//@Override
    public void drawPartialScrollable(Minecraft mc, int x, int y, int width, int height)
    {
        DrawUtil.drawRectangle(this.x, this.y, this.width, manager.rowHeight, background, .4f);
    }

    /**
     * Draw labels.
     *
     * @param mc    the mc
     * @param x     the x
     * @param y     the y
     * @param color the color
     */
    protected void drawLabels(Minecraft mc, int x, int y, Integer color)
    {
        if (this.waypoint == null)
        {
            return;
        }

        boolean waypointValid = waypoint.isEnable() && waypoint.isInPlayerDimension();

        if (color == null)
        {
            color = waypointValid ? waypoint.getSafeColor() : RGB.GRAY_RGB;
        }

        FontRenderer fr = FMLClientHandler.instance().getClient().fontRendererObj;

        int yOffset = 1 + (this.manager.rowHeight - fr.FONT_HEIGHT) / 2;
        fr.drawStringWithShadow(String.format("%sm", getDistance()), x + manager.colLocation, y + yOffset, color);

        String name = waypointValid ? waypoint.getName() : TextFormatting.STRIKETHROUGH + waypoint.getName();
        fr.drawStringWithShadow(name, manager.colName, y + yOffset, color);
    }

    /**
     * Draw waypoint.
     *
     * @param x the x
     * @param y the y
     */
    protected void drawWaypoint(int x, int y)
    {
        TextureImpl wpTexture = waypoint.getTexture();
        DrawUtil.drawColoredImage(wpTexture, waypoint.getColor(), 1f, x, y - (wpTexture.getHeight() / 2), 0);
    }

    /**
     * Enable waypoint.
     *
     * @param enable the enable
     */
    protected void enableWaypoint(boolean enable)
    {
        buttonEnable.setToggled(enable);
        waypoint.setEnable(enable);
    }

    /**
     * Gets button enable center x.
     *
     * @return the button enable center x
     */
    protected int getButtonEnableCenterX()
    {
        return buttonEnable.getCenterX();
    }

    /**
     * Gets name left x.
     *
     * @return the name left x
     */
    protected int getNameLeftX()
    {
        return this.x + manager.getMargin() + manager.colName;
    }

    /**
     * Gets location left x.
     *
     * @return the location left x
     */
    protected int getLocationLeftX()
    {
        return this.x + manager.getMargin() + manager.colLocation;
    }

    /**
     * Click scrollable boolean.
     *
     * @param mouseX the mouse x
     * @param mouseY the mouse y
     * @return the boolean
     */
//@Override
    public boolean clickScrollable(int mouseX, int mouseY)
    {
        boolean mouseOver = false;
        if (waypoint == null)
        {
            return false;
        }

        if (buttonChat.mouseOver(mouseX, mouseY))
        {
            FMLClientHandler.instance().getClient().displayGuiScreen(new WaypointChat(this.waypoint));
            mouseOver = true;
        }
        else if (buttonRemove.mouseOver(mouseX, mouseY))
        {
            manager.removeWaypoint(this);
            this.waypoint = null;
            mouseOver = true;
        }
        else
        {
            if (buttonEnable.mouseOver(mouseX, mouseY))
            {
                buttonEnable.toggle();
                waypoint.setEnable(buttonEnable.getToggled());
                if (waypoint.isDirty())
                {
                    WaypointStore.INSTANCE.save(waypoint);
                }
                mouseOver = true;
            }
            else
            {
                if (buttonEdit.mouseOver(mouseX, mouseY))
                {
                    UIManager.INSTANCE.openWaypointEditor(waypoint, false, manager);
                    mouseOver = true;
                }
                else
                {
                    if (buttonFind.isEnabled() && buttonFind.mouseOver(mouseX, mouseY))
                    {
                        UIManager.INSTANCE.openFullscreenMap(waypoint);
                        mouseOver = true;
                    }
                    else
                    {
                        if (manager.canUserTeleport && buttonTeleport.mouseOver(mouseX, mouseY))
                        {
                            new CmdTeleportWaypoint(waypoint).run();
                            Fullscreen.state().follow.set(true);
                            UIManager.INSTANCE.closeAll();
                            mouseOver = true;
                        }
                    }
                }
            }
        }

        return mouseOver;
    }

    /**
     * Gets distance.
     *
     * @return the distance
     */
    public int getDistance()
    {
        return distance == null ? 0 : distance;
    }

    /**
     * Returns the squared distance to the entity. Only calculated once.
     *
     * @param player the player
     * @return the distance to
     */
    public int getDistanceTo(EntityPlayer player)
    {
        if (distance == null)
        {
            distance = (int) player.getPositionVector().distanceTo(waypoint.getPosition());
        }
        return distance;
    }

    @Override
    public Collection<SlotMetadata> getMetadata()
    {
        return null;
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
    {
        Minecraft mc = manager.getMinecraft();
        this.width = listWidth;
        setPosition(x, y);

        if (this.waypoint == null)
        {
            return;
        }

        boolean hover = manager.isSelected(this) || (mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + manager.rowHeight);

        buttonListLeft.setOptions(true, hover, true);
        buttonListRight.setOptions(true, hover, true);

        Integer color = hover ? backgroundHover : background;
        float alpha = hover ? 1f : .4f;
        DrawUtil.drawRectangle(this.x, this.y, this.width, manager.rowHeight, color, alpha);

        int margin = manager.getMargin();
        drawWaypoint(this.x + margin + manager.colWaypoint, this.y + (manager.rowHeight / 2));
        drawLabels(mc, this.x + margin, this.y, null);

        buttonFind.setEnabled(waypoint.isInPlayerDimension());

        buttonTeleport.setEnabled(waypoint.isTeleportReady());

        buttonListRight.layoutHorizontal(x + width - margin, y, false, hgap).draw(mc, mouseX, mouseY);
        buttonListLeft.layoutHorizontal(buttonListRight.getLeftX() - (hgap * 2), y, false, hgap).draw(mc, mouseX, mouseY);
    }

    @Override
    public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
    {
        // ?
    }


    @Override
    public boolean mousePressed(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
        return this.clickScrollable(x, y);
    }

    @Override
    public String[] mouseHover(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
        for (Button button : buttonListLeft)
        {
            if (button.isMouseOver())
            {
                manager.drawHoveringText(button.getTooltip(), x, y, FMLClientHandler.instance().getClient().fontRendererObj);
            }
        }

        return new String[0]; // TODO
    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
        // TODO
    }

    @Override
    public boolean keyTyped(char c, int i)
    {
        return false; // TODO
    }

    @Override
    public List<ScrollListPane.ISlot> getChildSlots(int listWidth, int columnWidth)
    {
        return null; // TODO
    }

    @Override
    public SlotMetadata getLastPressed()
    {
        return null; // TODO
    }

    @Override
    public SlotMetadata getCurrentTooltip()
    {
        return null; // TODO
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        buttonEnable.setToggled(waypoint.isEnable());
    }

    @Override
    public int getColumnWidth()
    {
        return width;
    }

    @Override
    public boolean contains(SlotMetadata slotMetadata)
    {
        return false;
    }

    /**
     * The type Sort.
     */
    abstract static class Sort implements Comparator<WaypointManagerItem>
    {
        /**
         * The Ascending.
         */
        boolean ascending;

        /**
         * Instantiates a new Sort.
         *
         * @param ascending the ascending
         */
        Sort(boolean ascending)
        {
            this.ascending = ascending;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            return (ascending ? 1 : 0);
        }
    }

    /**
     * The type Name comparator.
     */
    static class NameComparator extends Sort
    {
        /**
         * Instantiates a new Name comparator.
         *
         * @param ascending the ascending
         */
        public NameComparator(boolean ascending)
        {
            super(ascending);
        }

        @Override
        public int compare(WaypointManagerItem o1, WaypointManagerItem o2)
        {
            if (ascending)
            {
                return o1.waypoint.getName().compareToIgnoreCase(o2.waypoint.getName());
            }
            else
            {
                return o2.waypoint.getName().compareToIgnoreCase(o1.waypoint.getName());
            }
        }
    }

    /**
     * The type Distance comparator.
     */
    static class DistanceComparator extends Sort
    {
        /**
         * The Player.
         */
        EntityPlayer player;

        /**
         * Instantiates a new Distance comparator.
         *
         * @param player    the player
         * @param ascending the ascending
         */
        public DistanceComparator(EntityPlayer player, boolean ascending)
        {
            super(ascending);
            this.player = player;
        }

        @Override
        public int compare(WaypointManagerItem o1, WaypointManagerItem o2)
        {
            double dist1 = o1.getDistanceTo(player);
            double dist2 = o2.getDistanceTo(player);

            if (ascending)
            {
                return Double.compare(dist1, dist2);
            }
            else
            {
                return Double.compare(dist2, dist1);
            }
        }
    }

}
