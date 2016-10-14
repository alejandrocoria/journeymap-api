package journeymap.client.render.texture;

import com.google.common.cache.*;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * TextureImpl with a resource location as the image source.
 * <p/>
 * TODO:  Look at Dynamic Texture to see if the data copy is more efficient
 */
public class ResourceLocationTexture extends TextureImpl
{
    protected static final LoadingCache<ResourceLocation, ResourceLocationTexture> CACHE = createCache();

    private boolean isExternallyBound;
    private final ResourceLocation location;

    public static void purge()
    {
        CACHE.invalidateAll();
    }

    public static ResourceLocationTexture get(ResourceLocation location)
    {
        return get(location, false, false);
    }

    public static ResourceLocationTexture getRetained(ResourceLocation location)
    {
        return get(location, true, false);
    }

    public static ResourceLocationTexture get(ResourceLocation location, boolean retainImage, boolean bindImmediately)
    {
        ResourceLocationTexture tex = CACHE.getUnchecked(location);
        if (tex.isBound())
        {
            if (retainImage && !tex.hasImage())
            {
                tex.setImage(TextureCache.resolveImage(location), true);
            }
        }
        else
        {
            if (!tex.hasImage())
            {
                tex.setImage(TextureCache.resolveImage(location), retainImage);
            }

            try
            {
                // Check for existing
                ITextureObject existing = Minecraft.getMinecraft().getTextureManager().getTexture(location);
                if (existing != null)
                {
                    bindImmediately = false;
                    tex.glTextureId = existing.getGlTextureId();
                    tex.isExternallyBound = true;
                }
                else
                {
                    tex.isExternallyBound = false;
                }
            }
            catch (Exception e)
            {
                tex.isExternallyBound = false;
            }

            if (bindImmediately)
            {
                tex.bindTexture();
            }
        }

        return tex;
    }

    public ResourceLocationTexture(ResourceLocation location)
    {
        super(null, null, false, false);
        this.location = location;
        this.description = location.getResourcePath();
    }

    public ResourceLocation getLocation()
    {
        return this.location;
    }

    /**
     * Build a cache for ResourceLocationTextures.  Weak values
     * let textures get unbound when no longer used.
     *
     * @return
     */
    public static LoadingCache<ResourceLocation, ResourceLocationTexture> createCache()
    {
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
        builder.concurrencyLevel(1);
        if (Journeymap.getClient().getCoreProperties().recordCacheStats.get())
        {
            builder.recordStats();
        }

        builder.weakValues().removalListener(new RemovalListener<ResourceLocation, TextureImpl>()
        {
            /**
             * Only delete texture if Minecraft's TextureManager doesn't own it already.
             * @param notification removal
             */
            @Override
            public void onRemoval(@Nonnull RemovalNotification<ResourceLocation, TextureImpl> notification)
            {
                TextureImpl tex = notification.getValue();
                if (tex != null)
                {
                    if (tex instanceof ResourceLocationTexture)
                    {
                        ResourceLocationTexture resourceLocationTexture = (ResourceLocationTexture) tex;
                        if (resourceLocationTexture.isExternallyBound)
                        {
                            return;
                        }
                    }

                    tex.queueForDeletion();
                }
            }
        });

        return builder.build(
                new CacheLoader<ResourceLocation, ResourceLocationTexture>()
                {
                    public ResourceLocationTexture load(@Nonnull ResourceLocation key) throws Exception
                    {
                        return new ResourceLocationTexture(key);
                    }
                });
    }
}
