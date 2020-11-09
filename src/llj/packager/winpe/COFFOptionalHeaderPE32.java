package llj.packager.winpe;

import llj.packager.DisplayFormat;
import llj.packager.coff.COFFOptionalHeaderStandard;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static llj.util.BinIOTools.getUnsignedInt;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedInt;
import static llj.util.BinIOTools.putUnsignedShort;

public class COFFOptionalHeaderPE32 extends COFFOptionalHeaderStandard<COFFOptionalHeaderPE32.FieldPE32> {

    public static final int SIZE = COFFOptionalHeaderStandard.SIZE + 200;
    public static final int MAGIC = 0x010b;

    public static enum FieldPE32 {

        BASE_OF_DATA {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.baseOfData = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.baseOfData);
            }

            public int size() { return DWORD; }
        },
        IMAGE_BASE {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.imageBase = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.imageBase);
            }

            public int size() { return DWORD; }
        },
        SECTION_ALIGNMENT {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.sectionAlignment = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sectionAlignment);
            }
            public int size() { return DWORD; }
        },
        FILE_ALIGNMENT {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.fileAlignment = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.fileAlignment);
            }
            public int size() { return DWORD; }
        },
        MAJOR_OS_VERSION {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.majorOperatingSystemVersion = getUnsignedShort(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedShort(dest, source.majorOperatingSystemVersion);
            }
            public int size() { return WORD; }
        },
        MINOR_OS_VERSION {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.minorOperatingSystemVersion = getUnsignedShort(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedShort(dest, source.majorOperatingSystemVersion);
            }
            public int size() { return WORD; }
        },
        MAJOR_IMAGE_VERSION {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.majorImageVersion = getUnsignedShort(source);
            }
            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedShort(dest, source.majorImageVersion);
            }

            public int size() { return WORD; }
        },
        MINOR_IMAGE_VERSION {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.minorImageVersion = getUnsignedShort(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedShort(dest, source.minorImageVersion);
            }

            public int size() { return WORD; }
        },
        MAJOR_SUBSYSTEM_VERSION {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.majorSubsystemVersion = getUnsignedShort(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedShort(dest, source.majorSubsystemVersion);
            }

            public int size() { return WORD; }
        },
        MINOR_SUBSYSTEM_VERSION {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.minorSubsystemVersion = getUnsignedShort(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedShort(dest, source.minorSubsystemVersion);
            }

            public int size() { return WORD; }
        },
        RESERVED1 {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.reserved1 = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.reserved1);
            }

            public int size() { return DWORD; }
        },
        SIZE_OF_IMAGE {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.sizeOfImage = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfImage);
            }

            public int size() { return DWORD; }
        },
        SIZE_OF_HEADERS {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.sizeOfHeaders = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfHeaders);
            }

            public int size() { return DWORD; }
        },
        CHECKSUM {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.checkSum = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.checkSum);
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
        },
        DLL_CHARACTERISTICS {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.dllCharacteristics = getUnsignedShort(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedShort(dest, source.dllCharacteristics);
            }

            public int size() { return WORD; }
        },
        SIZE_OF_STACK_RESERVE {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.sizeOfStackReserve = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfStackReserve);
            }

            public int size() { return DWORD; }
        },
        SIZE_OF_STACK_COMMIT {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.sizeOfStackCommit = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfStackCommit);
            }

            public int size() { return DWORD; }
        },
        SIZE_OF_HEAP_RESERVE {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.sizeOfHeapReserve = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfHeapReserve);
            }

            public int size() { return DWORD; }
        },
        SIZE_OF_HEAP_COMMIT {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.sizeOfHeapCommit = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfHeapCommit);
            }

            public int size() { return DWORD; }
        },
        LOADER_FLAGS {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.loaderFlags = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.loaderFlags);
            }

            public int size() { return DWORD; }
        },
        NUMBER_OF_RVA_AND_SIZES {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {
                dest.numberOfRvaAndSizes = getUnsignedInt(source);
            }

            public void write(COFFOptionalHeaderPE32 source, ByteBuffer dest) {
                putUnsignedInt(dest, source.numberOfRvaAndSizes);
            }

            public int size() { return DWORD; }
        },
        DATA_DIRECTORY {
            public void read(ByteBuffer source, COFFOptionalHeaderPE32 dest) {

                for (int i = 0; i < dest.numberOfRvaAndSizes; i++) {
                    DirectoryEntry entry = new DirectoryEntry();
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
        };

        public abstract void read(ByteBuffer source, COFFOptionalHeaderPE32 dest);

        public abstract void write(COFFOptionalHeaderPE32 source, ByteBuffer dest);

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

    public Optional<String> getStringValue(String fieldName, DisplayFormat displayFormat) {
        for (FieldPE32 field: FieldPE32.values()) {
            if (field.name().equals(fieldName)) {
                return getStringValue(field, displayFormat);
            }
        }
        return super.getStringValue(fieldName, displayFormat);
    }

    public Optional<String> getStringValue(FieldPE32 field, DisplayFormat displayFormat) {
        switch(field) {
            case BASE_OF_DATA:
                return Optional.of(String.valueOf(baseOfData));
            case IMAGE_BASE:
                return Optional.of(String.valueOf(imageBase));
            case SECTION_ALIGNMENT:
                return Optional.of(String.valueOf(sectionAlignment));
            case FILE_ALIGNMENT:
                return Optional.of(String.valueOf(fileAlignment));
            case MAJOR_OS_VERSION:
                return Optional.of(String.valueOf(majorOperatingSystemVersion));
            case MINOR_OS_VERSION:
                return Optional.of(String.valueOf(minorOperatingSystemVersion));
            case MAJOR_IMAGE_VERSION:
                return Optional.of(String.valueOf(majorImageVersion));
            case MINOR_IMAGE_VERSION:
                return Optional.of(String.valueOf(minorImageVersion));
            case MAJOR_SUBSYSTEM_VERSION:
                return Optional.of(String.valueOf(majorSubsystemVersion));
            case MINOR_SUBSYSTEM_VERSION:
                return Optional.of(String.valueOf(minorSubsystemVersion));
            case RESERVED1:
                return Optional.of(String.valueOf(reserved1));
            case SIZE_OF_IMAGE:
                return Optional.of(String.valueOf(sizeOfImage));
            case SIZE_OF_HEADERS:
                return Optional.of(String.valueOf(sizeOfHeaders));
            case CHECKSUM:
                return Optional.of(String.valueOf(checkSum));
            case SUBSYSTEM:
                return Optional.of(String.valueOf(subsystem));
            case DLL_CHARACTERISTICS:
                return Optional.of(String.valueOf(dllCharacteristics));
            case SIZE_OF_STACK_RESERVE:
                return Optional.of(String.valueOf(sizeOfStackReserve));
            case SIZE_OF_STACK_COMMIT:
                return Optional.of(String.valueOf(sizeOfStackCommit));
            case SIZE_OF_HEAP_RESERVE:
                return Optional.of(String.valueOf(sizeOfHeapReserve));
            case SIZE_OF_HEAP_COMMIT:
                return Optional.of(String.valueOf(sizeOfHeapCommit));
            case LOADER_FLAGS:
                return Optional.of(String.valueOf(loaderFlags));
            case NUMBER_OF_RVA_AND_SIZES:
                return Optional.of(String.valueOf(numberOfRvaAndSizes));
            case DATA_DIRECTORY:
                return Optional.of("Data directory");
            default:
                throw new IllegalArgumentException();
        }
    }
    

    public void writeTo(ByteBuffer writeBuffer) {
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
        for (COFFOptionalHeaderPE32.Field field: COFFOptionalHeaderPE32.Field.values()) {
            if (field.name().equals(fieldName)) {
                return field.size();
            }
        }
        return super.getSize(fieldName);
    }

    @Override
    public int getOffset(String fieldName) {
        int offset = COFFOptionalHeaderStandard.SIZE;
        for (COFFOptionalHeaderPE32.Field field: COFFOptionalHeaderPE32.Field.values()) {
            if (field.name().equals(fieldName)) {
                return offset;
            } else {
                offset += field.size();
            }
        }
        return super.getOffset(fieldName);
    }
}
