package llj.util;

public class HexTools {

    public static String hexBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append("0x").append(Integer.toHexString(b & 0xFF).toUpperCase()).append(" ");
        }
        return sb.toString();
    }


}
