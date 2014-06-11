package llj.asm.bytecode.exec;

import llj.asm.bytecode.FieldRefInstruction;
import llj.asm.bytecode.FieldReference;
import llj.asm.bytecode.InstructionCode;

public class DirectFieldRefInstruction extends FieldRefInstruction {

    public final FieldRuntimeData fieldRuntimeData;

    public DirectFieldRefInstruction(InstructionCode code, FieldReference fieldRef, FieldRuntimeData fieldRuntimeData) {
        super(code, fieldRef);
        this.fieldRuntimeData = fieldRuntimeData;
    }

    public DirectFieldRefInstruction(FieldRefInstruction base, FieldRuntimeData fieldRuntimeData) {
        super(base.code, base.fieldRef);
        this.fieldRuntimeData = fieldRuntimeData;
    }

}
