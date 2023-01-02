package llj.packager.jclass.constants;

import llj.util.ReadException;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static llj.util.BinIOTools.SIZE_SHORT;
import static llj.util.BinIOTools.getBytes;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putBytes;
import static llj.util.BinIOTools.putUnsignedShort;

public class StringConstant extends Constant {

    public final String value;

    public StringConstant(String value) {
        this.value = value;
    }

    @Override
    public ConstType getType() {
        return ConstType.STRING;
    }

    public static StringConstant readFrom(ReadableByteChannel bb) throws ReadException {
        String location = "";
        try {
            location = "length";
            int length = getUnsignedShort(bb, ByteOrder.BIG_ENDIAN);
            location = "content";
            byte[] data = getBytes(bb, length);
            String value = new String(data, StandardCharsets.UTF_8);
            return new StringConstant(value);
        } catch (ReadException e) {
            throw new ReadException("Was unable to read a " + location + " part of StringConstant", e);
        }
    }

    @Override
    public int writeTo(WritableByteChannel bb) throws IOException {
        byte[] data = value.getBytes(StandardCharsets.UTF_8);
        putUnsignedShort(bb, data.length, ByteOrder.BIG_ENDIAN);
        putBytes(bb, data);
        return SIZE_SHORT + data.length;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        Constant constant = (Constant)obj;
        if (constant.getType() == getType()) {
            StringConstant stringConstant = (StringConstant)constant;
            if (stringConstant.value.equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValid() {
        return value != null;
    }

    @Override
    public String toString() {
        return value;
    }
}
