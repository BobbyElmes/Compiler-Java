import java.util.List;
import java.util.ArrayList;
//Holds all of the symbols encountered in the parser
//in oreder for semantic analysis to take place
public class SymbolTable
 {
	 List<Symbol> table;
	 int Count, argCount;
	 public SymbolTable()
	{
		 table = new ArrayList<Symbol>();
		 Count = 0;
		 argCount = 0;
	}
	 
	 //adds a symbol to the table
	 public void AddSymbol(String name, Symbol.SymbolKind kind, String type)
	{
		 Symbol new_symbol = new Symbol();
		 new_symbol.type = type;
		 new_symbol.name = name;
		 new_symbol.kind = kind;
		 if(kind == Symbol.SymbolKind.argument)
			 new_symbol.offset = argCount++;
		 else
			 new_symbol.offset = Count++;
		 table.add(new_symbol);
	}
	 
	 //finds the symbol 'name'
	 public boolean FindSymbol(String name)
	{
		 for (Symbol s : table)
		 {
			 if (s.name.equals(name))
				 return true;
			 }
			 return false;
	}
	 
	 //sets initialized to true
	 public void initialized(String name) {
		 for (Symbol s : table)
		 {
			 if (s.name.equals(name))
				 s.initialized = true;
			 }
	 }
	 
	 //gets the offset of a given symbol
	 //(for code generation)
	 public int getOffset(String name) {
		 for (Symbol s : table)
		 {
			 if (s.name.equals(name))
				 return s.offset;
			 }
		 return -1;
	 }
	 
	 //returns the kind of symbol
	 public Symbol.SymbolKind getSymbolKind(String name) {
		 for (Symbol s : table)
		 {
			 if (s.name.equals(name))
				 return s.kind;
			 }
		 return Symbol.SymbolKind.var;
	 }
	 
	//checks if the symbol is initialised 
	//(for variable checking)
	 public boolean checkinitialized(String name) {
		 for (Symbol s : table)
		 {
			 if (s.name.equals(name))
				 if(s.initialized == true)
					 return true;
			 }
		 return false;
	 }
	 
	 //given the name of the symbol, it returns the type
	 public String getType(String name) {
		 for (Symbol s : table)
		 {
			 if (s.name.equals(name))
					 return s.type;
			 }
		 return "";
	 }
	 
	 //returns the name of the symbol given it's place
	 public String getName(int num) {
		 return table.get(num).name;
	 }
	 
	 public void Print ()
	 {
		 for (Symbol s : table)
		 	{
			 System.out.println(s.name + ", " + s.offset);
		 	}
	 }
 }
