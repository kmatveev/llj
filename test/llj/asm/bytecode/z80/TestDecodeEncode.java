package llj.asm.bytecode.z80;

import llj.asm.z80.*;

public class TestDecodeEncode {

    public static void main(String[] args) throws IncorrectOperandException {

        for (int i = 0; i < 256; i++) {
            Instruction inst = decode(-1, i);
            if (inst != null) {
                testEncode(i, -1, inst);
            }

            inst = decode(0xDD, i);
            if (inst != null) {
                testEncode(i, 0xDD, inst);
            }

            if ((i != 0x63) && (i != 0x6B)) {  // exclude some non-documented opcodes which have simple counterparts
                inst = decode(0xED, i);
                if (inst != null) {
                    testEncode(i, 0xED, inst);
                }
            }


        }

    }

    public static Instruction decode(int prefix, int code) throws IncorrectOperandException {

        // We copy into new instructions so instruction codes are generated inside them

        LdInstruction ldDecoded = LdInstruction.decodeInitial(code, prefix);
        if (ldDecoded != null) {
            LdInstruction result = new LdInstruction(ldDecoded.op1, ldDecoded.op2);
            if (result.tstates != ldDecoded.tstates) {
                throw new RuntimeException();
            }
            return result;
        }

        Op2Instruction op2decoded = Op2Instruction.decodeInitial(code, prefix);
        if (op2decoded != null) {
            return new Op2Instruction(op2decoded.operation, op2decoded.op1, op2decoded.op2);
        }

        IncDecInstruction incDecDecoded = IncDecInstruction.decodeInitial(code, prefix);
        if (incDecDecoded != null) {
            return new IncDecInstruction(incDecDecoded.operation, incDecDecoded.op);
        }

        return null;
    }

    public static void testEncode(int code, int prefix1, Instruction instruction) {
        if (instruction.opCode != code) {
            throw new RuntimeException();
        }
        if (instruction.prefix1 != prefix1) {
            throw new RuntimeException();
        }
        // byte[] encoded = instruction.encode();

    }

}
