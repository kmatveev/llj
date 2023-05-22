package llj.packager.coff;

import java.util.ArrayList;
import java.util.List;

public class Symbol {

    public final long offsetInSymbolsArea;
    public final SymbolTableEntry symbolTableEntry;
    public boolean isAux = false;
    public final List<Symbol> auxSymbols = new ArrayList<>();
    public String resolvedName;
    public Section resolvedSection;

    // special values for section numbers
    public static final int IMAGE_SYM_UNDEFINED = 0, IMAGE_SYM_ABSOLUTE = -1, IMAGE_SYM_DEBUG = -2;

    public Symbol(long offsetInSymbolsArea, SymbolTableEntry symbolTableEntry, boolean isAux) {
        this.offsetInSymbolsArea = offsetInSymbolsArea;
        this.symbolTableEntry = symbolTableEntry;
        this.isAux = isAux;
    }

}
