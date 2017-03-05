/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.waypoint;

import journeymap.client.model.Waypoint;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Chat waypoints minimally need [x:#, z:#].
 */
public class WaypointParserTest
{
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

    static List<String> NAMES = Arrays.asList(
            "[name:'Death Point 12:12:12',x:1,y:1,z:0]",
            "[name:'Death Point \" 12:12:12',x:1,y:1,z:0]",
            "[name:\"Death Point\",x:1,y:1,z:0]",
            "[name:\"Death Point 12:12:12\",x:1,y:1,z:0]",
            "[name:\"Death Point's Taken 12:12:12\",x:1,y:1,z:0]",
            "[name:Death Point, x:1, z:0]",
            "[x:1,name:Death Point,z:0]",
            "[x:1,y:1,z:0,name:Death Point]",
            "[name:Death Point,x:1,y:1,z:0]",
            "[name:'Death Point',x:1,y:1,z:0]"
    );



    @Test
    public void testSingleMatch() throws Exception
    {
        for (String chat : ONE_MATCH)
        {
            List<Waypoint> wp = WaypointParser.getWaypoints(chat);
            Assert.assertTrue("Should find waypoint in: " + chat, wp != null && wp.size() == 1);
        }
    }

    @Test
    public void testDoubleMatch() throws Exception
    {
        for (String chat : TWO_MATCH)
        {
            List<Waypoint> wp = WaypointParser.getWaypoints(chat);
            Assert.assertTrue("Should find 2nd waypoint in: " + chat, wp != null && wp.size() == 2);
        }
    }

    @Test
    public void testNoDoubleMatch() throws Exception
    {
        for (String chat : NOT_TWO_MATCH)
        {
            List<Waypoint> wp = WaypointParser.getWaypoints(chat);
            Assert.assertNotNull("Should find 1st waypoint in: " + chat, wp);
            Assert.assertEquals("Should not find 2nd waypoint in: " + chat, 1, wp.size());
        }
    }

    @Test
    public void testNoMatch() throws Exception
    {
        for (String chat : NO_MATCH)
        {
            List<Waypoint> wp = WaypointParser.getWaypoints(chat);
            Assert.assertTrue("Should not find any waypoint in: " + chat, wp == null);
        }
    }

    @Test
    public void testNames() throws Exception
    {
        for (String named : NAMES)
        {
            Assert.assertTrue("Should match pattern as valid waypoint:" + named, WaypointParser.PATTERN.matcher(named).find());

            List<Waypoint> wps = WaypointParser.getWaypoints(named);
            Assert.assertTrue("Should find waypoint in: " + named, wps != null && wps.size() == 1);
            for (Waypoint wp : wps)
            {
                String msg = String.format("Waypoint from %s should have 'Death Point' in name, but was: %s", named, wp.getName());
                Assert.assertTrue(msg, wp.getName().contains("Death Point"));

                Assert.assertNotNull("Should be able to export to chat: " + wp, WaypointParser.parse(wp.toChatString()));
            }
        }
    }
}