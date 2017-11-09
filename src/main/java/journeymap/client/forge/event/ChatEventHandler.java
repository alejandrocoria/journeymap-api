/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.forge.event;

import com.google.common.base.Strings;
import journeymap.client.waypoint.WaypointParser;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The type Chat event handler.
 *
 * @author techbrew 1/29/14.
 */
@SideOnly(Side.CLIENT)
public class ChatEventHandler implements EventHandlerManager.EventHandler
{
    /**
     * Invoke.
     *
     * @param event the event
     */
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void invoke(ClientChatReceivedEvent event)
    {
        if (event.getMessage() != null)
        {
            try
            {
                String text = event.getMessage().getFormattedText();
                if (!Strings.isNullOrEmpty(text))
                {
                    WaypointParser.parseChatForWaypoints(event, text);
                }
            }
            catch (Exception e)
            {
                Journeymap.getLogger().warn("Unexpected exception on ClientChatReceivedEvent: " + LogFormatter.toString(e));
            }
        }
    }
}
