package net.techbrew.journeymap.common;

import com.google.gson.GsonBuilder;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;
import net.techbrew.journeymap.feature.Feature;

import java.util.Map;

/**
* Data sent by server to client.
*/
public class ServerInfoData implements IMessage
{
    public transient static long SALT = 11569;

    public long hash;
    public Map<Feature, Boolean> features;

    public ServerInfoData()
    {

    }

    public ServerInfoData(World world, Map<Feature, Boolean> features)
    {
        this.features = features;
        this.hash = Math.abs(world.getSeed()) - SALT - world.getWorldInfo().getWorldName().hashCode();
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        // Read data to string
        String stringData = ByteBufUtils.readUTF8String(buf);

        ServerInfoData copy =  new GsonBuilder().create().fromJson(stringData, getClass());
        this.hash = copy.hash;
        this.features = copy.features;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        String stringData = new GsonBuilder().create().toJson(this);
        ByteBufUtils.writeUTF8String(buf, stringData);
    }
}
