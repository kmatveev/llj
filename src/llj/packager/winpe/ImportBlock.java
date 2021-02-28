package llj.packager.winpe;

import java.util.ArrayList;
import java.util.List;

public class ImportBlock {
    
    public ImportDirectoryTableEntry importTableEntry;
    public String name;
    
    public List<ImportLookupEntryPE32> importedFunctions;
    public List<ImportLookupEntryPE32Plus> importedFunctions64;
    public List<String> resolvedImportedFunctions = new ArrayList<>();
    
    public ImportBlock(ImportDirectoryTableEntry header, boolean is64Bit) {
        this.importTableEntry = header;
        if (is64Bit) {
            importedFunctions64 = new ArrayList<>();
        } else {
            importedFunctions = new ArrayList<>();
        }
    }
    
}
