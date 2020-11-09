package llj.packager.winpe;

import llj.packager.dosexe.DOSHeader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PEVerifier {

    public static List<String> validate(PEFormat peFormat) {

        List<String> errors = new ArrayList<String>();

        if (peFormat.coffHeader.sizeOfOptionalHeader != COFFOptionalHeaderPE32.SIZE) errors.add("coffHeader.sizeOfOptionalHeader is not equal to size of optional header");

        if (peFormat.coffHeader.numberOfSections != peFormat.sections.size()) errors.add("coffHeader.numberOfSections is not equal to number or sections");

        if (!Arrays.equals(peFormat.dosHeader.signature, DOSHeader.MAGIC)) errors.add("dosHeader.signature has incorrect value");

        if (peFormat.coffOptionalHeaderPE32.signature != COFFOptionalHeaderPE32.MAGIC) errors.add("peFormat.coffOptionalHeaderPE.signature has incorrect value");



        // if (peFormat.coffOptionalHeaderPE.sizeOfCode

        return errors;
    }


}
