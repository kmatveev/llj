package llj.asm.bytecode.exec;

import llj.asm.bytecode.ClassData;
import llj.asm.bytecode.ClassReference;

import java.util.ArrayList;
import java.util.List;

public class ClassHeap {

    private final List<ClassRuntimeData> classes = new ArrayList<ClassRuntimeData>();

    public ClassHeap() {

//        getOrCreate(ClassIntrinsics.objectClassData, null, this);
//
//        getOrCreate(ClassIntrinsics.boolClassData, null, this);
//        getOrCreate(ClassIntrinsics.byteClassData, null, this);
//        getOrCreate(ClassIntrinsics.charClassData, null, this);
//        getOrCreate(ClassIntrinsics.shortClassData, null, this);
//        getOrCreate(ClassIntrinsics.intClassData, null, this);
//        getOrCreate(ClassIntrinsics.longClassData, null, this);
//        getOrCreate(ClassIntrinsics.floatClassData, null, this);
//        getOrCreate(ClassIntrinsics.doubleClassData, null, this);
//        getOrCreate(ClassIntrinsics.voidClassData, null, this);
//
//        getOrCreate(ClassIntrinsics.boolArrayClassData, null, this);
//        getOrCreate(ClassIntrinsics.byteArrayClassData, null, this);
//        getOrCreate(ClassIntrinsics.charArrayClassData, null, this);
//        getOrCreate(ClassIntrinsics.shortArrayClassData, null, this);
//        getOrCreate(ClassIntrinsics.intArrayClassData, null, this);
//        getOrCreate(ClassIntrinsics.longArrayClassData, null, this);
//        getOrCreate(ClassIntrinsics.floatArrayClassData, null, this);
//        getOrCreate(ClassIntrinsics.doubleArrayClassData, null, this);
//        getOrCreate(ClassIntrinsics.refArrayClassData, null, this);
//
//        getOrCreate(ClassIntrinsics.stringClassData, null, this);

    }

    ClassRuntimeData get(int classRef) {
        return classes.get(classRef);
    }

    ClassRuntimeData get(ClassReference classRef) {
        if (classRef.isResolved()) {
            return get(classRef.get());
        } else {
            for (ClassRuntimeData runtimeData : classes) {
                if (runtimeData.classData.name.equals(classRef.id)) {
                    classRef.linkWith(runtimeData.classData);
                    return runtimeData;
                }
            }
            return null;
        }
    }

    public ClassRuntimeData getOrCreate(ClassData classData) {
        ClassRuntimeData runtimeData = get(classData);
        if (runtimeData == null) {
            int parentRef;
            if (classData.parent != null) {
                ClassRuntimeData parent = get(classData.parent);
                if (parent == null) throw new RuntimeException("Parent not found");
                parentRef = parent.selfRef;
            } else {
                // only for "java.lang.Object" class
                parentRef = -1;
            }
            runtimeData = new ClassRuntimeData(classes.size(), parentRef, classData);
            classes.add(runtimeData);
        }
        return runtimeData;
    }

    public ClassRuntimeData get(ClassData classData) {
        // TODO: this search has O(n) complexity, should be optimized
        for (ClassRuntimeData classRuntimeData : classes ) {
            if (classRuntimeData.classData == classData) return classRuntimeData;
        }
        return null;
    }

}
