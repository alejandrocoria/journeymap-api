package journeymap.server.legacyserver.util;

import journeymap.common.Journeymap;

import java.io.*;

/**
 * Created by Mysticdrew on 10/9/2014.
 */
public class FileManager
{

    public static String readFile(File file)
    {
        BufferedReader bReader;
        FileInputStream fileIn;
        //File file = new File(path, fileName);
        String row = "";
        StringBuilder fileOutput = new StringBuilder();
        try
        {
            fileIn = new FileInputStream(file);
            bReader = new BufferedReader(new InputStreamReader(fileIn));
            try
            {
                while ((row = bReader.readLine()) != null)
                {
                    fileOutput.append(row);
                }
                bReader.close();
            }
            catch (IOException e)
            {
                Journeymap.getLogger().error("Unable to read the JsonFile");
                Journeymap.getLogger().error("Error" + e);
                return null;
            }

            return fileOutput.toString();
        }
        catch (FileNotFoundException e)
        {
            return null;
        }
    }

}
