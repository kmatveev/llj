package llj.asm.bytecode;

public class InstructionReference {

    public int absoluteOffset;
    public Instruction instruction;

    public InstructionReference(int byteOffset) {
        this.absoluteOffset = byteOffset;
    }

    public boolean isResolved() {
        return instruction != null;
    }

    public Instruction get() {
        return instruction;
    }

    public void resolve(MethodData methodData) {
        instruction = methodData.atByteOffset(absoluteOffset);
    }

}
