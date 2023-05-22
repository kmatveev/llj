package llj.packager.coff;

import llj.packager.DisplayFormat;
import llj.packager.Format;
import llj.util.BinTools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.List;


public class Section implements Format {

    public final SectionHeader sectionHeader;
    public final List<RelocationEntry> relocations = new ArrayList<>();
    public byte[] data;
    public List<Usage> usages = new ArrayList();

    public Section(SectionHeader sectionHeader) {
        this.sectionHeader = sectionHeader;
    }

    public Section() {
        sectionHeader = new SectionHeader();
    }

    public boolean addUsage(long nameRva, long length, String comment) {
        return this.usages.add(new Usage(nameRva - this.sectionHeader.virtualAddress, length, comment));
    }

    public String getName() {
        return sectionHeader.getStringValue(SectionHeader.Field.NAME, DisplayFormat.ASCII).get();
    }
    
    public long getOffsetInFile() {
        return sectionHeader.pointerToRawData;
    }

    public long getSizeInFile() {
        return sectionHeader.sizeOfRawData;
    }

    @Override
    public void writeTo(ByteBuffer out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSize() {
        return (int)getSizeInFile();
    }

    @Override
    public String getStringValue() {
        return getName();
    }

    public void readFrom(SeekableByteChannel in) throws IOException {
        in.position(sectionHeader.pointerToRawData);
        readFrom((ReadableByteChannel)in);
    }
    
    public void readFrom(ReadableByteChannel in) throws IOException {
        ByteBuffer dataBuf = ByteBuffer.allocate((int)sectionHeader.sizeOfRawData);
        in.read(dataBuf);
        this.data = dataBuf.array();
    }

    public void readFrom(ByteBuffer readBuffer) {
        this.data = new byte[(int)sectionHeader.sizeOfRawData];
        readBuffer.get(this.data);
    }
    
    public ByteBuffer getByVirtualAddress(long virtualAddress) {
        if (sectionHeader.containsVirtualAddress(virtualAddress)) {
            ByteBuffer bb = ByteBuffer.wrap(data);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.position((int)(virtualAddress - sectionHeader.virtualAddress));
            return bb;
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    public String getStringByVirtualAddress(long virtualAddress) {
        return BinTools.readZeroTerminatedAsciiString(getByVirtualAddress(virtualAddress));
    }
    
    public static class Usage {
        public long offset;
        public long length;
        public String description;

        public Usage(long offset, long length, String description) {
            this.offset = offset;
            this.length = length;
            this.description = description;
        }
    }
}
