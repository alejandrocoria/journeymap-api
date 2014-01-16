package net.techbrew.mcjm.cartography;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockGrass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.init.Blocks;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.biome.BiomeGenBase;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.IconLoader;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkMD;
import net.techbrew.mcjm.render.BlockInfo;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Cache of block colors derived from the current texture pack.
 * @author mwoodman
 *
 */
public class ColorCache implements IResourceManagerReloadListener {
	
	private static class Holder {
        private static final ColorCache INSTANCE = new ColorCache();
    }
	
	public static ColorCache getInstance() {
        return Holder.INSTANCE;
    }
	
	private final HashMap<BlockInfo, Color> colors = new HashMap<BlockInfo, Color>(256);
	
	private final HashMap<String, Color> grassBiomeColors = new HashMap<String, Color>(16);
	private final HashMap<String, Color> waterBiomeColors = new HashMap<String, Color>(16);
	private final HashMap<String, Color> foliageBiomeColors = new HashMap<String, Color>(16);
	
	private static final int[] leafColorMeta =      {0,3,4,7,8,11};
	private static final int[] leafColorPineMeta =  {1,5,9};
	private static final int[] leafColorBirchMeta = {2,6,10};
	
	private IconLoader iconLoader;
	private String lastResourcePack;
	private final Object lock = new Object();
	
	private ColorCache() {
		
		IResourceManager rm = Minecraft.getMinecraft().getResourceManager();
		if(rm instanceof IReloadableResourceManager) {
			((IReloadableResourceManager) rm).registerReloadListener(this);
		} else {
			JourneyMap.getLogger().warning("Could not register ResourcePack ReloadListener.  Changing resource packs will require restart");
		}		

		this.onResourceManagerReload(rm);
	}
	
	@Override
	public void onResourceManagerReload(IResourceManager mgr) {
		
		// Check if the resourcepack has changed
		ResourcePackRepository repo = Minecraft.getMinecraft().getResourcePackRepository();

        // TODO Is this accomplishing what we want?
        StringBuffer sb = new StringBuffer();
        for(Object domain : mgr.getResourceDomains().toArray())
        {
            sb.append(domain).append(",");
        }
    	String currentPack = sb.toString();
    	
    	if(JourneyMap.getInstance().isMapping()) {
    		if(currentPack.equals(lastResourcePack)) {
    			JourneyMap.getLogger().info("ResourcePack unchanged: " + currentPack);
    		} else {
    			JourneyMap.getLogger().info("ResourcePack changed: " + lastResourcePack + " --> " + currentPack);
    			if(lastResourcePack!=null) {
    				serializeCache();
    			}
    			reset();
    			lastResourcePack = currentPack;
        		//deserializeCache();
        		
        		// TODO: avoid this?
        		iconLoader = new IconLoader();			
    		}
    		
    	} else if(iconLoader!=null) {
    		
    		JourneyMap.getLogger().info("Serializing texture-based cache...");
    		//serializeCache();
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
		if(MapBlocks.hasFlag(blockInfo.getBlock(), MapBlocks.Flag.BiomeColor)) {
			color = getBiomeBlockColor(chunkMd, blockInfo, x, y, z);
		} else {
			color = getCachedColor(blockInfo, x, y, z);
		}
		
		return color;
				
	}
	
	private Color getBiomeBlockColor(ChunkMD chunkMd, BlockInfo blockInfo, int x, int y, int z) {
		BiomeGenBase biome = chunkMd.stub.getBiomeGenForWorldCoords(x, z, chunkMd.worldObj.getWorldChunkManager());
		Block block = blockInfo.getBlock();

        if(block instanceof BlockGrass || block instanceof BlockBush) {
            return getGrassColor(blockInfo, biome, x, y, z);
        }

        if(blockInfo.isWater()) {
            return getWaterColor(blockInfo, biome, x, y, z);
        }

        if(blockInfo.isFoliage()) {
            return getFoliageColor(blockInfo, biome, x, y, z);
        }

        if(block==Blocks.vine) {
            return getVineFoliageColor(blockInfo, biome, x, y, z);
        }

        return Color.black;
	}

	private Color getFoliageColor(BlockInfo blockInfo, BiomeGenBase biome, int x, int y, int z) {
		String key = blockInfo.uid_modId + ":" + blockInfo.uid_name + "+" + biome.biomeName;
		Color color = foliageBiomeColors.get(key);
		if(color==null) {
            int leafColor;
            if(Arrays.binarySearch(leafColorPineMeta, blockInfo.meta)>=0) {
                leafColor = ColorizerFoliage.getFoliageColorPine();
            } else if(Arrays.binarySearch(leafColorBirchMeta, blockInfo.meta)>=0) {
                leafColor = ColorizerFoliage.getFoliageColorBirch();
            } else {
                leafColor = biome.getModdedBiomeFoliageColor(blockInfo.getBlock().func_149635_D());
            }
            color = colorMultiplier(getCachedColor(blockInfo, x, y, z), leafColor);

			foliageBiomeColors.put(key, color);
            System.out.println(key +" " + color.toString());
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
					color = getCachedColor(new BlockInfo(blockInfo, 0), x, y, z);
					break;
				}
				case 2 : {
					color = getCachedColor(new BlockInfo(blockInfo, 1), x, y, z);
					break;
				}
				case 4 : {
					color = getCachedColor(new BlockInfo(blockInfo, 2), x, y, z);
					break;
				}
				case 8 : {
					color = getCachedColor(new BlockInfo(blockInfo, 3), x, y, z);
					break;
				}
				case 9 : {
					color = getCachedColor(new BlockInfo(blockInfo, 3), x, y, z);
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
		Color color = grassBiomeColors.get(biome.biomeName);
		if(color==null) {
            Block block = blockInfo.getBlock();
			color = colorMultiplier(getCachedColor(blockInfo, x, y, z), biome.getModdedBiomeGrassColor(block.func_149635_D()));
			grassBiomeColors.put(biome.biomeName, color);
		}
		return color;
	}
	
	private Color getWaterColor(BlockInfo blockInfo, BiomeGenBase biome, int x, int y, int z) {
		Color color = waterBiomeColors.get(biome.biomeName);
		if(color==null) {
			color = colorMultiplier(getCachedColor(blockInfo, x, y, z), biome.waterColorMultiplier);
			waterBiomeColors.put(biome.biomeName, color);
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
                Block block = blockInfo.getBlock();
                BlockInfo biWithMeta0 = new BlockInfo(block, 0);

                // Transparent blocks get handled easily
                if(blockInfo.isTransparent()) {
                    color = Color.white;
                    colors.put(blockInfo, color);

                    MapBlocks.setAlpha(block, 0f);
                    MapBlocks.setFlags(block, MapBlocks.Flag.HasAir, MapBlocks.Flag.IgnoreOverhead, MapBlocks.Flag.NotTopBlock, MapBlocks.Flag.NoShadow);
                } else {
                    color = iconLoader.loadBlockColor(blockInfo);
                    if(color!=null){
                        Minecraft mc = FMLClientHandler.instance().getClient();

                        if(!MapBlocks.hasFlag(block, MapBlocks.Flag.BiomeColor)){
                            int tint = blockInfo.getBlock().func_149720_d(Minecraft.getMinecraft().theWorld, x, y, z);
                            if(tint!=16777215 && tint!=-1){
                                color = colorMultiplier(color, tint);
                            }
                        } else if(block== Blocks.waterlily) {
                            color = colorMultiplier(color, Blocks.waterlily.func_149635_D()); // getBlockColor
                        }
                        colors.put(blockInfo, color);
                        //JourneyMap.getLogger().info("Cached color for " + blockInfo.debugString());
                    } else {
                        color = Color.BLACK;
                        if(iconLoader.failedFor(blockInfo)) {
                            colors.put(blockInfo, color);
                            JourneyMap.getLogger().warning("Cached BLACK for " + blockInfo);
                        }
                    }
                }
			}
		}
			
		return color;		
	}		
	
	public void reset() {
		grassBiomeColors.clear();
		waterBiomeColors.clear();
		foliageBiomeColors.clear();
		colors.clear();
	}
	
	public void serializeCache() {
		
		if(lastResourcePack==null) return;
		
		Logger logger = JourneyMap.getLogger();
		StringBuffer sb = new StringBuffer();
		
		long start = System.currentTimeMillis();
		
		if(!colors.isEmpty()) {
			FileHandler.serializeCache(lastResourcePack + "_blocks", colors);
			sb.append(colors.size() + " block colors, ");
		}
		if(!grassBiomeColors.isEmpty()) {
			FileHandler.serializeCache(lastResourcePack + "_grass", grassBiomeColors);
			sb.append(grassBiomeColors.size() + " grass+biome colors, ");
		}
		if(!waterBiomeColors.isEmpty()) {
			FileHandler.serializeCache(lastResourcePack + "_water", waterBiomeColors);
			sb.append(waterBiomeColors.size() + " water+biome colors, ");
		}
		if(!foliageBiomeColors.isEmpty()) {
			FileHandler.serializeCache(lastResourcePack + "_foliage", foliageBiomeColors);
			sb.append(foliageBiomeColors.size() + " foliage+biome colors");
		}
		long stop = System.currentTimeMillis();
		logger.info("Serialized texture cache for " + lastResourcePack + " in " + (stop-start) + "ms: " + sb.toString());
	}
	
	private void deserializeCache() {

		Logger logger = JourneyMap.getLogger();		

        if(true) return; // TODO

		StringBuffer sb = new StringBuffer();
		long start = System.currentTimeMillis();

        try {
            HashMap<BlockInfo, Color> tempColors = FileHandler.deserializeCache(lastResourcePack + "_blocks", colors.getClass());
            HashMap<String, Color> tempGrass = FileHandler.deserializeCache(lastResourcePack + "_grass", grassBiomeColors.getClass());
            HashMap<String, Color> tempWater = FileHandler.deserializeCache(lastResourcePack + "_water", waterBiomeColors.getClass());
            HashMap<String, Color> tempFoliage = FileHandler.deserializeCache(lastResourcePack + "_foliage", foliageBiomeColors.getClass());

            if(tempColors!=null) {
                colors.putAll(tempColors);
                sb.append(tempColors.size() + " block colors,");
            }

            if(tempGrass!=null) {
                grassBiomeColors.putAll(tempGrass);
                sb.append(tempGrass.size() + " grass+biome colors,");
            }

            if(tempWater!=null) {
                waterBiomeColors.putAll(tempWater);
                sb.append(tempWater.size() + " water+biome colors,");
            }

            if(tempFoliage!=null) {
                foliageBiomeColors.putAll(tempFoliage);
                sb.append(tempFoliage.size() + " foliage+biome colors");
            }

            long stop = System.currentTimeMillis();
            logger.info("Deserialized texture cache for " + lastResourcePack + " in " + (stop-start) + "ms: " + sb.toString());
        } catch(Exception e) {
            JourneyMap.getLogger().severe("Could not deserialize caches: " + LogFormatter.toString(e));
        }
		
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
