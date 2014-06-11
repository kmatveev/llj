package llj.asm.bytecode.exec;

import llj.asm.bytecode.ClassReference;import llj.asm.bytecode.InstructionCode;import llj.asm.bytecode.NewInstanceInstruction;

public class DirectNewInstanceInstruction extends NewInstanceInstruction {

    final ClassRuntimeData runtimeData;

    public DirectNewInstanceInstruction(InstructionCode code, ClassReference classRef, ClassRuntimeData runtimeData) {
        super(code, classRef);
        this.runtimeData = runtimeData;
    }

    public DirectNewInstanceInstruction(NewInstanceInstruction base, ClassRuntimeData runtimeData) {
        super(base.code, base.classRef);
        this.runtimeData = runtimeData;
    }

}
