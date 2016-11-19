/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.waypoint;

import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.cartography.RGB;
import journeymap.client.command.CmdTeleportWaypoint;
import journeymap.client.forge.helper.ForgeHelper;
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
 * Created by Mark on 3/15/14.
 */
public class WaypointManagerItem implements ScrollListPane.ISlot
{

    static Integer background = new Color(20, 20, 20).getRGB();
    static Integer backgroundHover = new Color(40, 40, 40).getRGB();
    final FontRenderer fontRenderer;
    final WaypointManager manager;
    int x;
    int y;
    int width;
    int internalWidth;
    Integer distance;
    Waypoint waypoint;
    OnOffButton buttonEnable;
    Button buttonRemove;
    Button buttonEdit;
    Button buttonFind;
    Button buttonTeleport;
    Button buttonChat;
    int hgap = 4;
    ButtonList buttonListLeft;
    ButtonList buttonListRight;
    int slotIndex; // TODO
    SlotMetadata<Waypoint> slotMetadata;

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

    public int getSlotIndex()
    {
        return slotIndex;
    }

    public void setSlotIndex(int slotIndex)
    {
        this.slotIndex = slotIndex;
    }

    //@Override
    public void setPosition(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    //@Override
    public int getX()
    {
        return x;
    }

    //@Override
    public int getY()
    {
        return y;
    }

    //@Override
    public int getWidth()
    {
        return width;
    }

    //@Override
    public void setWidth(int width)
    {
        this.width = width;
    }

    //@Override
    public int getFitWidth(FontRenderer fr)
    {
        return width;
    }

    //@Override
    public int getHeight()
    {
        return manager.rowHeight;
    }

    //@Override
    public void drawPartialScrollable(Minecraft mc, int x, int y, int width, int height)
    {
        DrawUtil.drawRectangle(this.x, this.y, this.width, manager.rowHeight, background, .4f);
    }

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

        FontRenderer fr = ForgeHelper.INSTANCE.getFontRenderer();

        int yOffset = 1 + (this.manager.rowHeight - fr.FONT_HEIGHT) / 2;
        fr.drawStringWithShadow(String.format("%sm", getDistance()), x + manager.colLocation, y + yOffset, color);

        String name = waypointValid ? waypoint.getName() : TextFormatting.STRIKETHROUGH + waypoint.getName();
        fr.drawStringWithShadow(name, manager.colName, y + yOffset, color);
    }

    protected void drawWaypoint(int x, int y)
    {
        TextureImpl wpTexture = waypoint.getTexture();
        DrawUtil.drawColoredImage(wpTexture, waypoint.getColor(), 1f, x, y - (wpTexture.getHeight() / 2), 0);
    }

    protected void enableWaypoint(boolean enable)
    {
        buttonEnable.setToggled(enable);
        waypoint.setEnable(enable);
    }

    protected int getButtonEnableCenterX()
    {
        return buttonEnable.getCenterX();
    }

    protected int getNameLeftX()
    {
        return this.x + manager.getMargin() + manager.colName;
    }

    protected int getLocationLeftX()
    {
        return this.x + manager.getMargin() + manager.colLocation;
    }

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

    public int getDistance()
    {
        return distance == null ? 0 : distance;
    }

    /**
     * Returns the squared distance to the entity. Only calculated once.
     */
    public int getDistanceTo(EntityPlayer player)
    {
        if (distance == null)
        {
            distance = (int) ForgeHelper.INSTANCE.getEntityPositionVector(player).distanceTo(waypoint.getPosition());
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
                manager.drawHoveringText(button.getTooltip(), x, y, ForgeHelper.INSTANCE.getFontRenderer());
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

    abstract static class Sort implements Comparator<WaypointManagerItem>
    {
        boolean ascending;

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

    static class NameComparator extends Sort
    {
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

    static class DistanceComparator extends Sort
    {
        EntityPlayer player;

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
