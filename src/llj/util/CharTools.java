package llj.util;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CharTools {

    public static String readUntil(CharBuffer buffer, char endMarker, boolean include) {
        int p = buffer.position();
        while (buffer.get() != endMarker);
        int np = include ? buffer.position() : buffer.position() - 1;
        int l = buffer.limit();
        buffer.position(p);
        buffer.limit(np);
        String result = buffer.toString();
        buffer.limit(l);
        buffer.position(np);
        return result;
    }
    
    public static String bytesToAscii(byte[] bytes) {
        return new String(bytes, StandardCharsets.US_ASCII);
    }
}
