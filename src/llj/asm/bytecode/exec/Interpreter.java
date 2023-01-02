package llj.asm.bytecode.exec;

import llj.asm.bytecode.ArrayAccessInstruction;
import llj.asm.bytecode.Instruction;
import llj.asm.bytecode.JumpInstruction;
import llj.asm.bytecode.LoadImmediateInstruction;
import llj.asm.bytecode.LocalVarAccessInstruction;
import llj.asm.bytecode.ReturnInstruction;
import llj.asm.bytecode.TypeType;

public class Interpreter
{

    public static void interpret(ThreadState threadState, VM vm) throws RuntimeTrouble {
        while(threadState.getCurrentState() != ThreadState.State.TERMINATED) {
            interpretOne(threadState, vm);
        }
    }

    public static void interpretOne(ThreadState threadState, VM vm) throws RuntimeTrouble {
        Heap heap = vm.heap;
        ClassHeap classHeap = vm.classHeap;
        Instruction instr = threadState.currentFrame().currentInstruction();
        switch (instr.code) {
            case aload:
            case aload_0:
            case aload_1:
            case aload_2:
            case aload_3:
            case fload:
            case fload_0:
            case fload_1:
            case fload_2:
            case fload_3:
            case iload:
            case iload_0:
            case iload_1:
            case iload_2:
            case iload_3:
            {
                LocalVarAccessInstruction varInstr = (LocalVarAccessInstruction)instr;
                Value val = threadState.currentFrame().getLocalSingle(varInstr.localVarA);
                threadState.currentFrame().pushOp(val);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case dload:
            case dload_0:
            case dload_1:
            case dload_2:
            case dload_3:
            case lload:
            case lload_0:
            case lload_1:
            case lload_2:
            case lload_3:
            {
                LocalVarAccessInstruction varInstr = (LocalVarAccessInstruction)instr;
                Value val = threadState.currentFrame().getLocalDouble(varInstr.localVarA);
                threadState.currentFrame().pushOp(val);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case astore:
            case astore_0:
            case astore_1:
            case astore_2:
            case astore_3:
            case fstore:
            case fstore_0:
            case fstore_1:
            case fstore_2:
            case fstore_3:
            case istore:
            case istore_0:
            case istore_1:
            case istore_2:
            case istore_3:
            {
                Value val = threadState.currentFrame().popOpSingle();
                LocalVarAccessInstruction varInstr = (LocalVarAccessInstruction)instr;
                threadState.currentFrame().setLocal(varInstr.localVarA, val);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case lstore:
            case lstore_0:
            case lstore_1:
            case lstore_2:
            case lstore_3:
            case dstore:
            case dstore_0:
            case dstore_1:
            case dstore_2:
            case dstore_3:
            {
                Value val = threadState.currentFrame().popOpDouble();
                LocalVarAccessInstruction varInstr = (LocalVarAccessInstruction)instr;
                threadState.currentFrame().setLocal(varInstr.localVarA, val);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case aconst_null:
            {
                Heap.Pointer nullPointer = heap.nullPointer;
                threadState.currentFrame().pushOp(nullPointer);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case iconst_0:
            case iconst_1:
            case iconst_2:
            case iconst_3:
            case iconst_4:
            case iconst_5:
            case iconst_m1:
            case bipush:
            case sipush:
            {
                LoadImmediateInstruction immInstr = (LoadImmediateInstruction)instr;
                IntegerValue integerValue = new IntegerValue(immInstr.getIntValue());
                threadState.currentFrame().pushOp(integerValue);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case fconst_0:
            case fconst_1:
            case fconst_2:
            {
                LoadImmediateInstruction immInstr = (LoadImmediateInstruction)instr;
                FloatValue integerValue = new FloatValue(immInstr.getFloatValue());
                threadState.currentFrame().pushOp(integerValue);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case lconst_0:
            case lconst_1:
            {
                LoadImmediateInstruction immInstr = (LoadImmediateInstruction)instr;
                LongValue integerValue = new LongValue(immInstr.getLongValue());
                threadState.currentFrame().pushOp(integerValue);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case dconst_0:
            case dconst_1:
            {
                LoadImmediateInstruction immInstr = (LoadImmediateInstruction)instr;
                DoubleValue integerValue = new DoubleValue(immInstr.getDoubleValue());
                threadState.currentFrame().pushOp(integerValue);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case iadd:
            {
                IntegerValue op1 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                IntegerValue op2 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                IntegerValue result = IntegerValue.add(op1, op2);
                threadState.currentFrame().pushOp(result);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case isub:
            {
                IntegerValue op1 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                IntegerValue op2 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                IntegerValue result = IntegerValue.sub(op1, op2);
                threadState.currentFrame().pushOp(result);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case iand:
            {
                IntegerValue op1 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                IntegerValue op2 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                IntegerValue result = IntegerValue.and(op1, op2);
                threadState.currentFrame().pushOp(result);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case ior:
            {
                IntegerValue op1 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                IntegerValue op2 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                IntegerValue result = IntegerValue.or(op1, op2);
                threadState.currentFrame().pushOp(result);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case ixor:
            {
                IntegerValue op1 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                IntegerValue op2 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                IntegerValue result = IntegerValue.xor(op1, op2);
                threadState.currentFrame().pushOp(result);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case iinc:
            {
                IntegerValue op1 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                IntegerValue result = IntegerValue.inc(op1);
                threadState.currentFrame().pushOp(result);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case ineg:
            {
                IntegerValue op1 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                IntegerValue result = IntegerValue.neg(op1);
                threadState.currentFrame().pushOp(result);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case getfield:
            {
                DirectFieldRefInstruction fieldInstr = (DirectFieldRefInstruction)instr;
                Heap.Pointer objRef = Heap.Pointer.load(threadState.currentFrame().popOpSingle());
                // assertClass(heap, objRef.actualRef, instr.fieldRef.classRef);
                if (heap.isNull(objRef)) throw new NullPointer();
                FieldRuntimeData fieldRuntimeData = fieldInstr.fieldRuntimeData;
                Value fieldVal = fieldRuntimeData.get(heap, objRef);
                threadState.currentFrame().pushOp(fieldVal);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case putfield:
            {
                DirectFieldRefInstruction fieldInstr = (DirectFieldRefInstruction)instr;
                FieldRuntimeData fieldRuntimeData = fieldInstr.fieldRuntimeData;
                Value fieldVal = threadState.currentFrame().popOp(fieldRuntimeData.fieldData.type.type);
                Heap.Pointer objRef = Heap.Pointer.load(threadState.currentFrame().popOpSingle());
                if (heap.isNull(objRef)) throw new NullPointer();
                // assertClass(heap, objRef.actualRef, instr.fieldRef.classRef);
                fieldRuntimeData.set(heap, objRef, fieldVal);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case getstatic:
            case putstatic:
            {
                throw new UnsupportedOperationException();
            }
            case ldc:
            case ldc_w:
            case ldc2_w:
            {
                DirectLoadConstInstruction constInstr = (DirectLoadConstInstruction)instr;
                Value val = constInstr.constValue;
                threadState.currentFrame().pushOp(val);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case _new:
            {
                DirectNewInstanceInstruction newInstr = (DirectNewInstanceInstruction)instr;
                ClassRuntimeData classRuntimeData = newInstr.runtimeData;
                Heap.Pointer pointer = classRuntimeData.allocateInstance(heap);
                threadState.currentFrame().pushOp(pointer);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case anewarray:
            {
                DirectNewArrayInstruction newArrayInstr = (DirectNewArrayInstruction)instr;
                TypeType elemType = newArrayInstr.arrayElemType.type;
                IntegerValue numElements = IntegerValue.load(threadState.currentFrame().popOpSingle());
                Heap.Pointer pointer = Arrays.allocateRefArray(newArrayInstr.arrayClass, newArrayInstr.elementClass, numElements.val, heap);
                threadState.currentFrame().pushOp(pointer);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case newarray:
            {
                DirectNewArrayInstruction newArrayInstr = (DirectNewArrayInstruction)instr;
                TypeType elemType = newArrayInstr.arrayElemType.type;
                IntegerValue numElements = IntegerValue.load(threadState.currentFrame().popOpSingle());
                Heap.Pointer pointer = Arrays.allocateScalarArray(newArrayInstr.arrayClass, elemType, numElements.val, heap);
                threadState.currentFrame().pushOp(pointer);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case invokedynamic:
                throw new UnsupportedOperationException();
            case invokestatic:
            {
                DirectInvokeInstruction invokeInstr = (DirectInvokeInstruction)instr;
                MethodRuntimeData declared = invokeInstr.methodRuntimeData;
                threadState.currentFrame().advanceInstr();
                threadState.enterStatic(declared);
                break;
            }
            case invokespecial:
            case invokevirtual:
            case invokeinterface:
            {
                DirectInvokeInstruction invokeInstr = (DirectInvokeInstruction)instr;
                MethodRuntimeData declared = invokeInstr.methodRuntimeData;
                OpaqueSingleSizeValue pointerVal = (OpaqueSingleSizeValue) threadState.currentFrame().getOp(invokeInstr.getObjectOffsetOnOpStack(), TypeType.REF);
                Heap.Pointer objRef = Heap.Pointer.load(pointerVal);
                // TODO handling of NPE should involve extraction of method params from op stack
                if (heap.isNull(objRef)) throw new NullPointer();
                MethodRuntimeData actual = declared.findVirtual(ClassRuntimeData.readClassRef(classHeap, heap, objRef));
                actual.link(vm);
                threadState.currentFrame().advanceInstr();
                threadState.enterVirtual(actual);
                break;
            }

            case areturn:
            case ireturn:
            case freturn:
            case lreturn:
            case dreturn:
            case _return:
            {
                ReturnInstruction returnInstr = (ReturnInstruction)instr;
                Value returnValue = null;
                if (returnInstr.type != TypeType.VOID) {
                    returnValue = threadState.currentFrame().popOp(returnInstr.type);
                }
                threadState.popStackFrame();
                if (returnInstr.type != TypeType.VOID) {
                    threadState.currentFrame().pushOp(returnValue);
                }
                break;
            }
            case dup:
            {
                OpaqueSingleSizeValue val = threadState.currentFrame().popOpSingle();
                threadState.currentFrame().pushOp(val);
                threadState.currentFrame().pushOp(val);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case ifeq:
            {
                JumpInstruction jumpInstr = (JumpInstruction)instr;
                IntegerValue op1 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                if (op1.val == 0) {
                    threadState.currentFrame().jumpTo(jumpInstr.instrRef.absoluteOffset);
                } else {
                    threadState.currentFrame().advanceInstr();
                }
                break;
            }
            case ifne:
            {
                JumpInstruction jumpInstr = (JumpInstruction)instr;
                IntegerValue op1 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                if (op1.val != 0) {
                    threadState.currentFrame().jumpTo(jumpInstr.instrRef.absoluteOffset);
                } else {
                    threadState.currentFrame().advanceInstr();
                }
                break;
            }
            case iflt:
            {
                JumpInstruction jumpInstr = (JumpInstruction)instr;
                IntegerValue op1 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                if (op1.val < 0) {
                    threadState.currentFrame().jumpTo(jumpInstr.instrRef.absoluteOffset);
                } else {
                    threadState.currentFrame().advanceInstr();
                }
                break;
            }
            case ifge:
            {
                JumpInstruction jumpInstr = (JumpInstruction)instr;
                IntegerValue op1 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                if (op1.val >= 0) {
                    threadState.currentFrame().jumpTo(jumpInstr.instrRef.absoluteOffset);
                } else {
                    threadState.currentFrame().advanceInstr();
                }
                break;
            }
            case ifgt:
            {
                JumpInstruction jumpInstr = (JumpInstruction)instr;
                IntegerValue op1 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                if (op1.val > 0) {
                    threadState.currentFrame().jumpTo(jumpInstr.instrRef.absoluteOffset);
                } else {
                    threadState.currentFrame().advanceInstr();
                }
                break;
            }
            case ifle:
            {
                JumpInstruction jumpInstr = (JumpInstruction)instr;
                IntegerValue op1 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                if (op1.val <= 0) {
                    threadState.currentFrame().jumpTo(jumpInstr.instrRef.absoluteOffset);
                } else {
                    threadState.currentFrame().advanceInstr();
                }
                break;
            }
            case if_icmpeq:
            {
                JumpInstruction jumpInstr = (JumpInstruction)instr;
                IntegerValue op1 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                IntegerValue op2 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                if (op1.val == op2.val) {
                    threadState.currentFrame().jumpTo(jumpInstr.instrRef.absoluteOffset);
                } else {
                    threadState.currentFrame().advanceInstr();
                }
                break;
            }
            case if_icmpne:
            {
                JumpInstruction jumpInstr = (JumpInstruction)instr;
                IntegerValue op1 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                IntegerValue op2 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                if (op1.val != op2.val) {
                    threadState.currentFrame().jumpTo(jumpInstr.instrRef.absoluteOffset);
                } else {
                    threadState.currentFrame().advanceInstr();
                }
                break;
            }
            case if_icmplt:
            {
                JumpInstruction jumpInstr = (JumpInstruction)instr;
                IntegerValue op1 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                IntegerValue op2 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                if (op2.val < op1.val) {
                    threadState.currentFrame().jumpTo(jumpInstr.instrRef.absoluteOffset);
                } else {
                    threadState.currentFrame().advanceInstr();
                }
                break;
            }
            case if_icmpge:
            {
                JumpInstruction jumpInstr = (JumpInstruction)instr;
                IntegerValue op1 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                IntegerValue op2 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                if (op2.val >= op1.val) {
                    threadState.currentFrame().jumpTo(jumpInstr.instrRef.absoluteOffset);
                } else {
                    threadState.currentFrame().advanceInstr();
                }
                break;
            }
            case if_icmpgt:
            {
                JumpInstruction jumpInstr = (JumpInstruction)instr;
                IntegerValue op1 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                IntegerValue op2 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                if (op2.val > op1.val) {
                    threadState.currentFrame().jumpTo(jumpInstr.instrRef.absoluteOffset);
                } else {
                    threadState.currentFrame().advanceInstr();
                }
                break;
            }
            case if_icmple:
            {
                JumpInstruction jumpInstr = (JumpInstruction)instr;
                IntegerValue op1 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                IntegerValue op2 = IntegerValue.load(threadState.currentFrame().popOpSingle());
                if (op2.val <= op1.val) {
                    threadState.currentFrame().jumpTo(jumpInstr.instrRef.absoluteOffset);
                } else {
                    threadState.currentFrame().advanceInstr();
                }
                break;
            }
            case if_acmpeq:
            {
                JumpInstruction jumpInstr = (JumpInstruction)instr;
                Heap.Pointer op1 = Heap.Pointer.load(threadState.currentFrame().popOpSingle());
                Heap.Pointer op2 = Heap.Pointer.load(threadState.currentFrame().popOpSingle());
                if (op2.equals(op1)) {
                    threadState.currentFrame().jumpTo(jumpInstr.instrRef.absoluteOffset);
                } else {
                    threadState.currentFrame().advanceInstr();
                }
                break;
            }
            case if_acmpne:
            {
                JumpInstruction jumpInstr = (JumpInstruction)instr;
                Heap.Pointer op1 = Heap.Pointer.load(threadState.currentFrame().popOpSingle());
                Heap.Pointer op2 = Heap.Pointer.load(threadState.currentFrame().popOpSingle());
                if (!op2.equals(op1)) {
                    threadState.currentFrame().jumpTo(jumpInstr.instrRef.absoluteOffset);
                } else {
                    threadState.currentFrame().advanceInstr();
                }
                break;
            }
            case ifnonnull:
            {
                JumpInstruction jumpInstr = (JumpInstruction)instr;
                Heap.Pointer op1 = Heap.Pointer.load(threadState.currentFrame().popOpSingle());
                if (!heap.isNull(op1)) {
                    threadState.currentFrame().jumpTo(jumpInstr.instrRef.absoluteOffset);
                } else {
                    threadState.currentFrame().advanceInstr();
                }
                break;
            }
            case ifnull:
            {
                JumpInstruction jumpInstr = (JumpInstruction)instr;
                Heap.Pointer op1 = Heap.Pointer.load(threadState.currentFrame().popOpSingle());
                if (heap.isNull(op1)) {
                    threadState.currentFrame().jumpTo(jumpInstr.instrRef.absoluteOffset);
                } else {
                    threadState.currentFrame().advanceInstr();
                }
                break;
            }
            case _goto:
            case goto_w:
            {
                JumpInstruction jumpInstr = (JumpInstruction)instr;
                threadState.currentFrame().jumpTo(jumpInstr.instrRef.absoluteOffset);
                break;
            }
            case bastore:
            case castore:
            case sastore:
            case iastore:
            case lastore:
            case fastore:
            case dastore:
            case aastore:
            {
                ArrayAccessInstruction arrayInstr = (ArrayAccessInstruction)instr;
                Heap.Pointer arrayPointer = Heap.Pointer.load(threadState.currentFrame().popOpSingle());
                IntegerValue index = (IntegerValue)threadState.currentFrame().popOp(TypeType.INT);
                Value val = threadState.currentFrame().popOp(arrayInstr.elementType);
                Arrays.setArrayElement(classHeap, heap, arrayPointer, index.val, val);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case baload:
            case caload:
            case saload:
            case iaload:
            case laload:
            case faload:
            case daload:
            case aaload:
            {
                ArrayAccessInstruction arrayInstr = (ArrayAccessInstruction)instr;
                Heap.Pointer arrayPointer = Heap.Pointer.load(threadState.currentFrame().popOpSingle());
                IntegerValue index = (IntegerValue)threadState.currentFrame().popOp(TypeType.INT);
                Value val = threadState.currentFrame().popOp(arrayInstr.elementType);
                Arrays.setArrayElement(classHeap, heap, arrayPointer, index.val, val);
                threadState.currentFrame().advanceInstr();
                break;
            }
            case arraylength:
            {
                Heap.Pointer arrayPointer = Heap.Pointer.load(threadState.currentFrame().popOpSingle());
                IntegerValue length = new IntegerValue(Arrays.getArrayLength(classHeap, heap, arrayPointer));
                threadState.currentFrame().pushOp(length);
                threadState.currentFrame().advanceInstr();
                break;
            }
            default:
                throw new RuntimeException();
        }
        return;
    }

    private void assertClass(ClassHeap classHeap, Heap heap, Heap.Pointer actualRef, ClassRuntimeData expectedClass) throws WrongClass, MemoryAccessError {
        ClassRuntimeData actualClass = ClassRuntimeData.readClassRef(classHeap, heap, actualRef);
        if (actualClass != expectedClass) throw new WrongClass();
    }

}
