package llj.packager.winpe;

import llj.packager.DisplayFormat;
import llj.packager.FieldSequenceFormat;
import llj.packager.coff.COFFOptionalHeaderStandard;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static llj.util.BinIOTools.getUnsignedInt;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedInt;
import static llj.util.BinIOTools.putUnsignedShort;

public class COFFOptionalHeaderPE32 extends COFFOptionalHeaderStandard<COFFOptionalHeaderPE32.FieldPE32> {

    public static final int SIZE = COFFOptionalHeaderStandard.SIZE + 200;
    public static final int MAGIC = 0x010b;

    public static enum FieldPE32 implements FieldSequenceFormat.Field<COFFOptionalHeaderPE32> {

        BASE_OF_DATA {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.baseOfData = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.baseOfData);
            }

            public int size() { return DWORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.baseOfData, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        IMAGE_BASE {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.imageBase = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.imageBase);
            }

            public int size() { return DWORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.imageBase, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        SECTION_ALIGNMENT {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.sectionAlignment = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sectionAlignment);
            }
            public int size() { return DWORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.sectionAlignment, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        FILE_ALIGNMENT {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.fileAlignment = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.fileAlignment);
            }
            public int size() { return DWORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.fileAlignment, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        MAJOR_OS_VERSION {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.majorOperatingSystemVersion = getUnsignedShort(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedShort(dest, source.majorOperatingSystemVersion);
            }
            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.majorOperatingSystemVersion, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        MINOR_OS_VERSION {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.minorOperatingSystemVersion = getUnsignedShort(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedShort(dest, source.minorOperatingSystemVersion);
            }
            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.minorOperatingSystemVersion, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        MAJOR_IMAGE_VERSION {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.majorImageVersion = getUnsignedShort(source);
            }
            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedShort(dest, source.majorImageVersion);
            }

            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.majorImageVersion, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        MINOR_IMAGE_VERSION {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.minorImageVersion = getUnsignedShort(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedShort(dest, source.minorImageVersion);
            }

            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.minorImageVersion, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        MAJOR_SUBSYSTEM_VERSION {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.majorSubsystemVersion = getUnsignedShort(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedShort(dest, source.majorSubsystemVersion);
            }

            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.majorSubsystemVersion, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        MINOR_SUBSYSTEM_VERSION {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.minorSubsystemVersion = getUnsignedShort(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedShort(dest, source.minorSubsystemVersion);
            }

            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.minorSubsystemVersion, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        RESERVED1 {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.reserved1 = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.reserved1);
            }

            public int size() { return DWORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.reserved1, size(), ByteOrder.LITTLE_ENDIAN);
            }
                        
        },
        SIZE_OF_IMAGE {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.sizeOfImage = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfImage);
            }

            public int size() { return DWORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.sizeOfImage, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        SIZE_OF_HEADERS {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.sizeOfHeaders = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfHeaders);
            }

            public int size() { return DWORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.sizeOfHeaders, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        CHECKSUM {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.checkSum = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.checkSum);
            }

            @Override
            public int size() { 
                return DWORD;
            }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.checkSum, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        SUBSYSTEM {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.subsystem = getUnsignedShort(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.subsystem);
            }

            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.subsystem, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        DLL_CHARACTERISTICS {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.dllCharacteristics = getUnsignedShort(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedShort(dest, source.dllCharacteristics);
            }

            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.subsystem, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        SIZE_OF_STACK_RESERVE {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.sizeOfStackReserve = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfStackReserve);
            }

            public int size() { return DWORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.sizeOfStackReserve, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        SIZE_OF_STACK_COMMIT {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.sizeOfStackCommit = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfStackCommit);
            }

            public int size() { return DWORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.sizeOfStackCommit, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        SIZE_OF_HEAP_RESERVE {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.sizeOfHeapReserve = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfHeapReserve);
            }

            public int size() { return DWORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.sizeOfHeapReserve, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        SIZE_OF_HEAP_COMMIT {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.sizeOfHeapCommit = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfHeapCommit);
            }

            public int size() { return DWORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.sizeOfHeapCommit, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        LOADER_FLAGS {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.loaderFlags = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.loaderFlags);
            }

            public int size() { return DWORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.loaderFlags, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        NUMBER_OF_RVA_AND_SIZES {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.numberOfRvaAndSizes = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.numberOfRvaAndSizes);
            }

            public int size() { return DWORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.numberOfRvaAndSizes, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        DATA_DIRECTORY {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {

                for (int i = 0; i < dest.numberOfRvaAndSizes; i++) {
                    DirectoryEntry entry = new DirectoryEntry();
                    entry.name = PEFormat.directoryNameByIndex(i);
                    entry.readFrom(source);
                    dest.dataDirectory.add(entry);
                }
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                for (DirectoryEntry directory : source.dataDirectory) {
                    directory.writeTo(dest);
                }
            }

            public int size() { return 0; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat) {
                return Optional.of("Data directory");
            }
        };
        
        public abstract int size();

        public abstract void read(ByteBuffer source, COFFOptionalHeaderPE32 dest);

        public abstract void write(COFFOptionalHeaderPE32 source, ByteBuffer dest);

        public abstract Optional<String> getStringValue(COFFOptionalHeaderPE32 format, DisplayFormat displayFormat);

    }

    private static final int DIRECTORY_ENTRY_EXPORTS = 0, DIRECTORY_ENTRY_IMPORTS = 1;


    long baseOfData;

    long imageBase;
    long sectionAlignment;
    long fileAlignment;
    int majorOperatingSystemVersion;
    int minorOperatingSystemVersion;
    int majorImageVersion;
    int minorImageVersion;
    int majorSubsystemVersion;
    int minorSubsystemVersion;
    long reserved1;
    long sizeOfImage;
    long sizeOfHeaders;
    long checkSum;
    int subsystem;
    int dllCharacteristics;
    long sizeOfStackReserve;
    long sizeOfStackCommit;
    long sizeOfHeapReserve;
    long sizeOfHeapCommit;
    long loaderFlags;
    long numberOfRvaAndSizes;
    final List<DirectoryEntry> dataDirectory = new ArrayList<>();

    public COFFOptionalHeaderPE32() {
    }

    public static boolean canExtend(COFFOptionalHeaderStandard<Object> base) {
        return base.signature == MAGIC;
    }
    
    public static COFFOptionalHeaderPE32 extend(COFFOptionalHeaderStandard<Object> base) {
        
        if (!canExtend(base)) {
            throw new IllegalArgumentException();
        }

        COFFOptionalHeaderPE32 coffOptionalHeaderPE32 = new COFFOptionalHeaderPE32();

        coffOptionalHeaderPE32.signature = base.signature;
        coffOptionalHeaderPE32.majorLinkerVersion = base.majorLinkerVersion;
        coffOptionalHeaderPE32.minorLinkerVersion = base.minorLinkerVersion;
        coffOptionalHeaderPE32.sizeOfCode = base.sizeOfCode;
        coffOptionalHeaderPE32.sizeOfInitializedData = base.sizeOfInitializedData;
        coffOptionalHeaderPE32.sizeOfUninitializedData = base.sizeOfUninitializedData;
        coffOptionalHeaderPE32.addressOfEntryPoint = base.addressOfEntryPoint;
        coffOptionalHeaderPE32.baseOfCode = base.baseOfCode;

        ByteBuffer rawExtension = ByteBuffer.allocate(base.extensions.getSize());
        base.extensions.writeTo(rawExtension);
        rawExtension.flip();
        rawExtension.order(ByteOrder.LITTLE_ENDIAN);
        Location extensionLocation = coffOptionalHeaderPE32.readExtensionsFrom(rawExtension, true);
        if (extensionLocation != null) {
            throw new RuntimeException();
        }
        
        return coffOptionalHeaderPE32;
    }

    public Collection<FieldPE32> getExtensionFields() {
        return Arrays.asList(FieldPE32.values());
    }

    public Location readFrom(ReadableByteChannel in, ByteBuffer readBuffer) throws IOException {
        return readFrom(in, readBuffer, SIZE);
    }

    @Override
    public Location readExtensionsFrom(ByteBuffer readBuffer, boolean canTakeBuffer) {
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);
        for (FieldPE32 field : FieldPE32.values()) {
            try {
                field.read(readBuffer, this);
            } catch (BufferUnderflowException e) {
                return Location.atExtensionField(field);
            }
        }
        return null;
    }

    @Override
    public Optional<String> getStringValue(String fieldName, DisplayFormat displayFormat) {
        for (FieldPE32 field: FieldPE32.values()) {
            if (field.name().equals(fieldName)) {
                return getStringValue(field, displayFormat);
            }
        }

        if (fieldName.equals(Field.SIGNATURE.name())) {
            return getStringValue(Field.SIGNATURE, displayFormat);
        } else {
            return super.getStringValue(fieldName, displayFormat);
        }
    }

    @Override
    public Optional<String> getStringValue(Field field, DisplayFormat displayFormat) {
        if (field == Field.SIGNATURE) {
            if (displayFormat == DisplayFormat.DEFAULT) {
                if (signature == MAGIC) {
                    return Optional.of("PE32");
                } else {
                    return Optional.of(String.valueOf(signature) + " (doesn't match PE32 format)");
                }
            } else {
                return super.getStringValue(field, displayFormat);
            }
        } else {
            return super.getStringValue(field, displayFormat);
        }

    }

    @Override
    public Optional<String> getStringValue(FieldSequenceFormat.Field field, DisplayFormat displayFormat) {
        if (field instanceof FieldPE32) {
            return getStringValue((FieldPE32)field, displayFormat);
        } else {
            return super.getStringValue(field, displayFormat);
        }
    }

    public Optional<String> getStringValue(FieldPE32 field, DisplayFormat displayFormat) {
        return field.getStringValue(this, displayFormat);
    }

    @Override
    public void writeExtensionsTo(ByteBuffer writeBuffer) {
        writeBuffer.order(ByteOrder.LITTLE_ENDIAN);
        for (FieldPE32 field : FieldPE32.values()) {
            field.write(this, writeBuffer);
        }
    }

    @Override
    public List<String> getExtensionNames() {
        List<String> result = new ArrayList<String>();
        for (FieldPE32 field: FieldPE32.values()) {
            result.add(field.name());
        }
        return result;
    }

    @Override
    public int getSize() {
        return SIZE;
    }

    @Override
    public String getStringValue() {
        return "COFFOptionalHeaderPE";
    }

    @Override
    public int getSize(String fieldName) {
        for (COFFOptionalHeaderPE32.FieldPE32 field: COFFOptionalHeaderPE32.FieldPE32.values()) {
            if (field.name().equals(fieldName)) {
                return field.size();
            }
        }
        return super.getSize(fieldName);
    }

    @Override
    public int getOffset(String fieldName) {
        int offset = COFFOptionalHeaderStandard.SIZE;
        for (COFFOptionalHeaderPE32.FieldPE32 field: COFFOptionalHeaderPE32.FieldPE32.values()) {
            if (field.name().equals(fieldName)) {
                return offset;
            } else {
                offset += field.size();
            }
        }
        return super.getOffset(fieldName);
    }

    @Override
    public void setStringValue(String fieldName, DisplayFormat format) {
        throw new UnsupportedOperationException();
    }
}
