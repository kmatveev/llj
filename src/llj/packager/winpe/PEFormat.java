package llj.packager.winpe;

import llj.packager.Format;
import llj.packager.RawFormat;
import llj.packager.coff.COFFHeader;
import llj.packager.coff.COFFOptionalHeaderStandard;
import llj.packager.coff.RelocationEntry;
import llj.packager.coff.Section;
import llj.packager.coff.SectionHeader;
import llj.packager.coff.SymbolTableEntry;
import llj.packager.dosexe.DOSHeader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static llj.util.BinIOTools.*;

public class PEFormat implements Format {

    public final ExtendedDOSHeader dosHeader = new ExtendedDOSHeader();
    public final COFFHeader coffHeader = new COFFHeader();
    
    public COFFOptionalHeaderStandard<Object> coffOptionalHeaderStandard = null;
    public COFFOptionalHeaderPE32 coffOptionalHeaderPE32 = null;
    public COFFOptionalHeaderPE32Plus coffOptionalHeaderPE32Plus = null;
    
    public final List<Section> sections = new ArrayList<Section>();
    public final List<SymbolTableEntry> symbolTable = new ArrayList<SymbolTableEntry>();
    
    public final RawFormat dosStub = new RawFormat(null);
    public boolean peSignatureInPlace;

    public static int setBit(int bit) {
        return 1 << (bit);
    }

    public static long setBit(long bit) {
        return 1 << (bit);
    }

    int getPEHeadersTotalSize() {
        return COFFHeader.SIZE + coffHeader.sizeOfOptionalHeader;
    }

    long getPEOffset() {
        return dosHeader.newPos;
    }
    
    long getCOFFHeaderOffset() {
        return getPEOffset() + 2;
    }

    long getSectionHeadersOffset() {
        return (getCOFFHeaderOffset() + getPEHeadersTotalSize());
    }

    long getSectionHeadersSize() {
        return (sections.size() * SectionHeader.SIZE);
    }

    public void writeTo(WritableByteChannel out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeTo(ByteBuffer out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getStringValue() {
        return null;
    }

    public void readFrom(SeekableByteChannel in) throws PEFormatException, IOException {

        ByteBuffer readBuffer = ByteBuffer.allocate(500);
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);

        {
            Object lastField = dosHeader.readFrom(in, readBuffer);

            if (lastField != null) {
                throw new PEFormatException("Unable to read PE file, it is shorter than expected size of DOS EXE header");
            }
        }

        if (!Arrays.equals(dosHeader.signature, DOSHeader.MAGIC)) {
            throw new PEFormatException("Unable to read PE file, there are no DOS EXE signature in place");
        }

        long peHeaderPos = dosHeader.newPos;
        in.position(peHeaderPos);

        try {
            peSignatureInPlace = readPESignature(in, readBuffer);
        } catch (IOException e) {
            throw new PEFormatException("Unable to read PE file, got I/O error trying to read PE signature, most likely it is beyond file size: " + peHeaderPos);
        }
        if (!peSignatureInPlace) {
            throw new PEFormatException("Unable to read PE file, there are no PE signature in place");
        }

        // read COFF header
        {
            Object lastField = coffHeader.readFrom(in, readBuffer);

            if (lastField != null) {
                throw new PEFormatException("Unable to read PE file, COFF header is shorter than expected size");
            }

        }

        if (coffHeader.sizeOfOptionalHeader == 0) {
            throw new PEFormatException("Unable to read PE file, COFF optional header is absent");
        }

        // read PE optional COFF header
        {
            COFFOptionalHeaderStandard<Object> coffOptionalHeaderStandard = new COFFOptionalHeaderStandard<Object>();
            
            // no need to read more bytes, since we will not parse them
            int peOptionalHeaderReadSize = coffHeader.sizeOfOptionalHeader;

            Object lastField = coffOptionalHeaderStandard.readFrom(in, readBuffer, peOptionalHeaderReadSize);
            
            if (COFFOptionalHeaderPE32.canExtend(coffOptionalHeaderStandard)) {
                coffOptionalHeaderPE32 = COFFOptionalHeaderPE32.extend(coffOptionalHeaderStandard);
            } else if (COFFOptionalHeaderPE32Plus.canExtend(coffOptionalHeaderStandard)) {
                coffOptionalHeaderPE32Plus = COFFOptionalHeaderPE32Plus.extend(coffOptionalHeaderStandard);
            } else {
                this.coffOptionalHeaderStandard = coffOptionalHeaderStandard; 
            }

            if (lastField != null) {
                throw new PEFormatException("Unable to read PE file, PE optional header is shorter than expected size");
            }

        }

        for (int i = 0; i < coffHeader.numberOfSections; i++) {
            SectionHeader sectionHeader = new SectionHeader();
            sectionHeader.readFrom(in, readBuffer);
            sections.add(new Section(sectionHeader));
        }

        for (Section section : sections) {
            in.position(section.sectionHeader.pointerToRelocations);
            for (int i = 0; i < section.sectionHeader.numberOfRelocations; i++) {
                RelocationEntry relocationEntry = new RelocationEntry();
                relocationEntry.readFrom(in, readBuffer);
                section.relocations.add(relocationEntry);
            }
        }

        in.position(coffHeader.pointerToSymbolTable);
        for (int i = 0; i < coffHeader.numberOfSymbols; i++) {
            SymbolTableEntry symbolTableEntry = new SymbolTableEntry();
            symbolTableEntry.readFrom(in, readBuffer);
            symbolTable.add(symbolTableEntry);
        }
        
    }

    public static boolean readPESignature(ReadableByteChannel channel, ByteBuffer bb) throws IOException {
        readIntoBuffer(channel, bb, 2);
        if (bb.get() == 0x50 && bb.get() == 0x45) {
            readIntoBuffer(channel, bb, 2);
            return true;
        } else {
            return false;
        }
    }

    public static void writePESignature(WritableByteChannel channel) throws IOException {
        ByteBuffer bb = ByteBuffer.wrap(new byte[]{0x50, 0x45, 0, 0});
        channel.write(bb);
    }


}
