package net.techbrew.journeymap.cartography;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.ReloadableResourceManager;
import net.minecraft.client.resources.ResourceManager;
import net.minecraft.client.resources.ResourceManagerReloadListener;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.world.biome.BiomeGenBase;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.IconLoader;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.BlockUtils;
import net.techbrew.journeymap.model.ChunkMD;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Cache of block baseColors derived from the current texture pack.
 * @author mwoodman
 *
 */
public class ColorCache implements ResourceManagerReloadListener {
	
	private static class Holder {
        private static final ColorCache INSTANCE = new ColorCache();
    }
	
	public static ColorCache getInstance() {
        return Holder.INSTANCE;
    }
	
	private final HashMap<BlockMD, Color> baseColors = new HashMap<BlockMD, Color>(256);
    private final HashMap<String, HashMap<BlockMD, Color>> biomeColors = new HashMap<String, HashMap<BlockMD, Color>>(32);
	
	private volatile IconLoader iconLoader;
	private String lastResourcePack;
	
	private ColorCache() {
		
		ResourceManager rm = Minecraft.getMinecraft().getResourceManager();
		if(rm instanceof ReloadableResourceManager) {
			((ReloadableResourceManager) rm).registerReloadListener(this);
		} else {
			JourneyMap.getLogger().warning("Could not register ResourcePack ReloadListener.  Changing resource packs will require restart");
		}		

		this.onResourceManagerReload(rm);
	}
	
	@Override
	public void onResourceManagerReload(ResourceManager mgr) {
		
		// Check if the resourcepack has changed
		ResourcePackRepository repo = Minecraft.getMinecraft().getResourcePackRepository();

        // TODO Is this accomplishing what we want?
        StringBuffer sb = new StringBuffer();
        for(Object domain : mgr.getResourceDomains().toArray())
        {
            sb.append(domain).append(",");
        }
    	String currentPack = sb.toString();
    	
    	if(JourneyMap.getInstance().isMapping() || iconLoader==null) {
    		if(currentPack.equals(lastResourcePack)) {
    			JourneyMap.getLogger().fine("ResourcePack unchanged: " + currentPack);
    		} else {
    			JourneyMap.getLogger().info("ResourcePack: " + lastResourcePack + " --> " + currentPack);
    			reset();
    			lastResourcePack = currentPack;
        		// TODO: avoid this?
                BlockUtils.initialize();
        		iconLoader = new IconLoader();

    		}
    		
    	}
	}

	public Color getBlockColor(ChunkMD chunkMd, BlockMD blockMD, int x, int y, int z) {
		if(iconLoader==null) {
			return null;
		}
        Color color = null;
        if(!blockMD.isBiomeColored()) {
            // This may load a custom biome color and update
            // the flags on blockMD accordingly.
            color = getBaseColor(blockMD, x, y, z);
        }
        if(blockMD.isBiomeColored()) {
            color = getBiomeBlockColor(chunkMd, blockMD, x, y, z);
        }
        return color;
	}
	
	private Color getBiomeBlockColor(ChunkMD chunkMd, BlockMD blockMD, int x, int y, int z) {
		BiomeGenBase biome = chunkMd.stub.getBiomeGenForWorldCoords(x, z, chunkMd.worldObj.getWorldChunkManager());
		Block block = blockMD.getBlock();

        if(block instanceof BlockGrass || block instanceof BlockDeadBush || block instanceof BlockTallGrass) {
            return getGrassColor(blockMD, biome, x, y, z);
        }

        if(blockMD.isWater()) {
            return getWaterColor(blockMD, biome, x, y, z);
        }

        if(blockMD.isFoliage() || block instanceof BlockVine) {
            return getFoliageColor(blockMD, biome, x, y, z);
        }

        // Anything else, including those with CustomBiomeColor
        return getCustomBiomeColor(blockMD, biome, x, y, z);
	}

    private Color getCustomBiomeColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z) {
        Color color = getBiomeColor(blockMD, biome);
        if(color==null) {
            color = getBaseColor(blockMD, x, y, z);
            int tint = getBiomeColorMultiplier(blockMD, x, y, z);
            if ((tint != 0xFFFFFF) && (tint != 0xFFFFFFFF)) { // white without alpha, white with alpha
                color = colorMultiplier(color, tint);
                JourneyMap.getLogger().fine("Custom biome tint set for " + blockMD.key + " in " + biome.biomeName);
            } else {
                JourneyMap.getLogger().fine("Custom biome tint not found for " + blockMD.key + " in " + biome.biomeName);
            }
            putBiomeColor(blockMD, biome, color);
        }
        return color;
    }

	private Color getFoliageColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z) {
        Color color = getBiomeColor(blockMD, biome);
		if(color==null) {
            int leafColor = blockMD.getBlock().getRenderColor(blockMD.key.meta); // getRenderColor()
            int biomeColor = biome.getBiomeFoliageColor(); // getBiomeFoliageColor()
            int leafTimesBiome = colorMultiplier(biomeColor, leafColor);
            int darker = colorMultiplier(leafTimesBiome, 0xFFAAAAAA); // I added this, I'm sure it'll break with some custom leaf mod somewhere.
            color = new Color(darker);
            putBiomeColor(blockMD, biome, color);
		}
		return color;
	}
	
	private Color getGrassColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z) {
        Color color = getBiomeColor(blockMD, biome);
		if(color==null) {
            Color baseColor = getBaseColor(blockMD, x, y, z);
            int biomeColor = biome.getBiomeGrassColor(); // BiomeGenBase.getBiomeGrassColor()
            color = colorMultiplier(baseColor, biomeColor);
            putBiomeColor(blockMD, biome, color);
		}
		return color;
	}
	
	private Color getWaterColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z) {
        Color color = getBiomeColor(blockMD, biome);
		if(color==null) {
			color = colorMultiplier(getBaseColor(blockMD, x, y, z), biome.waterColorMultiplier);
            putBiomeColor(blockMD, biome, color);
		}
		return color;
	}

    private int getBiomeColorMultiplier(BlockMD blockMD, int x, int y, int z) {
        WorldClient world = FMLClientHandler.instance().getClient().theWorld;
        return blockMD.getBlock().colorMultiplier(world, x, 78, z) | 0xFF000000; // getColorMultiplier()
    }

    private HashMap<BlockMD, Color> getBiomeColorMap(BiomeGenBase biome) {
        synchronized (biomeColors) {
            HashMap<BlockMD, Color> biomeColorMap = biomeColors.get(biome.biomeName);
            if(biomeColorMap==null){
                biomeColorMap = new HashMap<BlockMD, Color>(16);
                biomeColors.put(biome.biomeName, biomeColorMap);
            }
            return biomeColorMap;
        }
    }

    private Color getBiomeColor(BlockMD blockMD, BiomeGenBase biome) {
        return getBiomeColorMap(biome).get(blockMD);
    }

    private void putBiomeColor(BlockMD blockMD, BiomeGenBase biome, Color color) {
        getBiomeColorMap(biome).put(blockMD, color);
    }
	
	/**
	 * Gets the color for the block from the cache, or
	 * gets it from the icon loader.
	 * @param blockMD
	 * @return
	 */
	private Color getBaseColor(BlockMD blockMD, int x, int y, int z) {
        Color color = baseColors.get(blockMD);
        if(color==null) {
            if(blockMD.isTransparent()) {
                color = Color.white;
                blockMD.setAlpha(0f);
                blockMD.addFlags(BlockUtils.Flag.HasAir, BlockUtils.Flag.NotHideSky, BlockUtils.Flag.NoShadow);
            } else {
                color = loadBaseColor(blockMD, x, y, z);
            }
            baseColors.put(blockMD, color);
        }
        return color;
    }

    /**
     * Provides a color using the icon loader.
     * For non-biome blocks, the base color is multiplied against the block's render color.
     * @return
     */
    private Color loadBaseColor(BlockMD blockMD, int x, int y, int z) {

        Color baseColor = null;

        // Get the color from the texture
        synchronized (iconLoader) {
            baseColor = iconLoader.loadBlockColor(blockMD);
        }

        // Non-biome block colors get multiplied by their render color.
        // Some blocks may have custom biome-based tints as well.
        if(baseColor!=null) {
            if(!blockMD.isBiomeColored()){
                // Check for custom biome-based color multiplier
                int tint = getBiomeColorMultiplier(blockMD, x, y, z);
                if ((tint != 0xFFFFFF) && (tint != 0xFFFFFFFF)) { // white without alpha, white with alpha
                    blockMD.addFlags(BlockUtils.Flag.CustomBiomeColor);
                    BlockUtils.setFlags(blockMD.key.uid, BlockUtils.Flag.BiomeColor);
                    JourneyMap.getLogger().fine("Custom biome tint discovered for " + blockMD);
                } else {
                    // Check for render color
                    int renderColor = blockMD.getBlock().getRenderColor(blockMD.key.meta & 0xf); // getRenderColor()
                    if(renderColor!=0xffffff && renderColor!=0xffffffff) { // white without alpha or white with alpha
                        baseColor = colorMultiplier(baseColor, 0xff000000 | renderColor); // Force opaque render color
                        JourneyMap.getLogger().fine("Applied render color for " + blockMD);
                    }
                }
            }
        }

        if(baseColor==null)
        {
            baseColor = Color.BLACK;
            if(iconLoader.failedFor(blockMD)) {
                JourneyMap.getLogger().warning("Iconloader failed to get base color for " + blockMD);
            } else {
                JourneyMap.getLogger().warning("Unknown failure, could not get base color for " + blockMD);
            }
        }
        return baseColor;
	}
	
	public void reset() {
        biomeColors.clear();
		baseColors.clear();
	}

    public String getCacheDebugHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append('\n').append("<html><head><title>JourneyMap Cached Block Colors</title><style>");
        sb.append('\n').append("h1{background-color:#ccc; width:100%;text-align:center}");
        sb.append('\n').append("span{vertical-align:middle; margin:2px}");
        sb.append('\n').append(".entry{width:300px;display:inline-block;}");
        sb.append('\n').append(".rgb{display:inline-block;height:32px;width:32px}");
        sb.append('\n').append("</style></head><body><div>");
        sb.append(debugCache(BlockUtils.getFlagsMap(), "Block Flags"));
        sb.append(debugCache(baseColors, "Base Colors"));

        List<String> biomeNames = new ArrayList<String>(biomeColors.keySet());
        Collections.sort(biomeNames);

        for(String biome : biomeNames) {
            HashMap<BlockMD, Color> colorsForBiome = biomeColors.get(biome);
            sb.append(debugCache(colorsForBiome, "Biome Colors: " + biome));
        }

        sb.append('\n').append("</div></body></html>");
        return sb.toString();
    }

    private String debugCache(HashMap cache, String name) {
        if(cache.isEmpty()) return "";

        List keyList = new ArrayList(cache.keySet());
        Collections.sort(keyList, new Comparator<Object>(){
            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

        String biome = null;
        StringBuilder sb = new StringBuilder().append('\n').append("<div><h1>").append(name).append("</h1>");
        for(Object key : keyList) {
            Object value = cache.get(key);
            String info;
            if(key instanceof BlockMD) {
                info = ((BlockMD) key).getName();
            } else {
                info = key.toString();
                if(info.indexOf("|")>=0){
                    String[] infoSplit = info.split("\\|");
                    if(!infoSplit[0].equals(biome)) {
                        biome = infoSplit[0];
                        sb.append("<h2>").append(biome).append("</h2>");
                    }
                    info = infoSplit[1];
                }
            }

            if(value instanceof Color) {
                Color color = (Color) value;
                String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
                sb.append('\n').append("<span class='entry' title='").append(hex).append("'>");
                sb.append("<span class='rgb' style='background-color:").append(hex).append("'></span>");
                sb.append(info).append("</span>");
            } else {
                sb.append('\n').append("<div class='other'><b>").append(info).append("</b>: ");
                sb.append(value).append("</div>");
            }

        }
        sb.append('\n').append("</div>");
        return sb.toString();
    }

		
	Color colorMultiplier(Color color, int mult) {
		return new Color(colorMultiplier(color.getRGB(), mult));
	}

	int colorMultiplier(int rgb, int mult) {

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

	    return result | -16777216;
	}

}
