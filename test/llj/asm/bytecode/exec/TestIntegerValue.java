package llj.asm.bytecode.exec;

public class TestIntegerValue {

    public static void main(String[] args) {
        System.out.println("TestIntegerValue.testPackUnpack():" + testPackUnpack());
    }

    public static boolean testPackUnpack() {
        for (int i = Integer.MIN_VALUE; i <= Integer.MAX_VALUE; i += 333) {
            if (!testPackUnpack(new IntegerValue(i))) return false;
        }
        return true;
    }

    public static boolean testPackUnpack(IntegerValue v) {
        OpaqueSingleSizeValue opaque = new OpaqueSingleSizeValue(v.getFirstWord());
        IntegerValue v2 = IntegerValue.load(opaque);
        return v2.val == v.val;
    }
}
