package llj.packager.coff;

import java.nio.ByteBuffer;

public class SectionNameOrStringTablePointer extends NameOrStringTablePointer {

    public void read(ByteBuffer source) {
        byte[] bytes = new byte[8];
        source.get(bytes);
        if (bytes[0] == '/') {
            type = NameOrStringTablePointer.Type.STRING_TABLE_POINTER;
            int endOfString = -1;
            for (int i = 0; i < name.length; i++) {
                name[i] = (char) (bytes[i] & 0xFF);
                if ((bytes[i] == 0) && (endOfString < 0)) {
                    endOfString = i;
                }
            }
            if (endOfString == -1) {
                endOfString = name.length;
            }
            stringTablePointer = Long.parseLong(new String(name, 1, endOfString - 1));
        } else {
            type = NameOrStringTablePointer.Type.NAME;
            for (int i = 0; i < name.length; i++) name[i] = (char) (bytes[i] & 0xFF);
        }

    }

}
