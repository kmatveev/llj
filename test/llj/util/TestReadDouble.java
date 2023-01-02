package llj.util;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class TestReadDouble {
    
    public static void main(String[] args) {
        ByteBuffer bb = ByteBuffer.allocate(10);
        double d = 55.39;
        byte[] test1 = new byte[8];
        
        bb.putDouble(d);
        bb.flip();
        bb.get(test1);
        System.out.println(Arrays.toString(test1));

        bb.clear();

        bb.putLong(Double.doubleToLongBits(d));
        bb.flip();
        bb.get(test1);
        System.out.println(Arrays.toString(test1));
        
    }
}
