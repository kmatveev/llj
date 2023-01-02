package llj.asm.intel;

import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.List;

public class TestAsmParser {
    
    public static void main(String[] args) {
        test1();
    }
    
    public static void test1() {
        String txt = "" +
                "mov al,bl\r\n" +    // 8-bit reg to 8-bit reg
                "mov ax, bx\r\n" +   // 16-bit reg to 16-bit reg
                "mov cx, [bx]\r\n" +  // mem by 16-bit reg to 16-bit reg
                "mov dh, [si]\r\n" +  // mem by 16-bit reg to 8-bit reg
                "mov [bx], dx\r\n" + // 16-bit reg to mem by 16-bit reg
                "mov [di], ch\r\n" + // 8-bit reg to mem by 16-bit reg  
                "mov ax, 5\r\n" + // immediate to 16-bit reg
                "mov ah, 5\r\n" + // immediate to 8-bit reg
                "mov ecx, edx\r\n" + 
                "";
        
        
        Parser parser = new Parser();
        List<Parser.ParsedResult> results;
        try {
            results = parser.parseAll(new StringReader(txt));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ByteBuffer bb = ByteBuffer.allocate(1000);
        for (Parser.ParsedResult res : results) {
            if (res instanceof Parser.ParsedStatement) {
                Instruction ins = ((Parser.ParsedStatement) res).instruction;
                ins.putMachineCode(bb, new LabelResolver() {
                    @Override
                    public int resolve(String label, Instruction requester) {
                        return 0;
                    }
                    public int resolveRelativeTo(String label, Instruction requester) {
                        return 0;
                    }
                });
            } else {
                throw new RuntimeException();
            }
        }

        int[] expected = new int[]{
                    0x8a, 0xc3,
                    0x66, 0x8b, 0xc3,
                    0x67, 0x66, 0x8b, 0x0f,
                    0x67,0x8a,0x34,
                    0x67,0x66,0x89,0x17,
                    0x67,0x88,0x2d,
                    0xb8,05,00,
                    0xb4,05
                };
        bb.remaining();
    }
    
}
