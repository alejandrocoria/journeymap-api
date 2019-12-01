package journeymap.common.network.impl.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Compressor
{
    public static String decompress(String zippedBase64Str) throws IOException
    {
        String result = null;

        byte[] bytes = Base64.decodeBase64(zippedBase64Str);
        GZIPInputStream zi = null;
        try
        {
            zi = new GZIPInputStream(new ByteArrayInputStream(bytes));
            result = IOUtils.toString(zi, "UTF-8");
        }
        finally
        {
            IOUtils.closeQuietly(zi);
        }
        return result;
    }

    public static String compress(String str) throws IOException
    {
        ByteArrayOutputStream rstBao = new ByteArrayOutputStream();
        GZIPOutputStream zos = new GZIPOutputStream(rstBao);
        zos.write(str.getBytes());
        IOUtils.closeQuietly(zos);
        byte[] bytes = rstBao.toByteArray();
        return Base64.encodeBase64String(bytes);
    }
}
