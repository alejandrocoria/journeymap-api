package net.techbrew.journeymap.task;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.ChunkCoordIntPair;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.properties.CoreProperties;
import net.techbrew.journeymap.ui.option.KeyedEnum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Mark on 2/27/2015.
 */
public class RenderSpec
{
    private final Minecraft minecraft;
    private final EntityPlayer player;
    private final Boolean underground;
    private final int primaryRenderDistance;
    private final int maxSecondaryRenderDistance;
    private final RevealShape revealShape;

    private ListMultimap<Integer, Offset> offsets = null;
    private ArrayList<ChunkCoordIntPair> primaryRenderCoords;
    private Comparator<ChunkCoordIntPair> comparator;
    private int lastSecondaryRenderDistance;

    private ChunkCoordIntPair lastPlayerCoord;

    RenderSpec(Minecraft minecraft, boolean underground)
    {
        this.minecraft = minecraft;
        this.player = minecraft.thePlayer;
        final CoreProperties props = JourneyMap.getCoreProperties();
        final int gameRenderDistance = Math.max(1, minecraft.gameSettings.renderDistanceChunks - 1);
        final int mapRenderDistanceMin = underground ? props.renderDistanceCaveMin.get() : props.renderDistanceSurfaceMin.get();
        final int mapRenderDistanceMax = underground ? props.renderDistanceCaveMax.get() : props.renderDistanceSurfaceMax.get();

        this.underground = underground;
        int rdMin = Math.min(gameRenderDistance, mapRenderDistanceMin);
        int rdMax = Math.min(gameRenderDistance, Math.max(rdMin, mapRenderDistanceMax));
        if (rdMin + 1 == rdMax)
        {
            rdMin++;
        }

        this.primaryRenderDistance = rdMin;
        this.maxSecondaryRenderDistance = rdMax;
        this.revealShape = JourneyMap.getCoreProperties().revealShape.get();

        lastPlayerCoord = new ChunkCoordIntPair(minecraft.thePlayer.chunkCoordX, minecraft.thePlayer.chunkCoordZ);
        lastSecondaryRenderDistance = this.primaryRenderDistance;
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

    static boolean inRange(ChunkCoordIntPair playerCoord, ChunkCoordIntPair coord, int renderDistance, RenderSpec.RevealShape revealShape)
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

    private static ListMultimap<Integer, Offset> calculateOffsets(int minOffset, int maxOffset, RevealShape revealShape)
    {
        ListMultimap<Integer, Offset> multimap = ArrayListMultimap.create();

        int offset = maxOffset;
        final int baseX = 0;
        final int baseZ = 0;
        final ChunkCoordIntPair baseCoord = new ChunkCoordIntPair(baseX, baseZ);
        while (offset >= minOffset)
        {
            for (int x = (baseX - offset); x <= (baseX + offset); x++)
            {
                for (int z = (baseZ - offset); z <= (baseZ + offset); z++)
                {
                    ChunkCoordIntPair coord = new ChunkCoordIntPair(x, z);
                    if (revealShape == RevealShape.Square || inRange(baseCoord, coord, offset, revealShape))
                    {
                        multimap.put(offset, new Offset(coord.chunkXPos, coord.chunkZPos));
                    }
                }
            }

            if (offset < maxOffset)
            {
                List<Offset> oneUp = multimap.get(offset + 1);
                oneUp.removeAll(multimap.get(offset));
            }

            offset--;
        }

        return new ImmutableListMultimap.Builder<Integer, Offset>().putAll(multimap).build();
    }

    protected Collection<ChunkCoordIntPair> getRenderAreaCoords()
    {
        // Lazy load offsets on first use
        if (offsets == null)
        {
            offsets = calculateOffsets(primaryRenderDistance, maxSecondaryRenderDistance, revealShape);
        }

        DataCache dataCache = DataCache.instance();

        // Reset coords if player moved
        if(lastPlayerCoord==null || lastPlayerCoord.chunkXPos!=player.chunkCoordX || lastPlayerCoord.chunkZPos!=player.chunkCoordZ)
        {
            primaryRenderCoords = null;
            lastSecondaryRenderDistance = primaryRenderDistance;
        }
        lastPlayerCoord= new ChunkCoordIntPair(minecraft.thePlayer.chunkCoordX, minecraft.thePlayer.chunkCoordZ);

        // Add min distance coords around player
        if (primaryRenderCoords == null || primaryRenderCoords.isEmpty())
        {
            List<Offset> primaryOffsets = offsets.get(primaryRenderDistance);
            primaryRenderCoords = new ArrayList<ChunkCoordIntPair>(primaryOffsets.size());
            for (Offset offset : primaryOffsets)
            {
                ChunkCoordIntPair primaryCoord = offset.from(lastPlayerCoord);
                primaryRenderCoords.add(primaryCoord);
                dataCache.getChunkMD(primaryCoord);
            }
        }

        if (maxSecondaryRenderDistance == primaryRenderDistance)
        {
            // Someday it may be necessary to return an immutable list if these will be consumed elsewhere
            return primaryRenderCoords;
        }
        else
        {
            if (lastSecondaryRenderDistance == maxSecondaryRenderDistance)
            {
                lastSecondaryRenderDistance = primaryRenderDistance;
            }
            lastSecondaryRenderDistance++;

            List<Offset> secondaryOffsets = offsets.get(lastSecondaryRenderDistance);

            ArrayList<ChunkCoordIntPair> renderCoords = new ArrayList<ChunkCoordIntPair>(primaryRenderCoords.size() + secondaryOffsets.size());
            renderCoords.addAll(primaryRenderCoords);

            for (Offset offset : secondaryOffsets)
            {
                ChunkCoordIntPair secondaryCoord = offset.from(lastPlayerCoord);
                renderCoords.add(secondaryCoord);
                dataCache.getChunkMD(secondaryCoord);
            }

            return renderCoords;
        }
    }

    public Boolean getUnderground()
    {
        return underground;
    }

    public int getPrimaryRenderDistance()
    {
        return primaryRenderDistance;
    }

    public int getMaxSecondaryRenderDistance()
    {
        return maxSecondaryRenderDistance;
    }

    public int getLastSecondaryRenderDistance()
    {
        return lastSecondaryRenderDistance;
    }

    public RevealShape getRevealShape()
    {
        return revealShape;
    }

    public int getLastSecondaryRenderSize()
    {
        return offsets == null ? 0 : offsets.get(lastSecondaryRenderDistance).size();
    }

    public int getPrimaryRenderSize()
    {
        return offsets == null ? 0 : offsets.get(primaryRenderDistance).size();
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

        if (maxSecondaryRenderDistance != that.maxSecondaryRenderDistance)
        {
            return false;
        }
        if (primaryRenderDistance != that.primaryRenderDistance)
        {
            return false;
        }
        if (revealShape != that.revealShape)
        {
            return false;
        }
        if (!underground.equals(that.underground))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = underground.hashCode();
        result = 31 * result + primaryRenderDistance;
        result = 31 * result + maxSecondaryRenderDistance;
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

    private static class Offset
    {
        final int x;
        final int z;

        private Offset(int x, int z)
        {
            this.x = x;
            this.z = z;
        }

        ChunkCoordIntPair from(ChunkCoordIntPair coord)
        {
            return new ChunkCoordIntPair(coord.chunkXPos + x, coord.chunkZPos + z);
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

            Offset offset = (Offset) o;

            if (x != offset.x)
            {
                return false;
            }
            if (z != offset.z)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = x;
            result = 31 * result + z;
            return result;
        }
    }
}
