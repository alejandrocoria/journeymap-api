package journeymap.client.ui.serveroption;

import com.google.gson.JsonObject;
import journeymap.client.Constants;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.CheckBox;

import static journeymap.client.ui.serveroption.ServerOptionsManager.formattedToolTipHeader;

public interface Draw
{
    void draw(int startX, int startY, int gap);

    ButtonList getButtons();

    default CheckBox checkBox(String key, final String field, final JsonObject properties)
    {
        if (properties.get(field) != null)
        {
            CheckBox checkBox = new CheckBox(Constants.getString(key), properties.get(field).getAsBoolean());
            checkBox.setTooltip(formattedToolTipHeader(key) + Constants.getString(key + ".tooltip"));
            checkBox.addToggleListener((button, toggled) -> {
                properties.remove(field);
                properties.addProperty(field, toggled);
                return true;
            });
            return checkBox;
        }
        // return a disabled checkbox if empty
        CheckBox checkBox = new CheckBox("", true);
        checkBox.enabled = false;
        return checkBox;
    }

    default void updateToggleProperty(ServerOption option, JsonObject properties, String node, String opNode)
    {
        properties.remove(node);
        properties.remove(opNode);
        properties.addProperty(node, option.getFieldValue());
        properties.addProperty(opNode, option.getOpFieldValue());
    }

    default String getToggleTooltipBase()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.getString("jm.server.edit.toggle.base.all")).append("\n");
        sb.append(Constants.getString("jm.server.edit.toggle.base.op")).append("\n");
        sb.append(Constants.getString("jm.server.edit.toggle.base.none")).append("\n");
        return sb.toString();
    }
}
