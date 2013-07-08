package net.techbrew.mcjm.thread;

import java.util.logging.Level;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.world.chunk.Chunk;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.Utils;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.data.PlayerData;
import net.techbrew.mcjm.log.LogFormatter;

public abstract class UpdateThreadBase implements Runnable {

	protected int playerChunkX;			
	protected int playerChunkZ;
	protected int playerChunkY;
	protected boolean underground;
	//protected Chunk playerChunk;
	
	public UpdateThreadBase() {
	}
	
	@Override
	public final void run() {

		try {
			
			JourneyMap jm = JourneyMap.getInstance();
			Minecraft mc = Minecraft.getMinecraft();
			
			// Bail if needed
			if(!jm.isMapping()) {
				jm.getLogger().fine("JM not mapping, aborting");
				return;
			}
						
			// Check player status
			EntityClientPlayerMP player = mc.thePlayer;
			if (player==null || player.isDead) {
				jm.getLogger().fine("Player dead, aborting");
				return;
			}
			
			Chunk playerChunk = Utils.getChunkIfAvailable(player.worldObj, player.chunkCoordX, player.chunkCoordZ);
			if(playerChunk!=null) {
				playerChunkX = player.chunkCoordX;				
				playerChunkZ = player.chunkCoordZ;
				playerChunkY = player.chunkCoordY;
				
				// TODO:  Decide whether the cached value is sufficient.  May not be.
				underground = (Boolean) DataCache.instance().get(PlayerData.class).get(EntityKey.underground);

				// Do the real task
				doTask();	
			} else {
				JourneyMap.getLogger().warning("Unable to get player chunk");
			}
			
		} catch (Throwable t) {
			String error = Constants.getMessageJMERR16(t.getMessage());
			JourneyMap.getLogger().severe(error);
			JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(t));		
			
		} finally {
			//JourneyMap.getLogger().fine(getClass() + "Done."); //$NON-NLS-1$
		}
		
	}
	
	/**
	 * Do the real work.
	 */
	protected abstract void doTask();
	

}
