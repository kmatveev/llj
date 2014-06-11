package llj.asm.bytecode.exec;

import llj.asm.bytecode.NewArrayInstruction;

public class DirectNewArrayInstruction extends NewArrayInstruction {

    final ClassRuntimeData arrayClass;
    final ClassRuntimeData elementClass;

    public DirectNewArrayInstruction(NewArrayInstruction base, ClassRuntimeData arrayClass, ClassRuntimeData elementClass) {
        super(base.code, base.arrayElemType, base.dimensions, base.effect);
        this.arrayClass = arrayClass;
        this.elementClass = elementClass;
    }
}
