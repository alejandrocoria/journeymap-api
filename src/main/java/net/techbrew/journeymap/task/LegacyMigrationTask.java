package net.techbrew.journeymap.task;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.Utils;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.io.RegionImageHandler;
import net.techbrew.journeymap.log.ChatLog;
import net.techbrew.journeymap.model.RegionCoord;
import net.techbrew.journeymap.model.RegionImageSet;

import java.io.File;
import java.io.FileFilter;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

;

public class LegacyMigrationTask implements IGenericTask {
	
	final int mapTaskDelay = JourneyMap.getInstance().configProperties.getChunkPoll();
	
	private static final Logger logger = JourneyMap.getLogger();
	
	final World world;
	final File jmWorldDir;
	final Stack<File> pngFiles;
	final int pngFilesFound;
	
	private LegacyMigrationTask(Minecraft minecraft, File jmWorldDir, Stack<File> pngFiles, int pngFilesFound) {
		this.world = minecraft.theWorld;
		this.jmWorldDir = jmWorldDir;
		this.pngFiles = pngFiles;
		this.pngFilesFound = pngFilesFound;
	}

	
	@Override
	public void performTask() {
		
		float total = 1F * pngFilesFound;
		float remaining = total-pngFiles.size();
		String percent = new DecimalFormat("##.#").format(remaining*100/total) + "%";
        ChatLog.announceI18N("JourneyMap.file_updates", percent);
		
		final long start = System.nanoTime();
		int count = 0;
		File png;
		String fileName;
		long elapsed = 0;
		
		while(!pngFiles.isEmpty()) {
			png = pngFiles.pop();
			if(!png.exists()) {
				continue;
			}
			fileName = png.getName();
			
			try {
				if(!png.canRead()) {
					processJunk(png);
				} else if(fileName.contains("_Normal.")) {
					processNormal(png);
				} else if(fileName.contains("_Cave.")) {
					processUnderground(png, 0);
				} else if(fileName.contains("_Nether.")) {
					processUnderground(png, -1);
				} else if(fileName.contains("_End.")) {
					processUnderground(png, 1);
				} else {
					processJunk(png);
				}
			} catch(Exception e) {
				logger.warning("Problem processing " + fileName + ": " + e);
				processJunk(png);
			}
				
			count++;
			elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start);
			if(elapsed>mapTaskDelay) break;
		}
		
		logger.info("Processed " + count + " legacy images in " + elapsed + "ms");
	}
	
	private void processNormal(File file) {
		String[] coords = file.getName().split("_")[0].split(",");
		if(coords.length!=2) {
			processJunk(file);
		} else {
			int regionX = Integer.parseInt(coords[0]);
			int regionZ = Integer.parseInt(coords[1]);
			RegionCoord rc = new RegionCoord(jmWorldDir, regionX, null, regionZ, 0);
			RegionImageSet ris = new RegionImageSet(rc);
			ris.loadLegacyNormal(file);
			ris.writeToDisk(true);
		}
	}
	
	private void processUnderground(File file, int dimension) {
		String[] coords = file.getName().split("_")[0].split(",");
		if(coords.length!=3) {
			processJunk(file);
		} else {
			int regionX = Integer.parseInt(coords[0]);
			int regionZ = Integer.parseInt(coords[1]);
			int regionY = Integer.parseInt(coords[2]);
			RegionCoord rCoord = new RegionCoord(jmWorldDir, regionX, regionY, regionZ, dimension);
			File newFile = RegionImageHandler.getInstance().getRegionImageFile(rCoord, MapType.underground, false);
			if(newFile.exists()) {
				newFile.delete();
			} else {
				newFile.getParentFile().mkdirs();
			}
			boolean moved = file.renameTo(newFile);
			if(!moved) {
				processJunk(file);
			}
		}
	}
	
	private void processJunk(File file) {
		try {
			File junkDir = new File(jmWorldDir, "junk");
			if(junkDir.exists()) {
				if(!junkDir.isDirectory()) {
					junkDir.delete();
					junkDir.mkdirs();
				}
			} else {
				junkDir.mkdirs();
			}
			
			int count=1;
			File junkFile = new File(junkDir, file.getName());
			while(junkFile.exists()) {
				junkFile = new File(junkDir, file.getName() + "_" + (count++) + ".png");
			}
			
			boolean moved = file.renameTo(junkFile);
			if(!moved) {
				logger.warning("Couldn't move to junk folder: " + file);
			} else {
				logger.warning("Junked file: " + file);
			}
		} catch(Exception e) {
			logger.warning("Couldn't move to junk folder: " + file);
		}
	}
	
	
	/**
	 * ITaskManager for MapPlayerTasks
	 * 
	 * @author mwoodman
	 *
	 */
	public static class Manager implements ITaskManager {
		
		boolean enabled;
		
		File jmWorldDir;
		long worldHash;
		
		Stack<File> pngFiles;
		int pngFilesFound;
		
		@Override
		public Class<? extends ITask> getTaskClass() {
			return LegacyMigrationTask.class;
		}
		
		@Override
		public boolean enableTask(Minecraft minecraft, Object params) {
			
			jmWorldDir = FileHandler.getJMWorldDir(minecraft, Utils.getWorldHash(minecraft));
			File[] files = jmWorldDir.listFiles(new PngFileFilter());
			if(files.length>0) {
				this.pngFiles = new Stack<File>();
				this.pngFiles.addAll(Arrays.asList(files));
				this.pngFilesFound = pngFiles.size();
				enabled = true;
			} else {
				logger.info("No legacy files found.");
				enabled = false;
			}
			
			return enabled;
		}
		
		@Override
		public boolean isEnabled(Minecraft minecraft) {
			return enabled;
		}
		
		@Override
		public void disableTask(Minecraft minecraft) {		
			this.jmWorldDir = null;
			if(this.pngFiles!=null) {
				this.pngFiles.clear();
				this.pngFiles = null;
			}
			
			enabled = false;
		}
		
		@Override
		public IGenericTask getTask(Minecraft minecraft) {
			if(!enabled) return null;
			
			if(this.pngFiles.isEmpty()) {
                ChatLog.announceI18N("JourneyMap.file_updates_complete");
				disableTask(minecraft);
				return null;				
			} else {
				return new LegacyMigrationTask(minecraft, this.jmWorldDir, this.pngFiles, this.pngFilesFound);
			}

		}
		
		@Override
		public void taskAccepted(boolean accepted) {
			// nothing to do
		}
		
	}
	
	static class PngFileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			return pathname.isFile() && pathname.getName().endsWith(".png");
		}
		
	}
	
}
