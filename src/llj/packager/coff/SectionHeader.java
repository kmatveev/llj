package llj.packager.coff;

import llj.packager.DisplayFormat;
import llj.packager.IntrospectableFormat;
import llj.packager.winpe.PEFormat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static llj.util.BinIOTools.getUnsignedChar;
import static llj.util.BinIOTools.getUnsignedInt;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedChar;
import static llj.util.BinIOTools.putUnsignedInt;
import static llj.util.BinIOTools.putUnsignedShort;
import static llj.util.BinIOTools.readIntoBuffer;

public class SectionHeader implements IntrospectableFormat {

    public static final int SIZE = 40;

    public static enum Field {
        NAME {
            public void read(ByteBuffer source, SectionHeader dest) {
                dest.name.read(source);
            }

            public void write(SectionHeader source, ByteBuffer dest) {
                source.name.write(dest);
            }
        },
        PHYSICAL_ADDRESS_OR_VIRTUAL_SIZE {
            public void read(ByteBuffer source, SectionHeader dest) {
                dest.physicalAddressOrVirtualSize = getUnsignedInt(source);
            }

            public void write(SectionHeader source, ByteBuffer dest) {
                putUnsignedInt(dest, source.physicalAddressOrVirtualSize);
            }
        },
        VIRTUAL_ADDRESS {
            public void read(ByteBuffer source, SectionHeader dest) {
                dest.virtualAddress = getUnsignedInt(source);
            }

            public void write(SectionHeader source, ByteBuffer dest) {
                putUnsignedInt(dest, source.virtualAddress);
            }
        },
        SIZE_OF_RAW_DATA {
            public void read(ByteBuffer source, SectionHeader dest) {
                dest.sizeOfRawData = getUnsignedInt(source);
            }

            public void write(SectionHeader source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfRawData);
            }
        },
        POINTER_TO_RAW_DATA {
            public void read(ByteBuffer source, SectionHeader dest) {
                dest.pointerToRawData = getUnsignedInt(source);
            }

            public void write(SectionHeader source, ByteBuffer dest) {
                putUnsignedInt(dest, source.pointerToRawData);
            }
        },
        POINTER_TO_RELOCATIONS {
            public void read(ByteBuffer source, SectionHeader dest) {
                dest.pointerToRelocations = getUnsignedInt(source);
            }

            public void write(SectionHeader source, ByteBuffer dest) {
                putUnsignedInt(dest, source.pointerToRelocations);
            }
        },
        POINTER_TO_LINE_NUMBERS {
            public void read(ByteBuffer source, SectionHeader dest) {
                dest.pointerToLinenumbers = getUnsignedInt(source);
            }

            public void write(SectionHeader source, ByteBuffer dest) {
                putUnsignedInt(dest, source.pointerToLinenumbers);
            }
        },
        NUMBER_OF_RELOCATIONS {
            public void read(ByteBuffer source, SectionHeader dest) {
                dest.numberOfRelocations = getUnsignedShort(source);
            }

            public void write(SectionHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.numberOfRelocations);
            }
        },
        NUMBER_OF_LINE_NUMBERS {
            public void read(ByteBuffer source, SectionHeader dest) {
                dest.numberOfLinenumbers = getUnsignedShort(source);
            }

            public void write(SectionHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.numberOfLinenumbers);
            }
        },
        CHARACTERISTICS {
            public void read(ByteBuffer source, SectionHeader dest) {
                dest.characteristics = getUnsignedInt(source);
            }

            public void write(SectionHeader source, ByteBuffer dest) {
                putUnsignedInt(dest, source.characteristics);
            }
        };

        public abstract void read(ByteBuffer source, SectionHeader dest);

        public abstract void write(SectionHeader source, ByteBuffer dest);

    }

    public static enum CharacteristicsField {
        IMAGE_SCN_CNT_CODE(5), // section contains executable code.
        IMAGE_SCN_CNT_INITIALIZED_DATA(6), // section contains data that gets a defined value before execution starts. In other words: the section's data in the file is meaningful.
        IMAGE_SCN_CNT_UNINITIALIZED_DATA(7), // section contains uninitialized data and will be initialized to all-0-bytes before execution starts. This is normally the BSS.
        IMAGE_SCN_LNK_INFO(9), // section doesn't contain image data but comments, description or other documentation. This information is part of an object file and may be information for the linker, such as which libraries are needed.
        IMAGE_SCN_LNK_REMOVE(11),  // the data is part of an object file's section that is supposed to be left out when the executable file is linked. Often combined with bit 9.
        IMAGE_SCN_LNK_COMDAT(12), // section contains "common block data", which are packaged functions of some sort.
        IMAGE_SCN_MEM_FARDATA(15), // we have far data - whatever that means. This bit's meaning is unsure.
        IMAGE_SCN_MEM_PURGEABLE(17), // the section's data is purgeable - but I don't think that this is the same as "discardable", which has a bit of its own, see below.
        // The same bit is apparently used to indicate 16-bit-information as there is also a define IMAGE_SCN_MEM_16BIT for it. This bit's meaning is unsure.
        IMAGE_SCN_MEM_LOCKED(18), // the section should not be moved in memory? Perhaps it indicates there is no relocation information? This bit's meaning is unsure.
        IMAGE_SCN_MEM_PRELOAD(19), // the section should be paged in before execution starts? This bit's meaning is unsure.

//            Bits 20 to 23 specify an alignment that I have no information
//            about. There are #defines IMAGE_SCN_ALIGN_16BYTES and the like. The
//            only value I've ever seen used is 0, for the default 16-byte-
//            alignment. I suspect that this is the alignment of objects in a
//            library file or the like.

        IMAGE_SCN_LNK_NRELOC_OVFL(24), // the section contains some extended relocations that I don't know about.
        IMAGE_SCN_MEM_DISCARDABLE(25), // the section's data is not needed after the process has started. This is the case, for example, with the relocation information.
        // I've seen it also for startup routines of drivers and services that are only executed once, and for import directories.
        IMAGE_SCN_MEM_NOT_CACHED(26), // the section's data should not be cached. Don't ask my why not. Does this mean to switch off the 2nd-level-cache?
        IMAGE_SCN_MEM_NOT_PAGED(27),  // the section's data should not be paged out. This is interesting for drivers.
        IMAGE_SCN_MEM_SHARED(28), // the section's data is shared among all running instances of the image.
        // If it is e.g. the initialized data of a DLL, all running instances of the DLL will at any time have the same variable contents.
        // Note that only the first instance's section is initialized. Sections containing code are always shared copy-on-write (i.e. the sharing doesn't work if relocations are necessary).
        IMAGE_SCN_MEM_EXECUTE(29), // the process gets 'execute'-access to the section's memory.
        IMAGE_SCN_MEM_READ(30), // the process gets 'read'-access to the section's memory.
        IMAGE_SCN_MEM_WRITE(31); // the process gets 'write'-access to the section's memory.

        private final int bit;

        CharacteristicsField(int bit) {
            this.bit = bit;
        }

        public boolean isSetInValue(long val) {
            return (val & PEFormat.setBit(bit)) > 0;
        }

        public long setInValue(long val) {
            return val | PEFormat.setBit(bit);
        }

        public long resetInValue(long val) {
            return val & (~PEFormat.setBit(bit));
        }

        public static List<CharacteristicsField> getAllSetInValue(long value) {
            List<CharacteristicsField> result = new ArrayList<>();
            for (CharacteristicsField field : values()) {
                if (field.isSetInValue(value)) {
                    result.add(field);
                }
            }
            return result;
        }

        public static long composeFrom(List<CharacteristicsField> fields) {
            long result = 0;
            for (CharacteristicsField field : fields) {
                result = field.setInValue(result);
            }
            return result;
        }

    }

    public final NameOrStringTablePointer name = new NameOrStringTablePointer();
    public long physicalAddressOrVirtualSize;
    public long virtualAddress;
    public long sizeOfRawData;
    public long pointerToRawData;
    public long pointerToRelocations;
    public long pointerToLinenumbers;
    public int numberOfRelocations;
    public int numberOfLinenumbers;
    public long characteristics;

    public void readFrom(ReadableByteChannel in, ByteBuffer readBuffer) throws IOException {
        readIntoBuffer(in, readBuffer, SIZE);
        readFrom(readBuffer);
    }

    public void readFrom(ByteBuffer readBuffer) {
        for (Field field : Field.values()) {
            field.read(readBuffer, this);
        }
    }

    public void writeTo(ByteBuffer writeBuffer) {
        for (Field field : Field.values()) {
            field.write(this, writeBuffer);
        }
    }

    @Override
    public List<String> getNames() {
        List<String> result = new ArrayList<>();
        for (Field field : Field.values()) {
            result.add(field.name());
        }
        return result;
    }

    @Override
    public Optional<String> getStringValue(String fieldName, DisplayFormat displayFormat) {
        List<String> result = new ArrayList<>();
        for (Field field : Field.values()) {
            if (field.name().equals(fieldName)) {
                return getStringValue(field, displayFormat);
            }
        }
        throw new IllegalArgumentException(fieldName);
    }

    public Optional<String> getStringValue(Field field, DisplayFormat displayFormat) {
        // all fields in section header are mandatory/fixed
        switch (field) {
            case NAME: return Optional.of(new String(name.name));
            case PHYSICAL_ADDRESS_OR_VIRTUAL_SIZE: return Optional.of(String.valueOf(physicalAddressOrVirtualSize));
            case VIRTUAL_ADDRESS: return Optional.of(String.valueOf(virtualAddress));
            case SIZE_OF_RAW_DATA: return Optional.of(String.valueOf(sizeOfRawData));
            case POINTER_TO_RAW_DATA: return Optional.of(String.valueOf(pointerToRawData));
            case POINTER_TO_RELOCATIONS: return Optional.of(String.valueOf(pointerToRelocations));
            case POINTER_TO_LINE_NUMBERS: return Optional.of(String.valueOf(pointerToLinenumbers));
            case NUMBER_OF_RELOCATIONS: return Optional.of(String.valueOf(numberOfRelocations));
            case NUMBER_OF_LINE_NUMBERS: return Optional.of(String.valueOf(numberOfLinenumbers));
            case CHARACTERISTICS: return Optional.of(String.valueOf(characteristics));
            default: throw new IllegalArgumentException(field.name());
        }
    }
    

    @Override
    public int getSize() {
        return SIZE;
    }

    @Override
    public String getStringValue() {
        return "";
    }
}
