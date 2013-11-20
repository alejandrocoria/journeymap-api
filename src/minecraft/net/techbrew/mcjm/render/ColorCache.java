package net.techbrew.mcjm.render;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.Block;
import net.minecraft.src.ColorizerFoliage;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ReloadableResourceManager;
import net.minecraft.src.ResourceManager;
import net.minecraft.src.ResourceManagerReloadListener;
import net.minecraft.src.ResourcePackRepository;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.IconLoader;
import net.techbrew.mcjm.model.ChunkMD;

/**
 * Cache of block colors derived from the current texture pack.
 * @author mwoodman
 *
 */
public class ColorCache implements ResourceManagerReloadListener {
	
	private final HashMap<BlockInfo, Color> colors = new HashMap<BlockInfo, Color>(256);
	
	private final HashMap<BiomeGenBase, Color> grassBiomeColors = new HashMap<BiomeGenBase, Color>(16);
	private final HashMap<BiomeGenBase, Color> waterBiomeColors = new HashMap<BiomeGenBase, Color>(16);
	private final HashMap<String, Color> foliageBiomeColors = new HashMap<String, Color>(16);
	
	private static final int[] leafColorMeta =      {0,3,4,7,8,11};
	private static final int[] leafColorPineMeta =  {1,5,9};
	private static final int[] leafColorBirchMeta = {2,6,10};
	
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
	
	public Color getBlockColor(ChunkMD chunkMd, BlockInfo blockInfo, int x, int y, int z) {
		
		if(iconLoader==null) {
			return Color.BLACK;
		}			
		
		// BlockInfo may already have it set
		Color color = blockInfo.getColor();
		if(color!=null) {
			return color;
		}
		
		// Check if colored by biome
		if(MapBlocks.biomeBlocks.contains(blockInfo.id)) {
			color = getBiomeBlockColor(chunkMd, blockInfo, x, y, z);
		} else {
			color = getCachedColor(blockInfo, x, y, z);
		}
		
		return color;
				
	}
	
	private Color getBiomeBlockColor(ChunkMD chunkMd, BlockInfo blockInfo, int x, int y, int z) {
		BiomeGenBase biome = chunkMd.stub.getBiomeGenForWorldCoords(x, z, chunkMd.worldObj.getWorldChunkManager());
		Color color = null;
		
		switch(blockInfo.id) {
			case 2 : {
				return getGrassColor(blockInfo, biome, x, y, z);
			}
			case 8 : {
				return getWaterColor(blockInfo, biome, x, y, z);
			}
			case 9 : {
				return getWaterColor(blockInfo, biome, x, y, z);
			}
			case 18 : {
				return getFoliageColor(blockInfo, biome, x, y, z);
			}
			case 31 : { // Tall grass and fern
				return getGrassColor(blockInfo, biome, x, y, z);
			}
			case 106 : { // Vine
				return getVineFoliageColor(blockInfo, biome, x, y, z);
			}
			default : {
				return Color.black;
			}
		}
	}

	private Color getFoliageColor(BlockInfo blockInfo, BiomeGenBase biome, int x, int y, int z) {
		String key = blockInfo.hashCode() + biome.biomeName;
		Color color = foliageBiomeColors.get(key);
		if(color==null) {
			int leafColor;
			if(Arrays.binarySearch(leafColorPineMeta, blockInfo.meta)>=0) {
				leafColor = ColorizerFoliage.getFoliageColorPine(); 
			} else if(Arrays.binarySearch(leafColorBirchMeta, blockInfo.meta)>=0) {
				leafColor = ColorizerFoliage.getFoliageColorBirch(); 
			} else {
				leafColor = biome.getBiomeFoliageColor();
			}
			
			color = colorMultiplier(getCachedColor(blockInfo, x, y, z), leafColor);			
			
			foliageBiomeColors.put(key, color);
		}
		return color;
	}
	
	private Color getVineFoliageColor(BlockInfo blockInfo, BiomeGenBase biome, int x, int y, int z) {
		String key = blockInfo.hashCode() + biome.biomeName;
		Color color = foliageBiomeColors.get(key);
		if(color==null) {
			final int leafColor = ColorizerFoliage.getFoliageColor(0.7D, 0.8D);
			final int meta = blockInfo.meta;
			switch(meta) {
				case 1 : {
					color = getCachedColor(new BlockInfo(blockInfo.id, 0), x, y, z);					
					break;
				}
				case 2 : {
					color = getCachedColor(new BlockInfo(blockInfo.id, 1), x, y, z);					
					break;
				}
				case 4 : {
					color = getCachedColor(new BlockInfo(blockInfo.id, 2), x, y, z);					
					break;
				}
				case 8 : {
					color = getCachedColor(new BlockInfo(blockInfo.id, 3), x, y, z);					
					break;
				}
				case 9 : {
					color = getCachedColor(new BlockInfo(blockInfo.id, 3), x, y, z);					
					break;
				}
				default : {
					color = getCachedColor(blockInfo, x, y, z);					
					break;
				}
			}			
			color = colorMultiplier(color, leafColor);
			foliageBiomeColors.put(key, color);
			//JourneyMap.getLogger().info("\tBiome-specific color for " + blockInfo.debugString() + ": " + Integer.toHexString(color.getRGB()));
		}
		return color;
	}
	
	private Color getGrassColor(BlockInfo blockInfo, BiomeGenBase biome, int x, int y, int z) {
		Color color = grassBiomeColors.get(biome);
		if(color==null) {
			color = colorMultiplier(getCachedColor(blockInfo, x, y, z), biome.getBiomeGrassColor());
			grassBiomeColors.put(biome, color);
		}
		return color;
	}
	
	private Color getWaterColor(BlockInfo blockInfo, BiomeGenBase biome, int x, int y, int z) {
		Color color = waterBiomeColors.get(biome);
		if(color==null) {
			color = colorMultiplier(getCachedColor(blockInfo, x, y, z), biome.waterColorMultiplier);
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
	private Color getCachedColor(BlockInfo blockInfo, int x, int y, int z) {
		
		Color color = blockInfo.getColor();
		if(color==null) {		
			color = colors.get(blockInfo);	
			if(color==null) {								
				color = iconLoader.loadBlockColor(blockInfo);
				if(color!=null){
					if(!MapBlocks.biomeBlocks.contains(blockInfo.id)){
						int tint = blockInfo.getBlock().colorMultiplier(Minecraft.getMinecraft().theWorld, x, y, z);
						if(tint!=16777215 && tint!=-1){
							color = colorMultiplier(color, tint);
						}
					} else if(blockInfo.id==Block.waterlily.blockID) {
						color = colorMultiplier(color, Block.waterlily.getBlockColor());
					} 
					colors.put(blockInfo, color);
					//JourneyMap.getLogger().info("Cached color for " + blockInfo.debugString());
				} else {
					color = Color.BLACK;
					if(iconLoader.failedFor(blockInfo)) {
						colors.put(blockInfo, color);
						JourneyMap.getLogger().warning("Cached BLACK for " + blockInfo.debugString());
					}					
				}					
			}
		}
			
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
	    
	    return new Color(result | -16777216);
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
	
	static Color average(Color... colors)
	{
		int count = colors.length;
		
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
