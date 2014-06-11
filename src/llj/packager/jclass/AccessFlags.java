package llj.packager.jclass;

import java.util.EnumSet;
import java.util.Iterator;

public enum AccessFlags {

    PUBLIC      (0x0001),
    PRIVATE     (0x0002),
    PROTECTED   (0x0004),
    STATIC      (0x0008),
    FINAL       (0x0010),
    SYNCHRONIZED(0x0020),
    SUPER       (0x0020),
    VOLATILE    (0x0040),
    TRANSIENT   (0x0080),
    NATIVE      (0x0100),
    INTERFACE   (0x0200),
    ABSTRACT    (0x0400),
    STRICT      (0x0800),
    SYNTHETIC   (0x1000),
    ANNOTATION  (0x2000),
    ENUM        (0x4000),
    BRIDGE      (0x0040),
    VARARGS     (0x0080);
    
    public final int code;

    private AccessFlags(int code) {
        this.code = code;
    }

    public static EnumSet<AccessFlags> maskFrom(EnumSet<AccessFlags> flags, int mask) {
        EnumSet<AccessFlags> result = EnumSet.copyOf(flags);
        for (Iterator<AccessFlags> it = result.iterator(); it.hasNext();) {
            AccessFlags flag = it.next();
            if ((flag.code & mask) == 0) {
                it.remove();
            }
        }
        return result;
    }

    public static int pack(EnumSet<AccessFlags> flags) {
        int result = 0;
        for (AccessFlags flag : flags) {
            result = result | flag.code;
        }
        return result;
    }

}
