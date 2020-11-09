package llj.packager.coff;

import llj.packager.DisplayFormat;
import llj.packager.IntrospectableFormat;
import llj.packager.dosexe.DOSHeader;
import llj.packager.winpe.PEFormat;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static llj.util.BinIOTools.getUnsignedInt;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedInt;
import static llj.util.BinIOTools.putUnsignedShort;
import static llj.util.BinIOTools.readIntoBuffer;

public class COFFHeader implements IntrospectableFormat {

    public static final int SIZE = 20;

    public static enum Field {
        MACHINE {
            public void read(ByteBuffer source, COFFHeader dest) {
                dest.machine = getUnsignedShort(source);
            }

            public void write(COFFHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.machine);
            }
            
            public int size() { return WORD; }
        },
        NUMBER_OF_SECTIONS {
            public void read(ByteBuffer source, COFFHeader dest) {
                dest.numberOfSections = getUnsignedShort(source);
            }

            public void write(COFFHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.numberOfSections);
            }

            public int size() { return WORD; }
        },
        TIME_DATE_STAMP {
            public void read(ByteBuffer source, COFFHeader dest) {
                dest.timeDateStamp = getUnsignedInt(source);
            }

            public void write(COFFHeader source, ByteBuffer dest) {
                putUnsignedInt(dest, source.timeDateStamp);
            }

            public int size() { return DWORD; }
        },
        POINTER_TO_SYMBOL_TABLE {
            public void read(ByteBuffer source, COFFHeader dest) {
                dest.pointerToSymbolTable = getUnsignedInt(source);
            }

            public void write(COFFHeader source, ByteBuffer dest) {
                putUnsignedInt(dest, source.pointerToSymbolTable);
            }

            public int size() { return DWORD; }
        },
        NUMBER_OF_SYMBOLS {
            public void read(ByteBuffer source, COFFHeader dest) {
                dest.numberOfSymbols = getUnsignedInt(source);
            }

            public void write(COFFHeader source, ByteBuffer dest) {
                putUnsignedInt(dest, source.numberOfSymbols);
            }

            public int size() { return DWORD; }
        },
        SIZE_OF_OPTIONAL_HEADER {
            public void read(ByteBuffer source, COFFHeader dest) {
                dest.sizeOfOptionalHeader = getUnsignedShort(source);
            }

            public void write(COFFHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.sizeOfOptionalHeader);
            }

            public int size() { return WORD; }
        },
        CHARACTERISTICS {
            public void read(ByteBuffer source, COFFHeader dest) {
                dest.characteristics = getUnsignedShort(source);
            }

            public void write(COFFHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.characteristics);
            }

            public int size() { return WORD; }
        };

        public abstract void read(ByteBuffer source, COFFHeader dest);

        public abstract void write(COFFHeader source, ByteBuffer dest);
        
        public abstract int size();

    }

    public static enum CharacteristicsField {
        IMAGE_FILE_RELOCS_STRIPPED(0),
        IMAGE_FILE_EXECUTABLE_IMAGE(1),
        IMAGE_FILE_LINE_NUMS_STRIPPED(2),
        IMAGE_FILE_LOCAL_SYMS_STRIPPED(3),
        IMAGE_FILE_AGGRESIVE_WS_TRIM(4),
        IMAGE_FILE_LARGE_ADDRESS_AWARE(5),
        IMAGE_FILE_BYTES_REVERSED_LO(7),
        IMAGE_FILE_32BIT_MACHINE(8),
        IMAGE_FILE_DEBUG_STRIPPED(9),
        IMAGE_FILE_REMOVABLE_RUN_FROM_SWAP(10),
        IMAGE_FILE_NET_RUN_FROM_SWAP(11),
        IMAGE_FILE_SYSTEM(12),
        IMAGE_FILE_DLL(13),
        IMAGE_FILE_UP_SYSTEM_ONLY(14),
        IMAGE_FILE_BYTES_REVERSED_HI(15);

        public final int bit;

        CharacteristicsField(int bit) {
            this.bit = bit;
        }

        public boolean isSetInValue(int val) {
            return (val & PEFormat.setBit(bit)) > 0;
        }

        public int setInValue(int val) {
            return val | PEFormat.setBit(bit);
        }

        public int resetInValue(int val) {
            return val & (~PEFormat.setBit(bit));
        }

        public static List<CharacteristicsField> getAllSetInValue(int value) {
            List<CharacteristicsField> result = new ArrayList<>();
            for (CharacteristicsField field : values()) {
                if (field.isSetInValue(value)) {
                    result.add(field);
                }
            }
            return result;
        }

        public static int composeFrom(List<CharacteristicsField> fields) {
            int result = 0;
            for (CharacteristicsField field : fields) {
                result = field.setInValue(result);
            }
            return result;
        }

    }


    public static enum Machine {

        // TODO
        UNKNOWN(0), I386(0x14c), X64(0x8664), MSIL(0xc0ee);

        public final int value;

        Machine(int value) {
            this.value = value;
        }

        public static Machine valueOf(int v) {
            for (Machine m : values()) {
                if (m.value == v) {
                    return m;
                }
            }
            return UNKNOWN;
        }
    }


    public int machine;
    public int numberOfSections = 0;
    public long timeDateStamp;
    public long pointerToSymbolTable;
    public long numberOfSymbols;
    public int sizeOfOptionalHeader;
    public int characteristics;

    public Field readFrom(ReadableByteChannel in, ByteBuffer readBuffer) throws IOException {
        readIntoBuffer(in, readBuffer, SIZE);
        return readFrom(readBuffer);
    }

    public Field readFrom(ByteBuffer readBuffer) {
        for (Field field : Field.values()) {
            try {
                field.read(readBuffer, this);
            } catch (BufferUnderflowException e) {
                return field;
            }
        }
        return null;
    }

    public void writeTo(ByteBuffer writeBuffer) {
        for (Field field : Field.values()) {
            field.write(this, writeBuffer);
        }
    }

    public List<String> getNames() {
        List<String> result = new ArrayList<String>();
        for (Field field: Field.values()) {
            result.add(field.name());
        }
        return result;
    }

    public Optional<String> getStringValue(String fieldName, DisplayFormat displayFormat) {
        for (Field field: Field.values()) {
            if (field.name().equals(fieldName)) {
                return getStringValue(field, displayFormat);
            }
        }
        throw new IllegalArgumentException(fieldName);
    }

    public Optional<String> getStringValue(Field field, DisplayFormat displayFormat) {
        // all fields in COFF header are mandatory/fixed
        switch(field) {
            case MACHINE: return Optional.of(String.valueOf(machine));
            case NUMBER_OF_SECTIONS: return Optional.of(String.valueOf(numberOfSections));
            case TIME_DATE_STAMP: return Optional.of(String.valueOf(timeDateStamp));
            case POINTER_TO_SYMBOL_TABLE: return Optional.of(String.valueOf(pointerToSymbolTable));
            case NUMBER_OF_SYMBOLS: return Optional.of(String.valueOf(numberOfSymbols));
            case SIZE_OF_OPTIONAL_HEADER: return Optional.of(String.valueOf(sizeOfOptionalHeader));
            case CHARACTERISTICS: return Optional.of(String.valueOf(characteristics));
            default: throw new IllegalArgumentException(field.toString());
        }
    }

    public int getSize(String fieldName) {
        for (COFFHeader.Field field: COFFHeader.Field.values()) {
            if (field.name().equals(fieldName)) {
                return field.size();
            }
        }
        throw new IllegalArgumentException(fieldName);
    }

    public int getOffset(String fieldName) {
        int offset = 0;
        for (COFFHeader.Field field: COFFHeader.Field.values()) {
            if (field.name().equals(fieldName)) {
                return offset;
            } else {
                offset += field.size();
            }
        }
        throw new IllegalArgumentException(fieldName);
    }

    @Override
    public String getStringValue() {
        return "COFFHeader";
    }

    @Override
    public int getSize() {
        return SIZE;
    }
    
}
