package llj.packager.coff;

public class COFFStringEntry {

    public final long offsetInStringsArea;
    public final String value;

    public COFFStringEntry(long location, String value) {
        this.offsetInStringsArea = location;
        this.value = value;
    }

    public int numBytes() {
        return value.length() + 1; // one byte for 0-terminator
    }
}
