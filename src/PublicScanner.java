import java.util.ArrayList;
import java.util.List;

//CLASS FOR SCANNING THROUGH ALL FILES BEFORE PARSING THE CODE IN ORDER TO 
//BE ABLE TO CHECK FUNCTIONS HAVE BEEN DECLARED WHEN REFERENCED BEFORE THEY ARE
//DECLARED
public class PublicScanner {
	public List<SymbolTable> MySymbolTables;
	public ArrayList<ArrayList<Procedure>> table;
	private Lexer MyLexer;
	public PublicScanner(){
		//2d list in case there is more than 1 class in a file
		
		 table = new ArrayList<ArrayList<Procedure>>();
	}
	
	//Scans through the code before parsing in order to check functions
	public void scan(ArrayList<String> filenames) {
		boolean test;
		Token t;
		String type = "";
		int localCount;
		boolean first = true;
		boolean method = false;
		String name = "";
		ArrayList<String> arguments = new ArrayList<String>();
		int count = 0;
		
		for(int i = 0; i < filenames.size(); i ++) {
			MyLexer = new Lexer();
			test = MyLexer.initialize(filenames.get(i));
			if(test == false) {
				System.out.println("Unable to initialise the lexer");
			}
			else {
				System.out.println("Parser initialised");
			}
			first = true;
			localCount = 0;
			while(MyLexer.peekNextToken().type != Token.TokenTypes.EOF) {
				table.add(new ArrayList<Procedure>());
				t = MyLexer.getNextToken();	
				
				if(t.lexeme.equals("method") || t.lexeme.equals("function") || t.lexeme.equals("constructor") ) {
					//localCount = 0;
					if(first != true) {
						table.get(count).add(addProcedure(name, type, arguments, localCount, method));
					}
					
					method = false;
					if(t.lexeme.equals("method"))
						method = true;
					else
						first = false;
					arguments = new ArrayList<String>();
					t = MyLexer.getNextToken();			
					type = t.lexeme;
					t = MyLexer.getNextToken();
					name = t.lexeme;
					t = MyLexer.getNextToken();
					if(t.lexeme.equals("(")) {
						t = MyLexer.getNextToken();
						if(t.type == Token.TokenTypes.id || t.lexeme.equals("int")
								|| t.lexeme.equals("char") || t.lexeme.equals("boolean"))
							arguments.add(t.lexeme);
						t = MyLexer.getNextToken();
						t = MyLexer.getNextToken();
						while(t.lexeme.equals(",")) {
							t = MyLexer.getNextToken();
							if(t.type == Token.TokenTypes.id || t.lexeme.equals("int")
									|| t.lexeme.equals("char") || t.lexeme.equals("boolean"))
								arguments.add(t.lexeme);
							t = MyLexer.getNextToken();
							t = MyLexer.getNextToken();
						}
						localCount = 0;
						
						
					}
					
				}
				if(t.lexeme.equals("var")) {
					localCount ++;
					t = MyLexer.getNextToken();
					t = MyLexer.getNextToken();
					t = MyLexer.getNextToken();
					while(t.lexeme.equals(",")) {
						localCount ++;
						t = MyLexer.getNextToken();
						t = MyLexer.getNextToken();
						
					}
					
				}
					
			}
			table.get(count).add(addProcedure(name, type, arguments, localCount, method));
			count ++;
		}
	}
	
	//adds subroutine when encountered with key values
	private Procedure addProcedure(String name, String type, ArrayList<String> arguments, int localCount, boolean method) {
		 Procedure new_proc = new Procedure();
		 new_proc.method = method;
		 new_proc.type = type;
		 new_proc.name = name;
		 new_proc.arguments = arguments;
		 new_proc.localCount = localCount;
		 
		 
		 return new_proc;
	}
	
	//checks if the subroutine exists 
	public boolean checkFull(String name) {
		for(int i = 0; i < table.size(); i ++) {
			for (Procedure s : table.get(i))
			 {
				 if (s.name.equals(name))
					 return true;
				 }
		}
		return false;
	}
	
	//checks if the method exists in given class
	public boolean checkMethod(String name, int CurrentClass) {
		for (Procedure s : table.get(CurrentClass))
		 {
			 if (s.name.equals(name))
				 return s.method;
			 }
		return false;
	}
	
	//gets the type of subroutine, given the class
	public String getType(String name, int CurrentClass) {
		for (Procedure s : table.get(CurrentClass))
		 {
			 if (s.name.equals(name))
				 return s.type;
			 }
		return "-";
	}
	
	//checks if the subroutine exists in the given class
	public boolean checkPublic(String name, int CurrentClass) {
		for (Procedure s : table.get(CurrentClass))
		 {
			 if (s.name.equals(name))
				 return true;
			 }
		return false;
	}
	
	//gets the number of local variables in the given class
	public int getLocal(String name, int CurrentClass){
		 for (Procedure s : table.get(CurrentClass))
		 {
			 if (s.name.equals(name))
				 return s.localCount;
			 }
		 return 0;
	}
	
	//returns the arguments in the given subroutine
	public ArrayList<String> getArguments(String name, int CurrentClass){
		 ArrayList<String> arguments = new ArrayList<String>();
		 for (Procedure s : table.get(CurrentClass))
		 {
			 if (s.name.equals(name))
				 return s.arguments;
			 }
		 return arguments;
	}
}
