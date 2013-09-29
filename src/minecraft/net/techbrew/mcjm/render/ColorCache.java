package net.techbrew.mcjm.render;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import net.minecraft.src.Block;
import net.minecraft.src.BlockFlower;
import net.minecraft.src.BlockGrass;
import net.minecraft.src.BlockLeaves;
import net.minecraft.src.BlockLeavesBase;
import net.minecraft.src.BlockLilyPad;
import net.minecraft.src.BlockSand;
import net.minecraft.src.BlockTallGrass;
import net.minecraft.src.BlockVine;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ReloadableResourceManager;
import net.minecraft.src.Resource;
import net.minecraft.src.ResourceManager;
import net.minecraft.src.ResourceManagerReloadListener;
import net.minecraft.src.TextureAtlasSprite;
import net.minecraft.src.ResourceLocation;
import net.minecraft.src.ResourcePack;
import net.minecraft.src.ResourcePackFileNotFoundException;
import net.minecraft.src.ResourcePackRepository;
import net.minecraft.src.ResourcePackRepositoryEntry;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.BiomeGenBase;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.IconLoader;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkStub;

/**
 * Cache of block colors derived from the current texture pack.
 * @author mwoodman
 *
 */
public class ColorCache implements ResourceManagerReloadListener {
	
	final HashMap<BlockInfo, Color> colors = new HashMap<BlockInfo, Color>(256);
	final HashMap<BiomeGenBase, Color> grassBiomeColors = new HashMap<BiomeGenBase, Color>(16);
	final HashMap<BiomeGenBase, Color> waterBiomeColors = new HashMap<BiomeGenBase, Color>(16);
	final HashMap<String, Color> foliageBiomeColors = new HashMap<String, Color>(16);
	
	IconLoader iconLoader;
	String lastResourcePack;
	
	public ColorCache() {
		
		ResourceManager rm = Minecraft.getMinecraft().getResourceManager();
		if(rm instanceof ReloadableResourceManager) {
			((ReloadableResourceManager) rm).registerReloadListener(this);
		} else {
			JourneyMap.getLogger().warning("Could not register ResourcePack ReloadListener.  Changing resource packs will require restart");
		}		
		reset();
	}
	
	@Override
	public void onResourceManagerReload(ResourceManager var1) {
		
		// Check if the resourcepack has changed
		ResourcePackRepository repo = Minecraft.getMinecraft().getResourcePackRepository();		
    	String currentPack = repo.getResourcePackName();

    	if(currentPack!=lastResourcePack) {    		
    		JourneyMap.getLogger().info("Map colors will be derived from ResourcePack: " + currentPack);    		
    		lastResourcePack = currentPack;	    		    	
			reset();
		}
    	
		// Ensure iconLoader instantiated
		if(iconLoader==null) {
			iconLoader = new IconLoader();
		}
	}
	
	public Color getBlockColor(ChunkStub chunkStub, BlockInfo blockInfo, int x, int y, int z) {
		
		if(iconLoader==null) {
			JourneyMap.getLogger().warning("Attempting to get BlockColor without iconLoader instantiated.");
			return Color.BLACK;
		}
		
		Color color = blockInfo.getColor();
		
		if(color==null) {
			
			Block block = blockInfo.getBlock();
			if(block==null) {
				color = colors.get(blockInfo==null);
				if(color==null) {
					JourneyMap.getLogger().warning("Unregistered block for " + blockInfo.debugString());
					color = Color.BLACK;
					colors.put(blockInfo, color);
				}
				return color;
			}
			
			BiomeGenBase biome = chunkStub.getBiomeGenForWorldCoords(x, z, chunkStub.worldObj.getWorldChunkManager());		
			
			if(block instanceof BlockLeavesBase || block instanceof BlockVine) {
				
				color = getFoliageColor(blockInfo, biome, x, y, z);			
				
			} else if(block instanceof BlockGrass || block instanceof BlockTallGrass) {
	
				color = getGrassColor(blockInfo, biome);
				
			} else if(block==Block.waterStill || block==Block.waterMoving) {
				
				color = getWaterColor(blockInfo, biome);
				
			} else if(block instanceof BlockLilyPad) {
				
				color = colorMultiplier(getBasicBlockColor(blockInfo), block.getBlockColor());
	
			} else {
				
				if(block instanceof BlockFlower) {
					MapBlocks.side2Textures.add(block.blockID);
				}
				
				int colorMultiplier = block.colorMultiplier(chunkStub.worldObj, x, y, z);
				if(colorMultiplier!=16777215) {
					color = colorMultiplier(getBasicBlockColor(blockInfo), colorMultiplier);
				}
				
				if(color==null) {				
					// TODO: determine if/when these variations happen, and if they work.
					int rc = block.getRenderColor(blockInfo.meta);
					if(rc!=16777215 && rc!=0) {
						color = new Color(rc);
						colors.put(blockInfo, color);
					} else {			
						color = getBasicBlockColor(blockInfo);
					}
				}
			}
		}
		return color;
	}

	private Color getFoliageColor(BlockInfo blockInfo, BiomeGenBase biome, int x, int y, int z) {
		String key = blockInfo.hashCode() + biome.biomeName;
		Color color = foliageBiomeColors.get(key);
		if(color==null) {
			color = colorMultiplier(blockInfo.getBlock().getBlockColor(), biome.getBiomeFoliageColor());
			foliageBiomeColors.put(key, color);
		}
		return color;
	}
	
	private Color getGrassColor(BlockInfo blockInfo, BiomeGenBase biome) {
		Color color = grassBiomeColors.get(biome);
		if(color==null) {
			color = colorMultiplier(getBasicBlockColor(blockInfo), biome.getBiomeGrassColor());
			grassBiomeColors.put(biome, color);
		}
		return color;
	}
	
	private Color getWaterColor(BlockInfo blockInfo, BiomeGenBase biome) {
		Color color = waterBiomeColors.get(biome);
		if(color==null) {
			color = colorMultiplier(getBasicBlockColor(blockInfo), biome.waterColorMultiplier);
			waterBiomeColors.put(biome, color);
		}
		return color;
	}
	
	/**
	 * Gets the color for the block from the cache, or
	 * lazy-loads it first.
	 * @param blockInfo
	 * @return
	 */
	private Color getBasicBlockColor(BlockInfo blockInfo) {
		
		Color color = blockInfo.getColor();
		if(color==null) {		
			color = colors.get(blockInfo);			
		}
		if(color==null) {
			color = iconLoader.loadBlockColor(blockInfo);
			colors.put(blockInfo, color);
		}
		blockInfo.setColor(color);
			
		return color;		
	}		
	
	private void reset() {
		if(iconLoader!=null) {
			iconLoader.initBlocksTexture();
		}
		grassBiomeColors.clear();
		waterBiomeColors.clear();
		foliageBiomeColors.clear();				
		colors.clear();    				
		colors.put(new BlockInfo(0,0), new Color(0x000000)); // air    		
		MapBlocks.resetAlphas();	
	}
	
	Color colorMultiplier(Color color, int mult) {
		return colorMultiplier(color.getRGB(), mult);
	}

	Color colorMultiplier(int rgb, int mult) {
		
	    int alpha1 = rgb >> 24 & 0xFF;
	    int red1 = rgb >> 16 & 0xFF;
	    int green1 = rgb >> 8 & 0xFF;
	    int blue1 = rgb >> 0 & 0xFF;

	    int alpha2 = mult >> 24 & 0xFF;
	    int red2 = mult >> 16 & 0xFF;
	    int green2 = mult >> 8 & 0xFF;
	    int blue2 = mult >> 0 & 0xFF;

	    int alpha = alpha1 * alpha2 / 255;
	    int red = red1 * red2 / 255;
	    int green = green1 * green2 / 255;
	    int blue = blue1 * blue2 / 255;

	    int result = (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
	    
	    return new Color(result);
	}

	static float safeColor(float original) {
		return Math.min(1F, (Math.max(0F, original)));
	}
	
	static int safeColor(int original) {
		return Math.max(0, (Math.min(255, original)));
	}
	
	static Color average(Collection<Color> colors)
	{
		if(colors.isEmpty()) return null;
		
		int count = colors.size();
		
		int r = 0;
		int g = 0;
		int b = 0;
		
		for(Color color : colors) {
			r+= color.getRed();			
			g+= color.getGreen();
			b+= color.getBlue();
		}
		return new Color(r/count, g/count, b/count);
	}

}
