package llj.asm.bytecode.exec;

import llj.asm.bytecode.TypeType;

public class DoubleValue extends Value {

    public final double val;

    public DoubleValue(double val) {
        this.val = val;
    }

    @Override
    public TypeType getType() {
        return TypeType.DOUBLE;
    }

    @Override
    public int getSize() {
        return SIZE_DOUBLE;
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
