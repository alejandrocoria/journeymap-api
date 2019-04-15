package journeymap.client.mod.impl;

import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class Pixelmon
{
    public static Pixelmon INSTANCE;

    public static boolean loaded = false;

    private Pixelmon()
    {
    }

    public Pixelmon(boolean loaded)
    {
        this.loaded = loaded;
        INSTANCE = new Pixelmon();
    }

    public ResourceLocation getPixelmonResource(Entity entity)
    {
        if (isInstanceOf(entity, "com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon"))
        {
            try
            {
                ResourceLocation pixelmonSprite = (ResourceLocation) entity.getClass().getMethod("getSprite").invoke(entity);
                if (pixelmonSprite != null)
                {
                    return pixelmonSprite;
                }
                return null;
            }
            catch (Exception e)
            {
                Journeymap.getLogger().error(String.format("Error getting pixelmon sprite from %s:", LogFormatter.toPartialString(e)));
            }

        }
        return null;
    }

    private static boolean isInstanceOf(Object pokemonEntity, String... classPaths)
    {
        Class matchedClass;
        for (String classPath : classPaths)
        {
            try
            {
                matchedClass = Class.forName(classPath);
                if (matchedClass.isInstance(pokemonEntity))
                {
                    return true;
                }
            }
            catch (Exception e)
            {
                return false;
            }
        }
        return false;
    }
}
