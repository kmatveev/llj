package llj.packager.winpe;

public class ExportAddressTableEntry {

    public static final int SIZE = 4;

    public long exportRvaOrForwarderRva;

    public ExportAddressTableEntry(long exportRvaOrForwarderRva) {
        this.exportRvaOrForwarderRva = exportRvaOrForwarderRva;
    }
}
