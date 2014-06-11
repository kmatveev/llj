package llj.asm.bytecode.exec;

import llj.asm.bytecode.ClassData;
import llj.asm.bytecode.ClassIntrinsics;
import llj.asm.bytecode.ClassReference;
import llj.asm.bytecode.ConstantData;
import llj.asm.bytecode.FieldRefInstruction;
import llj.asm.bytecode.Instruction;
import llj.asm.bytecode.InvokeInstruction;
import llj.asm.bytecode.LoadConstInstruction;
import llj.asm.bytecode.MethodData;
import llj.asm.bytecode.NewArrayInstruction;
import llj.asm.bytecode.NewInstanceInstruction;
import llj.asm.bytecode.RefType;
import llj.asm.bytecode.TypeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MethodRuntimeData {

    public final ClassRuntimeData thisClass;
    public final MethodData methodData;
    public List<Instruction> code = null;

    public MethodRuntimeData(ClassRuntimeData thisClass, MethodData method) {
        if (thisClass.classData != method.classData) throw new RuntimeException("Assertion failed");
        this.methodData = method;
        this.thisClass = thisClass;
    }

    public boolean isLinked() {
        return code != null;
    }

    public int getStackFrameSize() {
        return methodData.stackFrameSize;
    }

    public void link(VM vm) throws RuntimeTrouble {
        if (isLinked()) return;
        Set<ClassReference> dependencies = methodData.getDependencies();
        vm.loadClasses(dependencies);
        code = new ArrayList<Instruction>();
        for (Instruction instr : methodData.code) {
            Instruction linkedInstr = linkInstruction(vm, instr);
            this.code.add(linkedInstr);
        }
    }

    Instruction linkInstruction(VM vm, Instruction instr) throws NoSuchField, NoSuchMethod, OutOfMemory, ClassLoadingTrouble, MemoryAccessError, ArrayIndexOutOfBounds {
        Instruction linkedInstr;
        if (instr instanceof NewInstanceInstruction) {
            NewInstanceInstruction newInstr = (NewInstanceInstruction)instr;
            ClassReference classRef = newInstr.classRef;
            vm.loadClass(classRef);
            ClassRuntimeData runtimeRef = vm.classHeap.get(classRef);
            if (runtimeRef == null) throw new RuntimeException("Assertion failed");
            linkedInstr = new DirectNewInstanceInstruction(newInstr, runtimeRef);
        } else if (instr instanceof NewArrayInstruction) {
            NewArrayInstruction newInstr = (NewArrayInstruction)instr;
            ClassRuntimeData elementRuntimeData = null;
            if (newInstr.arrayElemType.type == TypeType.REF) {
                RefType elemRefType = (RefType) newInstr.arrayElemType;
                ClassReference elementClassRef = elemRefType.classRef;
                vm.loadClass(elementClassRef);
                elementRuntimeData = vm.classHeap.get(elementClassRef);
            }
            ClassData arrayClassData = ClassIntrinsics.getArrayClassData(newInstr.arrayElemType.type);
            ClassRuntimeData runtimeRef = vm.classHeap.get(arrayClassData);
            if (runtimeRef == null) throw new RuntimeException("Assertion failed");
            linkedInstr = new DirectNewArrayInstruction(newInstr, runtimeRef, elementRuntimeData);
        } else if (instr instanceof FieldRefInstruction) {
            FieldRefInstruction fieldRefInstr = (FieldRefInstruction)instr;
            vm.loadClass(fieldRefInstr.fieldRef.classRef);
            ClassRuntimeData runtimeRef = vm.classHeap.get(fieldRefInstr.fieldRef.classRef);
            FieldRuntimeData fieldRuntimeData = runtimeRef.getRuntimeData(fieldRefInstr.fieldRef, vm.classHeap);
            linkedInstr = new DirectFieldRefInstruction(fieldRefInstr, fieldRuntimeData);
        } else if (instr instanceof InvokeInstruction) {
            InvokeInstruction invokeInstr = (InvokeInstruction)instr;
            vm.loadClass(invokeInstr.methodRef.classRef);
            ClassRuntimeData runtimeRef = vm.classHeap.get(invokeInstr.methodRef.classRef);
            MethodRuntimeData methodRuntimeData = runtimeRef.getRuntimeData(invokeInstr.methodRef);
            linkedInstr = new DirectInvokeInstruction(invokeInstr, methodRuntimeData);
        } else if (instr instanceof LoadConstInstruction) {
            LoadConstInstruction loadInstr = (LoadConstInstruction)instr;
            ConstantData constData = loadInstr.constantData;
            // loading of classes "java.lang.String" and "java.lang.Class" happens inside this method, so we pass vm instance
            Value constValue = thisClass.getOrCreateFor(constData, vm);
            linkedInstr = new DirectLoadConstInstruction(loadInstr, constValue);
        } else {
            linkedInstr = instr;
        }
        return linkedInstr;
    }

    public Instruction getInstruction(int instructionRef) {
        return code.get(instructionRef);
    }

    public MethodRuntimeData findVirtual(ClassRuntimeData classRuntimeData) {
        // TODO
        return this;
    }
}
