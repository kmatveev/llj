package llj.asm.z80.exec;

import java.io.*;

public class Z80ExecGenerator {

    public static final String template1 = "execInstruction1();";

    public void generate(File in, File out, Z80Template template) throws IOException {

        Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(in)));
        Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(out)));

        InstructionDispatchGenerator dispatchGenerator = new InstructionDispatchGenerator(template);

        String currentTemplate = template1;
        int idx = 0;

        while (true) {
            int ch = reader.read();
            if (ch >= 0) {
                if ((currentTemplate != null) && (ch == currentTemplate.charAt(idx))) {
                    idx++;
                    if (idx == currentTemplate.length()) {
                        StringBuilder sb = new StringBuilder();
                        dispatchGenerator.generateSingleOpcodeInstructionDispatch(sb);
                        writer.write(sb.toString());
                        idx = 0;
                        // replace with progressing to next template and next generator
                        currentTemplate = null;
                        dispatchGenerator = null;
                    }
                } else {
                    if ((idx != 0) && (currentTemplate != null)){
                        writer.write(currentTemplate.substring(0, idx));
                        idx = 0;
                    }
                    writer.write(ch);
                }
            } else {
                reader.close();
                writer.flush();
                writer.close();
                break;
            }
        }

    }

}
