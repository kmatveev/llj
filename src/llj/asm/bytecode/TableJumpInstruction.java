package llj.asm.bytecode;

import llj.util.ref.Resolver;

import java.util.Map;
import java.util.SortedMap;

public class TableJumpInstruction extends Instruction {

    public final InstructionReference defaultInstr;

    public final SortedMap<Integer, InstructionReference> jumpTable;

    public final Effect effect;

    public TableJumpInstruction(InstructionCode code, InstructionReference instrRef, SortedMap<Integer, InstructionReference> jumpTable) {
        super(code);
        this.defaultInstr = instrRef;
        this.jumpTable = jumpTable;
        this.effect = Effect.make().get(ScalarType.scalar(TypeType.INT));
    }

    @Override
    public Effect getEffect() {
        return effect;
    }

    @Override
    public boolean isLinked() {
        for (Map.Entry<Integer, InstructionReference> jumpEntry : jumpTable.entrySet()) {
            if (!jumpEntry.getValue().isResolved()) return false;
        }
        return true;
    }

    @Override
    public void link(Resolver<ClassData, String> classCache, MethodData methodData) throws LinkException {
        for (Map.Entry<Integer, InstructionReference> jumpEntry : jumpTable.entrySet()) {
            jumpEntry.getValue().resolve(methodData);
        }
    }
}
