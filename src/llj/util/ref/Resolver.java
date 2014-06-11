package llj.util.ref;

public abstract class Resolver<T, M> {

    public abstract T resolve(M s);

    public boolean resolveAndCache(Reference<T, M> ref) {
        if (ref.isResolved()) {
            return true;
        } else {
            T result = resolve(ref.getId());
            boolean resolved = result != null;
            if (resolved) {
                ref.resolve(result);
            }
            return resolved;
        }
    }


}
