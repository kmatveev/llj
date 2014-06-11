package llj.asm.bytecode;

import llj.util.ref.Resolver;

public class JumpInstruction extends Instruction {

    public final InstructionReference instrRef;
    public final Effect effect;

    public JumpInstruction(InstructionCode code, InstructionReference instrRef) {
        super(code);
        this.instrRef = instrRef;
        effect = getStackEffect(code);
        isConditional();
    }

    public boolean isConditional() {
        switch (code)  {
            case ifeq:
            case ifne:
            case iflt:
            case ifge:
            case ifgt:
            case ifle:
            case if_icmpeq:
            case if_icmpne:
            case if_icmplt:
            case if_icmpge:
            case if_icmpgt:
            case if_icmple:
            case if_acmpeq:
            case if_acmpne:
            case ifnonnull:
            case ifnull:
                return true;
            case _goto:
            case goto_w:
                return false;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public Effect getEffect() {
        return effect;
    }

    private static Effect getStackEffect(InstructionCode code) {
        Effect result;
        switch (code) {
            case if_acmpeq:
            case if_acmpne:  result = Effect.make().get(RefType.anyRef()).get(RefType.anyRef()); break;
            case if_icmpeq:
            case if_icmpne:
            case if_icmplt:
            case if_icmpge:
            case if_icmpgt:
            case if_icmple: result = Effect.make().get(ScalarType.scalar(TypeType.INT)).get(ScalarType.scalar(TypeType.INT)); break;
            case ifeq:
            case ifne:
            case iflt:
            case ifge:
            case ifgt:
            case ifle: result = Effect.make().get(ScalarType.scalar(TypeType.INT)); break;
            case ifnonnull:
            case ifnull: result = Effect.make().get(RefType.anyRef()); break;
            case _goto:
            case goto_w: result = Effect.make(); break;
            default: throw new IllegalArgumentException();
        }
        return result;
    }

    @Override
    public String toString() {
        return code.name() + " " + instrRef.absoluteOffset;
    }

    @Override
    public boolean isLinked() {
        return instrRef.isResolved();
    }

    @Override
    public void link(Resolver<ClassData, String> classCache, MethodData methodData) throws LinkException {
        instrRef.resolve(methodData);
    }
}
