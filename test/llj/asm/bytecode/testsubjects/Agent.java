package llj.asm.bytecode.testsubjects;

public class Agent {

    public int method() {
        Child child = new Child();
        child.stringField = "stringValue";
        String obtained = child.stringField;
        return obtained.length();
    }

    public static void main(String[] args) {
        Agent agent = new Agent();
        int result = agent.method();
    }

}
