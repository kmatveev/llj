package llj.util.ref;

import llj.asm.bytecode.UnresolvedReference;

public class StringReference<T> implements Reference<T, String> {

    public final String id;
    protected T direct;

    protected StringReference(String id) {
        this.id = id;
    }

    protected StringReference(String id, T direct) {
        this.id = id;
        this.direct = direct;
    }

    public T get() throws UnresolvedReference {
        if (direct == null) throw new UnresolvedReference(id);
        return direct;
    }

    @Override
    public String getId() {
        return id;
    }

    public boolean isResolved() {
        return direct != null;
    }

    @Override
    public void resolve(T val) {
        if (isResolved()) throw new IllegalStateException("Already resolved");
        this.direct = val;
    }

    public String toString() {
        if (isResolved()) {
            return get().toString();
        } else {
            return "#" + id;
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        StringReference otherRef = (StringReference)obj;
        if (isResolved() && otherRef.isResolved()) {
            return get().equals(otherRef.get());
        } else {
            return id.equals(otherRef.id);
        }
    }

}
