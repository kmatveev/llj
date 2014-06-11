package llj.asm.bytecode.exec;

import llj.asm.bytecode.ClassData;
import llj.asm.bytecode.ClassIntrinsics;
import llj.asm.bytecode.MethodData;
import llj.asm.bytecode.TypeType;
import llj.util.BinIOTools;

import java.util.ArrayList;
import java.util.List;

public class Arrays {

    public static Heap.Pointer allocateScalarArray(TypeType elemType, int numElements, ClassHeap classHeap, Heap heap) throws OutOfMemory {
        // TODO create a separate ClassRuntimeIntrinsics for array class runtime data
        ClassData arrayClassData = ClassIntrinsics.getArrayClassData(elemType);
        ClassRuntimeData arrayClassRuntimeData = classHeap.get(arrayClassData);
        return allocateScalarArray(arrayClassRuntimeData, elemType, numElements, heap);
    }

    public static Heap.Pointer allocateScalarArray(ClassRuntimeData arrayClassRuntimeData, TypeType elemType, int numElements, Heap heap) throws OutOfMemory {
        ArrayHeader header = new ArrayHeader(arrayClassRuntimeData.selfRef, numElements);
        int sizeMultiplier = TypeType.size(elemType);
        Heap.Pointer pointer = heap.allocate(header.size() + (numElements * 4 * sizeMultiplier));
        // TODO init array with zeroes
        try {
            header.storeAt(heap, pointer);
        } catch (MemoryAccessError e) {
            throw new RuntimeException("Error accessing memory which was just allocated", e);
        }
        return pointer;
    }

    public static Heap.Pointer allocateRefArray(ClassRuntimeData elementClass, int numElements, ClassHeap classHeap, Heap heap) throws OutOfMemory {
        // TODO create a separate ClassRuntimeIntrinsics for array class runtime data
        ClassData arrayClassData = ClassIntrinsics.getArrayClassData(TypeType.REF);
        ClassRuntimeData arrayClassRuntimeData = classHeap.get(arrayClassData);
        return allocateRefArray(arrayClassRuntimeData, elementClass, numElements, heap);
    }

    public static Heap.Pointer allocateRefArray(ClassRuntimeData arrayClassRuntimeData, ClassRuntimeData elementClass, int numElements, Heap heap) throws OutOfMemory {
        RefArrayHeader header = new RefArrayHeader(arrayClassRuntimeData.selfRef, elementClass.selfRef, numElements);
        Heap.Pointer pointer = heap.allocate(header.size() + (numElements * 4));
        // TODO init array with nulls
        try {
            header.storeAt(heap, pointer);
        } catch (MemoryAccessError e) {
            throw new RuntimeException("Error accessing memory which was just allocated", e);
        }
        return pointer;
    }

    public static ArrayHeader loadFrom(ClassHeap classHeap, Heap heap, Heap.Pointer pointer) throws MemoryAccessError {
        int classIndex = pointer.readWord(heap);
        pointer = pointer.moveUp(heap, BinIOTools.SIZE_INT);
        int numElements = pointer.readWord(heap);
        pointer = pointer.moveUp(heap, BinIOTools.SIZE_INT);
        ClassRuntimeData runtimeData = classHeap.get(classIndex);
        if (runtimeData.classData == ClassIntrinsics.refArrayClassData) {
            return RefArrayHeader.loadFrom(classHeap, heap, pointer);
        } else {
            return new ArrayHeader(classIndex, numElements);
        }
    }

    public static List<MethodData> getArrayMethods() {
        // TODO
        return new ArrayList<MethodData>();
    }

    public static class ArrayHeader extends ClassRuntimeData.ObjHeader {

        private int numElements;

        public ArrayHeader(int classIndex, int numElements) {
            super(classIndex);
            this.numElements = numElements;
        }

        @Override
        public int size() {
            return super.size() + BinIOTools.SIZE_INT;
        }

        @Override
        public Heap.Pointer storeAt(Heap heap, Heap.Pointer pointer) throws MemoryAccessError {
            pointer = super.storeAt(heap, pointer);
            pointer.writeWord(heap, numElements);
            return pointer.moveUp(heap, Value.SIZE_SINGLE);
        }
    }

    public static class RefArrayHeader extends ArrayHeader {

        public final int elementClassIndex;

        public RefArrayHeader(int arrayClassIndex, int elemClassIndex, int numElements) {
            super(arrayClassIndex, numElements);
            this.elementClassIndex = elemClassIndex;
        }

        @Override
        public int size() {
            return super.size() + BinIOTools.SIZE_INT;
        }

        @Override
        public Heap.Pointer storeAt(Heap heap, Heap.Pointer pointer) throws MemoryAccessError {
            pointer = super.storeAt(heap, pointer);
            pointer.writeWord(heap, elementClassIndex);
            return pointer.moveUp(heap, Value.SIZE_SINGLE);
        }

        public static RefArrayHeader loadFrom(ClassHeap classHeap, Heap heap, Heap.Pointer pointer) throws MemoryAccessError {
            int classIndex = pointer.readWord(heap);
            pointer = pointer.moveUp(heap, BinIOTools.SIZE_INT);
            int numElements = pointer.readWord(heap);
            pointer = pointer.moveUp(heap, BinIOTools.SIZE_INT);
            int elementClassIndex = pointer.readWord(heap);
            pointer = pointer.moveUp(heap, BinIOTools.SIZE_INT);
            return new RefArrayHeader(classIndex, elementClassIndex, numElements);
        }
    }

    public static Value getArrayElement(ClassHeap classHeap, Heap heap, Heap.Pointer pointer, TypeType elemType, int index) throws MemoryAccessError, ArrayIndexOutOfBounds {
        Arrays.ArrayHeader arrayHeader = Arrays.loadFrom(classHeap, heap, pointer);
        ClassData arrayClass = classHeap.get(arrayHeader.classIndex).classData;
        if (ClassIntrinsics.arrayElementTypeFor(arrayClass) != elemType) throw new RuntimeException("Array element type mismatch");
        if (index < 0 || index >= arrayHeader.numElements) throw new ArrayIndexOutOfBounds();
        int sizeMultiplier = TypeType.size(elemType);
        Heap.Pointer elementPointer = pointer.moveUp(heap, arrayHeader.size() + (index * 4 * sizeMultiplier));
        if (sizeMultiplier == 1) {
            return new OpaqueSingleSizeValue(elementPointer.readWord(heap));
        } else {
            // read order must be consistent with write order
            int firstWord = elementPointer.readWord(heap);
            elementPointer = elementPointer.moveUp(heap, Value.SIZE_SINGLE);
            int secondWord = elementPointer.readWord(heap);
            return new OpaqueDoubleSizeValue(firstWord, secondWord);
        }
    }

    public static void setArrayElement(ClassHeap classHeap, Heap heap, Heap.Pointer pointer, int index, Value value) throws MemoryAccessError, ArrayIndexOutOfBounds {
        TypeType elemType = value.getType();
        Arrays.ArrayHeader arrayHeader = Arrays.loadFrom(classHeap, heap, pointer);
        ClassData arrayClass = classHeap.get(arrayHeader.classIndex).classData;
        if (ClassIntrinsics.arrayElementTypeFor(arrayClass) != elemType) throw new RuntimeException("Array element type mismatch");
        if (index < 0 || index >= arrayHeader.numElements) throw new ArrayIndexOutOfBounds();
        int sizeMultiplier = TypeType.size(elemType);

        Heap.Pointer elementPointer = pointer.moveUp(heap, arrayHeader.size() + (index * 4 * sizeMultiplier));
        if (sizeMultiplier == 1) {
            elementPointer.writeWord(heap, value.getFirstWord());
        } else {
            // write order must be consistent with read order
            elementPointer.writeWord(heap, value.getFirstWord());
            elementPointer = elementPointer.moveUp(heap, Value.SIZE_SINGLE);
            elementPointer.writeWord(heap, value.getSecondWord());
        }
    }

    public static int getArrayLength(ClassHeap classHeap, Heap heap, Heap.Pointer pointer) throws MemoryAccessError {
        Arrays.ArrayHeader arrayHeader = Arrays.loadFrom(classHeap, heap, pointer);
        return arrayHeader.numElements;
    }



}
