package journeymap.client.waypoint;

import journeymap.client.model.Waypoint;
import journeymap.common.Journeymap;
import net.minecraft.util.math.BlockPos;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Chat waypoints minimally need [x:#, z:#].
 */
public class WaypointParserTest
{
    Pattern pattern = WaypointParser.PATTERN;

    static List<String> ONE_MATCH = Arrays.asList(
            "I want to be at [x:100, z:200] soon.",
            "Me here [X: 100,   z:-2]",
            "? [x: 100, y:10, z:-2]",
            "ok [not] but [x: -100, y:-10, z:-2]"
    );

    static List<String> TWO_MATCH = Arrays.asList(
            "I want to be at [x:100, z:200] soon. Me here [x: 100,   Z:-2]",
            "Me here [x: 100,   z:-2] ? [x: 100, y:10,z:-2]",
            "? [x: 100, y:10,  z:-2] ok [not] but [x: -100,y:-10, z:-2]",
            "ok [not] but [X: -100,Y:-10, z:-2] I want to be at [x:100,z:200]"
    );

    static List<String> NOT_TWO_MATCH = Arrays.asList(
            "I want to be at [x:100,z:200] soon. Me here x: 100,   z:-2]",
            "Me here [x: 100,   z:-2] ? [x: 100, y:10  z:-2",
            "? [x: 100, y:10,  z:-2] ok [not] but [x -100y:-10  z:-2]",
            "ok [not] but [x: -100, y:-10,  z:-2] I want to be at [x:100z:200]"
    );

    static List<String> NO_MATCH = Arrays.asList(
            "I want to be at {x:100  z:200] soon.",
            "Me here [X: 100,]   z:-2]",
            "? (x: 100, y:10  z:-2]",
            "ok [not] but x: -100 y:-10  z:-2]"
    );

    private List<Waypoint> getWaypoints(String line)
    {
        List<Waypoint> list = null;
        Matcher matcher = pattern.matcher(line);
        while (matcher.find())
        {
            String raw = matcher.group().replaceAll("[\\[\\]]", "");
            Properties properties = new Properties();
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
                                y = Integer.parseInt(val);
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
                if (name == null)
                {
                    name = String.format("%s,%s", x, z);
                }
                Random r = new Random();
                Waypoint waypoint = new Waypoint(name, new BlockPos(x, y, z), new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)), Waypoint.Type.Normal, dim);
                if (list == null)
                {
                    list = new ArrayList<Waypoint>(3);
                }
                list.add(waypoint);
                System.out.println(waypoint);
            }
        }

        return list;
    }

    @Ignore
    public void temp3dhashtest()
    {

        // Constants: Horizontal (x,z) and Vertical (y) ranges of values < 0
        final int H = 2000;

        int total = H * H;

        int bucketSize = (int) Math.ceil(total / 100D);
        HashSet<Integer> set = new HashSet<Integer>(total);
        List<Integer> list = new ArrayList<Integer>(bucketSize);

        int count = 0;

        for (int x = -H; x <= H; x++)
        {

            for (int z = -H; z <= H; z++)
            {
                count++;

                list.add(spatialHash(H, x, z));

                if (count % (total / 100) == 0)
                {
                    System.out.println(String.format("%s pct complete of %s", (count * 100D) / total, total));
                    set.addAll(list);
                    list.clear();

                    int unique = set.size();
                    int collisions = count - unique;
                    System.out.println(String.format("%s collisions, %s unique (%s:%s)", collisions, unique, collisions * 1D / unique, 1));
                }
            }

        }
    }

    public int spatialHash(final int H, final int V, int x, int y, int z)
    {
        // Make coords positive with different prime bases
        int xh = x + H + 31;
        int yh = y + V + 37;
        int zh = z + H + 41;

        // Not pretty, but best I've found yet
        return yh + ((yh * 43) * (xh + (xh * 47)) * zh) + (zh * 53);
    }

    public int spatialHash(final int H, int x, int z)
    {
        // Make coords positive with different prime bases
        int xh = x + H + 31;
        int zh = z + H + 37;

        // Not pretty, but best I've found yet
        return ((xh + (xh * 41)) * zh) + (zh * 43);
    }

    @Test
    public void testSingleMatch() throws Exception
    {
        for (String chat : ONE_MATCH)
        {
            List<Waypoint> wp = getWaypoints(chat);
            Assert.assertTrue("Should find waypoint in: " + chat, wp != null && wp.size() == 1);
        }
    }

    @Test
    public void testDoubleMatch() throws Exception
    {
        for (String chat : TWO_MATCH)
        {
            List<Waypoint> wp = getWaypoints(chat);
            Assert.assertTrue("Should find 2nd waypoint in: " + chat, wp != null && wp.size() == 2);
        }
    }

    @Test
    public void testNoDoubleMatch() throws Exception
    {
        for (String chat : NOT_TWO_MATCH)
        {
            List<Waypoint> wp = getWaypoints(chat);
            Assert.assertNotNull("Should find 1st waypoint in: " + chat, wp);
            Assert.assertEquals("Should not find 2nd waypoint in: " + chat, 1, wp.size());
        }
    }

    @Test
    public void testNoMatch() throws Exception
    {
        for (String chat : NO_MATCH)
        {
            List<Waypoint> wp = getWaypoints(chat);
            Assert.assertTrue("Should not find any waypoint in: " + chat, wp == null);
        }
    }
}