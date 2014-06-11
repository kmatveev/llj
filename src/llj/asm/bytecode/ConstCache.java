package llj.asm.bytecode;

import java.util.TreeMap;

public class ConstCache {

    private final TreeMap<String, ConstantData> stringConstCache = new TreeMap<String, ConstantData>();

    public ConstantData intern(ConstantData constant) {
        switch (constant.type.type) {
            case REF: return constant;
            default : return constant;
        }
    }

    public ConstantData internString(ConstantData constant) {
        if (constant.type.type != TypeType.REF || !(constant.value instanceof String)) throw new IllegalArgumentException("String constant expected");
        String val = (String)constant.value;
        ConstantData existing = stringConstCache.get(val);
        if (existing != null) {
            return existing;
        } else {
            stringConstCache.put(val, constant);
            return constant;
        }
    }
}
