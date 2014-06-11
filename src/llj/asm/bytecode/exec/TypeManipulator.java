package llj.asm.bytecode.exec;

import llj.asm.bytecode.Type;
import llj.util.BinTools;

public class TypeManipulator implements BinTools.BinReader<Type>, BinTools.BinWriter<Type> {

    @Override
    public Type get(byte[] source, int offset) {
        return null;
    }

    @Override
    public void set(byte[] source, int offset, Type param) {

    }
}
