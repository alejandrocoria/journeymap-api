package net.techbrew.journeymap.task;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.ChunkCoordIntPair;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.ui.option.KeyedEnum;

import java.util.*;

/**
 * Created by Mark on 2/27/2015.
 */
public class RenderSpec
{
    private final Minecraft minecraft;
    private final EntityPlayer player;
    private final ChunkCoordinates playerPos;
    private final Boolean underground;
    private final int renderOffset;
    private final int renderPasses;
    private final RevealShape revealShape;
    private final int minAreaSize = 9;
    private Set<ChunkCoordIntPair> renderAreaCoords;
    private Comparator<ChunkCoordIntPair> comparator;
    private int reservedAreaSize;
    private int maxAreaSize;
    private int secondaryAreaSize;

    RenderSpec(Minecraft minecraft, boolean underground)
    {
        this.minecraft = minecraft;
        this.player = minecraft.thePlayer;
        final int gameRenderDistance = Math.max(1, minecraft.gameSettings.renderDistanceChunks - 1);
        final int mapRenderDistance = underground ? JourneyMap.getCoreProperties().renderDistanceCave.get() : JourneyMap.getCoreProperties().renderDistanceSurface.get();

        this.playerPos = new ChunkCoordinates(player.chunkCoordX, player.chunkCoordY, player.chunkCoordZ);
        this.underground = underground;
        this.renderOffset = Math.min(gameRenderDistance, mapRenderDistance);
        this.renderPasses = JourneyMap.getCoreProperties().renderPasses.get();
        this.revealShape = JourneyMap.getCoreProperties().revealShape.get();
    }

    private static Double blockDistance(ChunkCoordIntPair playerCoord, ChunkCoordIntPair coord)
    {
        int x = playerCoord.getCenterXPos() - coord.getCenterXPos();
        int z = playerCoord.getCenterZPosition() - coord.getCenterZPosition();
        return Math.sqrt(x * x + z * z);
    }

    private static Double chunkDistance(ChunkCoordIntPair playerCoord, ChunkCoordIntPair coord)
    {
        int x = playerCoord.chunkXPos - coord.chunkXPos;
        int z = playerCoord.chunkZPos - coord.chunkZPos;
        return Math.sqrt(x * x + z * z);
    }

    public static Comparator<ChunkCoordIntPair> getChunkDistanceComparator()
    {
        return new Comparator<ChunkCoordIntPair>()
        {
            Minecraft minecraft = FMLClientHandler.instance().getClient();
            ChunkCoordIntPair playerCoord = new ChunkCoordIntPair(minecraft.thePlayer.chunkCoordX, minecraft.thePlayer.chunkCoordZ);

            @Override
            public int compare(ChunkCoordIntPair o1, ChunkCoordIntPair o2)
            {
                int comp = blockDistance(playerCoord, o1).compareTo(blockDistance(playerCoord, o2));
                if (comp == 0)
                {
                    comp = Integer.compare(o1.chunkXPos, o2.chunkXPos);
                }
                if (comp == 0)
                {
                    comp = Integer.compare(o1.chunkZPos, o2.chunkZPos);
                }
                return comp;
            }
        };
    }

    protected Set<ChunkCoordIntPair> getRenderAreaCoords()
    {
        // Add peripheral coords around player
        if (renderAreaCoords == null || renderAreaCoords.isEmpty())
        {
            DataCache dataCache = DataCache.instance();
            ChunkCoordIntPair playerCoord = new ChunkCoordIntPair(minecraft.thePlayer.chunkCoordX, minecraft.thePlayer.chunkCoordZ);
            List<ChunkCoordIntPair> peripheralCoords = new ArrayList<ChunkCoordIntPair>((int) Math.pow(renderOffset * 2 + 1, 2));
            for (int x = (player.chunkCoordX - renderOffset); x <= (player.chunkCoordX + renderOffset); x++)
            {
                for (int z = (player.chunkCoordZ - renderOffset); z <= (player.chunkCoordZ + renderOffset); z++)
                {
                    ChunkCoordIntPair coord = new ChunkCoordIntPair(x, z);
                    if (inRange(playerCoord, coord, renderOffset, revealShape))
                    {
                        peripheralCoords.add(coord);
                        dataCache.getChunkMD(coord);
                    }
                }
            }

            maxAreaSize = peripheralCoords.size();
            if (renderPasses == 1)
            {
                reservedAreaSize = maxAreaSize;
            }
            else if (renderPasses == 2)
            {
                reservedAreaSize = Math.max(minAreaSize, (int) Math.ceil(maxAreaSize / 2.0));
                secondaryAreaSize = reservedAreaSize / 2;
            }
            else
            {
                reservedAreaSize = Math.max(minAreaSize, (int) Math.ceil(maxAreaSize / 4.0));
                secondaryAreaSize = reservedAreaSize;
            }

            // Sort by distance
            Collections.sort(peripheralCoords, getDistanceComparator());

            // Truncate to reserved area
            this.renderAreaCoords = Collections.unmodifiableSet(new HashSet<ChunkCoordIntPair>(peripheralCoords.subList(0, Math.min(peripheralCoords.size(), reservedAreaSize))));
        }
        return renderAreaCoords;
    }

    boolean inRange(ChunkCoordIntPair playerCoord, ChunkCoordIntPair coord, int renderDistance, RenderSpec.RevealShape revealShape)
    {
        if (revealShape == RenderSpec.RevealShape.Circle)
        {
            double distance = blockDistance(playerCoord, coord);
            double diff = distance - (renderDistance * 16);
            return diff <= 8; // Makes for fuller circles by letting in chunks that are half-way in the perimeter
        }
        else
        {
            float x = Math.abs(playerCoord.chunkXPos - coord.chunkXPos);
            float z = Math.abs(playerCoord.chunkZPos - coord.chunkZPos);
            return x <= renderDistance && z <= renderDistance;
        }
    }

    public ChunkCoordinates getPlayerPos()
    {
        return playerPos;
    }

    public Boolean getUnderground()
    {
        return underground;
    }

    public int getRenderOffset()
    {
        return renderOffset;
    }

    public int getRenderPasses()
    {
        return renderPasses;
    }

    public RevealShape getRevealShape()
    {
        return revealShape;
    }

    public int getMinAreaSize()
    {
        return minAreaSize;
    }

    public int getReservedAreaSize()
    {
        return reservedAreaSize;
    }

    public int getMaxAreaSize()
    {
        return maxAreaSize;
    }

    public int getSecondaryAreaSize()
    {
        return secondaryAreaSize;
    }

    public Comparator<ChunkCoordIntPair> getDistanceComparator()
    {
        if (comparator == null)
        {
            comparator = new Comparator<ChunkCoordIntPair>()
            {
                boolean useBlockDistance = revealShape == RevealShape.Circle;
                Minecraft minecraft = FMLClientHandler.instance().getClient();
                ChunkCoordIntPair playerCoord = new ChunkCoordIntPair(minecraft.thePlayer.chunkCoordX, minecraft.thePlayer.chunkCoordZ);

                @Override
                public int compare(ChunkCoordIntPair o1, ChunkCoordIntPair o2)
                {
                    int comp = 0;
                    if (o1 == o2 || o1.equals(o2))
                    {
                        return 0;
                    }
                    if (useBlockDistance)
                    {
                        comp = blockDistance(playerCoord, o1).compareTo(blockDistance(playerCoord, o2));
                    }
                    else
                    {
                        comp = chunkDistance(playerCoord, o1).compareTo(chunkDistance(playerCoord, o2));
                    }
                    if (comp == 0)
                    {
                        comp = Integer.compare(o1.chunkXPos, o2.chunkXPos);
                    }
                    if (comp == 0)
                    {
                        comp = Integer.compare(o1.chunkZPos, o2.chunkZPos);
                    }
                    return comp;
                }
            };
        }
        return comparator;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        RenderSpec that = (RenderSpec) o;

        return that.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode()
    {
        int result = playerPos.hashCode();
        result = 31 * result + underground.hashCode();
        result = 31 * result + renderOffset;
        result = 31 * result + renderPasses;
        result = 31 * result + revealShape.hashCode();
        return result;
    }

    public enum RevealShape implements KeyedEnum
    {
        Square("jm.minimap.shape_square"),
        Circle("jm.minimap.shape_circle");
        public final String key;

        RevealShape(String key)
        {
            this.key = key;
        }

        @Override
        public String getKey()
        {
            return key;
        }

        @Override
        public String toString()
        {
            return Constants.getString(this.key);
        }
    }
}
