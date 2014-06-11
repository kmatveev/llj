package llj.asm.bytecode;

import java.util.Stack;

public class OpStack {

    public final Stack<Type> content = new Stack<Type>();

    @Override
    protected OpStack clone() {
        OpStack opStack = new OpStack();
        opStack.content.addAll(this.content);
        return opStack;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OpStack) {
            return ((OpStack)obj).content.equals(this.content);
        } else {
            return false;
        }
    }
}
