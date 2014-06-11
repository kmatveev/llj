package llj.asm.bytecode.exec;

import llj.asm.bytecode.TypeType;

public class CharValue extends Value {

    public final char value;

    public CharValue(char value) {
        this.value = value;
    }

    @Override
    public TypeType getType() {
        return TypeType.CHAR;
    }

    @Override
    public int getSize() {
        return SIZE_SINGLE;
    }

    @Override
    public int getFirstWord() {
        return value;
    }

    @Override
    public int getSecondWord() {
        return 0;
    }

//    public static CharValue load(OpaqueSingleSizeValue opaque) {
//        return new CharValue(opaque.getFirstWord());
//    }

}
