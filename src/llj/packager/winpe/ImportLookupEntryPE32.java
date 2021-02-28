package llj.packager.winpe;

public class ImportLookupEntryPE32 {
    
    public static final int SIZE = 4;
    
    public int importLookupEntryVal;

    public ImportLookupEntryPE32(int importLookupEntryVal) {
        this.importLookupEntryVal = importLookupEntryVal;
    }

    public boolean isOrdinal() {
        return importLookupEntryVal < 0;
    }
    
    public int getOrdinal() {
        return importLookupEntryVal & 0xFFFF;
    }
    
    public int getHintNameRva() {
        return importLookupEntryVal & 0x7FFFFFFF;
    }

    public void setHintNameRva(int rva) {
        importLookupEntryVal = (rva & 0x7FFFFFFF);
    }

    public void setOrdinal(int rva) {
        if (rva > 0xFFFF) {
            throw new IllegalArgumentException();
        }
        importLookupEntryVal = (1 << 32) | (rva & 0xFFFF);
    }
    
    public boolean isEmpty(){
        return importLookupEntryVal == 0;
    }
    
}
