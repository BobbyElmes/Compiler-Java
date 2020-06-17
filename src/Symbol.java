
//Template for each symbol in the symbol table
public class Symbol
 {
 public enum SymbolKind {id, var, argument, Static, field, function, method, Class;};
 public SymbolKind kind;
 public String name;
 public String type;
 public boolean initialized;
 public int offset;
 }
