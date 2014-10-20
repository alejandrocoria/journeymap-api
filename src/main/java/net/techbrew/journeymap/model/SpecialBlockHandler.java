package net.techbrew.journeymap.model;

import cpw.mods.fml.common.registry.GameRegistry;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Mark on 10/19/2014.
 */
public class SpecialBlockHandler
{
    public static final GameRegistry.UniqueIdentifier OpenBlocksGrave = new GameRegistry.UniqueIdentifier("OpenBlocks:grave");
    public static final GameRegistry.UniqueIdentifier MobSpawner = new GameRegistry.UniqueIdentifier("minecraft:mob_spawner");

    public static final Set<GameRegistry.UniqueIdentifier> Blocks = new HashSet<GameRegistry.UniqueIdentifier>();

    static
    {
        //Blocks.add(OpenBlocksGrave);
        //Blocks.add(MobSpawner);
    }

    public SpecialBlockHandler()
    {

    }

    public void handleBlock(ChunkMD chunkMD, BlockMD blockMD, int x, int y, int z)
    {
        if (blockMD.uid.equals(OpenBlocksGrave))
        {
            handleOpenBlocksGrave(chunkMD, blockMD, x, y, z);
        }
        else if (blockMD.uid.equals(MobSpawner))
        {
            handleMobSpawner(chunkMD, blockMD, x, y, z);
        }
    }

    private void handleOpenBlocksGrave(ChunkMD chunkMD, BlockMD blockMD, int x, int y, int z)
    {
//        int blockX = (chunkMD.getCoord().chunkXPos<<4) + x;
//        int blockZ = (chunkMD.getCoord().chunkZPos<<4) + z;
//        //String name = I18n.format("tile.openblocks.grave.name");
//        Waypoint waypoint = new Waypoint(blockMD.getName(), new ChunkCoordinates(blockX, y, blockZ), Color.red, Waypoint.Type.Death, chunkMD.getWorldObj().provider.dimensionId);
//        WaypointStore.instance().add(waypoint);
    }

    private void handleMobSpawner(ChunkMD chunkMD, BlockMD blockMD, int x, int y, int z)
    {
//        int blockX = (chunkMD.getCoord().chunkXPos<<4) + x;
//        int blockZ = (chunkMD.getCoord().chunkZPos<<4) + z;
//        Waypoint waypoint = new Waypoint(blockMD.getName(), new ChunkCoordinates(blockX, y, blockZ), Color.black, Waypoint.Type.Death, chunkMD.getWorldObj().provider.dimensionId);
//        waypoint.setRandomColor();
//        WaypointStore.instance().add(waypoint);
    }
}
