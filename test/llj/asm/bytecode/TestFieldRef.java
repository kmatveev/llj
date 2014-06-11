package llj.asm.bytecode;

import llj.packager.jclass.ClassFileFormat;

import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestFieldRef {

    public static final String TEST_SUBJECT_ROOT = "out/test/llj";

    public static void main(String[] args) {
        try {
            testFieldRef();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void testFieldRef() throws Exception {

        Path path = FileSystems.getDefault().getPath(TEST_SUBJECT_ROOT + "/kmatveev/llj/asm/bytecode/testsubjects/Agent.class");
        Set<OpenOption> opts = new HashSet<OpenOption>();
        opts.add(StandardOpenOption.READ);
        FileChannel fileChannel = FileChannel.open(path, opts);

        ClassFileFormat format = ClassFileFormat.readFrom(fileChannel);

        ClassData classData = new ClassData(format);

        MethodData method = classData.getMethod("method", Collections.<Type>emptyList());

        List<Instruction> code = method.code;
        if (code.get(6).code == InstructionCode.putfield) {
            FieldRefInstruction putField = (FieldRefInstruction) code.get(6);
            System.out.println("putfield " + putField.fieldRef);
        }

        if (code.get(8).code == InstructionCode.getfield) {
            FieldRefInstruction getField = (FieldRefInstruction) code.get(8);
            System.out.println("getfield " + getField.fieldRef);
        }

    }

}
