/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.waypoint;

import journeymap.client.model.Waypoint;
import net.minecraft.client.gui.GuiChat;

/**
 * Workaround for the inability to move the cursor on a normal GuiChat.
 */
public class WaypointChat extends GuiChat
{
    public WaypointChat(Waypoint waypoint)
    {
        this(waypoint.toChatString());
    }

    public WaypointChat(String text)
    {
        super(text);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.inputField.setCursorPositionZero();
    }
}
