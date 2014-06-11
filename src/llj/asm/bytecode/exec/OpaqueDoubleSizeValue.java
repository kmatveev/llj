package llj.asm.bytecode.exec;

import llj.asm.bytecode.TypeType;

public class OpaqueDoubleSizeValue extends Value {

    public final int firstWord, secondWord;

    public OpaqueDoubleSizeValue(int firstWord, int secondWord) {
        this.firstWord = firstWord;
        this.secondWord = secondWord;
    }

    @Override
    public TypeType getType() {
        throw new UnsupportedOperationException("Opaque");
    }

    public int getSize() {
        return SIZE_DOUBLE;
    }

    @Override
    public int getFirstWord() {
        return firstWord;
    }

    @Override
    public int getSecondWord() {
        return secondWord;
    }

}
