package llj.packager.jclass;

import llj.packager.jclass.attributes.Attribute;
import llj.packager.jclass.constants.ClassRefConstant;
import llj.packager.jclass.constants.Constant;
import llj.packager.jclass.constants.ConstantRef;

import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassDump {

    public static void main(String[] args) {
        try {
            dump("C:\\projects\\eq-solver\\classes\\CompoundExpression.class");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dump(String file) throws Exception {

        Path path = FileSystems.getDefault().getPath(file);
        Set<OpenOption> opts = new HashSet<OpenOption>();
        opts.add(StandardOpenOption.READ);
        FileChannel fileChannel = FileChannel.open(path, opts);

        ClassFileFormat classFormat = ClassFileFormat.readFrom(fileChannel);

        List<String> errors = classFormat.validate();
        System.out.println("Found " + errors.size() + " validation errors");

        {
            System.out.println(" Size of constant pool: " + classFormat.constPool.size());
        }

        {
            ClassRefConstant classConst = classFormat.thisClassRef.resolve();
            if (classConst.getType() != Constant.ConstType.CLASS_REF) throw new RuntimeException();
            String name = classConst.resolveName();
            System.out.println("Name of class: " + name);
        }

        {
            ClassRefConstant classConst = classFormat.parentClassRef.resolve();
            if (classConst.getType() != Constant.ConstType.CLASS_REF) throw new RuntimeException();
            String name = classConst.resolveName();
            System.out.println("Name of parent class: " + name);
        }

        {
            System.out.println("Implemented interfaces:");
            for (ConstantRef<ClassRefConstant> interfaceRef : classFormat.interfaces) {
                ClassRefConstant interfaceDesc = interfaceRef.resolve();
                String name = interfaceDesc.resolveName();
                System.out.println("   " + name);
            }
        }

        {
            System.out.println("Fields:");
            for (FieldInfo fieldDesc : classFormat.fields) {
                String fieldName = fieldDesc.name.resolve().value;
                String fieldType = fieldDesc.descriptor.resolve().value;
                System.out.println("   " + fieldName + " => " + fieldType + " (" + fieldDesc.attributes.size() + " attribs)");
            }
        }

        {
            System.out.println("Methods:");
            for (MethodInfo methodDesc : classFormat.methods) {

                String methodName = methodDesc.name.resolve().value;
                String methodType = methodDesc.descriptor.resolve().value;
                System.out.println("   " + methodName + " => " + methodType);

                System.out.print("        " + methodDesc.attributes.size() + " attributes: ");
                for (Attribute attribute : methodDesc.attributes) {
                    System.out.print(attribute.resolveName() + ", ");
                }
                System.out.println(" ");
            }
        }

        {
            System.out.println("Class attributes:");
            for (Attribute attribute : classFormat.attributes) {
                String attrName = attribute.resolveName();
                System.out.println("   " + attrName);
            }
        }


    }




}
