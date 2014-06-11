package llj.asm.bytecode.exec;

import llj.asm.bytecode.ConstantData;
import llj.asm.bytecode.InstructionCode;
import llj.asm.bytecode.LoadConstInstruction;

public class DirectLoadConstInstruction extends LoadConstInstruction {

    public final Value constValue;

    public DirectLoadConstInstruction(InstructionCode code, ConstantData constantData, Value constValue) {
        super(code, constantData);
        this.constValue = constValue;
    }

    public DirectLoadConstInstruction(LoadConstInstruction base, Value constValue) {
        super(base.code, base.constantData);
        this.constValue = constValue;
    }
}
