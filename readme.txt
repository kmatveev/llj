LLJ is a Java library for reading and mofifying binary files containing executable code. For now it is only Java
class files.

Package llj.packager.jclass gives you raw access to Java .class files. Classes in this package follow
the underlying format as close as possible. For detailed documentation, see JVM specification. For a simple example
of how to work with the code, check test/llj.packager.jclass.ClassDump, just replace the path to the class file
with something valid.

Package llj.asm.bytecode gives you high-level representation of JVM class model without details of how class files are
stored. The focus here is on instructions of which methods are composed. Since code of valid class files must follow
certain rules regarding type safety, we provide some tools to ensure your code is typesafe. For a simple example
of how to work with the code of this package, check test/llj.asm.bytecode.ClassDump2, just replce the path to the class
file with something valid.

Package llj.asm.bytecode.exec is a simple execution engine of Java bytecodes.