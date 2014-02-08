package net.techbrew.mcjm.task;

import net.minecraft.client.Minecraft;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.io.nbt.ChunkLoader;
import net.techbrew.mcjm.io.nbt.RegionLoader;
import net.techbrew.mcjm.log.ChatLog;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkMD;
import net.techbrew.mcjm.model.RegionCoord;
import net.techbrew.mcjm.model.RegionImageCache;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MapRegionTask extends BaseMapTask {
	
	private static final Logger logger = JourneyMap.getLogger();
	
	private MapRegionTask(World world, int dimension, boolean underground, Integer chunkY, ChunkMD.Set chunkMdPool) {
		super(world, dimension, underground, chunkY, chunkMdPool, true);
	}
	
	public static BaseMapTask create(RegionCoord rCoord, Minecraft minecraft) {
		
		int missing = 0;

		final World world = minecraft.theWorld;
		final File mcWorldDir = FileHandler.getMCWorldDir(minecraft, rCoord.dimension);
		final ChunkMD.Set chunks = new ChunkMD.Set(1280); // 1024 * 1.25 alleviates map growth		
		final List<ChunkCoordIntPair> coords = rCoord.getChunkCoordsInRegion();
		
		while(!coords.isEmpty()) {
			ChunkCoordIntPair coord = coords.remove(0);
			ChunkMD stub = ChunkLoader.getChunkStubFromDisk(coord.chunkXPos, coord.chunkZPos, mcWorldDir, world);
			if(stub==null) {
				missing++;
			} else {
				chunks.add(stub);
			}
		}		

		if(logger.isLoggable(Level.FINE)) {
			logger.fine("Chunks: " + missing + " skipped, " + chunks.size() + " used");
		}
		
		if(chunks.size()>0) {
			logger.warning("No viable chunks found in region " + rCoord);
		}
		return new MapRegionTask(world, rCoord.dimension, rCoord.isUnderground(), rCoord.getVerticalSlice(), chunks);
	
	}
	
	/**
	 * Stateful ITaskManager for MapRegionTasks
	 * 
	 * @author mwoodman
	 *
	 */
	public static class Manager implements ITaskManager {
		
		RegionLoader regionLoader;
		boolean enabled;
		
		@Override
		public Class<? extends ITask> getTaskClass() {
			return MapRegionTask.class;
		}
		
		@Override
		public boolean enableTask(Minecraft minecraft, Object params) {

			enabled = (params!=null);
			if(!enabled) return false;
			
			enabled = false; // assume the worst
			if(minecraft.isIntegratedServerRunning()) {
				try {
					final int dimension = (Integer) DataCache.playerDataValue(EntityKey.dimension);
					final boolean underground = (Boolean) DataCache.playerDataValue(EntityKey.underground) && PropertyManager.getInstance().getBoolean(PropertyManager.Key.PREF_SHOW_CAVES);
					MapType mapType;
					Integer vSlice = null;
					if(underground) {
						mapType = MapType.underground;
						vSlice = (Integer) DataCache.playerDataValue(EntityKey.chunkCoordY);
					} else {
						final long time = minecraft.theWorld.getWorldInfo().getWorldTime() % 24000L;
						mapType = (time<13800) ? MapType.day : MapType.night; 
					}
					
					Boolean mapAll = params==null ? false : (Boolean) params;
					
					regionLoader = new RegionLoader(minecraft, dimension, mapType, vSlice, mapAll);
			    	if(regionLoader.getRegionsFound()==0) {
			    		disableTask(minecraft);
			    	} else {
			    		this.enabled = true;
			    	}
		    	} catch(Throwable t) {
		    		String error = Constants.getMessageJMERR00("Couldn't Auto-Map: " + t.getMessage()); //$NON-NLS-1$
					ChatLog.announceError(error);
					logger.severe(LogFormatter.toString(t));
		    	}
			}
			return this.enabled;
		}
		
		@Override
		public boolean isEnabled(Minecraft minecraft) {
			return this.enabled;
		}
		
		@Override
		public void disableTask(Minecraft minecraft) {
			
			if(regionLoader!=null) {				
				if(regionLoader.isUnderground()) {
					ChatLog.announceI18N("MapOverlay.automap_complete_underground", regionLoader.getVSlice());
				} else {
					ChatLog.announceI18N("MapOverlay.automap_complete");
				}
	    	}
			enabled = false;
	    	
			if(regionLoader!=null) {
				RegionImageCache.getInstance().flushToDisk();
				RegionImageCache.getInstance().clear();
				regionLoader.getRegions().clear();
				regionLoader = null;
			}
						
		}
		
		@Override
		public BaseMapTask getTask(Minecraft minecraft) {
			
			if(!enabled) return null;
			
			if(regionLoader.getRegions().isEmpty()) {
				disableTask(minecraft);
				return null;
	    	}
			
			RegionCoord rCoord = regionLoader.getRegions().peek();
			BaseMapTask baseMapTask = MapRegionTask.create(rCoord, minecraft);
			return baseMapTask;
		}
		
		@Override
		public void taskAccepted(boolean accepted) {
			if(accepted) {
				regionLoader.getRegions().pop();
				float total = 1F * regionLoader.getRegionsFound();
				float remaining = total-regionLoader.getRegions().size();
				String percent = new DecimalFormat("##.#").format(remaining*100/total) + "%";
				if(regionLoader.isUnderground()) {
					ChatLog.announceI18N("MapOverlay.automap_status_underground", regionLoader.getVSlice(), percent);
				} else {
					ChatLog.announceI18N("MapOverlay.automap_status", percent);
				}
			}
		}
	}

	@Override
	public void taskComplete() {
		
	}
}
