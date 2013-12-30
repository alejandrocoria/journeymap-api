package net.techbrew.mcjm.feature;

import net.minecraft.client.Minecraft;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

;

public enum Feature {

	/** Default feature is simply a place-holder. **/
	NoOp(false,false),
	
	/** Universal features **/
	// None
	
	/** Singleplayer-only features **/
	// None
	
	/** Multiplayer-only features **/
	RadarPlayers(false,true),
	RadarAnimals(false,true),
	RadarMobs(false,true),
	RadarVillagers(false,true),
	MapCaves(false,true);
	
	private final boolean restrictInSingleplayer;
	private final boolean restrictInMultiplayer;
	
	private Feature(boolean singlePlayer, boolean multiPlayer) {
		restrictInMultiplayer = multiPlayer;
		restrictInSingleplayer = singlePlayer;
	}
	
	public boolean isCurrentlyRestricted() {
		if(restrictInMultiplayer && restrictInSingleplayer) {
			return true;
		} else {		
			Minecraft mc = Minecraft.getMinecraft();
			boolean isSinglePlayer = mc.isSingleplayer();
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
}
