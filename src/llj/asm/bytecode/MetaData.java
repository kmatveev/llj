package llj.asm.bytecode;

import java.util.ArrayList;
import java.util.List;

public class MetaData {

    public OpStack stackStateBefore = null;
    public boolean handled = false;
    public boolean isConsistent = true;


    public final List<StateStaticInfo> from = new ArrayList<StateStaticInfo>();

    public static final class StateStaticInfo {
        public final OpStack stackStateBefore;
        public final int jumpedFromOffset;

        public StateStaticInfo(OpStack stackStateBefore, int jumpedFromOffset) {
            this.stackStateBefore = stackStateBefore;
            this.jumpedFromOffset = jumpedFromOffset;
        }
    }

    public boolean isStackConsistent() {
        if (from.size() < 1) {
            return false;
        } else if (from.size() == 1) {
            return true;
        } else {
            OpStack measure = from.get(0).stackStateBefore;
            for (StateStaticInfo stackStateInfo : from) {
                if (!stackStateInfo.stackStateBefore.equals(measure)) return false;
            }
            return true;
        }
    }
}
