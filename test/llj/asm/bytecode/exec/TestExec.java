package llj.asm.bytecode.exec;

public class TestExec {

    public static void main(String[] args) {

        VM vm = new VM();

        String[] classpath = new String[] {".\\out\\test\\llj", "..\\llj-rtlib-repack"};

        test2(vm, classpath);

    }

    private static void test1(VM vm, String[] classpath) {
        vm.execute("llj.asm.bytecode.testsubjects.SimpleExecutable", classpath);
    }

    private static void test2(VM vm, String[] classpath) {
        vm.execute("llj.asm.bytecode.testsubjects.Agent", classpath);
    }

}
