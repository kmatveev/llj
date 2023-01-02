package llj.packager.coff;

import llj.packager.DisplayFormat;
import llj.packager.FieldSequenceFormat;
import llj.packager.IntrospectableFormat;
import llj.packager.winpe.PEFormat;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static llj.util.BinIOTools.getUnsignedInt;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedInt;
import static llj.util.BinIOTools.putUnsignedShort;
import static llj.util.BinIOTools.readIntoBuffer;

public class COFFHeader extends FieldSequenceFormat {

    public static final int SIZE = 20;

    public static enum Field implements FieldSequenceFormat.Field<COFFHeader>{
        MACHINE {
            public void read(ByteBuffer source, COFFHeader dest) {
                dest.machine = getUnsignedShort(source, ByteOrder.LITTLE_ENDIAN);
            }

            public void write(COFFHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.machine, ByteOrder.LITTLE_ENDIAN);
            }
            
            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(COFFHeader format, DisplayFormat displayFormat) {
                if (displayFormat == DisplayFormat.DEFAULT) {
                    return Optional.of(Machine.valueOf(format.machine).name());
                } else {
                    return DisplayFormat.getIntegerString(displayFormat, format.machine, size(), ByteOrder.LITTLE_ENDIAN);
                }
            }
        },
        NUMBER_OF_SECTIONS {
            public void read(ByteBuffer source, COFFHeader dest) {
                dest.numberOfSections = getUnsignedShort(source, ByteOrder.LITTLE_ENDIAN);
            }

            public void write(COFFHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.numberOfSections, ByteOrder.LITTLE_ENDIAN);
            }

            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(COFFHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.numberOfSections, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        TIME_DATE_STAMP {
            public void read(ByteBuffer source, COFFHeader dest) {
                dest.timeDateStamp = getUnsignedInt(source, ByteOrder.LITTLE_ENDIAN);
            }

            public void write(COFFHeader source, ByteBuffer dest) {
                putUnsignedInt(dest, source.timeDateStamp, ByteOrder.LITTLE_ENDIAN);
            }

            public int size() { return DWORD; }

            @Override
            public Optional<String> getStringValue(COFFHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.timeDateStamp, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        POINTER_TO_SYMBOL_TABLE {
            public void read(ByteBuffer source, COFFHeader dest) {
                dest.pointerToSymbolTable = getUnsignedInt(source, ByteOrder.LITTLE_ENDIAN);
            }

            public void write(COFFHeader source, ByteBuffer dest) {
                putUnsignedInt(dest, source.pointerToSymbolTable, ByteOrder.LITTLE_ENDIAN);
            }

            public int size() { return DWORD; }

            @Override
            public Optional<String> getStringValue(COFFHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.pointerToSymbolTable, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        NUMBER_OF_SYMBOLS {
            public void read(ByteBuffer source, COFFHeader dest) {
                dest.numberOfSymbols = getUnsignedInt(source, ByteOrder.LITTLE_ENDIAN);
            }

            public void write(COFFHeader source, ByteBuffer dest) {
                putUnsignedInt(dest, source.numberOfSymbols, ByteOrder.LITTLE_ENDIAN);
            }

            public int size() { return DWORD; }

            @Override
            public Optional<String> getStringValue(COFFHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.numberOfSymbols, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        SIZE_OF_OPTIONAL_HEADER {
            public void read(ByteBuffer source, COFFHeader dest) {
                dest.sizeOfOptionalHeader = getUnsignedShort(source, ByteOrder.LITTLE_ENDIAN);
            }

            public void write(COFFHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.sizeOfOptionalHeader, ByteOrder.LITTLE_ENDIAN);
            }

            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(COFFHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.sizeOfOptionalHeader, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        CHARACTERISTICS {
            public void read(ByteBuffer source, COFFHeader dest) {
                dest.characteristics = getUnsignedShort(source, ByteOrder.LITTLE_ENDIAN);
            }

            public void write(COFFHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.characteristics, ByteOrder.LITTLE_ENDIAN);
            }

            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(COFFHeader format, DisplayFormat displayFormat) {
                if (displayFormat == DisplayFormat.DEFAULT || displayFormat == DisplayFormat.FLAGS_SET) {
                    List<COFFHeader.CharacteristicsField> fields = COFFHeader.CharacteristicsField.getAllSetInValue(format.characteristics);
                    return Optional.of("" + fields);
                } else {
                    return Optional.of(String.valueOf(format.characteristics));
                }

            }
        };

        public abstract void read(ByteBuffer source, COFFHeader dest);

        public abstract void write(COFFHeader source, ByteBuffer dest);
        
        public abstract int size();
        
        public abstract Optional<String> getStringValue(COFFHeader format, DisplayFormat displayFormat);

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

    @Override
    public Collection<Field> fields() {
        return Arrays.asList(Field.values());
    }

    public Field readFrom(ReadableByteChannel in, ByteBuffer readBuffer) throws IOException {
        readIntoBuffer(in, readBuffer, SIZE);
        return (Field)readFrom(readBuffer);
    }

    @Override
    public boolean isDisplayFormatSupported(String fieldName, DisplayFormat format) {
        return getStringValue(fieldName, format).isPresent();
    }

    public Optional<String> getStringValue(Field field, DisplayFormat displayFormat) {
        return field.getStringValue(this, displayFormat);
    }

    @Override
    public void setStringValue(String fieldName, DisplayFormat format) {
        throw new UnsupportedOperationException();
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
