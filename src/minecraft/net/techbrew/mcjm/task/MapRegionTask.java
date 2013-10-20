package net.techbrew.mcjm.task;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.Minecraft;
import net.minecraft.src.World;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.io.nbt.ChunkLoader;
import net.techbrew.mcjm.io.nbt.RegionLoader;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkStub;
import net.techbrew.mcjm.model.RegionCoord;
import net.techbrew.mcjm.model.RegionImageCache;

public class MapRegionTask extends BaseMapTask {
	
	private static final Logger logger = JourneyMap.getLogger();
	
	private MapRegionTask(World world, int dimension, boolean underground, Integer chunkY, Map<ChunkCoordIntPair, ChunkStub> chunkStubs) {
		super(world, dimension, underground, chunkY, chunkStubs, true);
	}
	
	public static BaseMapTask create(RegionCoord rCoord, Minecraft minecraft, long worldHash) {
		
		int missing = 0;

		final World world = minecraft.theWorld;
		final File mcWorldDir = FileHandler.getMCWorldDir(minecraft, rCoord.dimension);
		final Map<ChunkCoordIntPair, ChunkStub> chunks = new HashMap<ChunkCoordIntPair, ChunkStub>(1280); // 1024 * 1.25 alleviates map growth		
		final List<ChunkCoordIntPair> coords = rCoord.getChunkCoordsInRegion();
		
		while(!coords.isEmpty()) {
			ChunkCoordIntPair coord = coords.remove(0);
			ChunkStub stub = ChunkLoader.getChunkStubFromDisk(coord.chunkXPos, coord.chunkZPos, mcWorldDir, world, worldHash);
			if(stub==null) {
				missing++;
			} else {
				chunks.put(coord, stub);
			}
		}		

		if(logger.isLoggable(Level.FINE)) {
			logger.fine("Chunks: " + missing + " skipped, " + chunks.size() + " used");
		}
		
		if(chunks.size()>0) {
			return new MapRegionTask(world, rCoord.dimension, rCoord.isUnderground(), rCoord.getVerticalSlice(), chunks);
		} else {
			return null;
		}
	
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
		public boolean enableTask(Minecraft minecraft) {
			
			// Bail if automap not enabled
			enabled = PropertyManager.getInstance().getBoolean(PropertyManager.Key.AUTOMAP_ENABLED);
			if(!enabled) return false;
			
			// TODO: verify this is okay to use (instead of isSinglePlayer)
			enabled = false; // assume the worst
			if(minecraft.isIntegratedServerRunning()) {
				try {
					final int dimension = (Integer) DataCache.playerDataValue(EntityKey.dimension);
					final boolean underground = (Boolean) DataCache.playerDataValue(EntityKey.underground);
					MapType mapType;
					Integer vSlice = null;
					if(underground) {
						mapType = MapType.underground;
						vSlice = (Integer) DataCache.playerDataValue(EntityKey.chunkCoordY);
					} else {
						final long time = minecraft.theWorld.getWorldInfo().getWorldTime() % 24000L;
						mapType = (time<13800) ? MapType.day : MapType.night; 
					}
					
					regionLoader = new RegionLoader(minecraft, dimension, mapType, vSlice);
			    	if(regionLoader.getRegionsFound()==0) {
			    		disableTask(minecraft);
			    	} else {
			    		this.enabled = true;
			    	}
		    	} catch(Throwable t) {
		    		String error = Constants.getMessageJMERR00("Couldn't Auto-Map: " + t.getMessage()); //$NON-NLS-1$
					JourneyMap.getInstance().announce(error);
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
					JourneyMap.getInstance().announce(Constants.getString("MapOverlay.automap_complete_underground", regionLoader.getVSlice()), Level.INFO);					
				} else {
					JourneyMap.getInstance().announce(Constants.getString("MapOverlay.automap_complete"), Level.INFO);
				}
	    	}
			enabled = false;
	    	
			if(regionLoader!=null) {
				RegionImageCache.getInstance().flushToDisk();
				RegionImageCache.getInstance().clear();
				regionLoader.getRegions().clear();
				regionLoader = null;
			}
			
			PropertyManager.getInstance().setProperty(PropertyManager.Key.AUTOMAP_ENABLED, false);
			
		}
		
		@Override
		public BaseMapTask getTask(Minecraft minecraft, long worldHash) {
			
			if(!enabled) return null;
			
			if(regionLoader.getRegions().isEmpty()) {
				disableTask(minecraft);
				return null;
	    	}
			
			RegionCoord rCoord = regionLoader.getRegions().peek();
			BaseMapTask baseMapTask = MapRegionTask.create(rCoord, minecraft, worldHash);
			return baseMapTask;
		}
		
		@Override
		public void taskAccepted(boolean accepted) {
			if(accepted) {
				regionLoader.getRegions().pop();
				float total = 1F * regionLoader.getRegionsFound();
				float remaining = total-regionLoader.getRegions().size();
				String percent = new DecimalFormat("##.#").format(remaining*100/total) + "%%";
				if(regionLoader.isUnderground()) {
					String msg = Constants.getString("MapOverlay.automap_status_underground", regionLoader.getVSlice(), percent);
					JourneyMap.getInstance().announce(msg, Level.INFO);
				} else {
					JourneyMap.getInstance().announce(Constants.getString("MapOverlay.automap_status", percent), Level.INFO);
				}
			}
		}
	}
}
