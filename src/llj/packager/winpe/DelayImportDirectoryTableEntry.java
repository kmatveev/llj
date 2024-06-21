package llj.packager.winpe;

import llj.packager.DisplayFormat;
import llj.packager.FieldSequenceFormat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static llj.util.BinIOTools.getUnsignedInt;
import static llj.util.BinIOTools.putUnsignedInt;

public class DelayImportDirectoryTableEntry extends FieldSequenceFormat {

    public static final int SIZE = 32;

    public static enum ImportDirectoryTableField implements FieldSequenceFormat.Field<DelayImportDirectoryTableEntry> {


        ATTRIBUTES {
            @Override
            public int size() {
                return DWORD;
            }
            public void read(ByteBuffer source, DelayImportDirectoryTableEntry dest) {
                dest.attributes = getUnsignedInt(source);
            }

            public void write(DelayImportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.attributes);
            }

            @Override
            public Optional<String> getStringValue(DelayImportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.attributes, size(), ByteOrder.LITTLE_ENDIAN);
            }

        },
        NAME_RVA {
            @Override
            public int size() {
                return DWORD;
            }
            public void read(ByteBuffer source, DelayImportDirectoryTableEntry dest) {
                dest.nameRva = getUnsignedInt(source);
            }

            public void write(DelayImportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.nameRva);
            }

            @Override
            public Optional<String> getStringValue(DelayImportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.nameRva, size(), ByteOrder.LITTLE_ENDIAN);
            }

        },
        MODULE_HANDLE {
            @Override
            public int size() {
                return DWORD;
            }
            public void read(ByteBuffer source, DelayImportDirectoryTableEntry dest) {
                dest.moduleHandle = getUnsignedInt(source);
            }

            public void write(DelayImportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.moduleHandle);
            }

            @Override
            public Optional<String> getStringValue(DelayImportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.moduleHandle, size(), ByteOrder.LITTLE_ENDIAN);
            }

        },
        IMPORT_ADDRESS_TABLE_RVA {
            @Override
            public int size() {
                return DWORD;
            }
            public void read(ByteBuffer source, DelayImportDirectoryTableEntry dest) {
                dest.importAddressTableRva = getUnsignedInt(source);
            }

            public void write(DelayImportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.importAddressTableRva);
            }

            @Override
            public Optional<String> getStringValue(DelayImportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.importAddressTableRva, size(), ByteOrder.LITTLE_ENDIAN);
            }

        },
        IMPORT_LOOKUP_TABLE_RVA {
            @Override
            public int size() {
                return DWORD;
            }
            public void read(ByteBuffer source, DelayImportDirectoryTableEntry dest) {
                dest.importLookupTableRva = getUnsignedInt(source);
            }

            public void write(DelayImportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.importLookupTableRva);
            }

            @Override
            public Optional<String> getStringValue(DelayImportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.importLookupTableRva, size(), ByteOrder.LITTLE_ENDIAN);
            }
        },
        BOUND_IMPORT_ADDRESS_TABLE_RVA {
            @Override
            public int size() {
                return DWORD;
            }
            public void read(ByteBuffer source, DelayImportDirectoryTableEntry dest) {
                dest.boundImportAddressTableRva = getUnsignedInt(source);
            }

            public void write(DelayImportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.boundImportAddressTableRva);
            }

            @Override
            public Optional<String> getStringValue(DelayImportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.boundImportAddressTableRva, size(), ByteOrder.LITTLE_ENDIAN);
            }

        },
        UNLOAD_IMPORT_ADDRESS_TABLE_RVA {
            @Override
            public int size() {
                return DWORD;
            }
            public void read(ByteBuffer source, DelayImportDirectoryTableEntry dest) {
                dest.unloadImportAddressTableRva = getUnsignedInt(source);
            }

            public void write(DelayImportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.unloadImportAddressTableRva);
            }

            @Override
            public Optional<String> getStringValue(DelayImportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.unloadImportAddressTableRva, size(), ByteOrder.LITTLE_ENDIAN);
            }

        },
        TIME_DATE_STAMP {
            @Override
            public int size() {
                return DWORD;
            }
            public void read(ByteBuffer source, DelayImportDirectoryTableEntry dest) {
                dest.timeDateStamp = getUnsignedInt(source);
            }

            public void write(DelayImportDirectoryTableEntry source, ByteBuffer dest) {
                putUnsignedInt(dest, source.timeDateStamp);
            }

            @Override
            public Optional<String> getStringValue(DelayImportDirectoryTableEntry format, DisplayFormat displayFormat) {
                return DisplayFormat.getLongString(displayFormat, format.timeDateStamp, size(), ByteOrder.LITTLE_ENDIAN);
            }

        }

    }

    public long attributes;
    public long importLookupTableRva;
    public long timeDateStamp;
    public long nameRva;
    public long moduleHandle;
    public long importAddressTableRva, boundImportAddressTableRva, unloadImportAddressTableRva;

    @Override
    public Collection<? extends Field> fields() {
        return Arrays.asList(DelayImportDirectoryTableEntry.ImportDirectoryTableField.values());
    }

    @Override
    public boolean isDisplayFormatSupported(String fieldName, DisplayFormat format) {
        return false;
    }

    @Override
    public void setStringValue(String fieldName, DisplayFormat format) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSize() {
        return SIZE;
    }

    @Override
    public String getStringValue() {
        return "DelayImportDirectoryTableEntry";
    }

    public boolean allEmpty() {
        return (importLookupTableRva == 0) && (attributes == 0) && (moduleHandle == 0) && (nameRva == 0) && (importAddressTableRva == 0);
    }


    @Override
    public Object readFieldsFrom(ByteBuffer readBuffer) {
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return super.readFieldsFrom(readBuffer);
    }


}
