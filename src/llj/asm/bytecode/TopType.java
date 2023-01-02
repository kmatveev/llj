package llj.asm.bytecode;

public final class TopType extends Type {
    
    public static final TopType instance = new TopType();

    private TopType() {
        super(null);
    }

    @Override
    public String toString() {
        return "TopType";
    }

    @Override
    public String toCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAssignableFrom(Type another) throws ClassesNotLoadedException {
        return true;
    }
}
