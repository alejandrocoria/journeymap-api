package net.techbrew.journeymap.ui.config;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.gui.GuiScreen;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.util.List;

public class ConfigManager extends GuiConfig
{
    protected TextureImpl logo = TextureCache.instance().getLogo();

    public ConfigManager(GuiScreen parent)
    {
        super(parent, ConfigManagerFactory.getConfigElements(), JourneyMap.MOD_ID, false, false, "JourneyMap " + Constants.getString("jm.common.options"));
        this.needsRefresh = true;
    }

    public ConfigManager(GuiScreen parentScreen, List<IConfigElement> configElements, String modID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title, String titleLine2)
    {
        super(parentScreen, configElements, modID, allRequireWorldRestart, allRequireMcRestart, title, titleLine2);
        this.needsRefresh = true;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawLogo();
    }

    protected void drawLogo()
    {
        DrawUtil.sizeDisplay(mc.displayWidth, mc.displayHeight);
        DrawUtil.drawImage(logo, 8, 8, false, 1, 0);
        DrawUtil.sizeDisplay(width, height);
    }

    @Override
    public void drawWorldBackground(int p_146270_1_)
    {
        // this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
    }
}