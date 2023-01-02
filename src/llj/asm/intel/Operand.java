package llj.asm.intel;

import java.util.Optional;

public interface Operand {
    
    public static enum Type {
        IMMEDIATE, REGISTER, MEMORY
    }
    
    public static enum Register {
        
        AL(1),AH(1),AX(2),EAX(4),BL(1),BH(1),BX(2),EBX(4),CL(1),CH(1),CX(2),ECX(4),DL(1),DH(1),DX(2),EDX(4),SI(2),ESI(4),DI(2),EDI(4),SP(2),ESP(4),BP(2),EBP(4),DS(2),CS(2),SS(2),ES(2),FS(2),GS(2);
        
        public final int numBytes;

        Register(int numBytes) {
            this.numBytes = numBytes;
        }

        public static Optional<Register> get(String str) {
            try {
                return Optional.of(Register.valueOf(str.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        
    }
    
    public abstract boolean equals(Operand another);
    
    public static class ImmediateOp implements Operand {
        public final ValOrLabel val;

        public ImmediateOp(ValOrLabel val) {
            this.val = val;
        }

        public static ImmediateOp fromLabel(String text) {
            return new ImmediateOp(ValOrLabel.fromLabel(text));
        }

        public static ImmediateOp fromConstant(int number) {
            return new ImmediateOp(ValOrLabel.fromVal(number));
        }

        @Override
        public boolean equals(Operand another) {
            if (!(another instanceof ImmediateOp)) return false;
            ImmediateOp anotherImmediate = (ImmediateOp)another;
            return this.val.equals(anotherImmediate.val);
        }
    }
    
    public static class RegisterOp implements Operand {
        public final Register reg;

        public RegisterOp(Register reg) {
            this.reg = reg;
        }

        @Override
        public boolean equals(Operand another) {
            if (!(another instanceof RegisterOp)) return false;
            RegisterOp anotherReg = (RegisterOp)another;
            return this.reg  == anotherReg.reg;
        }
        
    }

    public static class MemoryContentOp implements Operand {
        public final Expression expr;

        public MemoryContentOp(Expression expr) {
            this.expr = expr;
        }

        @Override
        public boolean equals(Operand another) {
            if (!(another instanceof MemoryContentOp)) return false;
            MemoryContentOp anotherOp = (MemoryContentOp)another;
            return this.expr.equals(anotherOp.expr);
        }
        
    }
    
    public static class ValOrLabel {
        public static final int UNRESOLVED = -1;
        public String label;
        public int val = UNRESOLVED;

        private ValOrLabel(String label) {
            this.label = label;
        }

        private ValOrLabel(int val) {
            this.val = val;
        }

        public static ValOrLabel fromLabel(String label) {
            return new ValOrLabel(label);
        }

        public static ValOrLabel fromVal(int val) {
            return new ValOrLabel(val);
        }

        public void maybeResolve(LabelResolver resolver, Instruction requester) {
            if (!isResolved()) {
                this.val = resolver.resolve(label, requester);
            }
        }

        public void maybeResolveRelative(LabelResolver resolver, Instruction requester) {
            if (!isResolved()) {
                this.val = resolver.resolveRelativeTo(label, requester);
            }
        }

        public boolean isResolved() {
            return val != UNRESOLVED;
        }
    }
    
    public static class Expression {
        public Register register;
        public ValOrLabel valOrLabel;
        public Register register2;  // not null when we have register+register2

        private Expression(Register register) {
            this.register = register;
        }

        private Expression(ValOrLabel valOrLabel) {
            this.valOrLabel = valOrLabel;
        }

        public static Expression register(Register register) {
            return new Expression(register);
        }

        public static Expression valOrLabel(ValOrLabel valOrLabel) {
            return new Expression(valOrLabel);
        }

        public static Expression label(String text) {
            return valOrLabel(ValOrLabel.fromLabel(text));
        }
        
        public boolean equals(Expression anotherExpr) {
            // TODO 
            return (this.register == anotherExpr.register && this.register2 == anotherExpr.register2) || (this.register == anotherExpr.register2 && this.register2 == anotherExpr.register);
        }
    }
    
}
