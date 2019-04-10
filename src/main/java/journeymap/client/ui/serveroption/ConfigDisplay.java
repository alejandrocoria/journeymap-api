package journeymap.client.ui.serveroption;

import com.google.gson.JsonObject;
import journeymap.client.ui.component.ButtonList;
import net.minecraft.client.gui.FontRenderer;

public class ConfigDisplay implements Draw
{
    private TopBoxOptions topBoxOptions;
    private RadarOptions radarOptions;
    private MappingOptions mappingOptions;


    public ConfigDisplay(JsonObject properties, FontRenderer fontRenderer)
    {
        this.topBoxOptions = new TopBoxOptions(properties, fontRenderer);
        this.radarOptions = new RadarOptions(properties, fontRenderer);
        this.mappingOptions = new MappingOptions(properties, fontRenderer);
    }

    @Override
    public void draw(int startX, int startY, int gap)
    {
        topBoxOptions.draw(startX, startY, gap);
        radarOptions.draw(startX, topBoxOptions.getButtons().getBottomY(), gap);
        mappingOptions.draw(startX, radarOptions.getButtons().getBottomY(), gap);
    }

    @Override
    public ButtonList getButtons()
    {
        ButtonList list = new ButtonList();
        list.addAll(topBoxOptions.getButtons());
        list.addAll(radarOptions.getButtons());
        list.addAll(mappingOptions.getButtons());
        return list;
    }
}
