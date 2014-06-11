package llj.asm.bytecode;

import llj.packager.jclass.ClassFileFormat;
import llj.packager.jclass.FieldInfo;
import llj.packager.jclass.MethodInfo;
import llj.packager.jclass.attributes.Attribute;
import llj.packager.jclass.attributes.Code;
import llj.packager.jclass.constants.ConstantRef;
import llj.packager.jclass.constants.FieldRefConstant;
import llj.packager.jclass.constants.NameTypePairConstant;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class PatchMinecraftLoader {

    public static void main(String[] args) {
        try {
            // patchTestClass();
            patchMinecraft();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void patchMinecraft() throws Exception {
        String in = "C:\\projects\\my\\minecraft\\minecraft-reveng\\stage2-1\\com\\mojang\\authlib\\yggdrasil\\YggdrasilUserAuthentication.class";
        String out = in + ".out";
        String fieldName = "isOnline";
        readThenWrite(in, out, fieldName);
        // checkWritten(out);
    }

    private static void patchTestClass() throws Exception {
        String in = "C:\\projects\\my\\boolean-setfield-code\\TestSetBooleanField.class";
        String out = in + ".out";
        String fieldName = "myField";
        readThenWrite(in, out, fieldName);
    }

    public static void readThenWrite(String from, String to, String fieldName) throws Exception {

        FileChannel inChannel = getFileChannel(from, StandardOpenOption.READ);
        ClassFileFormat classFormat = ClassFileFormat.readFrom(inChannel);
        inChannel.close();

        // find constructor;
        MethodInfo patchedMethod = null;
        for (MethodInfo method : classFormat.methods) {
            if (method.name.resolve().value.equals("<init>")) {
                patchedMethod = method;
                break;
            }
        }

        if (patchedMethod == null) throw new RuntimeException("Cannot find method");

        Code code = null;
        for (Attribute attr : patchedMethod.attributes) {
            if (attr.getType() == Attribute.AttributeType.CODE) {
                code = (Code)attr;
                break;
            }
        }

        if (code == null) throw new RuntimeException("Cannot find code for method");

        System.out.println("Found method to be patched; code located");

        // find field "isOnline";
        FieldInfo patchedField = null;
        for (FieldInfo field : classFormat.fields) {
            if (field.name.resolve().value.equals(fieldName)) {
                patchedField = field;
                break;
            }
        }

        if (patchedField == null) throw new RuntimeException("Cannot find field");
        System.out.println("Found field to be patched:" + fieldName);

        // create NameTypePair constant for field reference. Re-use name and type from field definition
        NameTypePairConstant pairConstant = new NameTypePairConstant(patchedField.name, patchedField.descriptor);
        ConstantRef<NameTypePairConstant> pairRef = classFormat.constPool.addOrGetExisting(pairConstant);

        System.out.println("NameTypePairConstant added to pool at index=" + pairRef.getIndex());

        // create Field ref constant. Re-use this class ref from class definition
        FieldRefConstant fieldRefConstant = new FieldRefConstant(classFormat.thisClassRef, pairRef);
        ConstantRef<FieldRefConstant> refToFieldRef = classFormat.constPool.addOrGetExisting(fieldRefConstant);

        System.out.println("FieldRefConstant added to pool at index=" + refToFieldRef.getIndex());

        // patch method

        byte[] oldCode = code.code;
        System.out.println("Existing code size:" + oldCode.length);

        int insertedInstructionSize = 5;
        byte[] newCode = new byte[oldCode.length + insertedInstructionSize];
        System.arraycopy(oldCode, 0, newCode, 0, oldCode.length);

        System.out.println("Code space was expanded by " + insertedInstructionSize + " bytes, new size:"  + newCode.length);

        int pc = oldCode.length - 1;
        if (InstructionCode.getByCode(0xFF & newCode[pc]) != InstructionCode._return) throw new RuntimeException("No return at the end of the code");
        newCode[newCode.length - 1] = newCode[pc];
        System.out.println("Found 'return' instruction at the end of the old code; moved it to the end of new code to give space for new instructions");

        // push 'this' reference on stack
        newCode[pc] = (byte)InstructionCode.aload_0.code;
        pc++;

        // push boolean value 'true' on stack
        newCode[pc] = (byte)InstructionCode.iconst_1.code;
        pc++;

        newCode[pc] = (byte)InstructionCode.putfield.code;
        pc++;
        newCode[pc] = (byte) ((refToFieldRef.getIndex() >> 8) & 0xFF);
        pc++;
        newCode[pc] = (byte) ((refToFieldRef.getIndex()) & 0xFF);
        pc++;
        if (InstructionCode.getByCode(0xFF & newCode[pc]) != InstructionCode._return) throw new RuntimeException("No return at the end of the inserted code");
        System.out.println("Added some instructions into allocated space. Finished just before return instruction");

        code.code = newCode;

        if (code.maxStack < 2) {
            System.out.println("Stack depth for a method is less then 2, will make it equal 2");
            code.maxStack = 2;
        }

        Set<OpenOption> opts = new HashSet<OpenOption>();
        opts.add(StandardOpenOption.WRITE);
        opts.add(StandardOpenOption.CREATE_NEW);
        FileChannel outChannel = getFileChannel(to, opts);
        classFormat.writeTo(outChannel);
        outChannel.close();

        System.out.println("File was saved. Enjoy!");
    }

    private static FileChannel getFileChannel(String from, StandardOpenOption read) throws IOException {
        Set<OpenOption> opts = new HashSet<OpenOption>();
        opts.add(read);
        return getFileChannel(from, opts);
    }

    private static FileChannel getFileChannel(String from, Set<OpenOption> opts) throws IOException {
        FileChannel inChannel;
        Path path = FileSystems.getDefault().getPath(from);
        inChannel = FileChannel.open(path, opts);
        return inChannel;
    }

    private static void checkWritten(String name) throws Exception {
        FileChannel inChannel = getFileChannel(name, StandardOpenOption.READ);
        ClassFileFormat classFormat = ClassFileFormat.readFrom(inChannel);
        inChannel.close();

    }

}
