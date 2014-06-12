package net.techbrew.journeymap.common;

import com.google.gson.GsonBuilder;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;

import java.util.Map;

/**
 * Client Info sent to server
 */
public class ClientInfoData implements IMessage
{
    // Version
    public String version;

    // Edition
    public String edition;

    // Features
    Map<Feature, Boolean> features;

    public ClientInfoData()
    {
    }

    public static ClientInfoData create()
    {
        ClientInfoData data = new ClientInfoData();
        data.version = FeatureManager.instance().getFeatureSetName();
        data.edition = JourneyMap.JM_VERSION;
        data.features = FeatureManager.instance().getAllowedFeatures();
        return data;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        // Read data to string
        String stringData = ByteBufUtils.readUTF8String(buf);

        ClientInfoData copy =  new GsonBuilder().create().fromJson(stringData, getClass());
        this.version = copy.version;
        this.edition = copy.edition;
        this.features = copy.features;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        String stringData = new GsonBuilder().create().toJson(this);
        ByteBufUtils.writeUTF8String(buf, stringData);
    }
}
