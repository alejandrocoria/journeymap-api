package net.techbrew.journeymap.common;

import com.google.gson.GsonBuilder;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;

import java.util.Map;

/**
 * Client Info sent to server
 */
public class ClientInfoData extends PacketData
{
    // Version
    public String version;

    // Edition
    public String edition;

    // Features
    Map<Feature, Boolean> features;

    public static ClientInfoData create()
    {
        ClientInfoData data = new ClientInfoData();
        data.version = FeatureManager.instance().getFeatureSetName();
        data.edition = JourneyMap.JM_VERSION;
        data.features = FeatureManager.instance().getAllowedFeatures();
        return data;
    }

    public ClientInfoData()
    {
        super(ShortCode.ClientInfo);
    }

    @Override
    protected String getJson()
    {
        return new GsonBuilder().create().toJson(this);
    }
}
