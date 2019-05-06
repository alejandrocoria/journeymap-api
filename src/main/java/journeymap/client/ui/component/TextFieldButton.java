package journeymap.client.ui.component;

import journeymap.common.properties.config.CustomField;
import net.minecraft.client.Minecraft;

public class TextFieldButton extends Button implements IConfigFieldHolder<CustomField>
{

    protected TextBox textBox;
    protected final CustomField field;

    public TextFieldButton(CustomField field)
    {
        super(field.get());
        this.field = field;
        textBox = new TextBox(this.displayString, this.fontRenderer, this.width, this.height);
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks)
    {
        textBox.setMinLength(1);
        textBox.setHeight(this.height + 2);
        textBox.setWidth(this.getWidth());
        textBox.setX(this.getX());
        textBox.setY(this.getY());
        textBox.drawTextBox();
    }

    public void setValue(String value)
    {
        if (!field.get().equals(value))
        {
            field.set(value);
            field.save();
        }
        textBox.setText(value);
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY)
    {
        textBox.setFocused(true);
        return textBox.mouseClicked(mouseX, mouseY, 0);
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode)
    {
        boolean press = textBox.textboxKeyTyped(typedChar, keyCode);
        setValue(textBox.getText());
        return press;
    }

    @Override
    public boolean isMouseOver()
    {
        return textBox.isFocused();
    }

    @Override
    public CustomField getConfigField()
    {
        return field;
    }

    @Override
    public void refresh()
    {
        setValue(field.get());
    }
}
