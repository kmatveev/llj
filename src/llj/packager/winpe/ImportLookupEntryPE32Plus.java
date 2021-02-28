package llj.packager.winpe;

public class ImportLookupEntryPE32Plus {
    
    public static final int SIZE = 8;
    
    public long importLookupEntryVal;

    public ImportLookupEntryPE32Plus(long importLookupEntryVal) {
        this.importLookupEntryVal = importLookupEntryVal;
    }

    public boolean isOrdinal() {
        return importLookupEntryVal < 0;
    }

    public int getOrdinal() {
        return (int)(importLookupEntryVal & 0xFFFF);
    }

    public int getHintNameRva() {
        return (int)(importLookupEntryVal & 0x7FFFFFFF);
    }
    
    public void setHintNameRva(long rva) {
        if (rva > 0x7FFFFFFF) {
            throw new IllegalArgumentException();
        }
        importLookupEntryVal = (rva & 0x7FFFFFFF);
    }

    public void setOrdinal(long rva) {
        if (rva > 0xFFFF) {
            throw new IllegalArgumentException();
        }
        importLookupEntryVal = (1 << 64) | (rva & 0xFFFF);
    }
    public boolean isEmpty(){
        return importLookupEntryVal == 0;
    }
    

}
