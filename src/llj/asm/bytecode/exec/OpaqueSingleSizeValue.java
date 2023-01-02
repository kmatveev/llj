package llj.asm.bytecode.exec;

import llj.asm.bytecode.TypeType;

public final class OpaqueSingleSizeValue extends Value {

    public int word;

    public OpaqueSingleSizeValue(int word) {
        this.word = word;
    }

    @Override
    public TypeType getType() {
        throw new UnsupportedOperationException("Opaque");
    }

    public int getSize() {
        return SIZE_SINGLE;
    }

    @Override
    public int getFirstWord() {
        return word;
    }

    @Override
    public int getSecondWord() {
        throw new UnsupportedOperationException();
    }
}
