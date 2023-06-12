package llj.packager.winpe;

public class ExportAddressTableEntry {

    public static final int SIZE = 4;

    public long exportRvaOrForwarderRva;
    public String forwarderValue; // cached value. If exportRvaOrForwarderRva points into the same section as exports table, then it is a forwarder, which we can read and store here for convenience

    public ExportAddressTableEntry(long exportRvaOrForwarderRva) {
        this.exportRvaOrForwarderRva = exportRvaOrForwarderRva;
    }

}
