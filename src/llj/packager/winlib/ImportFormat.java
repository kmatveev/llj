package llj.packager.winlib;

import llj.packager.Format;
import llj.util.BinTools;

import java.nio.ByteBuffer;

public class ImportFormat implements Format {
    
    public ImportHeader header;
    public String importedSymbol, dllName;
    
    public void readFrom(ByteBuffer bb) {
        header = new ImportHeader();
        header.readFrom(bb);
        importedSymbol = BinTools.readZeroTerminatedAsciiString(bb);
        dllName = BinTools.readZeroTerminatedAsciiString(bb);
    }

    @Override
    public void writeTo(ByteBuffer out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public String getStringValue() {
        return "ImportFormat";
    }
    
    public void makeValid() {
        header.sig1 = ImportHeader.SIG1;
        header.sig2 = ImportHeader.SIG2;
        header.sizeOfData = importedSymbol.length() + 1 + dllName.length() + 1;
    }
}
