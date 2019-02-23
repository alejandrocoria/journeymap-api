package journeymap.client.mod.impl;

//import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
//import com.pixelmonmod.pixelmon.client.gui.GuiResources;
//import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
//import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;

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

    // This is the implementation if Pixelmon was a library.
//    public ResourceLocation getPixelmonResource(Entity entity)
//    {
//
//        if (entity instanceof EntityPixelmon)
//        {
//
//            EntityPixelmon pixelmon = (EntityPixelmon) entity;
//            Pokemon pokemon = pixelmon.getPokemonData();
//            String special = SpriteHelper.getSpriteExtra(pixelmon.getName(), pokemon.getForm());
//            String pokemonId = pixelmon.getSpecies().getNationalPokedexNumber();
//            ResourceLocation resource;
//            if (pokemon.isShiny())
//            {
//                resource = GuiResources.shinySprite(pokemonId + special);
//            }
//            else
//            {
//                resource = GuiResources.sprite(pokemonId + special);
//            }
//
//            return resource;
//        }
//        return null;
//    }


    // Reflection version, likely a bit slower.
    public ResourceLocation getPixelmonResource(Entity entity)
    {
        if (isInstanceOf(entity, "com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon"))
        {

            try
            {
                Object pokemon = entity.getClass().getMethod("getPokemonData").invoke(entity);
                String name = (String) entity.getClass().getMethod("getName").invoke(entity);
                int form = (int) pokemon.getClass().getMethod("getForm").invoke(pokemon);
                Class spriteHelper = Class.forName("com.pixelmonmod.pixelmon.util.helpers.SpriteHelper");
                String special = (String) spriteHelper.getMethod("getSpriteExtra", String.class, int.class).invoke(spriteHelper, name, form);
                Object species = entity.getClass().getMethod("getSpecies").invoke(entity);
                String pokemonId = (String) species.getClass().getMethod("getNationalPokedexNumber").invoke(species);

                Boolean shiny = (Boolean) pokemon.getClass().getMethod("isShiny").invoke(pokemon);
                Class guiResources = Class.forName("com.pixelmonmod.pixelmon.client.gui.GuiResources");
                if (shiny)
                {
                    return (ResourceLocation) guiResources.getMethod("shinySprite", String.class).invoke(guiResources, pokemonId + special);

                }
                else
                {
                    return (ResourceLocation) guiResources.getMethod("sprite", String.class).invoke(guiResources, pokemonId + special);
                }

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
