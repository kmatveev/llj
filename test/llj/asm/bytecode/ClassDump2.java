package llj.asm.bytecode;

import llj.packager.jclass.ClassFileFormat;
import llj.util.ref.MapResolver;

import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassDump2 {

    public static void main(String[] args) {
        try {
            dump("c:\\tools\\rmiviewer\\ShowRMIRegistry.class");
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

        MapResolver<ClassData, String> cache = new MapResolver<ClassData, String>();

        ClassData classData = new ClassData(classFormat);

        cache.cache.put(classData.name, classData);

        classData.linkAll(cache);

        {
            System.out.println("Name of class: " + classData.name);
        }

        {
            System.out.println("Name of parent class: " + classData.parent.id);
        }

//        {
//            System.out.println("Implemented interfaces:");
//            for (ClassFileFormat.ConstantRef<ClassFileFormat.ClassRefConstant> interfaceRef : classFormat.interfaces) {
//                ClassFileFormat.ClassRefConstant interfaceDesc = interfaceRef.pointTo();
//                String name = interfaceDesc.resolveName().value;
//                System.out.println("   " + name);
//            }
//        }

        {
            System.out.println("Fields:");
            for (FieldData field : classData.fields) {
                System.out.println("   " + field.name + " => " + field.type.toCode());
            }
        }

        {
            System.out.println("Methods:");
            for (MethodData method : classData.methods) {

                System.out.println(method.toSignature() + " {");

                List<Instruction> code = method.code;
                for (int i = 0; i < code.size(); i++) {
                    System.out.println("       " + i + "  " + code.get(i).toString());
                }
                System.out.println("    }");
            }
        }




//        {
//            String name = classFormat.parentClass.resolveName(classFormat.constPool.constants).value;
//            System.out.println("Name of parent class:" + name);
//        }



    }



}
