package llj.asm.intel;

public interface LabelResolver {
    
    public int resolve(String label, Instruction requester);
    
    public int resolveRelativeTo(String label, Instruction requester);
}
