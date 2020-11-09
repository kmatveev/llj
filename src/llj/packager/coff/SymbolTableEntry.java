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
                dest.storageClass = getUnsignedChar(source);
            }

            @Override
            public void write(SymbolTableEntry source, ByteBuffer dest) {
                putUnsignedChar(dest, source.storageClass);
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
    public char storageClass;
    public short auxCount;

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
