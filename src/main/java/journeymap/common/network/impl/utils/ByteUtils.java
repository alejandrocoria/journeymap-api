package journeymap.common.network.impl.utils;

/**
 * Simple utility for serializing and deserializing bytes so they can be used with the message system and stored in a JsonObject as a string.
 * This class not NOT handle {@link Exception} of any kind!
 */
public interface ByteUtils
{

    /**
     * Serializes a byte array to a string that can be stored in a Json Message.
     *
     * @param bytes - The bytes to be serialized.
     * @return - The string representation of the byte array.
     */
    static String serialize(byte[] bytes)
    {
        return serialize(bytes, ",");
    }

    /**
     * Serializes a byte array to a string that can be stored in a Json Message with a custom delimiter.
     *
     * @param bytes     - The bytes to be serialized.
     * @param delimiter - The custom delimiter.
     * @return - The string representation of the byte array.
     */
    static String serialize(byte[] bytes, String delimiter)
    {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
        {
            sb.append(b & 0xFF).append(delimiter);
        }
        sb.replace(sb.length() - 1, sb.length(), "");
        return sb.toString();
    }

    /**
     * Default deserialize method for getting the bytes from a serialzied byte array stored in the Json Message.
     *
     * @param string - The String of bytes.
     * @return - The deserialized byte array.
     */
    static byte[] deserialize(String string)
    {
        return deserialize(string, ",");
    }

    /**
     * Deserialize method for getting the bytes from a serialzied byte array stored in the Json Message with a custom delimiter.
     *
     * @param string    - The String of bytes.
     * @param delimiter - The custom delimiter.
     * @return - The deserialized byte array.
     */
    static byte[] deserialize(String string, String delimiter)
    {
        String[] byteArray = string.split(delimiter);
        byte[] bytes = new byte[byteArray.length];
        for (int i = 0; i < byteArray.length; i++)
        {
            bytes[i] = Byte.parseByte(byteArray[i]);
        }
        return bytes;
    }

}
