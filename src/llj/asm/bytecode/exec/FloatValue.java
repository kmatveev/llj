package llj.asm.bytecode.exec;

import llj.asm.bytecode.TypeType;

public class FloatValue extends Value {

    public final float val;

    public FloatValue(float val) {
        this.val = val;
    }

    @Override
    public TypeType getType() {
        return TypeType.FLOAT;
    }

    @Override
    public int getSize() {
        return SIZE_SINGLE;
    }

    @Override
    public int getFirstWord() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSecondWord() {
        throw new UnsupportedOperationException();
    }
}
