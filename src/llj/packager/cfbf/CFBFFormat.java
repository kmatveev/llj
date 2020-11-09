package llj.packager.cfbf;

import llj.packager.Format;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class CFBFFormat implements Format {

    public void readFrom(SeekableByteChannel in) throws Exception {

    }

    public static class CFBFHeader {
        byte[] signature;
        byte[] clsid;
        int minorVersion, majorVersion;
        int byteOrder;
        int sectorShift;
        int miniSectorShift;
        byte[] reserved;
        byte[] reserved1;
        int numOfSectsInFAT;
        int dirStartSector;
        byte[] transactionSignature;
        int miniSectorCutOff;
        int miniFatStartSector;
        int minFatNumSectors;
        int difatStartSector;
        int difatNumSectors;

    }

    @Override
    public void writeTo(ByteBuffer out) {

    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public String getStringValue() {
        return null;
    }
}
