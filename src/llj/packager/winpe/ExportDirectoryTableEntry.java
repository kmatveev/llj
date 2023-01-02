package llj.packager.winpe;

import llj.packager.DisplayFormat;
import llj.packager.FieldSequenceFormat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static llj.util.BinIOTools.getUnsignedInt;
import static llj.util.BinIOTools.getUnsignedShort;
import static llj.util.BinIOTools.putUnsignedInt;
import static llj.util.BinIOTools.putUnsignedShort;

public class ExportDirectoryTableEntry extends FieldSequenceFormat {

    public static final int SIZE = 40;

    public static enum Field implements FieldSequenceFormat.Field<ExportDirectoryTableEntry> {
        EXPORT_FLAGS {
            @Override
            public int size() {
                return DWORD;
            }

            public void read(ByteBuffer source, ExportDirectoryTableEntry dest) {
                dest.flags = getUnsignedInt(source);
            }

            public void write(ExportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.flags);
            }

            @Override
            public Optional<String> getStringValue(ExportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.flags, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        TIME_DATE_STAMP {
            public int size() {
                return DWORD;
            }

            public void read(ByteBuffer source, ExportDirectoryTableEntry dest) {
                dest.timeDateStamp = getUnsignedInt(source);
            }

            public void write(ExportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.timeDateStamp);
            }

            @Override
            public Optional<String> getStringValue(ExportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.timeDateStamp, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        MAJOR_VERSION {
            @Override
            public int size() {
                return WORD;
            }

            public void read(ByteBuffer source, ExportDirectoryTableEntry dest) {
                dest.majorVersion = getUnsignedShort(source);
            }

            public void write(ExportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedShort(dest, source.majorVersion);
            }

            @Override
            public Optional<String> getStringValue(ExportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.majorVersion, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        MINOR_VERSION {
            @Override
            public int size() {
                return WORD;
            }

            public void read(ByteBuffer source, ExportDirectoryTableEntry dest) {
                dest.minorVersion = getUnsignedShort(source);
            }

            public void write(ExportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedShort(dest, source.minorVersion);
            }

            @Override
            public Optional<String> getStringValue(ExportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getIntegerString(displayFormat, format.minorVersion, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        NAME_RVA {
            @Override
            public int size() {
                return DWORD;
            }

            public void read(ByteBuffer source, ExportDirectoryTableEntry dest) {
                dest.nameRva = getUnsignedInt(source);
            }

            public void write(ExportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.nameRva);
            }

            @Override
            public Optional<String> getStringValue(ExportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.nameRva, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        ORDINAL_BASE {
            @Override
            public int size() {
                return DWORD;
            }

            public void read(ByteBuffer source, ExportDirectoryTableEntry dest) {
                dest.ordinalBase = getUnsignedInt(source);
            }

            public void write(ExportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.ordinalBase);
            }

            @Override
            public Optional<String> getStringValue(ExportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.ordinalBase, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        NUM_ENTRIES {
            @Override
            public int size() {
                return DWORD;
            }

            public void read(ByteBuffer source, ExportDirectoryTableEntry dest) {
                dest.numEntries = getUnsignedInt(source);
            }

            public void write(ExportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.numEntries);
            }

            @Override
            public Optional<String> getStringValue(ExportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.numEntries, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        NUM_NAME_POINTERS {
            @Override
            public int size() {
                return DWORD;
            }

            public void read(ByteBuffer source, ExportDirectoryTableEntry dest) {
                dest.numNamePointers = getUnsignedInt(source);
            }

            public void write(ExportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.numNamePointers);
            }

            @Override
            public Optional<String> getStringValue(ExportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.numNamePointers, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        EXPORT_ADDRESS_TABLE_RVA {
            @Override
            public int size() {
                return DWORD;
            }

            public void read(ByteBuffer source, ExportDirectoryTableEntry dest) {
                dest.exportAddressTableRva = getUnsignedInt(source);
            }

            public void write(ExportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.exportAddressTableRva);
            }

            @Override
            public Optional<String> getStringValue(ExportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.exportAddressTableRva, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        NAME_POINTER_RVA {
            @Override
            public int size() {
                return DWORD;
            }
            public void read(ByteBuffer source, ExportDirectoryTableEntry dest) {
                dest.namePointerRva = getUnsignedInt(source);
            }

            public void write(ExportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.namePointerRva);
            }

            @Override
            public Optional<String> getStringValue(ExportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.namePointerRva, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        },
        ORDINAL_TABLE_RVA {
            @Override
            public int size() {
                return DWORD;
            }

            public void read(ByteBuffer source, ExportDirectoryTableEntry dest) {
                dest.ordinalTableRva = getUnsignedInt(source);
            }

            public void write(ExportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.ordinalTableRva);
            }

            @Override
            public Optional<String> getStringValue(ExportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.ordinalTableRva, size(), ByteOrder.LITTLE_ENDIAN);
            }
            
        }
        
        
    }
    
    public long flags;
    public long timeDateStamp;
    public int majorVersion, minorVersion;
    public long nameRva;
    public long ordinalBase;
    public long numEntries;
    public long numNamePointers;
    public long exportAddressTableRva;
    public long namePointerRva;
    public long ordinalTableRva;

    @Override
    public Collection<? extends FieldSequenceFormat.Field> fields() {
        return Arrays.asList(ExportDirectoryTableEntry.Field.values());
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
    public String getStringValue() {
        return "ExportDirectoryTableEntry";
    }
}
