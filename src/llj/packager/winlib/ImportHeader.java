package llj.packager.winlib;

import llj.packager.DisplayFormat;
import llj.packager.FieldSequenceFormat;
import llj.packager.coff.COFFHeader;
import llj.util.BinIOTools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static llj.util.BinIOTools.getUnsignedInt;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedInt;
import static llj.util.BinIOTools.putUnsignedShort;

public class ImportHeader extends FieldSequenceFormat {

    public static final int SIZE = 20;
    public static final int SIG1 = 0;
    public static final int SIG2 = 65535;

    public static enum Field implements FieldSequenceFormat.Field<ImportHeader> {
        
        SIG1 {
            public void read(ByteBuffer source, ImportHeader dest) {
                dest.sig1 = getUnsignedShort(source);
            }

            public void write(ImportHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.sig1);
            }
            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(ImportHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.sig1, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        SIG2 {
            public void read(ByteBuffer source, ImportHeader dest) {
                dest.sig2 = getUnsignedShort(source);
            }

            public void write(ImportHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.sig2);
            }
            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(ImportHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.sig2, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        VERSION {
            public void read(ByteBuffer source, ImportHeader dest) {
                dest.version = getUnsignedShort(source);
            }

            public void write(ImportHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.version);
            }
            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(ImportHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.version, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        MACHINE {
            public void read(ByteBuffer source, ImportHeader dest) {
                dest.machine = getUnsignedShort(source);
            }

            public void write(ImportHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.machine);
            }
            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(ImportHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.machine, size(), ByteOrder.LITTLE_ENDIAN);
            }

        },
        TIME_DATE_STAMP {
            public void read(ByteBuffer source, ImportHeader dest) {
                dest.timedatestamp = getUnsignedInt(source);
            }

            public void write(ImportHeader source, ByteBuffer dest) {
                putUnsignedInt(dest, source.timedatestamp);
            }
            public int size() { return DWORD; }

            @Override
            public Optional<String> getStringValue(ImportHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.timedatestamp, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        SIZE_OF_DATA {
            public void read(ByteBuffer source, ImportHeader dest) {
                dest.sizeOfData = getUnsignedInt(source);
            }

            public void write(ImportHeader source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfData);
            }
            public int size() { return DWORD; }

            @Override
            public Optional<String> getStringValue(ImportHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.sizeOfData, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        ORDINAL_OR_HINT {
            public void read(ByteBuffer source, ImportHeader dest) {
                dest.ordinalOrHint = getUnsignedShort(source);
            }

            public void write(ImportHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.ordinalOrHint);
            }
            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(ImportHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.ordinalOrHint, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        IMPORT_TYPE {
            public void read(ByteBuffer source, ImportHeader dest) {
                int position = source.position();
                dest.importType = ImportType.fromOrdinal(getUnsignedShort(source) & 0x3); // keep 2 lowest bits
                source.position(position); // this field overlaps with NAME_TYPE, so we restore position
            }

            public void write(ImportHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.importType.ordinal());
            }
            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(ImportHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getEnumString(displayFormat, format.importType);
            }
            
        },
        NAME_TYPE {
            public void read(ByteBuffer source, ImportHeader dest) {
                dest.importNameType = ImportNameType.fromOrdinal((getUnsignedShort(source) >> 2) & 0x7);
            }

            public void write(ImportHeader source, ByteBuffer dest) {
                BinIOTools.combineUnsignedShortWithExisting(dest, val -> val | ((source.importNameType.ordinal() & 0x7) << 2));
            }


            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(ImportHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getEnumString(displayFormat, format.importNameType);
            }
            
        }
        
    }
    
    public static enum ImportType {
        IMPORT_CODE, IMPORT_DATA, IMPORT_CONST;
        
        public static ImportType fromOrdinal(int ordinal) {
            for (ImportType val : values()) {
                if (val.ordinal() == ordinal) {
                    return val;
                }
            }
            throw new IllegalArgumentException(String.valueOf(ordinal));
        }
    }

    public static enum ImportNameType {
        IMPORT_ORDINAL, IMPORT_NAME, IMPORT_NAME_NOPREFIX, IMPORT_NAME_UNDECORATE;
        
        public static ImportNameType fromOrdinal(int ordinal) {
            for (ImportNameType val : values()) {
                if (val.ordinal() == ordinal) {
                    return val;
                }
            }
            throw new IllegalArgumentException(String.valueOf(ordinal));
        }
        
    }
    
    
    public int sig1, sig2;
    public int version, machine;
    public long timedatestamp;
    public long sizeOfData;
    public int ordinalOrHint;
    public ImportType importType;
    public ImportNameType importNameType;

    @Override
    public Collection<? extends FieldSequenceFormat.Field> fields() {
        return Arrays.asList(Field.values());
    }

    @Override
    public boolean isDisplayFormatSupported(String fieldName, DisplayFormat format) {
        return false;
    }

    @Override
    public void setStringValue(String fieldName, DisplayFormat format) {
        
    }

    @Override
    public int getSize() {
        return SIZE;
    }

    @Override
    public Object readFieldsFrom(ByteBuffer readBuffer) {
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return super.readFieldsFrom(readBuffer);
    }

    @Override
    public String getStringValue() {
        return "ImportHeader";
    }

    public boolean isValid() {
        return (sig1 == SIG1) && (sig2 == SIG2);
    }
    
    public static boolean canBeCreatedFrom(COFFHeader coffHeader) {
        return (coffHeader.machine == SIG1) && (coffHeader.numberOfSections == SIG2);
    }
    
    public static ImportHeader from(COFFHeader coffHeader) {
        ByteBuffer bb = ByteBuffer.allocate(coffHeader.getSize());
        coffHeader.writeTo(bb);
        bb.flip();
        ImportHeader importHeader = new ImportHeader();
        importHeader.readFrom(bb);
        return importHeader;
    }
    
}
