package llj.asm.bytecode;

import llj.packager.jclass.FormatException;
import llj.packager.jclass.attributes.StackMapTable;
import llj.packager.jclass.constants.ResolveException;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MethodStaticInfo {

    private MethodData method;
    private final InstructionStaticInfo[] instrInfo;
    private List<ClassData> dependentClasses = new ArrayList<ClassData>();

    private MethodStaticInfo(MethodData method) {
        this.method = method;
        this.instrInfo = new InstructionStaticInfo[method.code.size()];
        for (int i = 0; i < instrInfo.length; i++) {
            instrInfo[i] = new InstructionStaticInfo();
        }
    }

    public static MethodStaticInfo infer(MethodData methodData) {
        MethodStaticInfo methodStaticInfo = new MethodStaticInfo(methodData);
        LocalVariableTypes initialLocalVarTypes = populateInitialLocalVars(methodData);
        methodStaticInfo.inferStackStates(methodData, initialLocalVarTypes);
        return methodStaticInfo;
    }

    public static MethodStaticInfo load(MethodData methodData, StackMapTable stackMapTable) throws ResolveException, FormatException {
        MethodStaticInfo methodStaticInfo = new MethodStaticInfo(methodData);
        LocalVariableTypes initialLocalVarTypes = populateInitialLocalVars(methodData);

        int offset = 0;
        InstructionStaticInfo prevInfo = methodStaticInfo.instrInfo[0];
        prevInfo.stackStateBefore = new OpStack();
        prevInfo.localVarTypesBefore = initialLocalVarTypes;

        for (StackMapTable.StackMapFrame frame : stackMapTable.frames) {
            offset += frame.getOffsetDelta() + (offset == 0 ? 0 : 1);
            InstructionStaticInfo currInfo = methodStaticInfo.instrInfo[methodData.atByteOffset(offset).index];
            if (frame instanceof StackMapTable.SameFrame) {
                currInfo.localVarTypesBefore = prevInfo.localVarTypesBefore.clone();
                currInfo.stackStateBefore = new OpStack();
            } else if (frame instanceof StackMapTable.SameLocals1StackItemFrame) {
                currInfo.localVarTypesBefore = prevInfo.localVarTypesBefore.clone();
                currInfo.stackStateBefore = prevInfo.stackStateBefore.clone();
                StackMapTable.SameLocals1StackItemFrame stackItemFrame = (StackMapTable.SameLocals1StackItemFrame)frame;
                Type itemType = Type.fromVerificationTypeInfo(stackItemFrame.stackItemInfo);
                currInfo.stackStateBefore.content.push(itemType);
            } else if (frame instanceof StackMapTable.AppendFrame) {
                currInfo.localVarTypesBefore = prevInfo.localVarTypesBefore.clone();
                currInfo.stackStateBefore = new OpStack();
                StackMapTable.AppendFrame appendFrame = (StackMapTable.AppendFrame)frame;
                for (StackMapTable.VerificationTypeInfo typeInfo : appendFrame.appends) {
                    currInfo.localVarTypesBefore.add(Type.fromVerificationTypeInfo(typeInfo));
                }
            } else if (frame instanceof StackMapTable.ChopFrame) {
                currInfo.localVarTypesBefore = prevInfo.localVarTypesBefore.clone();
                currInfo.stackStateBefore = new OpStack();
                StackMapTable.ChopFrame chopFrame = (StackMapTable.ChopFrame)frame;
                for (int i = 0; i < chopFrame.chopped(); i++) {
                    currInfo.localVarTypesBefore.remove();
                }
            } else if (frame instanceof StackMapTable.FullFrame) {
                StackMapTable.FullFrame fullFrame = (StackMapTable.FullFrame)frame;

                currInfo.localVarTypesBefore = new LocalVariableTypes(methodData.stackFrameSize);
                Type[] localsTypes = new Type[fullFrame.locals.length];
                for (int i = 0; i < fullFrame.locals.length; i++) {
                    localsTypes[i] = Type.fromVerificationTypeInfo(fullFrame.locals[i]);
                }
                currInfo.localVarTypesBefore.set(localsTypes);

                currInfo.stackStateBefore = new OpStack();
                for (StackMapTable.VerificationTypeInfo typeInfo : fullFrame.stackItems) {
                    currInfo.stackStateBefore.content.push(Type.fromVerificationTypeInfo(typeInfo));
                }
            }

            prevInfo = currInfo;

        }

        return methodStaticInfo;
    }


    public static LocalVariableTypes populateInitialLocalVars(MethodData methodData) {
        // populate local variable types from types of params for this method
        LocalVariableTypes initialLocalVarTypes = new LocalVariableTypes(methodData.stackFrameSize);
        try {
            int localVarIdx = 0;
            if (!methodData.isStatic) {
                initialLocalVarTypes.assign(localVarIdx, RefType.instanceRef(new ClassReference(methodData.classData)));
                localVarIdx = 1;
            }
            for (int i = 0; i < methodData.params.size(); i++) {
                Type paramType = methodData.params.get(i);
                initialLocalVarTypes.assign(localVarIdx, paramType);
                localVarIdx += TypeType.size(paramType.type); // some vars (of long and double types) occupy two slots in local var tables
            }
        } catch (ClassesNotLoadedException e) {
            // Should not be thrown, since initially local var table is empty. Thus, catch original exception and re-throw via RuntimeException
            throw new RuntimeException(e);
        }
        return initialLocalVarTypes;
    }

    private void inferStackStates(MethodData methodData, LocalVariableTypes initialLocalVarTypes) {

        Stack<Instruction> instructionAnalysisStack = new Stack<Instruction>();
        {
            Instruction initial = methodData.code.get(0);
            instrInfo[initial.index].stackStateBefore = new OpStack();
            instrInfo[initial.index].localVarTypesBefore = initialLocalVarTypes;
            instructionAnalysisStack.push(initial);
        }

        while (!instructionAnalysisStack.isEmpty()) {
            Instruction currentInstr = instructionAnalysisStack.pop();
            OpStack opStack = instrInfo[currentInstr.index].stackStateBefore.clone();
            LocalVariableTypes localVarTypes = instrInfo[currentInstr.index].localVarTypesBefore.clone();
            try {
                currentInstr.getEffect().apply(opStack, localVarTypes);
                instrInfo[currentInstr.index].stackStateAfter = opStack;
                instrInfo[currentInstr.index].localVarTypesAfter = localVarTypes;
            } catch (IncompatibleStackEffectException e) {
                instrInfo[currentInstr.index].error = "Stack error";
                continue; // return;
            } catch (IncompatibleVariableException e) {
                instrInfo[currentInstr.index].error = "Variable error";
                continue; // return;
            } catch (ClassesNotLoadedException e) {
                instrInfo[currentInstr.index].error = "Cannot decide, classes not loaded";
                continue; // return;
            }


            if (currentInstr instanceof JumpInstruction) {
                JumpInstruction jumpInstr = (JumpInstruction) currentInstr;
                handleInstructionOutput(opStack, localVarTypes, instructionAnalysisStack, jumpInstr.instrRef.get());
                if (jumpInstr.isConditional()) {
                    int nextInstrIndex = currentInstr.index + 1;
                    if (nextInstrIndex < methodData.code.size()) {
                        handleInstructionOutput(opStack, localVarTypes, instructionAnalysisStack, methodData.code.get(nextInstrIndex));
                    }
                }
            } else {
                int nextInstrIndex = currentInstr.index + 1;
                if (nextInstrIndex < methodData.code.size()) {
                    handleInstructionOutput(opStack, localVarTypes, instructionAnalysisStack, methodData.code.get(nextInstrIndex));
                }
            }
        }
    }

    private void handleInstructionOutput(OpStack stackBefore, LocalVariableTypes localVarTypesBefore, Stack<Instruction> instructionAnalysisStack, Instruction nextInstr) {
        InstructionStaticInfo staticInfo = instrInfo[nextInstr.index];
        if (staticInfo.stackStateBefore != null) {
            if (!staticInfo.stackStateBefore.equals(stackBefore)) {
                staticInfo.error = "OpStack merge error";
            }
        } else {
            staticInfo.stackStateBefore = stackBefore;
        }
        if (staticInfo.localVarTypesBefore != null) {
            if (!staticInfo.localVarTypesBefore.equals(localVarTypesBefore)) {
                staticInfo.error = "LocalVarTypes merge error";
            }
        } else {
            staticInfo.localVarTypesBefore = localVarTypesBefore;
        }

        if (!staticInfo.isHandled()) {
            instructionAnalysisStack.push(nextInstr);
        }
    }

    public String getStackStateAfter(int instrIdx) {
        InstructionStaticInfo staticInfo = instrInfo[instrIdx];
        if (staticInfo.error != null) {
            return staticInfo.error;
        } else if (staticInfo.stackStateAfter != null) {
            return staticInfo.stackStateAfter.content.toString();
        } else {
            return "";
        }
    }

    public String getStackStateBefore(int instrIdx) {
        InstructionStaticInfo staticInfo = instrInfo[instrIdx];
        if (staticInfo.stackStateBefore != null) {
            return staticInfo.stackStateBefore.content.toString();
        } else {
            return "";
        }
    }

    public String getLocalsBefore(int instrIdx, boolean ignoreUsageInfo) {
        InstructionStaticInfo staticInfo = instrInfo[instrIdx];
        if (staticInfo.localVarTypesBefore != null) {
            int topIndex = ignoreUsageInfo ? staticInfo.localVarTypesBefore.getSize() : staticInfo.localVarTypesBefore.getUsed();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < topIndex; i++) {
                sb.append(staticInfo.localVarTypesBefore.get(i)).append(',');
            }
            return sb.toString();
        } else {
            return "";
        }
    }


    private static class InstructionStaticInfo {

        public OpStack stackStateBefore, stackStateAfter = null;
        public LocalVariableTypes localVarTypesBefore, localVarTypesAfter;
        public String error = null;

        public boolean isHandled() {
            return (stackStateAfter != null && localVarTypesAfter != null) || error != null;
        }

    }

}
