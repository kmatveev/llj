package llj.asm.intel;

import java.nio.ByteBuffer;
import java.util.List;

public class JmpInstruction implements Instruction {
    
    public final static int NUM_OPERANDS = 1;
    
    public final Operand op1;

    public JmpInstruction(Operand op1) {
        this.op1 = op1;
    }

    public static JmpInstruction create(List<Operand> operands) {
        if (operands.size() != NUM_OPERANDS) {
            throw new IllegalArgumentException("Incorrect number of operands:" + operands.size());
        }
        Operand op1 = operands.get(0);
        return new JmpInstruction(op1);
    }
    

    @Override
    public void putMachineCode(ByteBuffer bb, LabelResolver resolver) {
        if (op1 instanceof Operand.ImmediateOp) {
            Operand.ValOrLabel val = ((Operand.ImmediateOp)op1).val;
            val.maybeResolveRelative(resolver, this);
            int opCode;
            if ((val.val <= 127) && (val.val >= -128)) {
                opCode = 0xEB;
            } else if ((val.val <= 32767) && (val.val >= -32768)) {
                opCode = 0xE9;
                // TODO prefix
            } else if ((val.val <= 0x7fffffff) && (val.val >= -0x80000000)) {
                opCode = 0xE9;
                
            }
                
        }

    }

    @Override
    public MNEMONIC getMnemonic() {
        return MNEMONIC.JMP;
    }

    @Override
    public int numOperands() {
        return 1;
    }
}
