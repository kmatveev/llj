package llj.asm.bytecode.exec;

public class TestLongValue {

    public static void main(String[] args) {
        System.out.println("TestLongValue.testPackUnpack():" + testPackUnpack());
    }

    public static boolean testPackUnpack() {
        for (long i = Long.MIN_VALUE; i <= Long.MAX_VALUE; i += 333) {
            if (!testPackUnpack(new LongValue(i))) return false;
        }
        return true;
    }

    public static boolean testPackUnpack(LongValue v) {
        OpaqueDoubleSizeValue opaque = new OpaqueDoubleSizeValue(v.getFirstWord(), v.getSecondWord());
        LongValue v2 = LongValue.load(opaque);
        return v2.val == v.val;
    }

}
