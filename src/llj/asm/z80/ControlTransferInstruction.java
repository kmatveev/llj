package llj.asm.z80;

public abstract class ControlTransferInstruction extends Instruction {

    public static enum ConditionFlag {
        C, Z, S, PV
    }

    public static class Condition {
        ConditionFlag flag;
        boolean set;

        public static final Condition C = new Condition(ConditionFlag.C, true);
        public static final Condition NC = new Condition(ConditionFlag.C, false);
        public static final Condition Z = new Condition(ConditionFlag.Z, true);
        public static final Condition NZ = new Condition(ConditionFlag.Z, false);
        public static final Condition M = new Condition(ConditionFlag.S, true);
        public static final Condition P = new Condition(ConditionFlag.S, false);
        public static final Condition PE = new Condition(ConditionFlag.PV, true);
        public static final Condition PO = new Condition(ConditionFlag.PV, false);

        public Condition(ConditionFlag flag, boolean set) {
            this.flag = flag;
            this.set = set;
        }

        public String toString() {
            return (set ? "N" : "") + flag.name();
        }
    }
}
