package net.techbrew.journeymap.ui.waypoint;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.command.CmdTeleportWaypoint;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.ButtonList;
import net.techbrew.journeymap.ui.ScrollPane;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.map.MapOverlay;
import net.techbrew.journeymap.waypoint.WaypointStore;

import java.awt.*;
import java.util.Comparator;

/**
 * Created by Mark on 3/15/14.
 */
public class WaypointManagerItem implements ScrollPane.Scrollable
{

    final FontRenderer fontRenderer;
    final WaypointManager manager;

    int x;
    int y;
    int width;
    int internalWidth;

    Integer distance;
    Waypoint waypoint;

    Button buttonEnable;
    Button buttonRemove;
    Button buttonEdit;
    Button buttonFind;
    Button buttonTeleport;

    int hgap = 4;
    ButtonList buttonListLeft;
    ButtonList buttonListRight;

    static Color background = new Color(20, 20, 20);
    static Color backgroundHover = new Color(40, 40, 40);

    public WaypointManagerItem(Waypoint waypoint, FontRenderer fontRenderer, WaypointManager manager)
    {
        int id = 0;
        this.waypoint = waypoint;
        this.fontRenderer = fontRenderer;
        this.manager = manager;

        String on = Constants.getString("MapOverlay.on");
        String off = Constants.getString("MapOverlay.off");

        buttonEnable = new Button(id++, on, off, true); //$NON-NLS-1$
        buttonEnable.setToggled(waypoint.isEnable());
        buttonFind = new Button(id++, Constants.getString("Waypoint.find")); //$NON-NLS-1$

        buttonTeleport = new Button(id++, Constants.getString("Waypoint.teleport")); //$NON-NLS-1$
        buttonTeleport.setDrawButton(manager.canUserTeleport);
        buttonTeleport.setEnabled(manager.canUserTeleport);

        buttonListLeft = new ButtonList(buttonEnable, buttonFind, buttonTeleport);
        buttonListLeft.setHeights(manager.rowHeight);
        buttonListLeft.fitWidths(fontRenderer);

        buttonEdit = new Button(id++, Constants.getString("Waypoint.edit")); //$NON-NLS-1$
        buttonRemove = new Button(id++, Constants.getString("Waypoint.remove")); //$NON-NLS-1$

        buttonListRight = new ButtonList(buttonEdit, buttonRemove);
        buttonListRight.setHeights(manager.rowHeight);
        buttonListRight.fitWidths(fontRenderer);

        this.internalWidth = fontRenderer.getCharWidth('X') * 32; // Label width
        internalWidth += Math.max(manager.colLocation, manager.colName); // Add other columns
        internalWidth += buttonListLeft.getWidth(hgap); // Add buttons
        internalWidth += buttonListRight.getWidth(hgap); // Add buttons
        internalWidth += 10; // Pad that action
    }

    @Override
    public void setPosition(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    @Override
    public void setWidth(int width)
    {
        this.width = width;
    }

    @Override
    public int getX()
    {
        return x;
    }

    @Override
    public int getY()
    {
        return y;
    }

    @Override
    public int getWidth()
    {
        return width;
    }

    @Override
    public int getFitWidth(FontRenderer fr)
    {
        return width;
    }

    @Override
    public int getHeight()
    {
        return manager.rowHeight;
    }

    @Override
    public void drawPartialScrollable(Minecraft mc, int x, int y, int width, int height)
    {
        DrawUtil.drawRectangle(this.x, this.y, this.width, manager.rowHeight, background, 100);
    }

    @Override
    public void drawScrollable(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.waypoint == null)
        {
            return;
        }

        boolean hover = manager.isSelected(this) || (mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + manager.rowHeight);

        buttonListLeft.setOptions(true, hover, true);
        buttonListRight.setOptions(true, hover, true);

        Color color = hover ? backgroundHover : background;
        int alpha = hover ? 255 : 100;
        DrawUtil.drawRectangle(this.x, this.y, this.width, manager.rowHeight, color, alpha);

        int margin = manager.getMargin();
        drawWaypoint(this.x + margin + manager.colWaypoint, this.y + (manager.rowHeight / 2));
        drawLabels(mc, this.x + margin, this.y, null);

        buttonFind.setEnabled(waypoint.isInPlayerDimension());

        buttonTeleport.setEnabled(waypoint.isTeleportReady());

        buttonListRight.layoutHorizontal(x + width - margin, y, false, hgap).draw(mc, mouseX, mouseY);
        buttonListLeft.layoutHorizontal(buttonListRight.getLeftX() - (hgap * 2), y, false, hgap).draw(mc, mouseX, mouseY);
    }

    protected void drawLabels(Minecraft mc, int x, int y, Color color)
    {
        if (this.waypoint == null)
        {
            return;
        }

        boolean waypointValid = waypoint.isEnable() && waypoint.isInPlayerDimension();

        if (color == null)
        {
            color = waypointValid ? waypoint.getSafeColor() : Color.GRAY;
        }

        int yOffset = 1 + (this.manager.rowHeight - mc.fontRenderer.FONT_HEIGHT) / 2;
        mc.fontRenderer.drawStringWithShadow(String.format("%sm", getDistance()), x + manager.colLocation, y + yOffset, color.getRGB());

        String name = waypointValid ? waypoint.getName() : EnumChatFormatting.STRIKETHROUGH + waypoint.getName();
        mc.fontRenderer.drawStringWithShadow(name, x + manager.colName, y + yOffset, color.getRGB());
    }

    protected void drawWaypoint(int x, int y)
    {
        TextureImpl wpTexture = waypoint.getTexture();
        DrawUtil.drawColoredImage(wpTexture, 255, waypoint.getColor(), x, y - (wpTexture.height / 2));
    }

    protected void enableWaypoint(boolean enable)
    {
        buttonEnable.setToggled(enable);
        waypoint.setEnable(enable);
    }

    protected int getButtonEnableCenterX()
    {
        return buttonEnable.getX() + (buttonEnable.getWidth() / 2);
    }

    @Override
    public void clickScrollable(Minecraft mc, int mouseX, int mouseY)
    {

        if (waypoint == null)
        {
            return;
        }

        if (buttonRemove.mouseOver(mouseX, mouseY))
        {
            manager.removeWaypoint(this);
            this.waypoint = null;
        }
        else
        {
            if (buttonEnable.mouseOver(mouseX, mouseY))
            {
                buttonEnable.toggle();
                waypoint.setEnable(buttonEnable.getToggled());
                if (waypoint.isDirty())
                {
                    WaypointStore.instance().save(waypoint);
                }
            }
            else
            {
                if (buttonEdit.mouseOver(mouseX, mouseY))
                {
                    UIManager.getInstance().openWaypointEditor(waypoint, false, WaypointManager.class);
                }
                else
                {
                    if (buttonFind.isEnabled() && buttonFind.mouseOver(mouseX, mouseY))
                    {
                        UIManager.getInstance().openMap(waypoint);
                    }
                    else
                    {
                        if (manager.canUserTeleport && buttonTeleport.mouseOver(mouseX, mouseY))
                        {
                            new CmdTeleportWaypoint(waypoint).run();
                            MapOverlay.state().follow = true;
                            UIManager.getInstance().closeAll();
                            return;
                        }
                    }
                }
            }
        }
    }

    public int getDistance()
    {
        return distance == null ? 0 : distance;
    }

    /**
     * Returns the squared distance to the entity. Only calculated once.
     */
    public int getDistanceSqToEntity(EntityPlayer player)
    {
        int dimension = FMLClientHandler.instance().getClient().thePlayer.worldObj.provider.dimensionId;
        if (distance == null)
        {
            double d0 = this.waypoint.getX(dimension) - player.posX;
            double d1 = this.waypoint.getY(dimension) - player.posY;
            double d2 = this.waypoint.getZ(dimension) - player.posZ;
            distance = (int) Math.round(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2));
        }
        return distance;
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
                return o1.waypoint.getName().compareTo(o2.waypoint.getName());
            }
            else
            {
                return o2.waypoint.getName().compareTo(o1.waypoint.getName());
            }
        }
    }

    static class DistanceComparator extends Sort
    {
        final EntityPlayer player;

        public DistanceComparator(EntityPlayer player, boolean ascending)
        {
            super(ascending);
            this.player = player;
        }

        @Override
        public int compare(WaypointManagerItem o1, WaypointManagerItem o2)
        {
            if (ascending)
            {
                return Double.compare(o1.getDistanceSqToEntity(player), o2.getDistanceSqToEntity(player));
            }
            else
            {
                return Double.compare(o2.getDistanceSqToEntity(player), o1.getDistanceSqToEntity(player));
            }
        }
    }

}
