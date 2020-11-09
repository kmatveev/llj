package llj.packager;

import llj.util.CharTools;
import llj.util.HexTools;

import java.util.Optional;

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

    public static Optional<String> getIntegerString(DisplayFormat displayFormat, int lastSize) {
        if (displayFormat == DEC_NUMBER || displayFormat == DEFAULT) {
            return Optional.of(String.valueOf(lastSize));
        } else {
            return Optional.empty();
        }
    }
}
