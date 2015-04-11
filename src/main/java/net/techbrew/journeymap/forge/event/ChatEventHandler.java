package net.techbrew.journeymap.forge.event;

import com.google.common.base.Strings;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.ui.UIManager;

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
            try
            {
                String text = event.message.getFormattedText();
                if (!Strings.isNullOrEmpty(text))
                {
                    checkForControlCode(text.replaceAll(EnumChatFormatting.RESET.toString(), ""));
                }
            }
            catch (Exception e)
            {
                JourneyMap.getLogger().warn("Unexpected exception on ClientChatReceivedEvent: " + LogFormatter.toString(e));
            }
        }
    }

    private void checkForControlCode(String text)
    {
        if (text.contains("\u00a7"))
        {
            boolean resetRequired = false;
            for (String code : featureControlCodes)
            {
                if (text.contains(code))
                {
                    FeatureManager.instance().handleControlCode(code);
                    resetRequired = true;
                }
            }
            if (resetRequired)
            {
                DataCache.instance().purge();
                UIManager.getInstance().reset();
            }
        }
    }
}