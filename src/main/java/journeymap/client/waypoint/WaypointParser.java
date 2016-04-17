package journeymap.client.waypoint;

import journeymap.client.model.Waypoint;
import journeymap.common.Journeymap;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses text into a waypoint if possible. Pattern expected is comma-delimited key:value pairs, surrounded by square brackets.
 * Minimally a waypoint needs x and z to be useful. eg [x:23, y:35]
 */
public class WaypointParser
{
    public static Pattern PATTERN = Pattern.compile("\\[(\\w+\\s*:\\s*-?[\\w\\d\\s]+,\\s*)+(\\w+\\s*:\\s*-?[\\w\\d\\s]+)\\]", Pattern.CASE_INSENSITIVE);

    /**
     * Returns the substrings of line which can become waypoints.
     *
     * @param line text
     * @return null if none found.
     */
    public static List<String> getWaypointStrings(String line)
    {
        List<String> list = null;
        if (line.contains("["))
        {
            Matcher matcher = PATTERN.matcher(line);
            while (matcher.find())
            {
                String original = matcher.group();
                if (parse(original) != null)
                {
                    if (list == null)
                    {
                        list = new ArrayList<String>(1);
                    }
                    list.add(original);
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
    public static Waypoint parse(String original)
    {
        String raw = original.replaceAll("[\\[\\]]", "");
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
                            name = val.replaceAll("\"", "");
                            // remove matched singlequotes. leave single ones
                            // assuming they're used as apostrophes
                            if (name.indexOf("'") != name.lastIndexOf("'"))
                            {
                                name = name.replaceAll("'", "");
                            }
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
            if (event.message instanceof ChatComponentTranslation)
            {
                Object[] formatArgs = ((ChatComponentTranslation) event.message).getFormatArgs();
                for (int i = 0; i < formatArgs.length; i++)
                {
                    if (matches.isEmpty())
                    {
                        break;
                    }

                    if (formatArgs[i] instanceof IChatComponent)
                    {
                        IChatComponent arg = (IChatComponent) formatArgs[i];
                        IChatComponent result = addWaypointMarkup(arg.getUnformattedText(), matches);
                        if (result != null)
                        {
                            formatArgs[i] = result;
                            changed = true;
                        }
                    }
                    else if (formatArgs[i] instanceof String)
                    {
                        String arg = (String) formatArgs[i];
                        IChatComponent result = addWaypointMarkup(arg, matches);
                        if (result != null)
                        {
                            formatArgs[i] = result;
                            changed = true;
                        }
                    }
                }

                if (changed)
                {
                    event.message = new ChatComponentTranslation(((ChatComponentTranslation) event.message).getKey(), formatArgs);
                }
            }
            else if (event.message instanceof ChatComponentText)
            {
                IChatComponent result = addWaypointMarkup(event.message.getUnformattedText(), matches);
                if (result != null)
                {
                    event.message = result;
                    changed = true;
                }
            }
            else
            {
                Journeymap.getLogger().warn("No implementation for handling waypoints in IChatComponent: " + event.message.getClass());
            }

            if (!changed)
            {
                Journeymap.getLogger().warn(String.format("Matched waypoint in chat but failed to update message for %s : %s\n%s",
                        event.message.getClass(),
                        event.message.getFormattedText(),
                        IChatComponent.Serializer.componentToJson(event.message)));
            }
        }
    }

    private static IChatComponent addWaypointMarkup(String text, List<String> matches)
    {
        List<IChatComponent> newParts = new ArrayList<IChatComponent>();

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
                    newParts.add(new ChatComponentText(text.substring(index, start)));
                }

                matched = true;

                ChatComponentText clickable = new ChatComponentText(match);
                ChatStyle chatStyle = clickable.getChatStyle();
                chatStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/jm wpedit " + match));

                // TODO i18n
                ChatComponentText hover = new ChatComponentText("JourneyMap: ");
                hover.getChatStyle().setColor(EnumChatFormatting.YELLOW);

                // TODO i18n
                ChatComponentText hover2 = new ChatComponentText("Click to create Waypoint.\nCtrl+Click to view on map.");
                hover2.getChatStyle().setColor(EnumChatFormatting.AQUA);
                hover.appendSibling(hover2);

                chatStyle.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
                chatStyle.setColor(EnumChatFormatting.AQUA);

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
            newParts.add(new ChatComponentText(text.substring(index, text.length())));
        }

        if (!newParts.isEmpty())
        {
            ChatComponentText replacement = new ChatComponentText("");
            for (IChatComponent sib : newParts)
            {
                replacement.appendSibling(sib);
            }
            return replacement;
        }

        return null;
    }
}
