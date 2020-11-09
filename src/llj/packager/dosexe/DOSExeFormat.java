package llj.packager.dosexe;

import llj.packager.Format;
import llj.packager.RawFormat;
import llj.packager.winpe.PEFormatException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;

public class DOSExeFormat implements Format {

    public DOSHeader<Object> header;
    public RawFormat contents;

    @Override
    public void writeTo(ByteBuffer out) {
        header.writeTo(out);
        contents.writeTo(out);
    }

    public void readFrom(ReadableByteChannel in) throws IOException, DOSExeFormatException {
        ByteBuffer readBuffer = ByteBuffer.allocate(DOSHeader.getReadBufferSize());
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);

        header = new DOSHeader<Object>();
        Object lastField = header.readFrom(in, readBuffer);

        if (lastField != null) {
            throw new DOSExeFormatException("Unable to read EXE file, it is shorter than expected size of DOS EXE header");
        }

    }

    @Override
    public int getSize() {
        return header.getSize() + contents.getSize();
    }

    @Override
    public String getStringValue() {
        return null;
    }
}
