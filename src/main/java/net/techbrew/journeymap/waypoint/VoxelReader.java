package net.techbrew.journeymap.waypoint;

import cpw.mods.fml.client.FMLClientHandler;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.WorldData;
import net.techbrew.journeymap.log.ChatLog;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Read VoxelMap's waypoint files and return a collection of the results.
 */
public class VoxelReader
{
    // 1.7.2
    public static final String VOXEL_JAR_NAME = "voxelmap-1.7.2-1.0 (June 9, 2014)";
    public static final String VOXEL_JAR_URL = "http://www.mediafire.com/view/8tjjxvghv3jpv18/voxelmap-1.7.2-1.0";

    public static final String[] classNames = {"com.thevoxelbox.voxelmap.VoxelMap", "com.thevoxelbox.voxelmap.util.Waypoint"};
    public static Boolean modLoaded;

    int pointErrors = 0;
    int fileErrors = 0;

    /**
     * Get waypoints from VoxelMap instance.
     *
     * @return
     */
    public static java.util.List<Waypoint> loadWaypoints()
    {
        java.util.List voxelWaypoints = new ArrayList(0);

        try
        {
            voxelWaypoints.addAll(com.thevoxelbox.voxelmap.VoxelMap.getInstance().getWaypointManager().getWaypoints());
            modLoaded = true;
        } catch (Throwable e)
        {
            JourneyMap.getLogger().warning("Incompatible version of VoxelMap. Tried com.thevoxelbox.voxelmap.VoxelMap.instance.waypointManager.wayPts: " + e);
            ChatLog.announceI18N("jm.waypoint.import_vox_version");
            ChatLog.announceURL(VOXEL_JAR_NAME, VOXEL_JAR_URL);
            modLoaded = false;
        }

        if (voxelWaypoints.isEmpty())
        {
            return Collections.EMPTY_LIST;
        }

        try
        {
            ArrayList<Waypoint> converted = new ArrayList<Waypoint>(voxelWaypoints.size());
            for (Object wpObj : voxelWaypoints)
            {
                com.thevoxelbox.voxelmap.util.Waypoint voxWp = (com.thevoxelbox.voxelmap.util.Waypoint) wpObj;

                String name = voxWp.name.replaceAll("~comma~",",");
                int x = voxWp.x;
                int y = voxWp.y;
                int z = voxWp.z;
                boolean enabled = voxWp.enabled;
                int r = (int) (voxWp.red * 255.0F) & 255;
                int g = (int) (voxWp.green * 255.0F) & 255;
                int b =  (int) (voxWp.blue * 255.0F) & 255;
                Waypoint.Type type = ("skull".equals(voxWp.imageSuffix)) ? Waypoint.Type.Death : Waypoint.Type.Normal;

                Waypoint jmWp = new net.techbrew.journeymap.model.Waypoint(name,x,y,z,enabled,r,g,b,type,
                        Waypoint.Origin.VoxelMap,0, voxWp.dimensions);

                jmWp.setReadOnly(true);

                converted.add(jmWp);
            }
            return converted;

        }
        catch (Throwable e)
        {
            JourneyMap.getLogger().severe("Exception getting VoxelMap waypoints: " + LogFormatter.toString(e));
            modLoaded = false;
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Get waypoints from VoxelMap .points files
     *
     * @param waypointDir
     * @param deleteOnSuccess
     * @return
     */
    public Collection<Waypoint> loadWaypoints(File waypointDir, boolean deleteOnSuccess)
    {
        ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();

        File[] files = waypointDir.listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return !name.contains(".DIM") && name.endsWith(".points");
            }
        });

        if(files.length==0)
        {
            return waypoints;
        }

        for (File pointsFile : files)
        {
            try
            {
                int pointErrorCount = pointErrors;
                loadWaypoints(pointsFile, waypoints);

                if(deleteOnSuccess && pointErrorCount==pointErrors)
                {
                    pointsFile.deleteOnExit();
                    pointsFile.delete();
                }
            }
            catch (Exception e)
            {
                ChatLog.announceError(Constants.getString("jm.waypoint.import_vox_file_error", pointsFile.getName()));
                JourneyMap.getLogger().severe(LogFormatter.toString(e));
                fileErrors++;
            }
        }

        if(waypoints.isEmpty())
        {
            ChatLog.announceI18N("jm.waypoint.import_vox_failure");
        }
        else if(fileErrors == 0 && pointErrors==0)
        {
            ChatLog.announceI18N("jm.waypoint.import_vox_success", waypoints.size());
        }
        else
        {
            ChatLog.announceI18N("jm.waypoint.import_vox_errors", waypoints.size(), pointErrors);
        }

        return waypoints;
    }

    public static String getPointsFilename()
    {
        String worldName = WorldData.getWorldName(FMLClientHandler.instance().getClient());
        return String.format("mods\\VoxelMods\\voxelMap\\%s.points", worldName);
    }

    /**
     * Load waypoints for the given dimension from the VoxelMap pointsFile into the provided arraylist.
     * @param pointsFile
     * @param waypoints
     * @throws Exception
     */
    private void loadWaypoints(File pointsFile, ArrayList<Waypoint> waypoints) throws Exception
    {
        BufferedReader br = new BufferedReader(new FileReader(pointsFile));
        String line;
        while ((line = br.readLine()) != null)
        {
            Waypoint waypoint = loadWaypoint(line);
            if(waypoint!=null)
            {
                waypoints.add(waypoint);
            }
        }
        br.close();
    }

    /**
     * Create a waypoint from a VoxelMap line in a .points file.
     * @param line
     * @return
     */
    private Waypoint loadWaypoint(String line)
    {
        // Example:
        /*
        filetimestamp:201404221629
        name:Spawn,x:-260,z:255,y:71,enabled:true,red:0.105882354,green:0.6901961,blue:0.50980395,suffix:,world:,dimensions:0#
        name:My Nether One,x:-312,z:264,y:60,enabled:true,red:0.6975702,green:0.63540405,blue:0.017592847,suffix:,world:,dimensions:-1#0#
        name:Fire~comma~ Death~comma~ Dishonor,x:-488,z:424,y:62,enabled:true,red:0.021238565,green:0.77731663,blue:0.10206258,suffix:,world:,dimensions:-1#
         */

        if(!line.startsWith("name"))
        {
            return null;
        }

        try
        {
            HashMap<String,String> map = new HashMap<String, String>(16);
            String[] pairs = line.split("\\,");
            for(String pair : pairs)
            {
                String[] kv = pair.split("\\:");
                if(kv.length==2)
                {
                    map.put(kv[0], kv[1]);
                }
            }

            String name = map.get("name").replaceAll("~comma~",",");
            int x = Integer.parseInt(map.get("x"));
            int y = Integer.parseInt(map.get("y"));
            int z = Integer.parseInt(map.get("z"));
            boolean enable = Boolean.parseBoolean(map.get("enabled"));
            Color color = new Color(Float.parseFloat(map.get("red")), Float.parseFloat(map.get("green")), Float.parseFloat(map.get("blue")));
            Waypoint.Type type = "skull".equals(map.get("suffix")) ? Waypoint.Type.Death : Waypoint.Type.Normal;
            String[] dimStrings = map.get("dimensions").split("\\#");
            ArrayList<Integer> dims = new ArrayList<Integer>(dimStrings.length);
            for(int i=0;i<dimStrings.length;i++)
            {
                dims.add(Integer.parseInt(dimStrings[0]));
            }

            Waypoint waypoint = new Waypoint(name, x, y, z, enable, color.getRed(), color.getGreen(), color.getBlue(), type, Waypoint.Origin.VoxelMap, 0, dims);
            waypoint.setDirty(true);
            return waypoint;
        }
        catch(Exception e)
        {
            JourneyMap.getLogger().warning("Couldn't parse \"" + line + "\" because: " + e.getMessage());
            pointErrors++;
            return null;
        }
    }
}
