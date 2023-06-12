package llj.packager.winpe;

import llj.packager.coff.Section;

import java.util.ArrayList;
import java.util.List;

public class ExportBlock {

    public ExportDirectoryTableEntry exportTableEntry;
    public String name;

    public List<ExportAddressTableEntry> exportedFunctions;

    // these two lists must have same size
    public List<String> exportedFunctionNames;
    public List<Integer> exportedFunctionOrdinalIndexes;

    public ExportBlock(ExportDirectoryTableEntry header) {
        this.exportTableEntry = header;
        exportedFunctions = new ArrayList<>();
        exportedFunctionNames = new ArrayList<>();
        exportedFunctionOrdinalIndexes = new ArrayList<>();
    }

    public int numAddressEntries() {
        return exportedFunctions.size();
    }

    public int numFunctionEntries() {
        return exportedFunctionNames.size();
    }


    public int sizeInBytes() {
        return exportedFunctions.size() * ExportAddressTableEntry.SIZE;
    }

    public ExportAddressTableEntry getByName(String name) {
        for (int i = 0; i < exportedFunctionNames.size(); i++) {
            String exportedFuncName = exportedFunctionNames.get(i);
            if (exportedFuncName.equals(name)) {
                return getByOrdinal(i);
            }
        }
        return null;
    }

    public ExportAddressTableEntry getByOrdinal(int ordinal) {
        return exportedFunctions.get(exportedFunctionOrdinalIndexes.get(ordinal));
    }


}
