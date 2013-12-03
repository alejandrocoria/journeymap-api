package net.techbrew.mcjm.feature;

import net.minecraft.src.Minecraft;

public enum Feature {

	RadarPlayers(false,true),
	RadarAnimals(false,true),
	RadarMobs(false,true),
	RadarVillagers(false,true),
	MapCaves(false,true);
	
	private final boolean appliesToSinglePlayer;
	private final boolean appliesToMultiPlayer;	
	private Feature(boolean singlePlayer, boolean multiPlayer) {
		appliesToMultiPlayer = multiPlayer;
		appliesToSinglePlayer = singlePlayer;
	}
	
	public boolean isCurrentlyRestricted() {
		if(appliesToMultiPlayer && appliesToSinglePlayer) {
			return true;
		} else {		
			Minecraft mc = Minecraft.getMinecraft();
			boolean isSinglePlayer = mc.isSingleplayer();
			if(appliesToSinglePlayer && isSinglePlayer) {
				return true;
			}
			if(appliesToMultiPlayer && !isSinglePlayer) {
				return true;
			}
		}
		return false;
	}
}