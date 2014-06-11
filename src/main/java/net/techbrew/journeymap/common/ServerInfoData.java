package net.techbrew.journeymap.common;

import com.google.gson.GsonBuilder;
import net.minecraft.world.World;
import net.techbrew.journeymap.feature.Feature;

import java.util.Map;

/**
* Created by mwoodman on 6/10/2014.
*/
public class ServerInfoData extends PacketData
{
    public transient static long SALT = 11569;

    public long hash;
    public Map<Feature, Boolean> features;

    public ServerInfoData(World world, Map<Feature, Boolean> features)
    {
        super(ShortCode.ServerInfo);
        this.features = features;
        this.hash = Math.abs(world.getSeed()) - SALT - world.getWorldInfo().getWorldName().hashCode();
    }

    @Override
    protected String getJson()
    {
        return new GsonBuilder().create().toJson(this);
    }
}
