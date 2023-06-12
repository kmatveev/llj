package llj.asm.bytecode;

import java.util.EmptyStackException;
import java.util.List;

public class InvokeDynamicInstruction extends Instruction {

    public final CallSiteData callSiteData;

    public InvokeDynamicInstruction(InstructionCode code, CallSiteData callSite) {
        super(code);
        this.callSiteData = callSite;
    }

    @Override
    public Effect getEffect() {
        return new InvokeDynamicEffect();
    }

    public class InvokeDynamicEffect extends Effect {

        @Override
        public void apply(OpStack opStack, LocalVariableTypes localVarTypes) throws IncompatibleStackEffectException, IncompatibleVariableException, ClassesNotLoadedException {

            List<Type> paramTypes = callSiteData.methodTypeData.paramTypes;
            for (int i = paramTypes.size() - 1; i >= 0; i--) {
                Type t = paramTypes.get(i);
                Type valueType;
                try {
                    valueType = opStack.content.pop();
                } catch (EmptyStackException e) {
                    throw new IncompatibleStackEffectException("OpStack Underflow", e);
                }
                if (!t.isAssignableFrom(valueType)) throw new IncompatibleVariableException();
            }
            Type returnType = callSiteData.methodTypeData.resultType;
            if (returnType.type != TypeType.VOID) {
                opStack.content.push(returnType);
            }

        }
    }
}
