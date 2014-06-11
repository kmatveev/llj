package llj.asm.bytecode.exec;

import llj.asm.bytecode.FieldData;

public class FieldRuntimeData {

    public final FieldData fieldData;

    public final int selfOffset;

    public FieldRuntimeData(FieldData fieldData, int offset) {
        this.fieldData = fieldData;
        this.selfOffset = offset;
    }

    public Value get(Heap heap, Heap.Pointer pointer) throws MemoryAccessError {
        int size = Value.getSizeFor(fieldData.type.type);
        pointer = pointer.moveUp(heap, selfOffset + ClassRuntimeData.ObjHeader.HEADER_SIZE);
        if (size == Value.SIZE_SINGLE) {
            return new OpaqueSingleSizeValue(pointer.readWord(heap));
        } else if (size == Value.SIZE_DOUBLE) {
            int firstWord = pointer.readWord(heap);
            pointer = pointer.moveUp(heap, Value.SIZE_SINGLE);
            int secondWord = pointer.readWord(heap);
            return new OpaqueDoubleSizeValue(firstWord, secondWord);
        } else {
            throw new RuntimeException();
        }
    }

    public void set(Heap heap, Heap.Pointer pointer, Value val) throws MemoryAccessError {
        if (val.getSize() != Value.getSizeFor(fieldData.type.type)) throw new IllegalArgumentException("Size mismatch");
        pointer = pointer.moveUp(heap, selfOffset + ClassRuntimeData.ObjHeader.HEADER_SIZE);
        int size = val.getSize();
        if (size == Value.SIZE_SINGLE) {
            pointer.writeWord(heap, val.getFirstWord());
        } else if (size == Value.SIZE_DOUBLE) {
            pointer.writeWord(heap, val.getFirstWord());
            pointer = pointer.moveUp(heap, Value.SIZE_SINGLE);
            pointer.writeWord(heap, val.getSecondWord());
        } else {
            throw new RuntimeException();
        }
    }

}
