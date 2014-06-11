package llj.util.ref;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MapResolver<T, M> extends Resolver<T, M> {

    public final ConcurrentMap<M, T> cache = new ConcurrentHashMap<M, T>();

    @Override
    public T resolve(M s) {
        return cache.get(s);
    }

}
