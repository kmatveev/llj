package llj.packager.coff;

import llj.packager.DisplayFormat;
import llj.packager.FieldSequenceFormat;
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
import java.util.Arrays;
import java.util.Collection;
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

public class COFFOptionalHeaderStandard<T> extends FieldSequenceFormat {

    public static final int SIZE = 24 ;

    public static final String EXTENSION = "EXTENSION";
    
    public static enum Field implements FieldSequenceFormat.Field<COFFOptionalHeaderStandard> {
        SIGNATURE {
            public void read(ByteBuffer source, COFFOptionalHeaderStandard dest) { dest.signature = getUnsignedShort(source);}
            public void write(COFFOptionalHeaderStandard source, ByteBuffer dest) { putUnsignedShort(dest, source.signature);}
            public int size() { return WORD; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderStandard format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.signature, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        MAJOR_LINKER_VERSION {
            public void read(ByteBuffer source, COFFOptionalHeaderStandard dest) {dest.majorLinkerVersion = getUnsignedByte(source);}
            public void write(COFFOptionalHeaderStandard source, ByteBuffer dest) {
                putUnsignedByte(dest, source.majorLinkerVersion);}
            public int size() { return BYTE; }

            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderStandard format, DisplayFormat displayFormat) {
                return DisplayFormat.getByteString(displayFormat, format.majorLinkerVersion);
            }
        },
        MINOR_LINKER_VERSION {
            public void read(ByteBuffer source, COFFOptionalHeaderStandard dest) {dest.minorLinkerVersion = getUnsignedByte(source);}
            public void write(COFFOptionalHeaderStandard source, ByteBuffer dest) {
                putUnsignedByte(dest, source.minorLinkerVersion);}
            public int size() { return BYTE; }
            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderStandard format, DisplayFormat displayFormat) {
                return DisplayFormat.getByteString(displayFormat, format.minorLinkerVersion);
            }
            
        },
        SIZE_OF_CODE {
            public void read(ByteBuffer source, COFFOptionalHeaderStandard dest) {dest.sizeOfCode = getUnsignedInt(source);}
            public void write(COFFOptionalHeaderStandard source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfCode);}
            public int size() { return DWORD; }
            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderStandard format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.sizeOfCode, size(), ByteOrder.LITTLE_ENDIAN);
            }
                        
        },
        SIZE_OF_INITIALIZED_DATA {
            public void read(ByteBuffer source, COFFOptionalHeaderStandard dest) {dest.sizeOfInitializedData = getUnsignedInt(source);}
            public void write(COFFOptionalHeaderStandard source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfInitializedData);}
            public int size() { return DWORD; }
            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderStandard format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.sizeOfInitializedData, size(), ByteOrder.LITTLE_ENDIAN);
            }

        },
        SIZE_OF_UNINITIALIZED_DATA {
            public void read(ByteBuffer source, COFFOptionalHeaderStandard dest) {dest.sizeOfUninitializedData = getUnsignedInt(source);}
            public void write(COFFOptionalHeaderStandard source, ByteBuffer dest) {
                putUnsignedInt(dest, source.sizeOfUninitializedData);}
            public int size() { return DWORD; }
            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderStandard format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.sizeOfUninitializedData, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        ADDRESS_OF_ENTRY_POINT {
            public void read(ByteBuffer source, COFFOptionalHeaderStandard dest) {dest.addressOfEntryPoint = getUnsignedInt(source);}
            public void write(COFFOptionalHeaderStandard source, ByteBuffer dest) {
                putUnsignedInt(dest, source.addressOfEntryPoint);}
            public int size() { return DWORD; }
            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderStandard format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.addressOfEntryPoint, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        BASE_OF_CODE {
            public void read(ByteBuffer source, COFFOptionalHeaderStandard dest) {dest.baseOfCode = getUnsignedInt(source);}
            public void write(COFFOptionalHeaderStandard source, ByteBuffer dest) {
                putUnsignedInt(dest, source.baseOfCode);}
            public int size() { return DWORD; }
            @Override
            public Optional<String> getStringValue(COFFOptionalHeaderStandard format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.baseOfCode, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        };

        public abstract void read(ByteBuffer source, COFFOptionalHeaderStandard dest);

        public abstract void write(COFFOptionalHeaderStandard source, ByteBuffer dest);
        
        public abstract int size();

        public abstract Optional<String> getStringValue(COFFOptionalHeaderStandard format, DisplayFormat displayFormat);

    }
    
    public static class ExtensionsField implements FieldSequenceFormat.Field<COFFOptionalHeaderStandard> {
        
        public static final ExtensionsField instance = new ExtensionsField(); 
        
        @Override
        public void read(ByteBuffer source, COFFOptionalHeaderStandard dest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(COFFOptionalHeaderStandard source, ByteBuffer dest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String name() {
            return EXTENSION;
        }

        @Override
        public Optional<String> getStringValue(COFFOptionalHeaderStandard format, DisplayFormat displayFormat) {
            return Optional.of(format.extensions.getStringValue());
        }
        
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

    @Override
    public Collection<? extends FieldSequenceFormat.Field> fields() {
        ArrayList<FieldSequenceFormat.Field> result = new ArrayList<>();
        result.addAll(Arrays.asList(COFFOptionalHeaderStandard.Field.values()));
        result.addAll(getExtensionFields());
        return result;
    }
    
    public Collection<? extends FieldSequenceFormat.Field> getExtensionFields() {
        return Collections.singletonList(ExtensionsField.instance);
    }

    public List<String> getNames() {
        List<String> result = super.getNames();
        result.addAll(getExtensionNames());
        return result;
    }

    public List<String> getExtensionNames() {
        return Collections.singletonList(EXTENSION);
    }

    public Optional<String> getStringValue(String fieldName, DisplayFormat displayFormat) {
        if (fieldName.equals(EXTENSION)) {
            return ExtensionsField.instance.getStringValue(this, displayFormat);
        }
        return super.getStringValue(fieldName, displayFormat);
    }

    public Optional<String> getStringValue(FieldSequenceFormat.Field field, DisplayFormat displayFormat) {
        if (field instanceof ExtensionsField) {
            return ((ExtensionsField)field).getStringValue(this, displayFormat);
        } else if (field instanceof COFFOptionalHeaderStandard.Field) {
            return getStringValue((COFFOptionalHeaderStandard.Field)field, displayFormat);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public Optional<String> getStringValue(Field field, DisplayFormat displayFormat) {
        return field.getStringValue(this, displayFormat);
    }

    public int getSize(String fieldName) {
        if (fieldName.equals(EXTENSION)) {
            return extensions.getSize(); // ExtensionsField.getSize() could not be implemented
        }
        return super.getSize(fieldName);
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
        writeFieldsTo(writeBuffer);
        writeExtensionsTo(writeBuffer);
    }

    public void writeFieldsTo(ByteBuffer writeBuffer) {
        writeBuffer.order(ByteOrder.LITTLE_ENDIAN);
        for (Field field : Field.values()) {
            field.write(this, writeBuffer);
        }
    }
    
    public void writeExtensionsTo(ByteBuffer writeBuffer) {
        extensions.writeTo(writeBuffer);
    }

    @Override
    public int getSize() {
        return SIZE + extensions.getSize();
    }

    @Override
    public String getStringValue() {
        return "COFFOptionalHeaderStandard";
    }

    @Override
    public boolean isDisplayFormatSupported(String fieldName, DisplayFormat format) {
        return getStringValue(fieldName, format).isPresent();
    }

    @Override
    public void setStringValue(String fieldName, DisplayFormat format) {
        throw new UnsupportedOperationException();
    }
}
