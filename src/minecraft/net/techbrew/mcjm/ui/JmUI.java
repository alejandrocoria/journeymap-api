package net.techbrew.mcjm.ui;

import net.minecraft.src.GuiScreen;

public abstract class JmUI extends GuiScreen {
	
	public abstract void close();
	
	@Override
	public final boolean doesGuiPauseGame() {
		return false;
	}

}
