package journeymap.common.network;

import com.google.gson.JsonObject;
import journeymap.common.Journeymap;
import journeymap.common.network.impl.MessageProcessor;
import journeymap.common.network.impl.Response;
import journeymap.common.util.PlayerConfigController;
import net.minecraft.entity.player.EntityPlayerMP;

public class GetClientConfig extends MessageProcessor
{
    @Override
    protected JsonObject onServer(Response response)
    {
        EntityPlayerMP player = response.getContext().getServerHandler().player;

        return PlayerConfigController.getInstance().getPlayerConfig(player);
    }



    @Override
    public JsonObject onClient(Response response)
    {
        Journeymap.getClient().setJourneyMapServerConnection(true);
        // do nothing, handled by the callback.
        return null;
    }
}
