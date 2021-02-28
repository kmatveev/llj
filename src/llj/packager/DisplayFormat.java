package llj.packager;

import llj.util.BinIOTools;
import llj.util.CharTools;
import llj.util.HexTools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;

import static llj.packager.Format.DWORD;
import static llj.packager.Format.WORD;

public enum DisplayFormat {
    
    DEFAULT, HEX_BYTES, ASCII, DEC_NUMBER, HEX_NUMBER, FLAGS_SET;

    public static Optional<String> getBytesString(DisplayFormat displayFormat, byte[] val) {
        if (displayFormat == ASCII) {
            return Optional.of(CharTools.bytesToAscii(val));
        } else if ((displayFormat == HEX_BYTES || displayFormat == DEFAULT)) {
            return Optional.of(HexTools.hexBytes(val));
        } else {
            return Optional.empty();
        }
    }

    public static Optional<String> getByteString(DisplayFormat displayFormat, short val) {
        if (displayFormat == DEC_NUMBER || displayFormat == DEFAULT) {
            return Optional.of(String.valueOf(val));
        } else if (displayFormat == DisplayFormat.HEX_NUMBER) {
            return Optional.of(Integer.toHexString(val));
        } else if (displayFormat == DisplayFormat.HEX_BYTES) {
            return Optional.of(HexTools.hexBytes(new byte[] {(byte)val}));
        } else {
            return Optional.empty();
        }
    }    

    public static Optional<String> getIntegerString(DisplayFormat displayFormat, int val, ByteOrder byteOrder) {
        return getIntegerString(displayFormat, val, WORD, byteOrder);
    }

    public static Optional<String> getIntegerString(DisplayFormat displayFormat, int val, int size, ByteOrder byteOrder) {
        if (displayFormat == DEC_NUMBER || displayFormat == DEFAULT) {
            return Optional.of(String.valueOf(val));
        } else if (displayFormat == DisplayFormat.HEX_NUMBER) {
            return Optional.of("0x" + Integer.toHexString(val));
        } else if (displayFormat == DisplayFormat.HEX_BYTES) {
            ByteBuffer buffer = ByteBuffer.allocate(size);
            buffer.order(byteOrder);
            BinIOTools.putUnsignedShort(buffer, val);
            return Optional.of(HexTools.hexBytes(buffer.array()));
        } else {
            return Optional.empty();
        }
    }

    public static Optional<String> getLongString(DisplayFormat displayFormat, long val, ByteOrder byteOrder) {
        return getLongString(displayFormat, val, DWORD, byteOrder);
    }

    public static Optional<String> getLongString(DisplayFormat displayFormat, long val, int size, ByteOrder byteOrder) {
        if (displayFormat == DEC_NUMBER || displayFormat == DEFAULT) {
            return Optional.of(String.valueOf(val));
        } else if (displayFormat == DisplayFormat.HEX_NUMBER) {
            return Optional.of("0x" + Long.toHexString(val));
        } else if (displayFormat == DisplayFormat.HEX_BYTES) {
            ByteBuffer buffer = ByteBuffer.allocate(size);
            buffer.order(byteOrder);
            BinIOTools.putUnsignedInt(buffer, val);
            return Optional.of(HexTools.hexBytes(buffer.array()));
        } else {
            return Optional.empty();
        }
    }
    
}
