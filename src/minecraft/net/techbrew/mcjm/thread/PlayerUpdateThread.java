package net.techbrew.mcjm.thread;

import java.util.logging.Level;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.PlayerData;
import net.techbrew.mcjm.log.LogFormatter;

public class PlayerUpdateThread extends UpdateThreadBase {
	
	public PlayerUpdateThread(JourneyMap journeyMap, World world) {
		super(journeyMap, world);
	}

	@Override
	protected void doTask() {
		DataCache.instance().put(new PlayerData());
	}

}
