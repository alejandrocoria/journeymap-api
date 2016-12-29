package journeymap.client.waypoint;

import journeymap.client.model.Waypoint;
import journeymap.common.Journeymap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * Parses text into a waypoint if possible. Pattern expected is comma-delimited key:value pairs, surrounded by square brackets.
 * Minimally a waypoint needs x and z to be useful. eg [x:23, y:35]
 */
public class WaypointParser
{
    public static String[] QUOTES = {"'", "\""};
    public static Pattern PATTERN = Pattern.compile("(\\w+\\s*:\\s*-?[\\w\\d\\s'\"]+,\\s*)+(\\w+\\s*:\\s*-?[\\w\\d\\s'\"]+)", Pattern.CASE_INSENSITIVE);

    /**
     * Returns the substrings of line which can become waypoints.
     *
     * @param line text
     * @return null if none found.
     */
    public static List<String> getWaypointStrings(String line)
    {
        List<String> list = null;
        String[] candidates = StringUtils.substringsBetween(line, "[", "]");
        if (candidates != null)
        {
            for (String candidate : candidates)
            {
                if (PATTERN.matcher(candidate).find())
                {
                    if (parse(candidate) != null)
                    {
                        if (list == null)
                        {
                            list = new ArrayList<>(1);
                        }
                        list.add("[" + candidate + "]");
                    }
                }
            }
        }

        return list;
    }

    /**
     * Returns the substrings of line which can become waypoints.
     *
     * @param line text
     * @return null if none found.
     */
    public static List<Waypoint> getWaypoints(String line)
    {
        List<Waypoint> list = null;
        String[] candidates = StringUtils.substringsBetween(line, "[", "]");
        if (candidates != null)
        {
            for (String candidate : candidates)
            {
                if (PATTERN.matcher(candidate).find())
                {
                    Waypoint waypoint = parse(candidate);
                    if (waypoint != null)
                    {
                        if (list == null)
                        {
                            list = new ArrayList<>(1);
                        }
                        list.add(waypoint);
                    }
                }
            }
        }

        return list;
    }

    /**
     * Creates a waypoint from the text provided.
     *
     * @param original text
     * @return null if not parsable.
     */
    public static Waypoint parse(final String original)
    {
        String[] quotedVals = null;
        String raw = original.replaceAll("[\\[\\]]", "");
        for (String quoteChar : QUOTES)
        {
            if (raw.contains(quoteChar))
            {
                quotedVals = StringUtils.substringsBetween(raw, quoteChar, quoteChar);
                if (quotedVals != null)
                {
                    for (int i = 0; i < quotedVals.length; i++)
                    {
                        String val = quotedVals[i];
                        raw = raw.replaceAll(quoteChar + val + quoteChar, "__TEMP_" + i);
                    }
                }
            }
        }

        Integer x = null;
        Integer y = 63;
        Integer z = null;
        Integer dim = 0;
        String name = null;
        for (String part : raw.split(","))
        {
            if (part.contains(":"))
            {
                String[] prop = part.split(":");
                if (prop.length == 2)
                {
                    String key = prop[0].trim().toLowerCase();
                    String val = prop[1].trim();
                    try
                    {
                        if ("x".equals(key))
                        {
                            x = Integer.parseInt(val);
                        }
                        else if ("y".equals(key))
                        {
                            y = Math.max(0, Math.min(255, Integer.parseInt(val)));
                        }
                        else if ("z".equals(key))
                        {
                            z = Integer.parseInt(val);
                        }
                        else if ("dim".equals(key))
                        {
                            dim = Integer.parseInt(val);
                        }
                        else if ("name".equals(key))
                        {
                            name = val;
                        }
                    }
                    catch (Exception e)
                    {
                        Journeymap.getLogger().warn("Bad format in waypoint text part: " + part + ": " + e);
                    }
                }
            }
        }

        if (x != null && z != null)
        {
            if (name != null && quotedVals != null)
            {
                for (int i = 0; i < quotedVals.length; i++)
                {
                    String val = quotedVals[i];
                    name = name.replaceAll("__TEMP_" + i, val);
                }
            }

            if (name == null)
            {
                name = String.format("%s,%s", x, z);
            }
            Random r = new Random();
            Waypoint waypoint = new Waypoint(name, new BlockPos(x, y, z), new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)), Waypoint.Type.Normal, dim);
            return waypoint;
        }

        return null;
    }

    public static void parseChatForWaypoints(ClientChatReceivedEvent event, String unformattedText)
    {
        List<String> matches = getWaypointStrings(unformattedText);
        if (matches != null)
        {
            boolean changed = false;
            if (event.getMessage() instanceof TextComponentTranslation)
            {
                Object[] formatArgs = ((TextComponentTranslation) event.getMessage()).getFormatArgs();
                for (int i = 0; i < formatArgs.length; i++)
                {
                    if (matches.isEmpty())
                    {
                        break;
                    }

                    if (formatArgs[i] instanceof ITextComponent)
                    {
                        ITextComponent arg = (ITextComponent) formatArgs[i];
                        ITextComponent result = addWaypointMarkup(arg.getUnformattedText(), matches);
                        if (result != null)
                        {
                            formatArgs[i] = result;
                            changed = true;
                        }
                    }
                    else if (formatArgs[i] instanceof String)
                    {
                        String arg = (String) formatArgs[i];
                        ITextComponent result = addWaypointMarkup(arg, matches);
                        if (result != null)
                        {
                            formatArgs[i] = result;
                            changed = true;
                        }
                    }
                }

                if (changed)
                {
                    event.setMessage(new TextComponentTranslation(((TextComponentTranslation) event.getMessage()).getKey(), formatArgs));
                }
            }
            else if (event.getMessage() instanceof TextComponentString)
            {
                ITextComponent result = addWaypointMarkup(event.getMessage().getUnformattedText(), matches);
                if (result != null)
                {
                    event.setMessage(result);
                    changed = true;
                }
            }
            else
            {
                Journeymap.getLogger().warn("No implementation for handling waypoints in ITextComponent " + event.getMessage().getClass());
            }

            if (!changed)
            {
                Journeymap.getLogger().warn(String.format("Matched waypoint in chat but failed to update message for %s : %s\n%s",
                        event.getMessage().getClass(),
                        event.getMessage().getFormattedText(),
                        ITextComponent.Serializer.componentToJson(event.getMessage())));
            }
        }
    }

    private static ITextComponent addWaypointMarkup(String text, List<String> matches)
    {
        List<ITextComponent> newParts = new ArrayList<ITextComponent>();

        int index = 0;

        boolean matched = false;
        Iterator<String> iterator = matches.iterator();
        while (iterator.hasNext())
        {
            String match = iterator.next();
            if (text.contains(match))
            {
                int start = text.indexOf(match);
                if (start > index)
                {
                    newParts.add(new TextComponentString(text.substring(index, start)));
                }

                matched = true;

                TextComponentString clickable = new TextComponentString(match);
                Style chatStyle = clickable.getStyle();
                chatStyle.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/jm wpedit " + match));

                // TODO i18n
                TextComponentString hover = new TextComponentString("JourneyMap: ");
                hover.getStyle().setColor(TextFormatting.YELLOW);

                // TODO i18n
                TextComponentString hover2 = new TextComponentString("Click to create Waypoint.\nCtrl+Click to view on map.");
                hover2.getStyle().setColor(TextFormatting.AQUA);
                hover.appendSibling(hover2);

                chatStyle.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
                chatStyle.setColor(TextFormatting.AQUA);

                newParts.add(clickable);

                index = start + match.length();

                iterator.remove();
            }
        }

        if (!matched)
        {
            return null;
        }
        else if (index < text.length() - 1)
        {
            newParts.add(new TextComponentString(text.substring(index, text.length())));
        }

        if (!newParts.isEmpty())
        {
            TextComponentString replacement = new TextComponentString("");
            for (ITextComponent sib : newParts)
            {
                replacement.appendSibling(sib);
            }
            return replacement;
        }

        return null;
    }
}
