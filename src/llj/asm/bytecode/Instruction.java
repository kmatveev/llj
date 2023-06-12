package llj.asm.bytecode;

import llj.packager.jclass.FormatException;
import llj.packager.jclass.constants.*;
import llj.util.BinTools;
import llj.util.ref.Resolver;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class Instruction {

    public final InstructionCode code;
    public int index;
    public int byteOffset;
    public int size;
    public final MetaData meta = new MetaData();

    public Instruction(InstructionCode code) {
        this.code = code;
    }

    public boolean isLinked() {
        return true;
    }

    public void link(Resolver<ClassData, String> classCache, MethodData methodData) throws LinkException {
        // default implementation does nothing, some sub-classes override this
    }

    public abstract Effect getEffect();

    public static List<Instruction> readCode(byte[] source, ConstantPool constPool, List<BootstrapMethodData> bootstrapMethods) throws FormatException {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        for (int i = 0; i < source.length;) {
            Instruction instr = getInstruction(source, i, constPool, bootstrapMethods);
            instr.index = instructions.size();
            instructions.add(instr);
            i += instr.size;

        }
        return instructions;
    }

    public static Instruction getInstruction(byte[] source, int i, ConstantPool constPool, List<BootstrapMethodData> bootstrapMethods) throws FormatException {
        return getInstruction(source, i, constPool, bootstrapMethods, false);
    }

    public static Instruction getInstruction(byte[] source, int i, ConstantPool constPool, List<BootstrapMethodData> bootstrapMethods, boolean wide) throws FormatException {
        int code = 0xff & source[i];
        InstructionCode instrCode = InstructionCode.getByCode(code);
        int size = instrCode.size;
        Instruction instr;

        // byte[] args = Arrays.copyOfRange(source, i + 1, i + instrCode.size);
        switch (instrCode) {
            case aload:
            case astore:
            case dload:
            case dstore:
            case fload:
            case fstore:
            case iload:
            case istore:
            case lload:
            case lstore:
            {
                instr = new LocalVarAccessInstruction(instrCode, BinTools.getByte(source, i + 1));
                break;
            }
            case aload_0:
            case astore_0:
            case dload_0:
            case dstore_0:
            case fload_0:
            case fstore_0:
            case iload_0:
            case istore_0:
            case lload_0:
            case lstore_0:
            {
                instr = new LocalVarAccessInstruction(instrCode, 0);
                break;
            }
            case aload_1:
            case astore_1:
            case dload_1:
            case dstore_1:
            case fload_1:
            case fstore_1:
            case iload_1:
            case istore_1:
            case lload_1:
            case lstore_1:
            {
                instr = new LocalVarAccessInstruction(instrCode, 1);
                break;
            }
            case aload_2:
            case astore_2:
            case dload_2:
            case dstore_2:
            case fload_2:
            case fstore_2:
            case iload_2:
            case istore_2:
            case lload_2:
            case lstore_2:
            {
                instr = new LocalVarAccessInstruction(instrCode, 2);
                break;
            }
            case aload_3:
            case astore_3:
            case dload_3:
            case dstore_3:
            case fload_3:
            case fstore_3:
            case iload_3:
            case istore_3:
            case lload_3:
            case lstore_3:
            {
                instr = new LocalVarAccessInstruction(instrCode, 3);
                break;
            }
            case baload:
            case bastore:
            case caload:
            case castore:
            case saload:
            case sastore:
            case iaload:
            case iastore:
            case aaload:
            case aastore:
            case laload:
            case lastore:
            case faload:
            case fastore:
            case daload:
            case dastore:
            {
                instr = new ArrayAccessInstruction(instrCode);
                break;
            }
            case getfield:
            case putfield:
            case getstatic:
            case putstatic:
            {
                int index = BinTools.getShort(source, i + 1);
                Constant cnst = constPool.get(index);
                if (cnst.getType() != Constant.ConstType.FIELD_REF) throw new FormatException("Field description expected");
                FieldReference fieldRef = FieldReference.make((FieldRefConstant<FieldReference, ClassReference>) cnst);
                // TODO check access flags and static
                instr = new FieldRefInstruction(instrCode, fieldRef);
                break;
            }
            case invokedynamic: {
                int index = BinTools.getShort(source, i + 1);
                Constant cnst = constPool.get(index);
                if (cnst.getType() != Constant.ConstType.INVOKE_DYNAMIC) throw new FormatException("InvokeDynamic call site description expected");
                instr = new InvokeDynamicInstruction(instrCode, CallSiteData.readFrom((InvokeDynamicConstant) cnst, bootstrapMethods));
                break;
            }
            case invokeinterface:
            {
                int index = BinTools.getShort(source, i + 1);
                Constant cnst = constPool.get(index);
                if (cnst.getType() != Constant.ConstType.INTERFACE_METHOD_REF) throw new FormatException("Interface method description expected");
                MethodReference methodRef = MethodReference.make((InterfaceMethodRefConstant<MethodReference, ClassReference>) cnst);
                // TODO check access flags
                instr = new InvokeInstruction(instrCode, methodRef);
                break;
            }
            case invokespecial:
            case invokevirtual:
            {
                int index = BinTools.getShort(source, i + 1);
                Constant cnst = constPool.get(index);
                if (cnst.getType() != Constant.ConstType.METHOD_REF) throw new FormatException("Method description expected");
                MethodReference methodRef = MethodReference.make((MethodRefConstant<MethodReference, ClassReference>) cnst);
                // TODO check access flags and static
                instr = new InvokeInstruction(instrCode, methodRef);
                break;
            }
            case invokestatic:
            {
                int index = BinTools.getShort(source, i + 1);
                Constant cnst = constPool.get(index);
                MethodReference methodRef;
                if (cnst.getType() == Constant.ConstType.METHOD_REF) {
                    methodRef = MethodReference.make((MethodRefConstant<MethodReference, ClassReference>) cnst);
                } else if (cnst.getType() == Constant.ConstType.INTERFACE_METHOD_REF) {
                    methodRef = MethodReference.make((InterfaceMethodRefConstant<MethodReference, ClassReference>) cnst);
                } else {
                    throw new FormatException("Method description expected");
                }
                // TODO check access flags and static
                instr = new InvokeInstruction(instrCode, methodRef);
                break;
            }
            case ldc:
            {
                int index = BinTools.getByte(source, i + 1);
                Constant cnst = constPool.get(index);
                if (cnst.getType() != Constant.ConstType.INTEGER
                        && cnst.getType() != Constant.ConstType.FLOAT
                        && cnst.getType() != Constant.ConstType.STRING_REF
                        && cnst.getType() != Constant.ConstType.CLASS_REF)
                    throw new FormatException("Integer, Float, StringRef or ClassRefconst expected, but was:" + cnst.getType());
                ConstantData constantData = ConstantData.get(cnst);
                instr = new LoadConstInstruction(instrCode, constantData);
                break;
            }
            case ldc_w:
            {
                int index = BinTools.getShort(source, i + 1);
                Constant cnst = constPool.get(index);
                if (cnst.getType() != Constant.ConstType.INTEGER
                        && cnst.getType() != Constant.ConstType.FLOAT
                        && cnst.getType() != Constant.ConstType.STRING_REF
                        && cnst.getType() != Constant.ConstType.CLASS_REF)
                    throw new FormatException("Integer, Float, StringRef or ClassRefconst expected, but was:" + cnst.getType());
                ConstantData constantData = ConstantData.get(cnst);
                instr = new LoadConstInstruction(instrCode, constantData);
                break;
            }
            case ldc2_w:
            {
                int index = BinTools.getShort(source, i + 1);
                Constant cnst = constPool.get(index);
                if (cnst.getType() != Constant.ConstType.LONG && cnst.getType() != Constant.ConstType.DOUBLE)
                    throw new FormatException("Long or Double const expected, but was:" + cnst.getType());
                ConstantData constantData = ConstantData.get(cnst);
                instr = new LoadConstInstruction(instrCode, constantData);
                break;
            }
            case aconst_null:
            case iconst_0:
            case iconst_1:
            case iconst_2:
            case iconst_3:
            case iconst_4:
            case iconst_5:
            case iconst_m1:
            case lconst_0:
            case lconst_1:
            case fconst_0:
            case fconst_1:
            case fconst_2:
            case dconst_0:
            case dconst_1:
            {
                instr = new LoadImmediateInstruction(instrCode, (short)0);
                break;
            }
            case bipush:
            {
                short val = BinTools.getByte(source, i + 1);
                instr = new LoadImmediateInstruction(instrCode, val);
                break;
            }
            case sipush:
            {
                short val = (short)BinTools.getShort(source, i + 1);
                instr = new LoadImmediateInstruction(instrCode, val);
                break;
            }
            case lcmp :
            case dcmpg:
            case dcmpl:
            case fcmpg:
            case fcmpl:
            {
                instr = new CompareInstruction(instrCode);
                break;
            }
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
            {
                int branchOffset = BinTools.getSignedShort(source, i + 1);
                InstructionReference ref = new InstructionReference(i + branchOffset);
                instr = new JumpInstruction(instrCode, ref);
                break;
            }
            case _goto:
            {
                int branchOffset = BinTools.getSignedShort(source, i + 1);
                InstructionReference ref = new InstructionReference(i + branchOffset);
                instr = new JumpInstruction(instrCode, ref);
                break;
            }
            case goto_w:
            {
                int branchOffset = BinTools.getSignedInt(source, i + 1);
                InstructionReference ref = new InstructionReference(i + branchOffset);
                instr = new JumpInstruction(instrCode, ref);
                break;
            }
            case _new:
            {
                int index = BinTools.getShort(source, i + 1);
                Constant cnst = constPool.get(index);
                if (cnst.getType() != Constant.ConstType.CLASS_REF) throw new FormatException("Class reference expected");
                ClassReference classRef = ClassReference.make((ClassRefConstant<ClassReference>) cnst);
                instr = new NewInstanceInstruction(instrCode, classRef);
                break;

            }
            case lookupswitch:
            {
                int k = 1;
                while (((i + k) & 3) != 0) k++;
                int defaultVal = BinTools.getSignedInt(source, i + k);
                int npairs = BinTools.getSignedInt(source, i + k + 4);
                SortedMap<Integer, InstructionReference> jumpTable = new TreeMap<Integer, InstructionReference>();
                for (int h = 0; h < npairs; h++) {
                    int match = BinTools.getSignedInt(source, i + k + 8 + 8 * h);
                    int offset = BinTools.getSignedInt(source, i + k + 8 + 8 * h + 4);
                    jumpTable.put(new Integer(match), new InstructionReference(i + offset));
                }
                instr = new TableJumpInstruction(instrCode, new InstructionReference(i + defaultVal), jumpTable);
                size = k + 8 + 8*npairs;
                break;
            }
            case tableswitch:
            {
                int k = 1;
                while (((i + k) & 3) != 0) k++;
                int defaultVal = BinTools.getSignedInt(source, i + k);
                int lowVal = BinTools.getSignedInt(source, i + k + 4);
                int highVal = BinTools.getSignedInt(source, i + k + 8);
                SortedMap<Integer, InstructionReference> jumpTable = new TreeMap<Integer, InstructionReference>();
                for (int h = lowVal; h <= highVal; h++) {
                    int offset = BinTools.getSignedInt(source, i + k + 12 + 4 * (h - lowVal));
                    jumpTable.put(new Integer(h), new InstructionReference(i + offset));
                }
                instr = new TableJumpInstruction(instrCode, new InstructionReference(i + defaultVal), jumpTable);
                size = k + 12 + 4*(highVal - lowVal + 1);
                break;
            }
            case newarray:
            {
                ArrayElemType arrayElemType = ArrayElemType.getByCode(BinTools.getByte(source, i + 1));
                instr = new NewArrayInstruction(arrayElemType);
                break;
            }
            case anewarray:
            {
                int index = BinTools.getShort(source, i + 1);
                Constant cnst = constPool.get(index);
                if (cnst.getType() != Constant.ConstType.CLASS_REF) throw new FormatException("Class reference expected, but was:" + cnst.getType());
                ClassReference classRef = ClassReference.make((ClassRefConstant<ClassReference>) cnst);
                instr = new NewArrayInstruction(instrCode, classRef, 1);
                break;
            }
            case multianewarray:
            {
                int index = BinTools.getShort(source, i + 1);
                Constant cnst = constPool.get(index);
                if (cnst.getType() != Constant.ConstType.CLASS_REF) throw new FormatException("Class reference expected, but was:" + cnst.getType());
                ClassReference classRef = ClassReference.make((ClassRefConstant<ClassReference>) cnst);
                int dimensions = BinTools.getByte(source, i + 3);
                instr = new NewArrayInstruction(instrCode, classRef, dimensions);
                break;
            }
            case _return:
            case areturn:
            case ireturn:
            case lreturn:
            case freturn:
            case dreturn:
            {
                instr = new ReturnInstruction(instrCode);
                break;
            }
            case d2f:
            case d2i:
            case d2l:
            case f2d:
            case f2i:
            case f2l:
            case i2b:
            case i2c:
            case i2d:
            case i2f:
            case i2l:
            case i2s:
            case l2d:
            case l2f:
            case l2i:
            {
                instr = new TypeCastInstruction(instrCode);
                break;
            }
            case dadd:
            case ddiv:
            case dmul:
            case drem:
            case dsub:
            case fadd:
            case fdiv:
            case fmul:
            case frem:
            case fsub:
            case iadd:
            case iand:
            case idiv:
            case imul:
            case ior:
            case irem:
            case ishl:
            case ishr:
            case isub:
            case iushr:
            case ixor:
            case ladd:
            case land:
            case ldiv:
            case lmul:
            case lor:
            case lrem:
            case lshl:
            case lshr:
            case lsub:
            case lushr:
            case lxor:
            {
                instr = new BiOpInstruction(instrCode);
                break;
            }
            case ineg:
            case lneg:
            case fneg:
            case dneg:
            {
                instr = new UnOpInstruction(instrCode);
                break;
            }
            case iinc:
            {
                instr = new LocalVarUpdateInstruction(instrCode, BinTools.getByte(source, i + 1), BinTools.getByte(source, i + 1));
                break;
            }
            case dup:
            case dup_x1:
            case dup_x2:
            case dup2:
            case dup2_x1:
            case dup2_x2:
            case nop:
            case pop:
            case pop2:
            case swap:
            {
                instr = new StackInstruction(instrCode);
                break;
            }
            case monitorenter:
            case monitorexit:
            {
                instr = new MonitorInstruction(instrCode);
                break;
            }
            case athrow:
            {
                instr = new ThrowInstruction();
                break;
            }
            case _instanceof:
            case checkcast:
            {
                int index = BinTools.getShort(source, i + 1);
                Constant cnst = constPool.get(index);
                if (cnst.getType() != Constant.ConstType.CLASS_REF) throw new FormatException("Class reference expected");
                ClassReference classRef = ClassReference.make((ClassRefConstant<ClassReference>) cnst);
                instr = new InstanceofInstruction(instrCode, classRef);
                break;
            }
            case arraylength:
            {
                instr = new ArrayLengthInstruction();
                break;
            }
            case jsr:
            {
                int location = BinTools.getSignedShort(source, i + 1);
                instr = new JSRInstruction(instrCode, location);
                break;
            }
            case jsr_w:
            {
                int location = BinTools.getSignedInt(source, i + 1);
                instr = new JSRInstruction(instrCode, location);
                break;
            }
            case ret:
            {
                instr = new RetInstruction();
                break;
            }
            case breakpoint:
            case impdep1:
            case impdep2:
            {
                throw new FormatException("Unknown instruction");
            }
            case wide:
            {
                instr = getInstruction(source, i + 1, constPool, bootstrapMethods, true);
                break;
            }
            default:
                throw new FormatException("Unknown instruction");
        }

        instr.byteOffset = i;
        instr.size = size;
        return instr;
    }

    public String toString() {
        return code.name();
    }

}
