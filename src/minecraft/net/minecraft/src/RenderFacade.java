package net.minecraft.src;

public class RenderFacade extends Render {
	
	final Render render;
	
	public RenderFacade(Render render) {
	    this.render = render;
	}

	@Override
	public void doRender(Entity entity, double var2, double var4, double var6, float var8, float var9) {
		this.doRender(entity, var2, var4, var6, var8, var9);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return this.render.getEntityTexture(entity);
	}
	
	public static ResourceLocation getEntityTexture(Render render, Entity entity) {
	    return render.getEntityTexture(entity);
	}

}
