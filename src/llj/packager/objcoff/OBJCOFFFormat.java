package llj.packager.objcoff;

import llj.packager.Format;
import llj.packager.coff.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

public class OBJCOFFFormat extends COFFBasedFormat<OBJCOFFFormatException> implements Format {

    @Override
    public void writeTo(ByteBuffer out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getStringValue() {
        throw new UnsupportedOperationException();
    }

    public void readFrom(SeekableByteChannel in) throws OBJCOFFFormatException, IOException {

        ByteBuffer readBuffer = ByteBuffer.allocate(500);
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);

        try {
            readCOFFFormat(in, readBuffer);
        } catch (COFFFormatException e) {
            throw new OBJCOFFFormatException(e);
        }

    }

    public int getCOFFHeadersTotalSize() {
        return COFFHeader.SIZE + coffHeader.sizeOfOptionalHeader;
    }

    public long getSectionHeadersOffset() {
        return (getCOFFHeadersTotalSize());
    }

    @Override
    public void readCOFFOptionalHeader(SeekableByteChannel in, ByteBuffer readBuffer) throws IOException, OBJCOFFFormatException {
        // do nothing
    }
}
