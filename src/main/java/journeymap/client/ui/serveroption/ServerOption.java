package journeymap.client.ui.serveroption;

import com.google.gson.JsonObject;
import journeymap.client.Constants;

public class ServerOption
{
    private String field;
    private String opField;
    private JsonObject properties;
    public Option option;

    public ServerOption(String field, JsonObject properties)
    {
        this.field = field;
        this.properties = properties;
        this.opField = "op_" + field;
        this.option = findOption();
    }

    public Option getOption()
    {
        return option;
    }

    public boolean getFieldValue()
    {
        if (Option.ALL.equals(this.option))
        {
            return true;
        }
        return false;
    }

    public boolean getOpFieldValue()
    {
        if (Option.OPS.equals(this.option) || Option.ALL.equals(this.option))
        {
            return true;
        }
        return false;
    }

    public void setOption(Option option)
    {
        this.option = option;
    }

    private Option findOption()
    {
        if (properties.get(field) != null && properties.get(opField) != null)
        {
            boolean all = properties.get(field).getAsBoolean();
            boolean op = properties.get(opField).getAsBoolean();

            if (all)
            {
                return Option.ALL;

            }
            else if (op)
            {
                return Option.OPS;
            }

        }
        return Option.NONE;
    }


    public enum Option
    {
        ALL("jm.server.edit.option.all"),
        OPS("jm.server.edit.option.op"),
        NONE("jm.server.edit.option.none");

        private String key;

        Option(String key)
        {
            this.key = key;
        }

        public String displayName()
        {
            return Constants.getString(key);
        }

        @Override
        public String toString()
        {
            return displayName();
        }
    }
}
