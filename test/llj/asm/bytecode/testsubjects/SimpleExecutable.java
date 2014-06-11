package llj.asm.bytecode.testsubjects;

public class SimpleExecutable {

    private int field;

    public SimpleExecutable(int fieldVal) {
        this.field = fieldVal;
    }

    public int addToField(int val) {
        this.field = this.field + val;
        return this.field;
    }

    public static void main(String[] args) {

        int a = 1;

        int b = 2;

        int c = a + b;

        SimpleExecutable object = new SimpleExecutable(a);

        int d = object.addToField(c);

    }

}
