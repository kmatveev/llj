package llj.asm.bytecode;

import llj.packager.jclass.FormatException;
import llj.packager.jclass.constants.*;

public abstract class MethodHandle {

    public static MethodHandle read(MethodHandleConstant methodHandleDesc) throws FormatException {
        ConstantRef reference = methodHandleDesc.reference;
        Constant referent = reference.resolve();
        if (methodHandleDesc.referenceKind == MethodHandleConstant.ReferenceKind.REF_getField) {
            if (referent instanceof FieldRefConstant) {
                FieldReference fieldRef = FieldReference.make((FieldRefConstant) referent);
                return new GetField(fieldRef);
            } else {
                throw new RuntimeException();
            }
        } else if (methodHandleDesc.referenceKind == MethodHandleConstant.ReferenceKind.REF_getStatic) {
            if (referent instanceof FieldRefConstant) {
                FieldReference fieldRef = FieldReference.make((FieldRefConstant) referent);
                return new GetStatic(fieldRef);
            } else {
                throw new RuntimeException();
            }

        } else if (methodHandleDesc.referenceKind == MethodHandleConstant.ReferenceKind.REF_putField) {
            if (referent instanceof FieldRefConstant) {
                FieldReference fieldRef = FieldReference.make((FieldRefConstant) referent);
                return new PutField(fieldRef);
            } else {
                throw new RuntimeException();
            }
        } else if (methodHandleDesc.referenceKind == MethodHandleConstant.ReferenceKind.REF_putStatic) {
            if (referent instanceof FieldRefConstant) {
                FieldReference fieldRef = FieldReference.make((FieldRefConstant) referent);
                return new PutStatic(fieldRef);
            } else {
                throw new RuntimeException();
            }
        } else if (methodHandleDesc.referenceKind == MethodHandleConstant.ReferenceKind.REF_invokeVirtual) {
            if (referent instanceof MethodRefConstant) {
                MethodReference methodRef = MethodReference.make((MethodRefConstant)referent);
                return new InvokeVirtual(methodRef);
            } else {
                throw new RuntimeException();
            }
        } else if (methodHandleDesc.referenceKind == MethodHandleConstant.ReferenceKind.REF_invokeStatic) {
            if (referent instanceof MethodRefConstant) {
                MethodReference methodRef = MethodReference.make((MethodRefConstant)referent);
                return new InvokeStatic(methodRef);
            } else {
                throw new RuntimeException();
            }
        } else if (methodHandleDesc.referenceKind == MethodHandleConstant.ReferenceKind.REF_invokeInterface) {
            if (referent instanceof InterfaceMethodRefConstant) {
                MethodReference methodRef = MethodReference.make((InterfaceMethodRefConstant)referent);
                return new InvokeInterface(methodRef);
            } else {
                throw new RuntimeException();
            }
        } else if (methodHandleDesc.referenceKind == MethodHandleConstant.ReferenceKind.REF_invokeSpecial) {
            if (referent instanceof MethodRefConstant) {
                MethodReference methodRef = MethodReference.make((MethodRefConstant)referent);
                return new InvokeSpecial(methodRef);
            } else {
                throw new RuntimeException();
            }
        } else if (methodHandleDesc.referenceKind == MethodHandleConstant.ReferenceKind.REF_newInvokeSpecial) {
            if (referent instanceof ClassRefConstant) {
                ClassReference fieldRef = ClassReference.make((ClassRefConstant)referent);
                return new InvokeNewSpecial(fieldRef);
            } else {
                throw new RuntimeException();
            }
        } else {
            throw new RuntimeException();
        }
    }

    public static abstract class FieldAccess extends MethodHandle {
        public final FieldReference fieldRef;
        public FieldAccess(FieldReference fieldRef) {
            this.fieldRef = fieldRef;
        }
    }

    public static class GetField extends FieldAccess {
        public GetField(FieldReference fieldRef) {
            super(fieldRef);
        }
    }

    public static class GetStatic extends FieldAccess {
        public GetStatic(FieldReference fieldRef) {
            super(fieldRef);
        }
    }

    public static class PutField extends FieldAccess {
        public PutField(FieldReference fieldRef) {
            super(fieldRef);
        }
    }

    public static class PutStatic extends FieldAccess {
        public PutStatic(FieldReference fieldRef) {
            super(fieldRef);
        }
    }

    public static abstract class MethodCall extends MethodHandle {
        public final MethodReference methodRef;
        public MethodCall(MethodReference methodRef) {
            this.methodRef = methodRef;
        }
    }

    public static class InvokeVirtual extends MethodCall {
        public InvokeVirtual(MethodReference methodRef) {
            super(methodRef);
        }
    }

    public static class InvokeStatic extends MethodCall {
        public InvokeStatic(MethodReference methodRef) {
            super(methodRef);
        }
    }

    public static class InvokeInterface extends MethodCall {
        public InvokeInterface(MethodReference methodRef) {
            super(methodRef);
        }
    }
    public static class InvokeSpecial extends MethodCall {
        public InvokeSpecial(MethodReference methodRef) {
            super(methodRef);
        }
    }
    public static class InvokeNewSpecial extends MethodHandle {
        public final ClassReference classRef;
        public InvokeNewSpecial(ClassReference classRef) {
            this.classRef = classRef;
        }
    }


}
