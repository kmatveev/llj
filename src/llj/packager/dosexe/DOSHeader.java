package llj.packager.dosexe;

import llj.packager.DisplayFormat;
import llj.packager.FieldSequenceFormat;
import llj.packager.Format;
import llj.packager.IntrospectableFormat;
import llj.packager.RawFormat;
import llj.util.BinIOTools;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static llj.packager.dosexe.DOSHeader.Field.CHECKSUM;
import static llj.packager.dosexe.DOSHeader.Field.CS;
import static llj.packager.dosexe.DOSHeader.Field.HEADER_SIZE;
import static llj.packager.dosexe.DOSHeader.Field.IP;
import static llj.packager.dosexe.DOSHeader.Field.LAST_SIZE;
import static llj.packager.dosexe.DOSHeader.Field.MAX_ALLOC;
import static llj.packager.dosexe.DOSHeader.Field.MIN_ALLOC;
import static llj.packager.dosexe.DOSHeader.Field.NBLOCKS;
import static llj.packager.dosexe.DOSHeader.Field.NOVERLAY;
import static llj.packager.dosexe.DOSHeader.Field.NRELOCS;
import static llj.packager.dosexe.DOSHeader.Field.RELOC_POS;
import static llj.packager.dosexe.DOSHeader.Field.SP;
import static llj.packager.dosexe.DOSHeader.Field.SS;
import static llj.util.BinIOTools.getUnsignedInt;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedInt;
import static llj.util.BinIOTools.putUnsignedShort;
import static llj.util.BinIOTools.readIntoBuffer;

/**
 * Consists of 4 parts, in order:
 * 1. A set of standard fields
 * 2. Extension area, from end of standard header are to start of relocations
 * 3. Relocations. This area is pointed to by relocs_pos field. Size of area is calculated from value of nrelocs field
 * 4. Free space, from end of relocations till end of DOS header, which is defined by header_size field
 *
 */
public class DOSHeader<T> extends FieldSequenceFormat {

    public static final int FIXED_HEADER_SIZE = 28;
    public static final int EXTENSION_EXPECTED_SIZE = 100; // just a number of bytes which we expect extension will occupy.
    public static final int RELOCATIONS_EXPECTED_SIZE = 100; // just a number of bytes which we expect extension will occupy.
    
    public static final byte[] MAGIC = new byte[] {0x4D, 0x5A};
    
    public static final String RELOCATIONS = "RELOCATIONS";
    public static final String EXTENSION = "EXTENSION";

    @Override
    public String getStringValue() {
        return "DOSHeader";
    }

    public static enum Field implements FieldSequenceFormat.Field<DOSHeader> {
        SIGNATURE {
            public int size() {
                return WORD;
            }

            public void read(ByteBuffer source, DOSHeader dest) {
                dest.signature = new byte[2]; source.get(dest.signature);
            }

            public void write(DOSHeader source, ByteBuffer dest) {
                dest.put(source.signature);
            }

            @Override
            public Optional<String> getStringValue(DOSHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getBytesString(displayFormat, format.signature);
            }
        },
        LAST_SIZE {

            public int size() {
                return WORD;
            }

            public void read(ByteBuffer source, DOSHeader dest) {
                dest.lastSize = getUnsignedShort(source);
            }

            public void write(DOSHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.lastSize);
            }

            @Override
            public Optional<String> getStringValue(DOSHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.lastSize, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        NBLOCKS {

            public int size() {
                return WORD;
            }

            public void read(ByteBuffer source, DOSHeader dest) {
                dest.nBlocks = getUnsignedShort(source);
            }

            public void write(DOSHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.nBlocks);
            }

            @Override
            public Optional<String> getStringValue(DOSHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.nBlocks, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        NRELOCS {

            public int size() {
                return WORD;
            }

            public void read(ByteBuffer source, DOSHeader dest) {
                dest.nRelocs = getUnsignedShort(source);
            }

            public void write(DOSHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.nRelocs);
            }

            @Override
            public Optional<String> getStringValue(DOSHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.nRelocs, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        HEADER_SIZE {

            public int size() {
                return WORD;
            }

            public void read(ByteBuffer source, DOSHeader dest) {
                dest.headerSize = getUnsignedShort(source);
            }

            public void write(DOSHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.headerSize);
            }

            @Override
            public Optional<String> getStringValue(DOSHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.headerSize, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        MIN_ALLOC {

            public int size() {
                return WORD;
            }

            public void read(ByteBuffer source, DOSHeader dest) {
                dest.minalloc = getUnsignedShort(source);
            }

            public void write(DOSHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.minalloc);
            }

            @Override
            public Optional<String> getStringValue(DOSHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.minalloc, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        MAX_ALLOC {

            public int size() {
                return WORD;
            }

            public void read(ByteBuffer source, DOSHeader dest) {
                dest.maxalloc = getUnsignedShort(source);
            }

            public void write(DOSHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.maxalloc);
            }

            @Override
            public Optional<String> getStringValue(DOSHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.maxalloc, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        SS {

            public int size() {
                return WORD;
            }

            public void read(ByteBuffer source, DOSHeader dest) {
                dest.ss = getUnsignedShort(source);
            }

            public void write(DOSHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.ss);
            }

            @Override
            public Optional<String> getStringValue(DOSHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.ss, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        SP {

            public int size() {
                return WORD;
            }

            public void read(ByteBuffer source, DOSHeader dest) {
                dest.sp = getUnsignedShort(source);
            }

            public void write(DOSHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.sp);
            }

            @Override
            public Optional<String> getStringValue(DOSHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.sp, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        CHECKSUM {

            public int size() {
                return WORD;
            }

            public void read(ByteBuffer source, DOSHeader dest) {
                dest.checksum = getUnsignedShort(source);
            }

            public void write(DOSHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.checksum);
            }

            @Override
            public Optional<String> getStringValue(DOSHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.checksum, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        IP {

            public int size() {
                return WORD;
            }

            public void read(ByteBuffer source, DOSHeader dest) {
                dest.ip = getUnsignedShort(source);
            }

            public void write(DOSHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.ip);
            }

            @Override
            public Optional<String> getStringValue(DOSHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.ip, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        CS {

            public int size() {
                return WORD;
            }

            public void read(ByteBuffer source, DOSHeader dest) {
                dest.cs = getUnsignedShort(source);
            }

            public void write(DOSHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.cs);
            }

            @Override
            public Optional<String> getStringValue(DOSHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.cs, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        RELOC_POS {

            public int size() {
                return WORD;
            }

            public void read(ByteBuffer source, DOSHeader dest) {
                dest.relocPos = getUnsignedShort(source);
            }

            public void write(DOSHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.relocPos);
            }

            @Override
            public Optional<String> getStringValue(DOSHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.relocPos, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        NOVERLAY {

            public int size() {
                return WORD;
            }

            public void read(ByteBuffer source, DOSHeader dest) {
                dest.nOverlay = getUnsignedShort(source);
            }

            public void write(DOSHeader source, ByteBuffer dest) {
                putUnsignedShort(dest, source.nOverlay);
            }

            @Override
            public Optional<String> getStringValue(DOSHeader format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.nOverlay, size(), ByteOrder.LITTLE_ENDIAN);
            }
        };

        public abstract int size();

        public abstract void read(ByteBuffer source, DOSHeader dest);

        public abstract void write(DOSHeader source, ByteBuffer dest);

    }

    public static class Location<T> {

        public static enum LocationType {
            FIELD,EXTENSION,RELOCATIONS;
        }

        public final LocationType type;
        public final Field field;
        public final T extensionLocation;

        private Location(LocationType type, Field field, T extensionLocation) {
            this.type = type;
            this.field = field;
            this.extensionLocation = extensionLocation;
        }

        public static Location atField(Field f) {
            return new Location(LocationType.FIELD, f, null);
        }

        public static <T> Location atExtension(T el) {
            return new Location(LocationType.EXTENSION, null, el);
        }
    }


    // paragraph is 16 bytes

    // These fields are common for all DOS headers
    public byte[] signature;      // signature number
    public int lastSize;          // how many bytes are used in last block of file
    public int nBlocks;           // number of blocks (of 512 bytes) in file
    public int nRelocs;           // number of relocations
    public int headerSize;        // Size of header+relocations in paragraphs
    public int minalloc;          // Minimum extra paragraphs needed
    public int maxalloc;          // Maximum extra paragraphs needed
    public int ss;                // Initial (relative) SS value
    public int sp;                // Initial SP value
    public int checksum;          // Checksum
    public int ip;                // Initial IP value
    public int cs;                // Initial (relative) CS value
    public int relocPos;          // File address of relocation table. It should be 64 for PE files
    public int nOverlay;          // Overlay number    public int minalloc;          // Minimum extra paragraphs needed

    public Format headerExtension;
    public long[] relocations;

    public int freeSpace;

    @Override
    public List<String> getNames() {
        List<String> result = super.getNames();
        result.addAll(getExtensionNames());
        result.add(RELOCATIONS);
        return result;
    }

    @Override
    public Collection<Field> fields() {
        return Arrays.asList(Field.values());
    }

    public List<String> getExtensionNames() {
        return Collections.singletonList(EXTENSION);
    }

    @Override
    public Optional<String> getStringValue(String fieldName, DisplayFormat displayFormat) {
        if (fieldName.equals(RELOCATIONS)) {
            return Optional.of(Arrays.toString(relocations));
        } else if (fieldName.equals(EXTENSION)) {
            return Optional.of(headerExtension.getStringValue());
        } else {
            return super.getStringValue(fieldName, displayFormat);
        }
    }

    public Optional<String> getStringValue(Field field, DisplayFormat displayFormat) {
        return field.getStringValue(this, displayFormat);
    }

    @Override
    public int getSize(String fieldName) {
        if (fieldName.equals(RELOCATIONS)) {
            return getRelocationsSize();
        } else if (fieldName.equals(EXTENSION)) {
            return getHeaderExtensionsSize();
        } else {
            return super.getSize(fieldName);
        }
    }

    @Override
    public int getOffset(String fieldName) {
        int offset = 0;
        for (Field field: Field.values()) {
            if (field.name().equals(fieldName)) {
                return offset;
            } else {
                offset += field.size();
            }
        }
        if (fieldName.equals(EXTENSION)) {
            return offset;
        } else {
            offset += getHeaderExtensionsSize();
            if (fieldName.equals(RELOCATIONS)) {
                return offset;
            } else {
                throw new IllegalArgumentException(fieldName);
            }
        }
    }

    public static int getReadBufferSize() {
        return Collections.max(Arrays.asList( FIXED_HEADER_SIZE, EXTENSION_EXPECTED_SIZE, RELOCATIONS_EXPECTED_SIZE));
    }

    public Location readFrom(ReadableByteChannel in, ByteBuffer readBuffer) throws IOException {
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);
        readIntoBuffer(in, readBuffer, FIXED_HEADER_SIZE);  // we expect that readBuffer will not be less than FIXED_HEADER_SIZE, otherwise RuntimeException will be thrown
        Field field = readFieldsFrom(readBuffer);
        if (field != null) return Location.atField(field);
        Location extLoc = readHeaderExtensionsFrom(in, readBuffer);
        if (extLoc != null) {
            return extLoc;
        }
        readRelocationsFrom(in, readBuffer);
        int remains = getDeclaredHeaderSize() - getSize();
        freeSpace = remains;
        return null;
    }

    public int getDeclaredHeaderSize() {
        return headerSize*16;
    }

    /**
     * Tries to read fields sequentially, and returns Field which it was unable to read, or null if all Fields have been successfully read
     *
     * @param readBuffer
     * @return last Field which was not read, null if all Fields have been read
     */
    public Location readFrom(ByteBuffer readBuffer) {
        int pos = readBuffer.position();
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);
        Field field = readFieldsFrom(readBuffer);
        if (field != null) return Location.atField(field);
        Location extLoc = readHeaderExtensionsFrom(readBuffer);
        if (extLoc != null) {
            return extLoc;
        }
        readRelocationsFrom(readBuffer);
        int remains = getDeclaredHeaderSize() - (readBuffer.position() - pos);
        for (int i = 0; i < remains; i++) {
            readBuffer.get();
        }
        freeSpace = remains;
        return null;
    }

    public Field readFieldsAbsolute(ByteBuffer readBuffer) {
        readBuffer.position(0);
        return readFieldsFrom(readBuffer);
    }

    public Field readFieldsFrom(ByteBuffer readBuffer) {
        for (Field field : Field.values()) {
            try {
                field.read(readBuffer, this);
            } catch (BufferUnderflowException e) {
                return field;
            }
        }
        return null;
    }

    public Location readHeaderExtensionsFrom(ReadableByteChannel readChannel, ByteBuffer readBuffer) throws IOException {
        int headerExtensionSize = relocPos - FIXED_HEADER_SIZE;
        if (headerExtensionSize < 0) {
            return Location.atExtension(null);
        }
        // since a size of atExtensionField is not exactly known when readBuffer was allocated, 
        // so we use readBuffer only if it has enough capacity, and we allocate a temp buffer otherwise
        ByteBuffer extensionRawData = readBuffer.capacity() < headerExtensionSize ? ByteBuffer.allocate(headerExtensionSize) : readBuffer ;
        if (headerExtensionSize > 0) {
            BinIOTools.readIntoBuffer(readChannel, extensionRawData, headerExtensionSize);
        }
        return createHeaderExtensionsFrom(extensionRawData, extensionRawData != readBuffer);
    }

    protected Location createHeaderExtensionsFrom(ByteBuffer readBuffer, boolean canTakeBuffer) {
        int extensionSize = readBuffer.remaining();
        ByteBuffer extensionRawData;
        if (canTakeBuffer) {
            extensionRawData = readBuffer;
        } else {
            extensionRawData = ByteBuffer.allocate(extensionSize);
            if (extensionSize > 0) {
                extensionRawData.put(readBuffer);
                extensionRawData.flip();
            }
        }
        extensionRawData.order(ByteOrder.LITTLE_ENDIAN);
        headerExtension = new RawFormat(extensionRawData);
        return null;
    }

    public Location readHeaderExtensionAbsolute(ByteBuffer readBuffer) {
        readBuffer.position(FIXED_HEADER_SIZE);
        return readHeaderExtensionsFrom(readBuffer);
    }

    public Location readHeaderExtensionsFrom(ByteBuffer readBuffer) {
        int headerExtensionSize = relocPos - FIXED_HEADER_SIZE;
        if (headerExtensionSize < 0) {
            return Location.atExtension(null);
        }

        int lim = readBuffer.limit();
        readBuffer.limit(readBuffer.position() + headerExtensionSize);
        createHeaderExtensionsFrom(readBuffer, false);
        readBuffer.limit(lim);
        
        return null;
    }

    public void writeHeaderExtensionsTo(ByteBuffer writeBuffer) {
        headerExtension.writeTo(writeBuffer);
    }

    public void readRelocationsAbsolute(ByteBuffer readBuffer) {
        readBuffer.position(relocPos);
        readRelocationsFrom(readBuffer);
    }

    public void readRelocationsFrom(ReadableByteChannel readChannel, ByteBuffer readBuffer) throws IOException {
        if (nRelocs > 0) {
            int relocaionsRawSize = nRelocs * 4;
            ByteBuffer relocationRawData = readBuffer.capacity() < relocaionsRawSize ? ByteBuffer.allocate(relocaionsRawSize) : readBuffer ;
            BinIOTools.readIntoBuffer(readChannel, relocationRawData, relocaionsRawSize);
            relocationRawData.flip();
            readRelocationsFrom(relocationRawData);
        } else {
            relocations = new long[0];
        }
    }

    public void readRelocationsFrom(ByteBuffer readBuffer) {
        relocations = new long[nRelocs];
        for(int i = 0; i < nRelocs; i++) {
            relocations[i] = BinIOTools.getUnsignedInt(readBuffer);
        }
    }

    public void writeRelocationsTo(ByteBuffer readBuffer) {
        for(int i = 0; i < relocations.length; i++) {
            BinIOTools.putUnsignedInt(readBuffer, relocations[i]);
        }
    }

    public void writeTo(ByteBuffer writeBuffer) {
        int pos = writeBuffer.position();
        for (Field field : Field.values()) {
            field.write(this, writeBuffer);
        }
        writeHeaderExtensionsTo(writeBuffer);
        writeRelocationsTo(writeBuffer);
        int remains = getFreeSpaceSize();
        for (int i = 0; i < remains; i++) {
            writeBuffer.put((byte)0);
        }
    }
    
    public void writeFieldsTo(ByteBuffer writeBuffer, Iterator<Field> fields) {
        while (fields.hasNext()) {
            Field field = fields.next();
            field.write(this, writeBuffer);
        }
    }

    @Override
    public int getSize() {
        return FIXED_HEADER_SIZE + getHeaderExtensionsSize() + getRelocationsSize() + getFreeSpaceSize();
    }

    public int getRelocationsSize() {
        return relocations.length*4;
    }

    public int getHeaderExtensionsSize() {
        return headerExtension.getSize();
    }

    public int getFreeSpaceSize() {
        return freeSpace;
    }

    public boolean isWellFormed() {
        boolean signatureOk = Arrays.equals(signature, MAGIC);
        boolean sizeOk = getDeclaredHeaderSize() == getSize();
        boolean relocsOk = (nRelocs == relocations.length) && (relocPos == (FIXED_HEADER_SIZE + getHeaderExtensionsSize()));
        return signatureOk && sizeOk && relocsOk;
    }

    public void makeWellFormed() {
        signature = MAGIC;
        maxalloc = 0xFFFF;
        if (getSize() > ((getSize()/16)*16)) {
            throw new IllegalStateException("Size should be multiply of 16");
        }
        headerSize = getSize() / 16;
        relocPos = FIXED_HEADER_SIZE + getHeaderExtensionsSize();
        nRelocs = relocations.length;
    }

    @Override
    public boolean isDisplayFormatSupported(String fieldName, DisplayFormat format) {
        return false;
    }

    @Override
    public void setStringValue(String fieldName, DisplayFormat format) {
        throw new UnsupportedOperationException();
    }
}
