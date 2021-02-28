package llj.packager.winpe;

import llj.packager.DisplayFormat;
import llj.packager.FieldSequenceFormat;
import llj.packager.IntrospectableFormat;
import llj.util.BinIOTools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static llj.util.BinIOTools.getInt;

public class ResourceDirectoryEntry implements IntrospectableFormat {

    public static final int SIZE = 8;

    public boolean isName;
    public int nameOffset;
    public int integerID;
    public boolean isDataEntry;
    public int valueOffset;

    @Override
    public List<String> getNames() {
        return Arrays.asList((isName ? "NameOffset" : "IntegerID") , (isDataEntry ? "DataEntryOffset" : "SubdirectoryOffset"));
    }

    @Override
    public Optional<String> getStringValue(String fieldName, DisplayFormat format) {
        if (isName && fieldName.equals("NameOffset")) {
            return Optional.of(String.valueOf(nameOffset));
        }
        if (!isName && fieldName.equals("IntegerID")) {
            return Optional.of(String.valueOf(integerID));
        }
        if (isDataEntry && fieldName.equals("DataEntryOffset")) {
            return Optional.of(String.valueOf(valueOffset));
        }
        if (!isDataEntry && fieldName.equals("SubdirectoryOffset")) {
            return Optional.of(String.valueOf(valueOffset));
        }
        throw new IllegalArgumentException();
    }

    @Override
    public int getSize(String fieldName) {
        if (isName && fieldName.equals("NameOffset")) {
            return DWORD;
        }
        if (!isName && fieldName.equals("IntegerID")) {
            return DWORD;
        }
        if (isDataEntry && fieldName.equals("DataEntryOffset")) {
            return DWORD;
        }
        if (!isDataEntry && fieldName.equals("SubdirectoryOffset")) {
            return DWORD;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public int getOffset(String fieldName) {
        if (isName && fieldName.equals("NameOffset")) {
            return 0;
        }
        if (!isName && fieldName.equals("IntegerID")) {
            return 0;
        }
        if (isDataEntry && fieldName.equals("DataEntryOffset")) {
            return 4;
        }
        if (!isDataEntry && fieldName.equals("SubdirectoryOffset")) {
            return 4;
        }
        throw new IllegalArgumentException();

    }

    public void readFrom(ByteBuffer source) {
        source.order(ByteOrder.LITTLE_ENDIAN);
        if (isName) {
            nameOffset = 0x7FFFFFFF & getInt(source);
        } else {
            integerID = getInt(source);
        }
        int v = getInt(source);
        valueOffset = 0x7FFFFFFF & v;
        isDataEntry = v >= 0;

    }    

    @Override
    public void writeTo(ByteBuffer out) {
        throw new UnsupportedOperationException();
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
        return "ResourceDirectoryEntry";
    }
}
