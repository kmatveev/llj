package llj.util;

public class HexTools {

    public static String hexBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(Integer.toString(b, 16).toUpperCase()).append(" ");
        }
        return sb.toString();
    }


}
