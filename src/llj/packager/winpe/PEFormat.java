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
import llj.util.BinIOTools;

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

    public static final String[] DIRECTORY_ENTRY_NAMES = new String[]{"Export table", "Import table", "Resource table", "Exception table", "Certificate table", "Base relocation table", "Debug", "Architecture", "Global ptr", "TLS table", "Load config table", "Bound import", "IAT", "Delay import descriptor", "CLR Runtime header", "Reserved"};
    public final ExtendedDOSHeader dosHeader = new ExtendedDOSHeader();
    public final COFFHeader coffHeader = new COFFHeader();
    
    public COFFOptionalHeaderStandard<Object> coffOptionalHeaderStandard = null;
    public COFFOptionalHeaderPE32 coffOptionalHeaderPE32 = null;
    public COFFOptionalHeaderPE32Plus coffOptionalHeaderPE32Plus = null;
    
    public ExportDirectoryTableEntry exportDirectoryTable;
    public List<ImportBlock> imports;
    public ResourceDirectory resourceRoot;
    
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
        return getPEOffset() + 4;
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
            Section section = new Section(sectionHeader);
            sections.add(section);
        }
        
        // now we've completed sequential reading of total COFF header (including optional header and section headers)
        // so from this point we can use in.position to move file read pointer
        
        for (Section section : sections) {
            section.readFrom(in);
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

        
        if (coffOptionalHeaderPE32 != null) {
            if ((coffOptionalHeaderPE32.dataDirectory.size() > 0) && coffOptionalHeaderPE32.dataDirectory.get(0).VirtualAddress > 0) {
                exportDirectoryTable = new ExportDirectoryTableEntry();
            }
            if ((coffOptionalHeaderPE32.dataDirectory.size() > 1) && coffOptionalHeaderPE32.dataDirectory.get(1).VirtualAddress > 0) {
                imports = new ArrayList<>();
                long importDirTableRva = coffOptionalHeaderPE32.dataDirectory.get(1).VirtualAddress;
                readImportDirectoryTable(importDirTableRva, false);
            }
            if ((coffOptionalHeaderPE32.dataDirectory.size() > 2) && coffOptionalHeaderPE32.dataDirectory.get(2).VirtualAddress > 0) {
                long resourceRootRva = coffOptionalHeaderPE32.dataDirectory.get(2).VirtualAddress;
                Section section = findByRelativeVirtualAddress(resourceRootRva);
                ByteBuffer bb = section.getByVirtualAddress(resourceRootRva);
                resourceRoot = new ResourceDirectory();
                List<Section.Usage> usages = resourceRoot.resolve(bb, "/Root");
                section.usages.addAll(usages);
            }
            
        } else if (coffOptionalHeaderPE32Plus != null) {
            if ((coffOptionalHeaderPE32Plus.dataDirectory.size() > 0) && coffOptionalHeaderPE32Plus.dataDirectory.get(0).VirtualAddress > 0) {
                exportDirectoryTable = new ExportDirectoryTableEntry();
            }
            if ((coffOptionalHeaderPE32Plus.dataDirectory.size() > 1) && coffOptionalHeaderPE32Plus.dataDirectory.get(1).VirtualAddress > 0) {
                imports = new ArrayList<>();
                long importDirTableRva = coffOptionalHeaderPE32Plus.dataDirectory.get(1).VirtualAddress;
                readImportDirectoryTable(importDirTableRva, true);
            }
            if ((coffOptionalHeaderPE32Plus.dataDirectory.size() > 2) && coffOptionalHeaderPE32Plus.dataDirectory.get(2).VirtualAddress > 0) {
                long resourceRootRva = coffOptionalHeaderPE32Plus.dataDirectory.get(2).VirtualAddress;
                Section section = findByRelativeVirtualAddress(resourceRootRva);
                ByteBuffer bb = section.getByVirtualAddress(resourceRootRva);
                resourceRoot = new ResourceDirectory();
                List<Section.Usage> usages = resourceRoot.resolve(bb, "/Root");
                section.usages.addAll(usages);
            }
            
        }

        

    }

    public void readImportDirectoryTable(long importDirTableRva, boolean is64Bit) {
        Section section = findByRelativeVirtualAddress(importDirTableRva);
        ByteBuffer bb = section.getByVirtualAddress(importDirTableRva);
        int count = 0;
        int size = 0;
        while (true) {
            ImportDirectoryTableEntry entry = new ImportDirectoryTableEntry();
            entry.readFrom(bb);
            size += entry.getSize();
            if (entry.allEmpty()) {
                break;
            } else {
                ImportBlock importBlock = new ImportBlock(entry, is64Bit);
                imports.add(importBlock);
                importBlock.name = findByRelativeVirtualAddress(entry.nameRva).getStringByVirtualAddress(entry.nameRva);
                section.usages.add(new Section.Usage(entry.nameRva - section.sectionHeader.virtualAddress, importBlock.name.length() + 1, "ImportDirectoryTableEntry" + String.valueOf(count) + ".Name"));
                ByteBuffer importLookupEntryRaw = findByRelativeVirtualAddress(entry.importLookupTableRva).getByVirtualAddress(entry.importLookupTableRva);
                ByteBuffer importAddressEntryRaw = findByRelativeVirtualAddress(entry.importAddressTableRva).getByVirtualAddress(entry.importAddressTableRva);
                while (true) {
                    if (is64Bit) {
                        ImportLookupEntryPE32Plus lookupEntry = new ImportLookupEntryPE32Plus(BinIOTools.getLong(importLookupEntryRaw));
                        section.usages.add(new Section.Usage(entry.importLookupTableRva - section.sectionHeader.virtualAddress, ImportLookupEntryPE32Plus.SIZE, "ImportLookupEntryPE32Plus"));
                        ImportLookupEntryPE32Plus addressEntry = new ImportLookupEntryPE32Plus(BinIOTools.getLong(importAddressEntryRaw));
                        section.usages.add(new Section.Usage(entry.importAddressTableRva - section.sectionHeader.virtualAddress, ImportLookupEntryPE32Plus.SIZE, "ImportAddressEntryPE32Plus"));
                        if (lookupEntry.isEmpty()) {
                            break;
                        } else {
                            importBlock.importedFunctions64.add(lookupEntry);
                            if (!lookupEntry.isOrdinal()) {
                                HintNameEntry hintNameEntry = HintNameEntry.readFrom(findByRelativeVirtualAddress(lookupEntry.getHintNameRva()).getByVirtualAddress(lookupEntry.getHintNameRva()));
                                section.usages.add(new Section.Usage(lookupEntry.getHintNameRva() - section.sectionHeader.virtualAddress, hintNameEntry.getSize(), "HintNameEntry"));
                                String name = hintNameEntry.value;
                                importBlock.resolvedImportedFunctions.add(name);
                            } else {
                                importBlock.resolvedImportedFunctions.add(String.valueOf(lookupEntry.getOrdinal()));
                            }
                        }
                    } else {
                        ImportLookupEntryPE32 lookupEntry = new ImportLookupEntryPE32(BinIOTools.getInt(importLookupEntryRaw));
                        section.usages.add(new Section.Usage(entry.importLookupTableRva - section.sectionHeader.virtualAddress, ImportLookupEntryPE32.SIZE, "ImportLookupEntryPE32"));
                        ImportLookupEntryPE32 addressEntry = new ImportLookupEntryPE32(BinIOTools.getInt(importAddressEntryRaw));
                        section.usages.add(new Section.Usage(entry.importAddressTableRva - section.sectionHeader.virtualAddress, ImportLookupEntryPE32.SIZE, "ImportAddressEntryPE32"));
                        if (lookupEntry.isEmpty()) {
                            break;
                        } else {
                            importBlock.importedFunctions.add(lookupEntry);
                            if (!lookupEntry.isOrdinal()) {
                                HintNameEntry hintNameEntry = HintNameEntry.readFrom(findByRelativeVirtualAddress(lookupEntry.getHintNameRva()).getByVirtualAddress(lookupEntry.getHintNameRva()));
                                section.usages.add(new Section.Usage(lookupEntry.getHintNameRva() - section.sectionHeader.virtualAddress, hintNameEntry.getSize(), "HintNameEntry"));
                                String name = hintNameEntry.value;
                                importBlock.resolvedImportedFunctions.add(name);
                            } else {
                                importBlock.resolvedImportedFunctions.add(String.valueOf(lookupEntry.getOrdinal()));
                            }
                        }
                    }
                }
                
            }
            count += 1;
        }
        section.usages.add(new Section.Usage(importDirTableRva - section.sectionHeader.virtualAddress, size, "ImportDirectoryTable"));
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

    public Section findByRelativeVirtualAddress(long rva) {
        for (Section section : sections) {
            // in PE file sections contain RVA in field "Virtual address"
            if (section.sectionHeader.containsVirtualAddress(rva)) {
                return section;
            }
        }
        return null;
    }

    public long getImageBaseVA() {
        long imageBaseVA;
        if (coffOptionalHeaderPE32 != null) {
            imageBaseVA = coffOptionalHeaderPE32.imageBase;
        } else if (coffOptionalHeaderPE32Plus != null) {
            imageBaseVA = coffOptionalHeaderPE32Plus.imageBase;
        } else {
            throw new IllegalStateException("Can't find imageBaseVA");
        }
        return imageBaseVA;
    }

    // accepts full virtual address
    public Section findByVirtualAddress(long virtualAddress) {
        for (Section section : sections) {
            // in PE file sections contain RVA in field "Virtual address"
            if (section.sectionHeader.containsVirtualAddress(virtualAddress - getImageBaseVA())) {
                return section;
            }
        }
        return null;
    }
    
    public static String directoryNameByIndex(int idx) {
        return DIRECTORY_ENTRY_NAMES[idx];
    }

}
