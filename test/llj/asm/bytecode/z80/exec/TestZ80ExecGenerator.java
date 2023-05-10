package llj.asm.bytecode.z80.exec;

import llj.asm.z80.exec.Z80ExecGenerator;
import llj.asm.z80.exec.Z80SlowTemplate;

import java.io.File;

public class TestZ80ExecGenerator {

    public static void main(String[] args) throws Exception {
        Z80ExecGenerator gen = new Z80ExecGenerator();
        gen.generate(new File("Z80SlowTemplate.java"), new File("Z80Slow.java"), new Z80SlowTemplate());
    }
}
