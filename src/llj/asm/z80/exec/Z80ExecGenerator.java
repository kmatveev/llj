package llj.asm.z80.exec;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Z80ExecGenerator {

    public void generate(File in, File out, Z80Template template) throws IOException {

        InstructionDispatchGenerator dispatchGenerator = new InstructionDispatchGenerator(template);
        StringBuilder sb = new StringBuilder();

        Map<String, Runnable> templateActions = new HashMap<>();
        templateActions.put("singleOpcodeInstruction();", new Runnable() {
            @Override
            public void run() {
                dispatchGenerator.generateSingleOpcodeInstructionDispatch(sb);
            }
        });
        templateActions.put("xInstruction();", new Runnable() {
            @Override
            public void run() {
                dispatchGenerator.generateIndexDispatch(sb, 0xDD);
            }
        });
        templateActions.put("yInstruction();", new Runnable() {
            @Override
            public void run() {
                dispatchGenerator.generateIndexDispatch(sb, 0xFD);
            }
        });
        templateActions.put("edInstruction();", new Runnable() {
            @Override
            public void run() {
                dispatchGenerator.generateEDDispatch(sb);
            }
        });
        templateActions.put("cbInstruction();", new Runnable() {
            @Override
            public void run() {
                dispatchGenerator.generateCBDispatch(sb);
            }
        });
        templateActions.put("qxBitsInstruction();", new Runnable() {
            @Override
            public void run() {
                dispatchGenerator.generateIndexCBDispatch(sb, 0xDD);
            }
        });
        templateActions.put("wyBitsInstruction();", new Runnable() {
            @Override
            public void run() {
                dispatchGenerator.generateIndexCBDispatch(sb, 0xFD);
            }
        });



        Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(in)));
        Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(out)));

        String currentTemplate = null;
        int idx = 0;

        while (true) {
            int ch = reader.read();
            if (ch >= 0) {

                if (idx == 0) {
                    // check if we may found any template points
                    for (String tmpl : templateActions.keySet()) {
                        if (ch == tmpl.charAt(0)) {
                            if (currentTemplate != null) throw new RuntimeException();  // check
                            currentTemplate = tmpl;
                            // since template point names are more than single character,then we can just advance
                            idx++;
                        }
                    }
                    if (currentTemplate == null) {
                        writer.write(ch);
                    }
                } else {
                    if (ch == currentTemplate.charAt(idx)) {
                        idx++;
                        if (idx == currentTemplate.length()) {
                            // this will update StringBuilder
                            templateActions.get(currentTemplate).run();
                            writer.write(sb.toString());
                            sb.setLength(0);
                            idx = 0;
                            currentTemplate = null;
                        }
                    } else {
                        writer.write(currentTemplate.substring(0, idx));
                        currentTemplate = null;
                        idx = 0;
                        writer.write(ch);
                    }
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
