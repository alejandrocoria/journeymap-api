package journeymap.client.ui.option;

import journeymap.client.Constants;
import journeymap.client.ui.component.ListPropertyButton;
import journeymap.common.properties.config.StringField;

import java.util.Arrays;
import java.util.List;

public class DateFormat
{
    private static String[] timeFormatValues = new String[]{
            "MM-dd-yyyy", "MM-dd-yy", "dd-MM-yyyy", "dd-MM-yy", "yyyy-MM-dd", "yy-MM-dd"};

    public static class Provider implements StringField.ValuesProvider
    {
        public Provider()
        {
        }

        @Override
        public List<String> getStrings()
        {
            return Arrays.asList(timeFormatValues);
        }

        @Override
        public String getDefaultString()
        {
            return timeFormatValues[0];
        }
    }

    public static class Button extends ListPropertyButton<String>
    {
        DateFormat timeFormat;

        public Button(StringField valueHolder)
        {
            super(Arrays.asList(timeFormatValues), Constants.getString("jm.common.date_format"), valueHolder);
            if (timeFormat == null)
            {
                timeFormat = new DateFormat();
            }
        }
    }
}
