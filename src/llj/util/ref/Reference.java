package llj.util.ref;

import llj.asm.bytecode.UnresolvedReference;

public interface Reference<T, M> {

    public abstract M getId();

    public abstract T get() throws UnresolvedReference;

    public abstract boolean isResolved();

    public void resolve(T val);

}
