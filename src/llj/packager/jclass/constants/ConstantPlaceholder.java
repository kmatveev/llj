package llj.packager.jclass.constants;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

public class ConstantPlaceholder<E> extends Constant<E> {

    @Override
    public ConstType getType() {
        return ConstType.PLACEHOLDER;
    }

    @Override
    public int writeTo(WritableByteChannel bb) throws IOException {
        return 0;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
