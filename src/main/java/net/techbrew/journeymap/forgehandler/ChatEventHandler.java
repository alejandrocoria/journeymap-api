package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.techbrew.journeymap.feature.FeatureManager;

import java.util.EnumSet;
import java.util.Set;

/**
 * Created by mwoodman on 1/29/14.
 */
@SideOnly(Side.CLIENT)
public class ChatEventHandler implements EventHandlerManager.EventHandler
{

    Set<String> featureControlCodes = FeatureManager.instance().getControlCodes();

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus()
    {
        return EnumSet.of(EventHandlerManager.BusType.MinecraftForgeBus);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void invoke(ClientChatReceivedEvent event)
    {
        if (event.message != null)
        {
            checkForControlCode(event.message.getFormattedText());
        }
    }

    private void checkForControlCode(String text)
    {
        if (text.contains("§3"))
        {
            text = text.replaceAll("§r", "");
            for (String code : featureControlCodes)
            {
                if (text.contains(code))
                {
                    FeatureManager.instance().handleControlCode(code);
                }
            }
        }
    }
}
