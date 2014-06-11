package llj.asm.bytecode.exec;

import llj.asm.bytecode.ArrayElemType;
import llj.asm.bytecode.ArrayRefType;
import llj.asm.bytecode.ClassData;
import llj.asm.bytecode.ClassIntrinsics;
import llj.asm.bytecode.ClassReference;
import llj.asm.bytecode.ConstantData;
import llj.asm.bytecode.FieldData;
import llj.asm.bytecode.FieldReference;
import llj.asm.bytecode.MethodData;
import llj.asm.bytecode.MethodReference;
import llj.asm.bytecode.RefType;
import llj.asm.bytecode.ScalarType;
import llj.asm.bytecode.TypeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassRuntimeData {

    public final ClassData classData;
    public final int objectDataSize;
    public final int selfRef;
    public int parentRef;
    public final List<Integer> interfacesIndexes = new ArrayList<Integer>();

    private final Map<FieldData, FieldRuntimeData> fields = new HashMap<FieldData, FieldRuntimeData>();
    private final Map<MethodData, MethodRuntimeData> methods = new HashMap<MethodData, MethodRuntimeData>();
    private final Map<ConstantData, Value> constants = new HashMap<ConstantData, Value>();

    public ClassRuntimeData(int selfRef, int parentRef, ClassData classData) {
        this.classData = classData;
        this.parentRef = parentRef;
        this.selfRef = selfRef;
        int offset = 0;
        for (FieldData field : classData.fields) {
            int size = Value.getSizeFor(field.type.type);
            FieldRuntimeData runtimeData = new FieldRuntimeData(field, offset);
            this.fields.put(field, runtimeData);
            offset += size;
        }
        for (MethodData method : classData.methods) {
            MethodRuntimeData runtimeMethodData = new MethodRuntimeData(this, method);
            methods.put(method, runtimeMethodData);
        }
        objectDataSize = offset;
    }

    public MethodRuntimeData getRuntimeData(MethodData methodData) {
        return methods.get(methodData);
    }

    public MethodRuntimeData getRuntimeData(MethodReference ref) throws NoSuchMethod {
        if (ref.isLinked()) {
            return getRuntimeData(ref.follow());
        } else {
            for (Map.Entry<MethodData, MethodRuntimeData> methodEntry : methods.entrySet()) {
                if (methodEntry.getKey().matches(ref.methodName, ref.paramTypes)) {
                    ref.linkWith(methodEntry.getKey());
                    return methodEntry.getValue();
                }
            }
            throw new NoSuchMethod(ref.toString());
        }
    }

    public FieldRuntimeData getRuntimeData(FieldData fieldData, ClassHeap classHeap) {
        if (fieldData.classData == this.classData) {
            return fields.get(fieldData);
        } else {
            if (parentRef >= 0) {
                ClassRuntimeData parent = classHeap.get(parentRef);
                return parent.getRuntimeData(fieldData, classHeap);
            } else {
                throw new RuntimeException();
            }
        }
    }

    public FieldRuntimeData getRuntimeData(FieldReference fieldRef, ClassHeap classHeap) throws NoSuchField {
        if (fieldRef.isLinked()) {
            return getRuntimeData(fieldRef.follow(), classHeap);
        } else {
            for (Map.Entry<FieldData, FieldRuntimeData> fieldEntry : fields.entrySet()) {
                FieldData fieldData = fieldEntry.getKey();
                if (fieldData.matches(fieldRef.fieldName)) {
                    fieldRef.linkWith(fieldData);
                    return fieldEntry.getValue();
                }
            }
            // TODO verify this algorithm against spec
            // check field in parent classes
            if (parentRef >= 0) {
                ClassRuntimeData parent = classHeap.get(parentRef);
                return parent.getRuntimeData(fieldRef, classHeap);
            } else {
                throw new NoSuchField(fieldRef.toString());
            }
        }
    }


    public static ClassRuntimeData readClassRef(ClassHeap classHeap, Heap heap, Heap.Pointer actualRef) throws MemoryAccessError {
        ObjHeader objHeader = ObjHeader.loadFrom(heap, actualRef);
        ClassRuntimeData runtimeData = classHeap.get(objHeader.classIndex);
        if (ClassIntrinsics.isArrayClassData(runtimeData.classData)) {
            Arrays.ArrayHeader arrayHeader = Arrays.loadFrom(classHeap, heap, actualRef);
            return runtimeData;
        } else {
            return runtimeData;
        }
    }

    public Heap.Pointer allocateInstance(Heap heap) throws OutOfMemory {
        ObjHeader header = new ObjHeader(selfRef);
        Heap.Pointer pointer = heap.allocate(header.size() + objectDataSize);
        try {
            header.storeAt(heap, pointer);
            return pointer;
        } catch (MemoryAccessError e) {
            throw new RuntimeException(e);
        }
    }

    public Value getOrCreateFor(ConstantData constantData, VM vm) throws OutOfMemory, ClassLoadingTrouble, NoSuchField, MemoryAccessError, ArrayIndexOutOfBounds {
        Value cached = constants.get(constantData);
        if (cached != null) {
            return cached;
        } else {
            Value created = convertConst(constantData, vm);
            constants.put(constantData, created);
            return created;
        }
    }

    private Value convertConst(ConstantData constantData, VM vm) throws OutOfMemory, ClassLoadingTrouble, NoSuchField, MemoryAccessError, ArrayIndexOutOfBounds {
        if ((constantData.type.type == TypeType.REF) ) {
            RefType refToConstValue = (RefType) constantData.type;
            if (refToConstValue.classRef.equals(ClassIntrinsics.STRING_CLASS_REF)) {
                String val = (String)constantData.value;
                TypeType charType = ArrayElemType.CHAR.type;
                ClassData arrayClassData = ClassIntrinsics.getArrayClassData(charType);
                ClassRuntimeData arrayClassRuntimeData = vm.classHeap.getOrCreate(arrayClassData);
                Heap.Pointer charArrayPointer = Arrays.allocateScalarArray(arrayClassRuntimeData, charType, val.length(), vm.heap);
                // TODO make this a bulk operation
                for (int i = 0; i < val.length(); i++) {
                    Arrays.setArrayElement(vm.classHeap, vm.heap, charArrayPointer, i, new CharValue(val.charAt(i)));
                }
                // TODO maybe add ad-hoc boolean flag like 'stringClassLoaded' to avoid loading attempts
                vm.loadClass(ClassIntrinsics.STRING_CLASS_REF);
                ClassRuntimeData stringClass = vm.classHeap.get(ClassIntrinsics.STRING_CLASS_REF);
                Heap.Pointer stringPointer = stringClass.allocateInstance(vm.heap);
                FieldReference charArrayFieldRef = new FieldReference(ClassIntrinsics.STRING_CLASS_REF, "value", ArrayRefType.arrayOf(ScalarType.scalar(TypeType.CHAR)));
                FieldRuntimeData charArrayField = stringClass.getRuntimeData(charArrayFieldRef, vm.classHeap);
                charArrayField.set(vm.heap, stringPointer, charArrayPointer);
                return stringPointer;
            } else if (refToConstValue.classRef.equals(ClassIntrinsics.CLASS_CLASS_REF)) {
                vm.loadClass(ClassIntrinsics.CLASS_CLASS_REF);
                ClassRuntimeData classClass = vm.classHeap.get(ClassIntrinsics.CLASS_CLASS_REF);
                Heap.Pointer classPointer = classClass.allocateInstance(vm.heap);
                ClassReference specificClassRef = (ClassReference)constantData.value;
                ClassRuntimeData specificClass = vm.loadClass(specificClassRef);
                // TODO bind classClass to classRuntimeData
                return classPointer;
            } else {
                throw new RuntimeException();
            }
        } else if (constantData.type.type == TypeType.INT) {
            return new IntegerValue(((Integer)constantData.value).intValue());
        } else if (constantData.type.type == TypeType.LONG) {
            // TODO correct value
            return new LongValue(((Long)constantData.value).longValue());
        } else if (constantData.type.type == TypeType.FLOAT) {
            // TODO correct value
            return new FloatValue(((Float)constantData.value).floatValue());
        } else if (constantData.type.type == TypeType.DOUBLE) {
            // TODO correct value
            return new DoubleValue(((Double)constantData.value).doubleValue());
        } else {
            throw new RuntimeException();
        }

    }


    public static class ObjHeader {

        public static final int HEADER_SIZE = 4;

        public final int classIndex;

        public ObjHeader(int classIndex) {
            this.classIndex = classIndex;
        }

        public int size() {
            return HEADER_SIZE;
        }

        public Heap.Pointer storeAt(Heap heap, Heap.Pointer pointer) throws MemoryAccessError {
            pointer.writeWord(heap, classIndex);
            return pointer.moveUp(heap, Value.SIZE_SINGLE);
        }

        public static ObjHeader loadFrom(Heap heap, Heap.Pointer pointer) throws MemoryAccessError {
            int classIndex = pointer.readWord(heap);
            return new ClassRuntimeData.ObjHeader(classIndex);
        }

    }


}
