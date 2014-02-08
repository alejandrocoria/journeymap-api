package net.techbrew.journeymap.task;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.MapSaver;

import java.util.logging.Logger;

;

public class SaveMapTask implements IGenericTask {
	
	private static final Logger logger = JourneyMap.getLogger();
	
	MapSaver mapSaver;
	
	private SaveMapTask(MapSaver mapSaver) {
		this.mapSaver = mapSaver;
	}
	
	@Override
	public void performTask() {
		mapSaver.saveMap();
	}
	
	/**
	 * ITaskManager for MapPlayerTasks
	 * 
	 * @author mwoodman
	 *
	 */
	public static class Manager implements ITaskManager {
		
		MapSaver mapSaver;
		
		@Override
		public Class<? extends IGenericTask> getTaskClass() {
			return SaveMapTask.class;
		}
		
		@Override
		public boolean enableTask(Minecraft minecraft, Object params) {
			if(params!=null && params instanceof MapSaver) {
				mapSaver = (MapSaver) params;
			}
			return isEnabled(minecraft);
		}
		
		@Override
		public boolean isEnabled(Minecraft minecraft) {
			return (mapSaver!=null);
		}
		
		@Override
		public void disableTask(Minecraft minecraft) {
			mapSaver = null;
		}
		
		@Override
		public SaveMapTask getTask(Minecraft minecraft) {
			if(mapSaver==null) return null;			
			return new SaveMapTask(mapSaver);
		}
		
		@Override
		public void taskAccepted(boolean accepted) {
			mapSaver = null;
		}
		
	}
}
