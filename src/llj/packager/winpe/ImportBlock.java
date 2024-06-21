package llj.packager.winpe;

import llj.packager.coff.Section;
import llj.util.BinIOTools;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ImportBlock {
    
    public ImportDirectoryTableEntry importTableEntry;
    public String name;
    
    public List<ImportLookupEntryPE32> importedFunctions;
    public List<ImportLookupEntryPE32Plus> importedFunctions64;
    public List<String> resolvedImportedFunctions = new ArrayList<>();
    private long nameRva;
    private long importLookupTableRva;
    private long importAddressTableRva;

    public static ImportBlock createFrom(ImportDirectoryTableEntry header, boolean is64Bit) {
        return new ImportBlock(is64Bit, header.nameRva, header.importLookupTableRva, header.importAddressTableRva);
    }

    public static ImportBlock createFrom(DelayImportDirectoryTableEntry header, boolean is64Bit) {
        return new ImportBlock(is64Bit, header.nameRva, header.importLookupTableRva, header.importAddressTableRva);
    }

    private ImportBlock(boolean is64Bit, long nameRva, long importLookupTableRva, long importAddressTableRva) {
        this.nameRva = nameRva;
        this.importLookupTableRva = importLookupTableRva;
        this.importAddressTableRva = importAddressTableRva;
        if (is64Bit) {
            importedFunctions64 = new ArrayList<>();
        } else {
            importedFunctions = new ArrayList<>();
        }
    }

    public long getNameRva() {
        return nameRva;
    }

    public long getImportLookupTableRva() {
        return importLookupTableRva;
    }

    public long getImportAddressTableRva() {
        return importAddressTableRva;
    }

    public void processImportBlock(PEFormat peFormat, int count) {

        boolean is64Bit = importedFunctions64 != null;

        Section importBlockSection = peFormat.findSectionByRVA(nameRva);
        this.name = importBlockSection.getStringByVirtualAddress(nameRva);
        importBlockSection.addUsage(nameRva, this.name.length() + 1, "ImportDirectoryTableEntry" + String.valueOf(count) + ".Name");
        Section importLookupTableSection = peFormat.findSectionByRVA(importLookupTableRva);
        ByteBuffer importLookupEntryRaw = importLookupTableSection.getByVirtualAddress(importLookupTableRva);
        Section importAddressTableSection = peFormat.findSectionByRVA(importAddressTableRva);
        ByteBuffer importAddressEntryRaw = importAddressTableSection.getByVirtualAddress(importAddressTableRva);
        for (int i = 0; true; i++) {
            if (is64Bit) {
                ImportLookupEntryPE32Plus lookupEntry = new ImportLookupEntryPE32Plus(BinIOTools.getLong(importLookupEntryRaw));
                importLookupTableSection.addUsage(importLookupTableRva + i * ImportLookupEntryPE32Plus.SIZE, ImportLookupEntryPE32Plus.SIZE, "ImportLookupEntryPE32Plus#" + i);
                ImportLookupEntryPE32Plus addressEntry = new ImportLookupEntryPE32Plus(BinIOTools.getLong(importAddressEntryRaw));
                importAddressTableSection.addUsage(importAddressTableRva + i * ImportLookupEntryPE32Plus.SIZE, ImportLookupEntryPE32Plus.SIZE, "ImportAddressEntryPE32Plus#" + i);
                if (lookupEntry.isEmpty()) {
                    break;
                } else {
                    this.importedFunctions64.add(lookupEntry);
                    if (!lookupEntry.isOrdinal()) {
                        Section hintNameSection = peFormat.findSectionByRVA(lookupEntry.getHintNameRva());
                        HintNameEntry hintNameEntry = HintNameEntry.readFrom(hintNameSection.getByVirtualAddress(lookupEntry.getHintNameRva()));
                        hintNameSection.addUsage(lookupEntry.getHintNameRva(), hintNameEntry.getSize(), "HintNameEntry");
                        String name = hintNameEntry.value;
                        this.resolvedImportedFunctions.add(name);
                    } else {
                        this.resolvedImportedFunctions.add(String.valueOf(lookupEntry.getOrdinal()));
                    }
                }
            } else {
                ImportLookupEntryPE32 lookupEntry = new ImportLookupEntryPE32(BinIOTools.getInt(importLookupEntryRaw));
                importLookupTableSection.addUsage(importLookupTableRva + i * ImportLookupEntryPE32.SIZE, ImportLookupEntryPE32.SIZE, "ImportLookupEntryPE32#" + i);
                ImportLookupEntryPE32 addressEntry = new ImportLookupEntryPE32(BinIOTools.getInt(importAddressEntryRaw));
                importAddressTableSection.addUsage(importAddressTableRva + i * ImportLookupEntryPE32.SIZE, ImportLookupEntryPE32.SIZE, "ImportAddressEntryPE32#" + i);
                if (lookupEntry.isEmpty()) {
                    break;
                } else {
                    this.importedFunctions.add(lookupEntry);
                    if (!lookupEntry.isOrdinal()) {
                        Section hintNameSection = peFormat.findSectionByRVA(lookupEntry.getHintNameRva());
                        HintNameEntry hintNameEntry = HintNameEntry.readFrom(hintNameSection.getByVirtualAddress(lookupEntry.getHintNameRva()));
                        hintNameSection.addUsage(lookupEntry.getHintNameRva(), hintNameEntry.getSize(), "HintNameEntry");
                        String name = hintNameEntry.value;
                        this.resolvedImportedFunctions.add(name);
                    } else {
                        this.resolvedImportedFunctions.add(String.valueOf(lookupEntry.getOrdinal()));
                    }
                }
            }
        }
    }



    public int numEntries() {
        if (importedFunctions != null) {
            return importedFunctions.size();
        } else if (importedFunctions64 != null) {
            return importedFunctions64.size();
        } else {
            throw new IllegalStateException();
        }
    }

    public int sizeInBytes() {
        if (importedFunctions != null) {
            return importedFunctions.size() * ImportLookupEntryPE32.SIZE;
        } else if (importedFunctions64 != null) {
            return importedFunctions64.size() * ImportLookupEntryPE32Plus.SIZE;
        } else {
            throw new IllegalStateException();
        }
    }
    
    
}
