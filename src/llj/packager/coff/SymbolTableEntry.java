package llj.packager.coff;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import static llj.util.BinIOTools.getUnsignedByte;
import static llj.util.BinIOTools.getUnsignedChar;
import static llj.util.BinIOTools.getUnsignedInt;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedByte;
import static llj.util.BinIOTools.putUnsignedChar;
import static llj.util.BinIOTools.putUnsignedInt;
import static llj.util.BinIOTools.putUnsignedShort;
import static llj.util.BinIOTools.readIntoBuffer;

public class SymbolTableEntry {

    public static final int SIZE = 18;


    public static enum Field {
        NAME {
            @Override
            public void read(ByteBuffer source, SymbolTableEntry dest) {
                dest.name.read(source);
            }

            @Override
            public void write(SymbolTableEntry source, ByteBuffer dest) {
                source.name.write(dest);
            }
        },
        VALUE {
            @Override
            public void read(ByteBuffer source, SymbolTableEntry dest) {
                dest.value = getUnsignedInt(source);
            }

            @Override
            public void write(SymbolTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.value);
            }
        },
        SECTION_NUMBER {
            @Override
            public void read(ByteBuffer source, SymbolTableEntry dest) {
                dest.sectionNumber = getUnsignedShort(source);
            }

            @Override
            public void write(SymbolTableEntry source, ByteBuffer dest) {
                putUnsignedShort(dest, source.sectionNumber);
            }
        },
        SYMBOL_TYPE {
            @Override
            public void read(ByteBuffer source, SymbolTableEntry dest) {
                dest.symbolType = getUnsignedShort(source);
            }

            @Override
            public void write(SymbolTableEntry source, ByteBuffer dest) {
                putUnsignedShort(dest, source.symbolType);
            }
        },
        STORAGE_CLASS {
            @Override
            public void read(ByteBuffer source, SymbolTableEntry dest) {
                dest.storageClass = getUnsignedByte(source);
            }

            @Override
            public void write(SymbolTableEntry source, ByteBuffer dest) {
                putUnsignedByte(dest, source.storageClass);
            }
        },
        AUX_COUNT {
            @Override
            public void read(ByteBuffer source, SymbolTableEntry dest) {
                dest.auxCount = getUnsignedByte(source);
            }

            @Override
            public void write(SymbolTableEntry source, ByteBuffer dest) {
                putUnsignedByte(dest, source.auxCount);
            }
        };

        public abstract void read(ByteBuffer source, SymbolTableEntry dest);

        public abstract void write(SymbolTableEntry source, ByteBuffer dest);

    }

    public final NameOrStringTablePointer name = new NameOrStringTablePointer();

    public long value;
    public int sectionNumber;
    public int symbolType;
    public short storageClass;
    public short auxCount;
    public static enum SymbolBaseType {
        NULL(0),
        VOID(1),
        CHAR(2),
        SHORT(3),
        INT(4),
        LONG(5),
        FLOAT(6),
        DOUBLE(7),
        STRUCT(8),
        UNION(9),
        ENUM(10),
        MOE(11),
        BYTE(12),
        WORD(13),
        UINT(14),
        DWORD(15);

        private final int code;

        SymbolBaseType(int code) {
            this.code = code;
        }

        public static SymbolBaseType valueOf(int code) {
            for (SymbolBaseType type : values()) {
                if (type.code == code) return type;
            }
            return null;
        }

    }
    public static enum SymbolComplexType {
        NULL(0),
        POINTER(1),
        FUNCTION(2),
        ARRAY(3);

        private final int code;

        SymbolComplexType(int code) {
            this.code = code;
        }

        public static SymbolComplexType valueOf(int code) {
            for (SymbolComplexType type : values()) {
                if (type.code == code) return type;
            }
            return null;
        }


    }

    public SymbolComplexType getComplexType() {
        return SymbolComplexType.valueOf((symbolType >> 4) & 0xF);
    }

    public SymbolBaseType getBaseType() {
        return SymbolBaseType.valueOf(symbolType & 0x0F);
    }

    public int encodeSymbolType(SymbolComplexType complexType, SymbolBaseType baseType) {
        return complexType.code << 4 | baseType.code;
    }
    
    public static enum StorageClass {
        END_OF_FUNCTION(0xFF),
        NULL(0),
        AUTOMATIC(1),
        EXTERNAL(2),
        STATIC(3),
        REGISTER(4),
        EXTERNAL_DEF(5),
        LABEL(6),
        UNDEFINED_LABEL(7),
        MEMBER_OF_STRUCT(8),
        ARGUMENT(9),
        STRUCT_TAG(10),
        MEMBER_OF_UNION(11),
        UNION_TAG(12),
        TYPE_DEFINITION(13),
        UNDEFINED_STATIC(14),
        ENUM_TAG(15),
        MEMBER_OF_ENUM(16),
        REGISTER_PARAM(17),
        BIT_FIELD(18),
        BLOCK(100),
        FUNCTION(101),
        END_OF_STRUCT(102),
        FILE(103),
        SECTION(104),
        WEAK_EXTERNAL(105),
        CLR_TOKEN(107);

        private final int code;

        StorageClass(int code) {
            this.code = code;
        }

        public static StorageClass valueOf(int code) {
            for (StorageClass type : values()) {
                if (type.code == code) return type;
            }
            return null;
        }

    }    

    public static final int SECTION_UNDEFINED = 0, SECTION_ABSOLUTE = -1, SECTION_DEBUG = -2;

    public void readFrom(ReadableByteChannel in, ByteBuffer readBuffer) throws IOException {
        readIntoBuffer(in, readBuffer, SIZE);
        readFrom(readBuffer);
    }

    public void readFrom(ByteBuffer readBuffer) {
        for (SymbolTableEntry.Field field : SymbolTableEntry.Field.values()) {
            field.read(readBuffer, this);
        }
    }

    public void writeTo(ByteBuffer writeBuffer) {
        for (SymbolTableEntry.Field field : SymbolTableEntry.Field.values()) {
            field.write(this, writeBuffer);
        }
    }


}
