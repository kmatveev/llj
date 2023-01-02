package llj.asm.intel;

import java.util.Optional;

public interface Directive {
    
    public enum Keyword {
        PROC, ENDP, SEGMENT, ENDS;

        public static Optional<Keyword> get(String str) {
            try {
                return Optional.of(Keyword.valueOf(str.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        
    }
    
}
