package llj.asm.bytecode.exec;

public class OutOfStack extends RuntimeTrouble {

    public OutOfStack() {
    }

    public OutOfStack(String message) {
        super(message);
    }
}
