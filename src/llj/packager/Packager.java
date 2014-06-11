package llj.packager;

import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;

public interface Packager {

    public void packTo(WritableByteChannel out) throws Exception;

    public void unpackFrom(SeekableByteChannel in) throws Exception;

}
