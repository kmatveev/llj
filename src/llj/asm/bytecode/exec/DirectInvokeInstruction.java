package llj.asm.bytecode.exec;

import llj.asm.bytecode.InstructionCode;
import llj.asm.bytecode.InvokeInstruction;
import llj.asm.bytecode.MethodReference;
import llj.asm.bytecode.Type;

public class DirectInvokeInstruction extends InvokeInstruction {

    public final MethodRuntimeData methodRuntimeData;

    public DirectInvokeInstruction(InstructionCode code, MethodReference methodRef, MethodRuntimeData methodRuntimeData) {
        super(code, methodRef);
        this.methodRuntimeData = methodRuntimeData;
    }

    public DirectInvokeInstruction(InvokeInstruction base, MethodRuntimeData methodRuntimeData) {
        super(base.code, base.methodRef);
        this.methodRuntimeData = methodRuntimeData;
    }

    public int getObjectOffsetOnOpStack() {
        int offset = 0;
        for (Type type : methodRuntimeData.methodData.params) {
            offset += Value.getSizeFor(type.type);
        }
        return offset / Value.SIZE_SINGLE;
    }
}
