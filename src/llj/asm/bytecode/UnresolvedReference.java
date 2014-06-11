package llj.asm.bytecode;

public class UnresolvedReference extends RuntimeException {

    public UnresolvedReference(String message) {
        super(message);
    }
}
