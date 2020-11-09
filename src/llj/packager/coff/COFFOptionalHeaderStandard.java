package llj.packager.coff;

import llj.packager.DisplayFormat;
import llj.packager.Format;
import llj.packager.IntrospectableFormat;
import llj.packager.RawFormat;
import llj.packager.dosexe.DOSHeader;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static llj.util.BinIOTools.getUnsignedByte;
import static llj.util.BinIOTools.getUnsignedInt;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedByte;
import static llj.util.BinIOTools.putUnsignedInt;
import static llj.util.BinIOTools.putUnsignedShort;
import static llj.util.BinIOTools.readIntoBuffer;

public class COFFOptionalHeaderStandard<T> implements IntrospectableFormat {

    public static final int SIZE = 24 ;

    public static final String EXTENSION = "EXTENSION";
    
    public static enum Field {
        SIGNATURE {
            public void read(ByteBuffer source, COFFOptionalHeaderStandard dest) { dest.signature = getUnsignedShort(source);}
            public void write(COFFOptionalHeaderStandard source, ByteBuffer dest) { putUnsignedShort(dest, source.signature);}
            public int size() { return WORD; }
        },
        MAJOR_LINKER_VERSION {
            public void read(ByteBuffer source, COFFOptionalHeaderStandard dest) {dest.majorLinkerVersion = getUnsignedByte(source);}
            public void write(COFFOptionalHeaderStandard source, ByteBuffer dest) {
                putUnsignedByte(dest, source.majorLinkerVersion);}
            public int size() { return BYTE; }
        },
        MINOR_LINKER_VERSION {
            public void read(ByteBuffer source, COFFOptionalHeaderStandard dest) {dest.minorLinkerVersion = getUnsignedByte(source);}
            public void write(COFFOptionalHeaderStandard source, ByteBuffer dest) {
                putUnsignedByte(dest, source.minorLinkerVersion);}
            public int size() { return BYTE; }
        },
        SIZE_OF_CODE {
            public void read(ByteBuffer source, COFFOptionalHeaderStandard dest) {dest.sizeOfCode = getUnsignedInt(source);}
            public void write(COFFOptionalHeaderStandard source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfCode);}
            public int size() { return DWORD; }
        },
        SIZE_OF_INITIALIZED_DATA {
            public void read(ByteBuffer source, COFFOptionalHeaderStandard dest) {dest.sizeOfInitializedData = getUnsignedInt(source);}
            public void write(COFFOptionalHeaderStandard source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfInitializedData);}
            public int size() { return DWORD; }
        },
        SIZE_OF_UNINITIALIZED_DATA {
            public void read(ByteBuffer source, COFFOptionalHeaderStandard dest) {dest.sizeOfUninitializedData = getUnsignedInt(source);}
            public void write(COFFOptionalHeaderStandard source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfUninitializedData);}
            public int size() { return DWORD; }
        },
        ADDRESS_OF_ENTRY_POINT {
            public void read(ByteBuffer source, COFFOptionalHeaderStandard dest) {dest.addressOfEntryPoint = getUnsignedInt(source);}
            public void write(COFFOptionalHeaderStandard source, ByteBuffer dest) {
                putUnsignedInt(dest, source.addressOfEntryPoint);}
            public int size() { return DWORD; }
        },
        BASE_OF_CODE {
            public void read(ByteBuffer source, COFFOptionalHeaderStandard dest) {dest.baseOfCode = getUnsignedInt(source);}
            public void write(COFFOptionalHeaderStandard source, ByteBuffer dest) {
                putUnsignedInt(dest, source.baseOfCode);}
            public int size() { return DWORD; }
        };

        public abstract void read(ByteBuffer source, COFFOptionalHeaderStandard dest);

        public abstract void write(COFFOptionalHeaderStandard source, ByteBuffer dest);
        
        public abstract int size();

    }

    public static class Location<T> {
        public static enum LocationType {STANDARD_HEADER, EXTENSION}

        public final Location.LocationType locationType;
        public final Field field;
        public final T extensionField;

        public Location(Location.LocationType locationType, Field field, T extensionField) {
            this.locationType = locationType;
            this.field = field;
            this.extensionField = extensionField;
        }

        public static Location atStandardField(Field field) {
            return new Location(Location.LocationType.STANDARD_HEADER, field, null);
        }

        public static <T> Location atExtensionField(T fieldPE32) {
            return new Location(Location.LocationType.EXTENSION, null, fieldPE32);
        }
    }
    

    public int    signature ;
    public short  majorLinkerVersion;
    public short  minorLinkerVersion;
    public long   sizeOfCode;
    public long   sizeOfInitializedData;
    public long   sizeOfUninitializedData;
    public long   addressOfEntryPoint;
    public long   baseOfCode;
    
    public Format extensions;

    public COFFOptionalHeaderStandard() {
    }


    public List<String> getNames() {
        List<String> result = new ArrayList<String>();
        for (COFFOptionalHeaderStandard.Field field: COFFOptionalHeaderStandard.Field.values()) {
            result.add(field.name());
        }
        result.addAll(getExtensionNames());
        return result;
    }

    public List<String> getExtensionNames() {
        return Collections.singletonList(EXTENSION);
    }

    public Optional<String> getStringValue(String fieldName, DisplayFormat displayFormat) {
        for (Field field: Field.values()) {
            if (field.name().equals(fieldName)) {
                return getStringValue(field, displayFormat);
            }
        }
        if (fieldName.equals(EXTENSION)) {
            return Optional.of(extensions.getStringValue());
        }
        throw new IllegalArgumentException(fieldName);
    }

    public Optional<String> getStringValue(Field field, DisplayFormat displayFormat) {
        switch(field) {
            case SIGNATURE: return Optional.of(String.valueOf(signature));
            case MAJOR_LINKER_VERSION: return Optional.of(String.valueOf(majorLinkerVersion));
            case MINOR_LINKER_VERSION: return Optional.of(String.valueOf(minorLinkerVersion));
            case SIZE_OF_CODE: return Optional.of(String.valueOf(sizeOfCode));
            case SIZE_OF_INITIALIZED_DATA: return Optional.of(String.valueOf(sizeOfInitializedData));
            case SIZE_OF_UNINITIALIZED_DATA: return Optional.of(String.valueOf(sizeOfUninitializedData));
            case ADDRESS_OF_ENTRY_POINT: return Optional.of(String.valueOf(addressOfEntryPoint));
            case BASE_OF_CODE: return Optional.of(String.valueOf(baseOfCode));
            default: throw new IllegalArgumentException(field.toString());
        }
        
    }

    public int getSize(String fieldName) {
        for (COFFOptionalHeaderStandard.Field field: COFFOptionalHeaderStandard.Field.values()) {
            if (field.name().equals(fieldName)) {
                return field.size();
            }
        }
        throw new IllegalArgumentException(fieldName);
    }

    public int getOffset(String fieldName) {
        int offset = 0;
        for (COFFOptionalHeaderStandard.Field field: COFFOptionalHeaderStandard.Field.values()) {
            if (field.name().equals(fieldName)) {
                return offset;
            } else {
                offset += field.size();
            }
        }
        throw new IllegalArgumentException(fieldName);
    }

    public Location readFrom(ReadableByteChannel in, ByteBuffer readBuffer) throws IOException {
        return readFrom(in, readBuffer, SIZE);
    }

    public Location readFrom(ReadableByteChannel in, ByteBuffer readBuffer, int size) throws IOException {
        if (readBuffer.capacity() < size) {
            readBuffer = ByteBuffer.allocate(size);
        }
        readIntoBuffer(in, readBuffer, size);
        return readFrom(readBuffer);
    }

    public Location readFrom(ByteBuffer readBuffer) {
        Location stdLoc = readFieldsFrom(readBuffer);
        if (stdLoc != null) return stdLoc;
        Location extLoc = readExtensionsFrom(readBuffer, false);
        if (extLoc != null) {
            return extLoc;
        }
        return null;
    }

    public Location readFieldsFrom(ByteBuffer readBuffer) {
        for (Field field : Field.values()) {
            try {
                field.read(readBuffer, this);
            } catch (BufferUnderflowException e) {
                return Location.atStandardField(field);
            }
        }
        return null;
    }

    public Location readExtensionsFrom(ByteBuffer readBuffer, boolean canTakeBuffer) {
        ByteBuffer rawExtensionData;
        if (canTakeBuffer) {
            rawExtensionData = readBuffer;
        } else {
            rawExtensionData = ByteBuffer.allocate(readBuffer.remaining());
            rawExtensionData.order(ByteOrder.LITTLE_ENDIAN);
            rawExtensionData.put(readBuffer);
            rawExtensionData.flip();
        }
        extensions = new RawFormat(rawExtensionData);
        return null;
    }

    public void writeTo(ByteBuffer writeBuffer) {
        for (Field field : Field.values()) {
            field.write(this, writeBuffer);
        }
    }

    @Override
    public int getSize() {
        return SIZE + extensions.getSize();
    }

    @Override
    public String getStringValue() {
        return "COFFOptionalHeaderStandard";
    }
}
