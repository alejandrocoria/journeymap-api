package journeymap.client.ui.component;

import journeymap.common.properties.config.CustomField;

public class TextFieldButton extends TextBoxButton implements IConfigFieldHolder<CustomField>
{
    protected final CustomField field;

    public TextFieldButton(CustomField field)
    {
        super(field.get().toString());
        this.field = field;
        if (field.isNumber())
        {
            textBox = new TextBox(this.displayString, this.fontRenderer, this.width, this.height, field.isNumber(), field.allowNeg());
            textBox.setClamp(field.getMinValue(), field.getMaxValue());
        }
        else
        {
            textBox = new TextBox(this.displayString, this.fontRenderer, this.width, this.height);
        }
        textBox.setY(textBox.getY() - 1);
        textBox.setHeight(textBox.getHeight() - 4);
    }

    public void setValue(Object value)
    {
        if (!field.get().equals(value))
        {
            field.set(value);
            field.save();
        }
        textBox.setText(value);
    }


    @Override
    public boolean keyTyped(char typedChar, int keyCode)
    {
        boolean press = textBox.textboxKeyTyped(typedChar, keyCode);
        setValue(textBox.getText());
        return press;
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
