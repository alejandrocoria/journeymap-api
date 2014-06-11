package net.techbrew.journeymap.feature;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.Constants;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Do not re-order the enum values, since the order is implicit in packet data.
 * Changing the order would break backward compat with the server plugin.
 */
public enum Feature
{
	/** Universal features (true,true) **/
	// None
	
	/** Singleplayer-only features (true,false) **/
	// None
	
	/** Multiplayer-only features (false,true) **/
    MapCaves("Feature.name_map_caves",false,true),
    RadarAnimals("Feature.name_radar_animals",false,true),
    RadarMobs("Feature.name_radar_mobs",false,true),
    RadarPlayers("Feature.name_radar_players",false,true),
	RadarVillagers("Feature.name_radar_villagers",false,true);

	private final String key;
	private final boolean restrictInSingleplayer;
	private final boolean restrictInMultiplayer;
	
	private Feature(String key, boolean singlePlayer, boolean multiPlayer) {
        this.key = key;
		restrictInMultiplayer = multiPlayer;
		restrictInSingleplayer = singlePlayer;
	}
	
	public boolean isCurrentlyRestricted(Minecraft mc) {
		if(restrictInMultiplayer && restrictInSingleplayer) {
			return true;
		} else {
            boolean isSinglePlayer = mc.isSingleplayer() && !mc.getIntegratedServer().getPublic();

			if(restrictInSingleplayer && isSinglePlayer) {
				return true;
			}
			if(restrictInMultiplayer && !isSinglePlayer) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get a subset of features by what they restrict.
	 * @param restrictSingleplayer
	 * @param restrictMultiplayer
	 * @return
	 */
	public static Set<Feature> getSubset(boolean restrictSingleplayer, boolean restrictMultiplayer) {
		Set<Feature> subset = EnumSet.noneOf(Feature.class);
		for(Feature feature : Feature.values()) {
			if((restrictSingleplayer && feature.restrictInSingleplayer)
					|| restrictMultiplayer && feature.restrictInMultiplayer) {
				subset.add(feature);
			}
		}
		return Collections.unmodifiableSet(subset);
	}

    public String getLocalizedName()
    {
        return Constants.getString(this.key);
    }
}
