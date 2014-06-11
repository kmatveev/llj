package llj.packager.winpe;

import java.util.ArrayList;
import java.util.List;

public class PEVerifier {

    public static List<String> validate(PEFormat peFormat) {

        List<String> errors = new ArrayList<String>();

        if (peFormat.peHeader.SizeOfOptionalHeader != PEFormat.PEOptionalHeader.SIZE) errors.add("peHeader.SizeOfOptionalHeader is not equal to size of optional header");

        if (peFormat.peHeader.NumberOfSections != peFormat.sections.size()) errors.add("peHeader.NumberOfSections is not equal to number or sections");

        if (peFormat.dosHeader.e_magic != PEFormat.DOSHeader.MAGIC) errors.add("dosHeader.Magic has incorrect value");

        if (peFormat.peOptionalHeader.Magic != PEFormat.PEOptionalHeader.MAGIC) errors.add("peFormat.peOptionalHeader.Magic has incorrect value");



        // if (peFormat.peOptionalHeader.SizeOfCode

        return errors;
    }


}
