package llj.asm.bytecode.exec;


import llj.asm.bytecode.Instruction;
import llj.asm.bytecode.MethodData;
import llj.asm.bytecode.Type;
import llj.asm.bytecode.TypeType;

import java.util.ArrayList;
import java.util.List;

public class ThreadState {

    public enum State {
        NEW, RUNNING, BLOCKED, WAITING, TIMED_WAIT, TERMINATED;
    }

    private State currentState = State.NEW;

    private final List<StackFrame> callStack = new ArrayList<StackFrame>();
    private static final int STACK_SIZE = 128;

    public ThreadState() {
        StackFrame frame = new StackFrame();
        callStack.add(frame);
    }

    public void enterVirtual(MethodRuntimeData method) throws OutOfStack, StackUnderflow, IllegalLocalVarIndex {
        if (callStack.size() > STACK_SIZE) throw new OutOfStack();
        if (currentState == State.TERMINATED ) throw new IllegalStateException();
        if (currentState == State.NEW ) {
            currentState = State.RUNNING;
        }

        StackFrame frame = new StackFrame(method);

        List<Type> params = method.methodData.params;

        int totalSlots = MethodData.localVarSlotsFor(params) + 1; // add one slot for variable holding "this" reference
        int currentLocalVarIdx = totalSlots;
        for (int i = params.size(); i >= 0; i--) {
            // "this" reference is an implicit parameter
            TypeType paramType = i == 0 ? TypeType.REF : params.get(i - 1).type;
            Value val = currentFrame().popOp(paramType);
            currentLocalVarIdx -= TypeType.size(paramType);  // some vars (of long and double types) occupy two slots in local var tables
            frame.setLocal(currentLocalVarIdx, val);
        }

        callStack.add(frame);
    }

    public void enterStatic(MethodRuntimeData method) throws OutOfStack, StackUnderflow, IllegalLocalVarIndex {
        if (callStack.size() > STACK_SIZE) throw new OutOfStack();
        if (currentState == State.TERMINATED ) throw new IllegalStateException();
        if (currentState == State.NEW ) {
            currentState = State.RUNNING;
        }

        StackFrame frame = new StackFrame(method);

        List<Type> params = method.methodData.params;
        int totalSlots = MethodData.localVarSlotsFor(params) + 1;
        int currentLocalVarIdx = totalSlots;
        for (int i = params.size() - 1; i >= 0; i--) {
            TypeType paramType = params.get(i).type;
            Value val = currentFrame().popOp(paramType);
            currentLocalVarIdx -= TypeType.size(paramType); // some vars (of long and double types) occupy two slots in local var tables
            frame.setLocal(currentLocalVarIdx, val);
        }

        callStack.add(frame);
    }


    public void popStackFrame() {
        callStack.remove(callStack.size() - 1);
        if (callStack.size() == 1) {
            currentState = State.TERMINATED;
        }
    }

    public StackFrame currentFrame() {
        return callStack.get(callStack.size() - 1);
    }

    public State getCurrentState() {
        return currentState;
    }

    public static final class StackFrame {

        public final MethodRuntimeData method;
        public int currentInstructionIndex;
        private final List<Integer> opStack = new ArrayList<Integer>();
        public final int[] locals;

        public StackFrame(MethodRuntimeData method) {
            this.method = method;
            this.locals = new int[method.getStackFrameSize()];
        }

        public StackFrame() {
            this.method = null;
            this.locals = new int[0];
        }

        public OpaqueSingleSizeValue getLocalSingle(int index) throws IllegalLocalVarIndex{
            if (index < locals.length) {
                return new OpaqueSingleSizeValue(locals[index]);
            } else {
                throw new IllegalLocalVarIndex("Index out of range:" + index);
            }
        }

        public OpaqueDoubleSizeValue getLocalDouble(int index) throws IllegalLocalVarIndex {
            if (index < (locals.length - 1)) {
                return new OpaqueDoubleSizeValue(locals[index], locals[index]);
            } else {
                throw new IllegalLocalVarIndex("Index out of range:" + index);
            }
        }

        public void setLocal(int index, Value val) throws IllegalLocalVarIndex {
            if (val.getSize() == Value.SIZE_SINGLE) {
                if (index < locals.length) {
                    locals[index] = val.getFirstWord();
                } else {
                    throw new IllegalLocalVarIndex("Index out of range:" + index);
                }
            } else if (val.getSize() == Value.SIZE_DOUBLE) {
                if (index < locals.length + 1) {
                    locals[index] = val.getFirstWord();
                    locals[index + 1] = val.getSecondWord();
                } else {
                    throw new IllegalLocalVarIndex("Index out of range:" + index);
                }
            } else {
                throw new IllegalArgumentException("Size unknown:" + val.getSize());
            }
        }

        public Instruction currentInstruction() {
            return method.getInstruction(currentInstructionIndex);
        }

        public void jumpTo(int absOffset) {
            if (absOffset < 0 || absOffset >= method.code.size()) throw new IllegalArgumentException();
            currentInstructionIndex = absOffset;
        }

        public void advanceInstr() {
            currentInstructionIndex++;
        }

        public void pushOp(Value val) {
            if (val.getSize() > Value.SIZE_SINGLE) {
                opStack.add(val.getSecondWord());
            }
            opStack.add(val.getFirstWord());
        }

        public Value popOp(TypeType type) throws StackUnderflow {
            int size = Value.getSizeFor(type);
            if (size == Value.SIZE_SINGLE) {
                return popOpSingle();
            } else if (size == Value.SIZE_DOUBLE) {
                return popOpDouble();
            } else {
                throw new RuntimeException();
            }
        }

        public Value getOp(int offset, TypeType type) throws StackUnderflow {
            int firstWordIndex = opStack.size() - 1 - offset;
            int size = Value.getSizeFor(type);
            if (size == Value.SIZE_SINGLE) {
                Integer firstWord = opStack.get(firstWordIndex);
                return new OpaqueSingleSizeValue(firstWord.intValue());
            } else if (size == Value.SIZE_DOUBLE) {
                Integer firstWord = opStack.get(firstWordIndex);
                Integer secondWord = opStack.get(firstWordIndex - 1);
                return new OpaqueDoubleSizeValue(firstWord.intValue(), secondWord.intValue());
            } else {
                throw new RuntimeException();
            }
        }

        public OpaqueSingleSizeValue popOpSingle() throws StackUnderflow {
            try {
                Integer stackContent = opStack.remove(opStack.size() - 1);
                return new OpaqueSingleSizeValue(stackContent.intValue());
            } catch (IndexOutOfBoundsException e) {
                throw new StackUnderflow(e.getMessage());
            }
        }

        public OpaqueDoubleSizeValue popOpDouble() throws StackUnderflow {
            try {
                Integer firstWord = opStack.remove(opStack.size() - 1);
                Integer secondWord = opStack.remove(opStack.size() - 1);
                return new OpaqueDoubleSizeValue(firstWord.intValue(), secondWord.intValue());
            } catch (IndexOutOfBoundsException e) {
                throw new StackUnderflow(e.getMessage());
            }
        }



    }


}
