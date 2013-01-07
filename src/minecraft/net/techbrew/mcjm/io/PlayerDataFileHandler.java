package net.techbrew.mcjm.io;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.logging.Level;

import org.lwjgl.opengl.Display;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.Chunk;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.MathHelper;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.WorldInfo;
import net.techbrew.mcjm.ChunkStub;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.EntityHelper;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.PropertyManager;
import net.techbrew.mcjm.Utils;
import net.techbrew.mcjm.VersionCheck;

public class PlayerDataFileHandler {

	public static File getPlayerFile(Minecraft minecraft) {		
		try {
			return new File(FileHandler.getWorldDir(minecraft), Constants.PLAYER_LOC_FILE);
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}		
	}
	
	/**
	 * Update player and world info
	 * @param mc
	 * @param underground
	 */
	public static synchronized void updatePlayer(Minecraft mc, boolean underground) {
		
		// Multiplayer: Bail if server info not available yet
		if(mc==null || mc.theWorld==null) return;
		if(!mc.isSingleplayer() && mc.getServerData()==null) return;
		
		// Ensure playerFile can be written
		File playerFile = PlayerDataFileHandler.getPlayerFile(mc);
		String worldDirName;
		try {
			worldDirName = FileHandler.getSafeName(mc);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		// Write player file
		FileHandler.writeToFile(playerFile, getPlayerData(mc, worldDirName, underground));
		
	}
	
	private static String getPlayerData(Minecraft mc, String worldDirName, boolean underground) {
		
		EntityPlayerSP player = mc.thePlayer;
		
		// Calculate worldPath for http requests
		StringBuffer worldPath = new StringBuffer("/jm?worldType="); //$NON-NLS-1$
		worldPath.append(mc.isSingleplayer() ? "sp" : "mp"); //$NON-NLS-1$ //$NON-NLS-2$
		worldPath.append("&worldName=").append(worldDirName); //$NON-NLS-1$
		worldPath.append("&worldProviderType=").append(mc.theWorld.provider.dimensionId); //$NON-NLS-1$
		if(!mc.isSingleplayer()) {
			worldPath.append("&worldSeed=").append(JourneyMap.getLastWorldHash()); //$NON-NLS-1$
		}
		
		// Player heading
		double xHeading = -MathHelper.sin((player.rotationYaw * 3.141593F) / 180F);
	    double zHeading = MathHelper.cos((player.rotationYaw * 3.141593F) / 180F);
		double degrees = Math.atan2(xHeading, zHeading) * (180 / Math.PI);
	    if(degrees > 0 || degrees < 180) degrees = 180 - degrees;
	    
	    // World time
		long ticks = (mc.theWorld.getWorldTime() % 24000L);
		
		StringBuffer sb = new StringBuffer();
		
		// software
		sb.append("jm_version='").append(JourneyMap.JM_VERSION).append("';").append('\n'); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("latest_journeymap_version='").append(VersionCheck.getVersionAvailable()).append("';").append('\n'); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("mc_version='").append(Display.getTitle().split("\\s(?=\\d)")[1]).append("';").append('\n'); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		Integer refreshRate = PropertyManager.getInstance().getInteger(PropertyManager.BROWSER_POLL_PROP);
		sb.append("clientRefreshRate=").append(refreshRate).append(";\n"); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Check world info for hardcode and map enabled
		WorldInfo worldInfo = mc.theWorld.getWorldInfo();
		Boolean hardcore = worldInfo.isHardcoreModeEnabled();
		
		// world
		String worldName;		
		if(!mc.isSingleplayer() && mc.getServerData()!=null) {
			worldName = mc.getServerData().serverName; 
		} else {		
			try {
				worldName = URLEncoder.encode(mc.theWorld.getWorldInfo().getWorldName(), "UTF-8").replaceAll("\\+", " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} catch (UnsupportedEncodingException e) {
				worldName = mc.theWorld.getWorldInfo().getWorldName();
			}
		}
		sb.append("worldName='"); //$NON-NLS-1$
		sb.append(worldName).append("';").append('\n'); //$NON-NLS-1$
		sb.append("worldProviderType=").append(mc.theWorld.provider.dimensionId).append(";").append('\n'); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("worldPath='").append(worldPath).append("';").append('\n'); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("worldTime='").append(ticks).append("';").append('\n'); //$NON-NLS-1$ //$NON-NLS-2$

		// biome
		String biomeName = "?";
		ChunkStub playerChunk = JourneyMap.getLastPlayerChunk();
		if(playerChunk!=null) {
			biomeName = playerChunk.biomeName;
		}		
		
		// player
		sb.append("player={chunkCoordX:").append(Integer.toString(player.chunkCoordX)) //$NON-NLS-1$
				.append(",").append("chunkCoordY:").append(Integer.toString(player.chunkCoordY)) //$NON-NLS-1$ //$NON-NLS-2$
				.append(",").append("chunkCoordZ:").append(Integer.toString(player.chunkCoordZ)) //$NON-NLS-1$ //$NON-NLS-2$
				.append(",").append("posX:").append(Integer.toString((int) player.posX)) //$NON-NLS-1$ //$NON-NLS-2$
				.append(",").append("posY:").append(Integer.toString((int) player.posY)) //$NON-NLS-1$ //$NON-NLS-2$
				.append(",").append("posZ:").append(Integer.toString((int) player.posZ)) //$NON-NLS-1$ //$NON-NLS-2$
				.append(",").append("heading:").append(degrees) //$NON-NLS-1$ //$NON-NLS-2$
				.append(",").append("biome:'").append(biomeName).append("'") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				.append(",").append("underground:").append(underground) //$NON-NLS-1$ //$NON-NLS-2$
				.append(",").append("name:'").append(player.username).append("'") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				// .append(",").append("health:").append(player.health) // TODO
				.append("};").append('\n'); //$NON-NLS-1$
				
		if(!hardcore) {
			// Nearby mobs
		    Iterator<Entity> mobIter = EntityHelper.getEntitiesNearby(mc).iterator();
			sb.append("mobs=["); //$NON-NLS-1$
			if(mobIter!=null) {
				while(mobIter.hasNext()) {
					Entity mob = (Entity)mobIter.next();
					if(EntityHelper.entityMap.containsKey(mob.getClass())) {
						sb.append("{type:'").append(EntityHelper.entityMap.get(mob.getClass())).append("',") //$NON-NLS-1$ //$NON-NLS-2$
						    .append("posX:").append(Integer.toString((int) mob.posX)) //$NON-NLS-1$
							.append(",").append("posZ:") //$NON-NLS-1$ //$NON-NLS-2$
							.append(Integer.toString((int) mob.posZ)).append("}"); //$NON-NLS-1$
						if(mobIter.hasNext()) sb.append(","); //$NON-NLS-1$
						sb.append("\n"); //$NON-NLS-1$
					}
				}
			}
			sb.append("];\n"); //$NON-NLS-1$
			
			// other players		
			sb.append("others=["); //$NON-NLS-1$
			if(!mc.isSingleplayer()) {
				
				Iterator others = mc.theWorld.playerEntities.iterator();
				while(others.hasNext()) {
					EntityPlayer other = (EntityPlayer) others.next();				
					sb.append("{username:'").append(other.username).append("',") //$NON-NLS-1$ //$NON-NLS-2$
					    .append("posX:").append(Integer.toString((int) other.posX)) //$NON-NLS-1$
						.append(",").append("posZ:") //$NON-NLS-1$ //$NON-NLS-2$
						.append(Integer.toString((int) other.posZ)).append("}"); //$NON-NLS-1$
					if(others.hasNext()) sb.append(","); //$NON-NLS-1$
					sb.append("\n");				 //$NON-NLS-1$
				}			
			}
			sb.append("];\n"); //$NON-NLS-1$
		}
		return sb.toString();
	}
	
}
