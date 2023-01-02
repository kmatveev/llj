package llj.asm.intel;


import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Parser {
    
    public enum State {BEGIN, OPERANDS, IN_COMMENT}
    
    public static interface ParsedResult {
        
    }
    
    public static class ParsedStatement implements ParsedResult {
        String label;
        Instruction instruction;
        String comment;

        public ParsedStatement(Instruction instruction) {
            this.instruction = instruction;
        }
    }

    public static class ParsedDirective implements ParsedResult {
        Directive directive;
    }

    public List<ParsedResult> parseAll(Reader input) throws IOException, SyntaxException {
        List<ParsedResult> results = new ArrayList<>();
        while (true) {
            ParsedResult result = parse(input);
            if (result == null) {
                break;
            } else {
                results.add(result);
            }
        }
        return results;
    }
    
    public ParsedResult parse(Reader input) throws IOException, SyntaxException {
        Lexer lexer = new Lexer();

        ParsedResult result = null;
        State state = State.BEGIN;
        Instruction.MNEMONIC mnemonic = null;
        List<Operand> operands = new ArrayList<>();
        // Instruction instruction; // instruction in progress of parsing
        
        while (true) {
            
            if (state == State.BEGIN) {
                Lexer.LexerResult item = lexer.takeNextItem(input);
                if (item instanceof Lexer.NewLine) {
                    // just ignore
                } else if (item == null) {
                    break;
                } else if (item instanceof Lexer.Word) {
                    Lexer.Word word = (Lexer.Word)item;
                    Optional<Instruction.MNEMONIC> maybeMnemonic = Instruction.MNEMONIC.get(word.text);
                    Optional<Directive.Keyword> maybeDirective = Directive.Keyword.get(word.text);
                    if (maybeMnemonic.isPresent()) {
                        mnemonic = maybeMnemonic.get();
                        operands.clear();
                        state = State.OPERANDS;
                    } else if (maybeDirective.isPresent()) {
                        
                    } else {
                        // expr
                    }
                
                }
            } else if (state == State.OPERANDS) {

                ParsedOperand parsedOp = parseOperand(lexer, input);

                if (parsedOp.nextToken instanceof Lexer.NewLine) {
                    if (parsedOp.operand != null) {
                        operands.add(parsedOp.operand);
                    }
                    Instruction instruction = Instruction.newInstance(mnemonic, operands);
                    result = new ParsedStatement(instruction);
                    state = State.BEGIN;
                    break;
                } else if (parsedOp.nextToken instanceof Lexer.Symbol) {
                    Lexer.Symbol symbol = (Lexer.Symbol)parsedOp.nextToken;
                    if (symbol.symbol == ',') {
                        if (parsedOp.operand != null) {
                            operands.add(parsedOp.operand);
                            // no state switch, we allow as many comma-separated operands as possible, so it's up to instruction to check how many is needed
                        } else {
                            throw new SyntaxException("',' without operand before it");
                        }
                    } else if (symbol.symbol == ';') {
                        if (parsedOp.operand != null) {
                            operands.add(parsedOp.operand);
                        }
                        Instruction instruction = Instruction.newInstance(mnemonic, operands);
                        result = new ParsedStatement(instruction);
                        state = State.IN_COMMENT;
                    } else {
                        throw new SyntaxException("Unexpected symbol");
                    }
                    
                }
                
            } else if (state == State.IN_COMMENT) {
                Lexer.LexerResult item = lexer.takeNextItem(input);
                if (item instanceof Lexer.NewLine || item == null) {
                    state = State.BEGIN;
                    break;
                }
            }
        }
        return result;
    }
    
    public static class ParsedOperand {
        public final Operand operand;
        public final Lexer.LexerResult nextToken;

        public ParsedOperand(Operand operand, Lexer.LexerResult nextToken) {
            this.operand = operand;
            this.nextToken = nextToken;
        }
    }
    
    public ParsedOperand parseOperand(Lexer lexer, Reader input) throws IOException, SyntaxException  {
        
        Operand op = null;
        boolean squaredOpen = false, squaredClosed = false;

        Lexer.LexerResult item = null;
        while (true) {
            item = lexer.takeNextItem(input);
            if (item instanceof Lexer.NewLine || item == null) {
                break; // new line, end of instruction
            } else if (item instanceof Lexer.Whitespace) {
                // just ignore
            } else if (item instanceof Lexer.Word) {
                Lexer.Word word = (Lexer.Word)item;
                if (squaredOpen) {
                    // TODO parse further
                    Optional<Operand.Register> maybeReg = Operand.Register.get(word.text);
                    if (maybeReg.isPresent()) {
                        op = new Operand.MemoryContentOp(Operand.Expression.register(maybeReg.get()));
                    } else {
                        op = new Operand.MemoryContentOp(Operand.Expression.label(word.text));
                    }
                } else {
                    Optional<Operand.Register> maybeReg = Operand.Register.get(word.text);
                    if (maybeReg.isPresent()) {
                        op = new Operand.RegisterOp(maybeReg.get());
                    } else {
                        op = Operand.ImmediateOp.fromLabel(word.text);
                    }
                }
            }  else if (item instanceof Lexer.Number) {
                Lexer.Number num = (Lexer.Number)item;
                op = Operand.ImmediateOp.fromConstant(num.number);
            } else if (item instanceof Lexer.Symbol) {
                Lexer.Symbol symbol = (Lexer.Symbol)item;
                if (symbol.symbol == ',') {
                    break; // comma, separator of operands
                } else if (symbol.symbol == ';') {
                    break;  // comment, end of operand
                } else if (symbol.symbol == '[') {
                    if (squaredOpen) {
                        throw new SyntaxException("[ inside ["); 
                    }
                    squaredOpen = true;
                    squaredClosed = false;
                } else if (symbol.symbol == ']') {
                    if (!squaredOpen) {
                        throw new SyntaxException("] without [");
                    }
                    squaredOpen = true;
                    squaredClosed = true;
                    
                }
            }
 
        }
        
        if (squaredOpen && !squaredClosed) {
            throw new SyntaxException("[ without ]");
        }
        return new ParsedOperand(op, item);
    }

    public static class SyntaxException extends Exception {
        public SyntaxException(String message) {
            super(message);
        }
    }
    
    public static class Lexer {
        public enum State {
            BEGIN, IN_NUMBER, IN_WORD, IN_WHITESPACE
        }
        
        private StringBuilder buffer = new StringBuilder();
        
        private State state = State.BEGIN;
        
        private LexerResult[] resultBuffer;
        private int resultPointer = 0;
        
        public void reset() {
            state = State.BEGIN;
            buffer.setLength(0);
        }
        
        public LexerResult takeNextItem(Reader input) throws IOException {
            LexerResult result;
            if (resultBuffer == null) {
                resultBuffer = readNextItem(input);
                if (resultBuffer == null) {
                    return null;
                }
                result = resultBuffer[0];
                if (resultBuffer.length == 1) {
                    resultBuffer = null;
                } else {
                    resultPointer = 1;
                }
            } else {
                result = resultBuffer[resultPointer];
                resultPointer++;
                if (resultPointer == resultBuffer.length) {
                    resultBuffer = null;
                }
            }
            return result;
        }
        
        public LexerResult[] readNextItem(Reader input) throws IOException {
            
            while (true) {
                int c = input.read();
                if (c < 0) return null;
                if (state == State.IN_WHITESPACE || state == State.BEGIN) {
                    if (Character.isAlphabetic(c) || c == '_') {
                        state = State.IN_WORD;
                        buffer.setLength(0);
                        buffer.append((char) c);
                        if (state == State.IN_WHITESPACE) {
                            return new LexerResult[] {new Whitespace()};
                        }
                    } else if (isLineSeparator((char) c)) { 
                        // This check should go before whitespace check, since line separators are whitespaces!
                        state = State.BEGIN;
                        return new LexerResult[] { new NewLine()};
                    } else if (Character.isWhitespace(c)) {
                        // ignore
                    } else if (isSymbol((char) c)) {
                        state = State.BEGIN;
                        return new LexerResult[] {new Symbol((char)c)};
                    } else if (Character.isDigit(c)) {
                        state = State.IN_NUMBER;
                        buffer.setLength(0);
                        buffer.append((char) c);
                        if (state == State.IN_WHITESPACE) {
                            return new LexerResult[] {new Whitespace()};
                        }
                    } else {
                        throw new RuntimeException();
                    }
                } else if (state == State.IN_WORD) {
                    if (Character.isAlphabetic(c) || c == '_') {
                        buffer.append((char) c);
                    } else if (Character.isDigit(c)) {
                        buffer.append((char) c);
                    } else if (isLineSeparator((char) c)) {
                        // This check should go before whitespace check, since line separators are whitespaces!
                        state = State.BEGIN;
                        Word word = new Word(buffer.toString());
                        buffer.setLength(0);
                        return new LexerResult[] {word, new NewLine()};
                    } else if (Character.isWhitespace(c)) {
                        state = State.IN_WHITESPACE;
                        Word word = new Word(buffer.toString());
                        buffer.setLength(0);
                        return new LexerResult[] {word};
                    } else if (isSymbol((char) c)) {
                        state = State.BEGIN;
                        Word word = new Word(buffer.toString());
                        buffer.setLength(0);
                        return new LexerResult[] {word, new Symbol((char)c)};
                    } else {
                        throw new RuntimeException();
                    }
                    
                } else if (state == State.IN_NUMBER) {
                    if (Character.isAlphabetic(c)) {
                        throw new RuntimeException();
                    } else if (Character.isDigit(c)) {
                        buffer.append((char) c);
                    } else if (isLineSeparator((char) c)) {
                        // This check should go before whitespace check, since line separators are whitespaces!
                        state = State.BEGIN;
                        Number number = new Number(Integer.parseInt(buffer.toString()));
                        buffer.setLength(0);
                        return new LexerResult[] {number, new NewLine()};
                    } else if (Character.isWhitespace(c)) {
                        state = State.IN_WHITESPACE;
                        Number number = new Number(Integer.parseInt(buffer.toString()));
                        buffer.setLength(0);
                        return new LexerResult[] {number};
                    } else if (isSymbol((char) c)) {
                        state = State.BEGIN;
                        Number number = new Number(Integer.parseInt(buffer.toString()));
                        buffer.setLength(0);
                        return new LexerResult[] {number, new Symbol((char)c)};
                    } else {
                        throw new RuntimeException();
                    }
                    
                } else {
                    throw new RuntimeException();
                }
            }
            
        }

        public static boolean isSymbol(char c) {
            return c == ';' || c == '[' || c == ']' || c == '(' || c == ')' || c == '+' || c == '-' || c == ',' || c == ':';
        }

        public static boolean isLineSeparator(char c) {
            // TODO try Character.LINE_SEPARATOR
            return (c == '\r') || (c =='\n');
        }

        public interface LexerResult {}

        public static class Number implements LexerResult {
            public final int number;

            public Number(int number) {
                this.number = number;
            }
        }

        public static class Word implements LexerResult {
            public final java.lang.String text;

            public Word(java.lang.String text) {
                this.text = text;
            }
        }

        public static class Symbol implements LexerResult {
            public final char symbol;

            public Symbol(char symbol) {
                this.symbol = symbol;
            }
        }

        public static class String implements LexerResult {
            public final String string;

            public String(String string) {
                this.string = string;
            }
        }

        public static class NewLine implements LexerResult {
        }
        
        public static class Whitespace implements LexerResult {
            
        }
        
    }
    
    
    
}
