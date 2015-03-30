package net.techbrew.journeymap.model.mod;

import com.google.common.base.Joiner;
import cpw.mods.fml.common.registry.GameRegistry;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.ChunkMD;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class ModBlockDelegate
{
    // TODO: Move special block handling (torches, biomes) in BlockRegistry here

    //public static final GameRegistry.UniqueIdentifier MobSpawner = new GameRegistry.UniqueIdentifier("minecraft:mob_spawner");

    public static final HashMap<GameRegistry.UniqueIdentifier, IModBlockHandler> Blocks = new HashMap<GameRegistry.UniqueIdentifier, IModBlockHandler>();

    private static final HashMap<GameRegistry.UniqueIdentifier, AtomicInteger> Errors = new HashMap<GameRegistry.UniqueIdentifier, AtomicInteger>();
    private static final int ERROR_LIMIT = 25;
    private static Logger logger = JourneyMap.getLogger();

    private static void register(IModBlockHandler handler)
    {
        try
        {
            List<GameRegistry.UniqueIdentifier> success = new ArrayList<GameRegistry.UniqueIdentifier>();
            for (GameRegistry.UniqueIdentifier uid : handler.getBlockUids())
            {
                if (uid != null)
                {
                    if (register(handler, uid))
                    {
                        success.add(uid);
                    }
                }
            }
            if (success.size() > 0)
            {
                logger.info(String.format("Registered IModBlockHandler for: '%s'.", Joiner.on(", ").join(success)));
            }
        }
        catch (Throwable t)
        {
            String message = String.format("Couldn't register IModBlockHandler '%s': %s",
                    Joiner.on(", ").join(handler.getBlockUids()),
                    LogFormatter.toString(t));
            logger.error(message);
        }
    }

    private static boolean register(IModBlockHandler handler, GameRegistry.UniqueIdentifier uid)
    {
        if (uid == null)
        {
            throw new IllegalStateException("UID cannot be null");
        }
        if (Blocks.containsKey(uid))
        {
            throw new IllegalStateException("UID already registered to " + Blocks.get(uid));
        }
        Blocks.put(uid, handler);
        Errors.put(uid, new AtomicInteger(0));
        return true;
    }

    public boolean canHandle(BlockMD blockMD)
    {
        return Blocks.containsKey(blockMD.uid);
    }

    public BlockMD handleBlock(ChunkMD chunkMD, final BlockMD blockMD, int localX, int y, int localZ)
    {
        BlockMD delegatedBlockMD = null;
        try
        {
            IModBlockHandler handler = Blocks.get(blockMD.uid);
            if (handler != null)
            {
                delegatedBlockMD = handler.handleBlock(chunkMD, blockMD, localX, y, localZ);
            }
        }
        catch (Throwable t)
        {
            int count = Errors.get(blockMD.uid).incrementAndGet();
            String message = String.format("Error (%s) handling block '%s': %s", count, blockMD.uid, LogFormatter.toString(t));
            logger.error(message);
            if (count >= ERROR_LIMIT)
            {
                logger.warn(String.format("Deregistering problematic IModBlockHandler for '%s'.", blockMD.uid));
                Blocks.remove(blockMD.uid);
            }
        }
        if (delegatedBlockMD == null)
        {
            delegatedBlockMD = blockMD;
        }
        return delegatedBlockMD;
    }

    public static interface IModBlockHandler
    {
        public Collection<GameRegistry.UniqueIdentifier> getBlockUids();

        public BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, int localX, int y, int localZ);
    }

    static
    {
        register(new OpenBlocks.GraveHandler());
        register(new CarpentersBlocks.CommonHandler());
    }
}
