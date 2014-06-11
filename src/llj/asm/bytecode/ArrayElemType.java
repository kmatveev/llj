package llj.asm.bytecode;

public enum ArrayElemType {

    BOOLEAN(4, TypeType.BOOLEAN), CHAR(5, TypeType.CHAR), FLOAT(6, TypeType.FLOAT), DOUBLE(7, TypeType.DOUBLE), BYTE(8, TypeType.BYTE), SHORT(9, TypeType.SHORT), INT(10, TypeType.INT), LONG(11, TypeType.LONG);

    public final int code;
    public final TypeType type;

    private ArrayElemType(int code, TypeType type) {
        this.code = code;
        this.type = type;
    }

    public static ArrayElemType getByCode(int code) {
        for (ArrayElemType val : values()) {
            if (val.code == code) return val;
        }
        throw new IllegalArgumentException("Provided code is illegal:" + code);
    }

}
