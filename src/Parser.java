import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
public class Parser {
	private Lexer MyLexer;
	List<List<SymbolTable>> MySymbolTables;
	int CurrentSymbolTable; //holds the current symboltable
	int CurrentClass; //holds the value of currentclass in symbol table
	boolean ret = false; //used so that return values aren't checked for being already initialized
	private int numThis;
	private String dir = "";
	//an array of all the source files used
	private String []sourceFiles = {"Array.jack", "Keyboard.jack", "Math.jack", "Memory.jack", "Output.jack", "Screen.jack", "String.jack", "Sys.jack"};
	private ArrayList<String>files; //for inputed files
	private ArrayList<String>code; //holds the generated code
	private PublicScanner scan; //for scanning over the code to find subroutines
	private String currentSegment, className;
	private boolean not = false, currentMethod = false;

	//Constructor for initializing the parser
	public Parser(){
		//2d list in case there is more than 1 class in a file
		 code = new ArrayList<String>();
		 currentSegment = "static";
		 MySymbolTables = new ArrayList<List<SymbolTable>>();
		 scan = new PublicScanner();
		 files = new ArrayList<String>();
		 CurrentSymbolTable = 0; // 0 is the global symbol table
		 CurrentClass = 0; //0 is the first class

		 for(int i = 0; i < 8; i++) {
			 files.add(sourceFiles[i]);
		 }
	}

	//initialises the lexer and scans/parses all of the files
	public void init(ArrayList<String> filename) {
		boolean test;
		for(int i = 0; i < filename.size(); i ++) {
			System.out.println(filename.get(i));
			files.add(filename.get(i));
		}
		scan.scan(files);
		for(int i = 0; i < files.size(); i++) {
			if(i <= 8)
				code = new ArrayList<String>();
			else {
				printCode();
				code = new ArrayList<String>();
			}
			MyLexer = new Lexer();
			test = MyLexer.initialize(files.get(i));
			if(test == false) {
				System.out.println("Unable to initialise the lexer");
			}
			else {
				//System.out.println("Parser initialised");
			}
			 MySymbolTables.add(new ArrayList<SymbolTable>());
			 entry();
			 CurrentClass ++;
		}
	}


	//for printing out an error to the user
	private void error(Token token, String message) {
		System.out.println("Error on line " + token.lineNum + " at or near " + token.lexeme + ", " + message);
		System.exit(0);
	}

	//confirming the given token abides by the grammar rules
	private void okay(Token token) {
		//System.out.println(token.lexeme + ": OK");
	}

	//entry point for the recursive descent parser
	public void entry() {
		Token t = MyLexer.getNextToken();
		MySymbolTables.get(CurrentClass).add(new SymbolTable());
		CurrentSymbolTable = 0;
		//new symbol table for inside the class

		if(t.lexeme.equals("class")) {
			numThis = 0;
			okay(t);
			t = MyLexer.getNextToken();
			if(t.type == Token.TokenTypes.id) {
				className = t.lexeme;
				if(MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).FindSymbol(t.lexeme) == true) {
					error(t, " Class was redeclared");
				}
				MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).AddSymbol(t.lexeme, Symbol.SymbolKind.Class, className);
				okay(t);
				t = MyLexer.getNextToken();
				if(t.lexeme.equals("{")) {
					okay(t);
					while(!t.lexeme.equals("}")) {
						//MySymbolTables.get(CurrentClass).add(new SymbolTable());
						currentSegment = "static";
						memberDeclar();
						t = MyLexer.peekNextToken();
					}
					if(t.lexeme.equals("}"))
						okay(t);
					else
						error(t, "");
				}
				else {
					error(t, "No class identifier at start of program");
				}
			}
			else
				error(t, "");
		}
		else {
			error(t, "No 'class' declaration at start of program");
		}

	}

	//For declaration of class level vars and procedures
	public void memberDeclar() {
		Token t = MyLexer.peekNextToken();
		//changing currentsymboltable to point at correct table
		//CurrentSymbolTable = MySymbolTables.size()-1;
		if(t.lexeme.equals("field") || t.lexeme.equals("static") ) {
			classVarDeclar();
		}
		else {
			if(t.lexeme.equals("method") || t.lexeme.equals("function")
					|| t.lexeme.equals("constructor")) {
				currentSegment = "argument";
				subroutineDeclar();
			}
			else
				error(t, "");
		}
	}

	//for declaring class variables
	public void classVarDeclar() {
		//type is used to remember the type of each variable to it can be added to the symbol table
		String type = "";
		Token t = MyLexer.getNextToken();
		boolean staticc = false;
		if(t.lexeme.equals("static") || t.lexeme.equals("field")) {
			okay(t);
			if(t.lexeme.equals("static"))
				staticc = true;
			type = type();
		}
		else
			error(t, "");
		t = MyLexer.getNextToken();
		if(t.type == Token.TokenTypes.id) {
			okay(t);
			if(MySymbolTables.get(CurrentClass).get(0).FindSymbol(t.lexeme) == true) {
				error(t, " Variable was redeclared");
			}
			if(staticc == true)
				MySymbolTables.get(CurrentClass).get(0).AddSymbol(t.lexeme, Symbol.SymbolKind.Static, type);
			else {
				MySymbolTables.get(CurrentClass).get(0).AddSymbol(t.lexeme, Symbol.SymbolKind.field, type);
				numThis ++;
			}
			t = MyLexer.peekNextToken();
			while(t.lexeme.equals(",")) {
				MyLexer.getNextToken();
				okay(t);
				t = MyLexer.getNextToken();
				if(t.type == Token.TokenTypes.id) {
					if(MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).FindSymbol(t.lexeme) == true) {
						error(t, " Variable was redeclared");
					}
					if(staticc == true)
						MySymbolTables.get(CurrentClass).get(0).AddSymbol(t.lexeme, Symbol.SymbolKind.Static, type);
					else {
						MySymbolTables.get(CurrentClass).get(0).AddSymbol(t.lexeme, Symbol.SymbolKind.field, type);
						numThis ++;
					}
					okay(t);
				}
				else
					error(t, " ");
				MyLexer.peekNextToken();
			}
		}
		else
			error(t, "");
		t = MyLexer.getNextToken();
		if(t.lexeme.equals(";"))
			okay(t);
		else
			error(t, "");


	}

	//type checking, returns the type of the variable to help semantics and code
	//generation
	public String type() {
		Token t = MyLexer.getNextToken();
		if(t.type == Token.TokenTypes.id || t.lexeme.equals("int")
				|| t.lexeme.equals("char") || t.lexeme.equals("boolean")) {
			okay(t);
			return t.lexeme;
		}
		else
			error(t, "");
		return "";

	}

	//for declaring subroutines
	public void subroutineDeclar() {
		String type = " ";
		String type1 = " ";
		String type2 = "";
		String name = "";
		boolean free = false, cons = false, method = false;
		int localCount;
		CurrentSymbolTable ++;
		MySymbolTables.get(CurrentClass).add(new SymbolTable());

		Token t = MyLexer.getNextToken();
		if(t.lexeme.equals("method") || t.lexeme.equals("function")
				|| t.lexeme.equals("constructor")) {
			okay(t);

			if(t.lexeme.equals("constructor")) {
				if(numThis > 0) {
					cons = true;

				}
			}
			if(t.lexeme.equals("method")) {
				method = true;
				currentMethod = true;
			}

		}
		else
			error(t,"");
		t = MyLexer.peekNextToken();
		if(t.lexeme.equals("void")) {
			free = true;
			okay(t);
			MyLexer.getNextToken();
		}
		else
			type = type();
		type1 = type;
		t = MyLexer.getNextToken();

		//fix this
		if(t.type == Token.TokenTypes.id) {
			okay(t);
			if(method == true)
				MySymbolTables.get(CurrentClass).get(0).AddSymbol(t.lexeme, Symbol.SymbolKind.method, type);
			else
				MySymbolTables.get(CurrentClass).get(0).AddSymbol(t.lexeme, Symbol.SymbolKind.function, type);
			name = t.lexeme;
		}
		else
			error(t, "");
		type = MySymbolTables.get(CurrentClass).get(0).getName(0);
		MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).AddSymbol("this", Symbol.SymbolKind.var, type);
		MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).initialized("this");

		t = MyLexer.getNextToken();
		if(t.lexeme.equals("(")) {
			okay(t);
			t = MyLexer.peekNextToken();
			if(!t.lexeme.equals(")"))
				paramList();
			t = MyLexer.getNextToken();
			if(t.lexeme.equals(")")) {
				okay(t);
				localCount = scan.getLocal(name, CurrentClass);
				writeCode("function " + className + "." + name + " " + Integer.toString(localCount));
				if(method == true) {
					writeCode("push argument 0");
					writeCode("pop pointer 0");
				}
				if(cons == true) {
					writeCode("push constant " + Integer.toString(numThis));
					writeCode("call Memory.alloc 1");
					writeCode("pop pointer 0");
				}
				currentSegment = "local";
				type2 = subroutineBody();
				if(free == true)
					writeCode("push constant 0");
				writeCode("return");
				currentMethod = false;
			}
			else
				error(t, "");
		}
		else
			error(t, "");
		if(!type1.equals(type2) && !type2.equals("-"))
				error(t, " Return value must equal function type");

	}

	public int paramList() {
		String type = "";
		int num = 1;
		type = type();
		Token t = MyLexer.getNextToken();
		if(t.type == Token.TokenTypes.id) {
			if(MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).FindSymbol(t.lexeme) == true) {
				error(t, " Variable was redeclared");
			}
			MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).AddSymbol(t.lexeme, Symbol.SymbolKind.argument, type);
			MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).initialized(t.lexeme);
			okay(t);
			t = MyLexer.peekNextToken();
		}
		else
			error(t, "");
		while(t.lexeme.equals(",")) {
			num ++;
			okay(t);
			t = MyLexer.getNextToken();
			type = type();
			t = MyLexer.getNextToken();
			if(t.type == Token.TokenTypes.id) {
				if(MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).FindSymbol(t.lexeme) == true) {
					error(t, " Variable was redeclared");
				}
				MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).AddSymbol(t.lexeme, Symbol.SymbolKind.argument, type);
				MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).initialized(t.lexeme);
				okay(t);
				t = MyLexer.peekNextToken();
			}
			else
				error(t, "");

		}

		return num;
	}

	//The body of any subroutine's code
	public String subroutineBody() {
		Token t = MyLexer.getNextToken();
		String type = "-";
		if(t.lexeme.equals("{")) {
			okay(t);
			t = MyLexer.peekNextToken();
			while(!t.lexeme.equals("}")) {
				if(t.lexeme.equals("return"))
					type = statement();
				else
					statement();
				t = MyLexer.peekNextToken();
			}
			t = MyLexer.getNextToken();
			if(t.lexeme.equals("}")) {
				okay(t);
			}
			else
				error(t, "} expected");
		}
		else
			error(t, "{ expected");
		return type;
	}

	//Statements in the jack language
	//Returns 'type' so that type checking on return values can happen
	public String statement() {
		Token t = MyLexer.peekNextToken();
		String type = " ";
		if(t.lexeme.equals("var"))
			varDeclarStatement();
		else
			if(t.lexeme.equals("let"))
				letStatement();
			else
				if(t.lexeme.equals("if"))
					ifStatement();
				else
					if(t.lexeme.equals("while"))
						whileStatement();
					else
						if(t.lexeme.equals("do"))
							doStatement();
						else
							if(t.lexeme.equals("return")) {
								type = returnStatement();
							}
							else
								error(t, "");

		return type;

	}

	//for declaration of vars inside subroutines
	public void varDeclarStatement() {
		String type;
		Token t = MyLexer.getNextToken();
		if(t.lexeme.equals("var")) {
			okay(t);
			type = type();
			t = MyLexer.getNextToken();
			if(t.type == Token.TokenTypes.id) {
				if(MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).FindSymbol(t.lexeme) == true) {
					error(t, " Variable was redeclared");
				}
				MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).AddSymbol(t.lexeme, Symbol.SymbolKind.var, type);
				okay(t);
				t = MyLexer.peekNextToken();
				while(t.lexeme.equals(",")) {
					okay(t);
					MyLexer.getNextToken();
					t = MyLexer.getNextToken();
					if(t.type == Token.TokenTypes.id) {
						if(MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).FindSymbol(t.lexeme) == true) {
							error(t, " Variable was redeclared");
						}
						MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).AddSymbol(t.lexeme, Symbol.SymbolKind.var, type);
						okay(t);
					}
					else
						error(t, "");
					t = MyLexer.peekNextToken();
				}
				t = MyLexer.getNextToken();
				if(t.lexeme.equals(";"))
					okay(t);
				else
					error(t, " ; expected");
			}
			else
				error(t, "");
		}
		else
			error(t, "");
	}

	public void letStatement() {
		String type1 = "", type2 = "";
		String code = "";
		Token t = MyLexer.getNextToken();
		if(t.lexeme.equals("let")) {
			okay(t);
			t = MyLexer.getNextToken();
			if(t.type == Token.TokenTypes.id) {
				if(MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).FindSymbol(t.lexeme) == true) {
					type1 = MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).getType(t.lexeme);
					code = "local " + Integer.toString(MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).getOffset(t.lexeme)-1);
					if(MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).getSymbolKind(t.lexeme) == Symbol.SymbolKind.argument) {
						int x = 0;
						if(currentMethod == true)
							x = -1;
						code = "argument" + " " + Integer.toString(MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).getOffset(t.lexeme)-x);

					}
				}
				else {
					if(MySymbolTables.get(CurrentClass).get(0).FindSymbol(t.lexeme) == true) {
						type1 = MySymbolTables.get(CurrentClass).get(0).getType(t.lexeme);
						if(MySymbolTables.get(CurrentClass).get(0).getSymbolKind(t.lexeme) == Symbol.SymbolKind.Static)
							code = "static " + Integer.toString(MySymbolTables.get(CurrentClass).get(0).getOffset(t.lexeme)-1);
						else
							code = "this " + Integer.toString(MySymbolTables.get(CurrentClass).get(0).getOffset(t.lexeme)-1);
					}
					else
						error(t, " Variable must be declared before use");
				}

				//set symbol to initialized
				MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).initialized(t.lexeme);
				okay(t);
				t = MyLexer.peekNextToken();
				if(t.lexeme.equals("[")) {
					writeCode("push " + code );
					okay(t);
					MyLexer.getNextToken();
					expression();
					writeCode("add");
					writeCode("pop pointer 1");
					code = "that 0";
					t = MyLexer.getNextToken();
					if(t.lexeme.equals("]"))
						okay(t);
					else
						error(t, "");
					t = MyLexer.getNextToken();
					if(t.lexeme.equals("=")) {
						type2 = expression();
					}
					else
						error(t, "");
				}
				else {
					t = MyLexer.getNextToken();
					if(t.lexeme.equals("=")) {
						okay(t);
						type2 = expression();

					}
				}
			}
			else
				error(t,"");
		}
		else
			error(t,"");
		 t = MyLexer.getNextToken();
		 if(t.lexeme.equals(";"))
			 okay(t);
		 else
			 error(t, " ; expected");
		 boolean check = false;
		 if(type1.equals("Array") || type1.equals("int")) {
			 if(type2.equals("Array") || type2.equals("int")) {
				 check = true;
			 }
		 }
		 else {
			 if(type1.equals("char") || type1.equals("int")) {
				 if(type2.equals("char") || type2.equals("int")) {
					 check = true;
				 }
			 }
		 }
		 if(check == false) {
			 if(!(type1.equals(type2))) {
				 error(t, "can't assign variables of different types");
			 }
		 }

		 writeCode("pop " + code );


	}

	//For any expression, returns the type of the expression
	public String expression() {
		String type = "";
		String type2 = "";
		type = relationalExpression();
		Token t = MyLexer.peekNextToken();

		while(t.lexeme.equals("&") || t.lexeme.equals("|")) {
			okay(t);
			MyLexer.getNextToken();

			type2 = relationalExpression();
			if(t.lexeme.equals("&")) {
				writeCode("and");
			}
			else {
				writeCode("or");
			}

			if(!type2.equals(""))
					type = type2;
			t = MyLexer.peekNextToken();
		}
		return type;
	}

	public String relationalExpression() {
		String type = "";
		String type2 = "";
		type = arithmaticExpression();
		Token t = MyLexer.peekNextToken();

		while(t.lexeme.equals("=") || t.lexeme.equals(">") || t.lexeme.equals("<")) {
			okay(t);
			MyLexer.getNextToken();

			type2 = arithmaticExpression();

			//if not equal then jump to else

			if(t.lexeme.equals("="))
				writeCode("eq");
			if(t.lexeme.equals(">"))
				writeCode("gt");
			if(t.lexeme.equals("<"))
				writeCode("lt");

			if(not == false) {
				writeCode("not");
			}
			else
				not = false;
			if(!type2.equals(""))
					type = type2;
			t = MyLexer.peekNextToken();
		}
		return type;
	}

	public String arithmaticExpression() {
		String type = "";
		String type2 = "";
		type = term();
		Token t = MyLexer.peekNextToken();

		while(t.lexeme.equals("+") || t.lexeme.equals("-")) {
			okay(t);
			MyLexer.getNextToken();

			type2 = term();
			if(t.lexeme.equals("+"))
				writeCode("add");
				else
					writeCode("sub");
			if(!type2.equals("")) {
					type = type2;
			}
			t = MyLexer.peekNextToken();
		}
		return type;
	}

	public String term() {
		String type = "";
		String type2 = "";
		type = factor();

		Token t = MyLexer.peekNextToken();

		while(t.lexeme.equals("*") || t.lexeme.equals("/")) {
			okay(t);
			MyLexer.getNextToken();
			type2 = factor();
			if(t.lexeme.equals("*"))
					writeCode("call Math.multiply 2");
			if(t.lexeme.equals("/"))
				writeCode("call Math.divide 2");
			if(!type2.equals(""))
					type = type2;
			t = MyLexer.peekNextToken();
		}
		return type;
	}

	public String factor() {
		String type = "";
		Token t = MyLexer.peekNextToken();
		if(t.lexeme.equals("-") || t.lexeme.equals("~")) {
			okay(t);
			 MyLexer.getNextToken();
		}
		type = operand();

		if(t.lexeme.equals("~"))
			writeCode("not");
		if(t.lexeme.equals("-")) {
			writeCode("neg");
		}
		return type;

	}

	//checks if the given class (t) exists in the symbol table
	private int checkClass(String t) {
		for(int i = 0; i < MySymbolTables.size(); i ++)
				if(MySymbolTables.get(i).get(0).getName(0).equals(t)){
					return i;
				}
			return -1;
	}

	//Checks each individual operand, a very long class
	//as there are many different potential scenarios here
	public String operand() {
		ArrayList<String> types, arguments = new ArrayList<String>();
		String type = "";
		String code = "";
		String memory;
		int method = 0;
		int object = 0;
		int foreignClass = -1;
		Token t = MyLexer.getNextToken();
		types = new ArrayList<String>();
		if(t.lexeme.equals("true") || t.lexeme.equals("false")) {
			okay(t);
			type = "boolean";
			if(t.lexeme.equals("true")) {
				writeCode ("push constant " + Integer.toString(0));
				code = "not";
			}
			else
				code = "push constant " + Integer.toString(0);
		}
		else {
			if(t.type == Token.TokenTypes.number ) {
				okay(t);
				type = "int";
				code = "push constant " + t.lexeme;
			}
			else {
				if(t.type == Token.TokenTypes.id) {
					memory = t.lexeme;
					if(MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).FindSymbol(t.lexeme) == true) {
						type = MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).getType(t.lexeme);
						code = "push " + currentSegment + " " + Integer.toString(MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).getOffset(t.lexeme)-1);

						if(MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).getSymbolKind(t.lexeme) == Symbol.SymbolKind.argument) {
							int x = 0;
							if(currentMethod == true)
								x = -1;
							code = "push argument" + " " + Integer.toString(MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).getOffset(t.lexeme)-x);

						}
						t = MyLexer.peekNextToken();
						if(t.lexeme.equals(".")) {
							writeCode(code);
							object ++;

							code = "call " + type;
						}
					}
					else {
						if(MySymbolTables.get(CurrentClass).get(0).FindSymbol(t.lexeme) == true) {
							type = MySymbolTables.get(CurrentClass).get(0).getType(t.lexeme);
							if(MySymbolTables.get(CurrentClass).get(0).getSymbolKind(t.lexeme) == Symbol.SymbolKind.Static)
								code = "push static " + Integer.toString(MySymbolTables.get(CurrentClass).get(0).getOffset(t.lexeme)-1);
							else {

								code = "push this " + Integer.toString(MySymbolTables.get(CurrentClass).get(0).getOffset(t.lexeme)-1);
							}

							t = MyLexer.peekNextToken();
							if(t.lexeme.equals(".")) {
								if(MySymbolTables.get(CurrentClass).get(0).getSymbolKind(memory) != Symbol.SymbolKind.Class) {
									writeCode(code);
								}
								object ++;
								code = "call " + type;
							}
						}
						else {
							//check for class
							foreignClass = checkClass(t.lexeme);
							type = t.lexeme;
							code += "call " + t.lexeme;
							if(foreignClass == -1) {
								error(t, " Variable must be declared before use");
							}
						}
					}
					if(MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).checkinitialized(t.lexeme) == false) {
						if(ret == false && type.equals("-"))
							error(t, " Variable must be initialized before use");
					}
					okay(t);
					t = MyLexer.peekNextToken();
					if(t.lexeme.equals(".")) {
						code += ".";
						okay(t);
						MyLexer.getNextToken();
						t = MyLexer.getNextToken();
						if(t.type == Token.TokenTypes.id) {
							code += t.lexeme;
							if(foreignClass != -1) {
								if(scan.checkPublic(t.lexeme, foreignClass) == false) {
									error(t, " Variable must be declared before use");
								}
								type = scan.getType(t.lexeme, foreignClass);
								if(scan.checkMethod(t.lexeme, foreignClass) == true)
									method = 1;
							}
							else {

								if(MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).FindSymbol(t.lexeme) == true) {
									type = MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).getType(t.lexeme);
								}
								else {
									if(MySymbolTables.get(CurrentClass).get(0).FindSymbol(t.lexeme) == true) {
										type = MySymbolTables.get(CurrentClass).get(0).getType(t.lexeme);
										if(scan.checkMethod(t.lexeme, CurrentClass) == true)
											method = 1;
									}
									else {
										if(scan.checkPublic(t.lexeme, CurrentClass) == false) {
											foreignClass = checkClass(type);

											if(foreignClass == -1) {
												error(t, " Variable must be declared before use");
											}
											else
												if(scan.checkPublic(t.lexeme, foreignClass) == false) {
													error(t, " Variable must be declared before use");
												}
												else {
													type = scan.getType(t.lexeme, foreignClass);
													if(scan.checkMethod(t.lexeme, foreignClass) == true)
														method = 1;
												}

										}
										else {
											type = scan.getType(t.lexeme, CurrentClass);
											if(scan.checkMethod(t.lexeme, CurrentClass) == true)
												method = 1;
										}
									}

								}
							}
							if(MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).checkinitialized(t.lexeme) == false) {
								if(ret == false && type.equals("-"))
									error(t, " Variable must be initialized before use");
							}
							MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).getType(t.lexeme);
							okay(t);
						}
					}
							t = MyLexer.peekNextToken();
							if(t.lexeme.equals("[")) {
								okay(t);
								t = MyLexer.getNextToken();
								writeCode(code);
								String type2 = expression();
							//	if(!type2.equals("int") && !type2.equals("char"))
								//	error(t, " Array indices must be of integer value only");
								writeCode("add");
								writeCode("pop pointer 1");
								code = "push that 0";
								t = MyLexer.getNextToken();
								if(t.lexeme.equals("]"))
									okay(t);
								else
									error(t, "");
							}
							else
								if(t.lexeme.equals("(")) {
									okay(t);
									MyLexer.getNextToken();
									t = MyLexer.peekNextToken();
									if(!t.lexeme.equals(")")) {
											types = expressionList();

									}
									t = MyLexer.getNextToken();
									if(t.lexeme.equals(")")) {
										okay(t);
										code += " " + Integer.toString(types.size()+method);
									}
									else
										error(t, "");
								}




			}
				else {
					if(t.lexeme.equals("this")) {
						type = MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).getType(t.lexeme);
						code = "push pointer 0";
					}
					else {
						if(t.lexeme.equals("(")) {
							okay(t);
							type = expression();
							t = MyLexer.getNextToken();
							if(t.lexeme.equals(")"))
								okay(t);
							else
								error(t, "");
						}
							else {
								if(t.type == Token.TokenTypes.string_literal) {
									writeCode("push constant " + Integer.toString(t.lexeme.length()));
									writeCode("call String.new 1");
									for(int i = 0; i < t.lexeme.length(); i++) {
										code = "push constant " + (int)t.lexeme.charAt(i);
										writeCode(code);
										code = "call String.appendChar 2";
										writeCode(code);
									}
									//code = "push constant " + t.lexeme.length();
									//writeCode(code);
									//code = "call String.new 1";
									code = "";
									type = "String";
									okay(t);
								}
								else {
										if( t.lexeme.equals("null")) {
											okay(t);
											code = "push constant 0";
											type = "int";
										}
										else
											error(t, "");
								}
							}

					}
				}
			}
		}
	/*	t = MyLexer.peekNextToken();
		if(t.lexeme.equals(")")) {
			t = MyLexer.getNextToken();
			okay(t);
		} */
		if(!code.equals(""))
			writeCode(code);
		return type;
	}

	public void ifStatement() {
		Token t = MyLexer.getNextToken();
		if(t.lexeme.equals("if")) {
			okay(t);
			t = MyLexer.getNextToken();
			if(t.lexeme.equals("(")) {
				okay(t);
				expression();
				t = MyLexer.getNextToken();
				if(t.lexeme.equals(")"))
						okay(t);
				else
					error(t, "");
				//writeCode("not");
				writeCode("if goto elseLabel");
				t = MyLexer.getNextToken();
				if(t.lexeme.equals("{")) {
					okay(t);
					t = MyLexer.peekNextToken();
					if(t.lexeme.equals("}"))
						okay(t);
					else {
						while(!t.lexeme.equals("}")) {
							statement();
							t = MyLexer.peekNextToken();
						}
						okay(t);
					}
					writeCode("goto contLabel:");
					writeCode("elseLabel:");
					MyLexer.getNextToken();
					t = MyLexer.peekNextToken();
					if(t.lexeme.equals("else")) {
						okay(t);
						MyLexer.getNextToken();
						t = MyLexer.getNextToken();
						if(t.lexeme.equals("{")) {
							t = MyLexer.peekNextToken();
							while(!t.lexeme.equals("}")) {
								statement();
								t = MyLexer.peekNextToken();
							MyLexer.getNextToken();
							okay(t);

							}

						}
					}
					writeCode("contLabel");
				}
				else
					error(t, "");
			}
			else
				error(t, " ");
		}
		else
			error(t, " ");
	}

	public void whileStatement() {
		Token t = MyLexer.getNextToken();
		if(t.lexeme.equals("while")) {
			okay(t);
			t = MyLexer.getNextToken();
			if(t.lexeme.equals("(")) {
				writeCode("label loop");
				okay(t);
				expression();
				t = MyLexer.getNextToken();
				if(t.lexeme.equals(")")) {
					writeCode("if-goto end");
					okay(t);
					t = MyLexer.getNextToken();
					if(t.lexeme.equals("{")) {
						okay(t);
						t = MyLexer.peekNextToken();
						while(!t.lexeme.equals("}")) {
							statement();
							t = MyLexer.peekNextToken();
						}
						okay(t);
						t = MyLexer.getNextToken();
						writeCode("goto loop");
						writeCode("label end");
					}
					else
						error(t, "");
				}
				else
					error(t, ") expected");
			}
			else
				error(t, "( expected");
		}
		else
			error(t, "error, 'while' expected");
	}

	public void doStatement() {
		Token t = MyLexer.getNextToken();
		if(t.lexeme.equals("do")) {
			ret = true;
			okay(t);
			subroutineCall();
			writeCode("pop temp 0");
			t = MyLexer.getNextToken();
			if(t.lexeme.equals(";"))
				okay(t);
			else
				error(t, "");
		}
		else
			error(t, "");
		ret = false;
	}

	public void subroutineCall() {
		Token t = MyLexer.getNextToken();
		String name = "";
		String code = "";
		String type = "";
		boolean method = false;
		int object = 0;
		ArrayList<String> types, arguments = new ArrayList<String>();
		int checkClass = -1;
		if(t.type == Token.TokenTypes.id) {
			name = t.lexeme;
			code += "call " + name;
			if(scan.checkPublic(t.lexeme, CurrentClass) == false) {
				checkClass = checkClass(t.lexeme);
				if(checkClass == -1) {
					type = MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).getType(t.lexeme);
					if(type.equals("")) {
						type = MySymbolTables.get(CurrentClass).get(0).getType(t.lexeme);
						writeCode("push this " + Integer.toString(MySymbolTables.get(CurrentClass).get(0).getOffset(t.lexeme)-1));
						object ++;
						if(type.equals(""))
							error(t, " Subroutine called without being delcared in the source code");
					}
					else {
						writeCode("push local " + Integer.toString(MySymbolTables.get(CurrentClass).get(CurrentSymbolTable).getOffset(t.lexeme)-1));
						object ++;
					}
					checkClass = checkClass(type);
					code = "call " + type;
				}
			}
			else {
				if(scan.checkMethod(t.lexeme, CurrentClass) == true) {
					method = true;
					writeCode("push pointer 0");
				}
				arguments = scan.getArguments(t.lexeme, CurrentClass);
				type = className;
				name = t.lexeme;
			}
			okay(t);
			t = MyLexer.peekNextToken();
			if(t.lexeme.equals( ".")) {
				if(checkClass == -1)
					error(t, " Can only call a subroutine of a class not of another subroutine");
				okay(t);
				MyLexer.getNextToken();
				t = MyLexer.getNextToken();
				if(t.type == Token.TokenTypes.id) {
					name = t.lexeme;
					if(scan.checkPublic(t.lexeme, checkClass) == true) {
						if(scan.checkMethod(t.lexeme,checkClass ) == true) {
							method = true;
							//writeCode("push pointer 0");
						}
						okay(t);
						arguments = scan.getArguments(t.lexeme, checkClass);
					}
					else {
						error(t, " Subroutine not declared");
					}

				}
				else
					error(t, " Subroutine ID expected");
				code += "." + name;
			}
			else {
				code = "call " + type + "." + name;
			}


			t = MyLexer.getNextToken();
			if(t.lexeme.equals("(")) {
				okay(t);
				t = MyLexer.peekNextToken();
				if(!t.lexeme.equals(")")) {
						types = expressionList();
						if(types.size() == arguments.size()) {
							for(int i = 0; i < arguments.size(); i ++) {
								if(arguments.get(i).equals("Array") || types.get(i).equals("Array")) {
									if(types.get(i).equals("int") || types.get(i).equals("boolean") || types.get(i).equals("char")
											||arguments.get(i).equals("int") || arguments.get(i).equals("boolean") ||
											arguments.get(i).equals("char")) {

									}

								}
								else {
									if(arguments.get(i).equals("char") || types.get(i).equals("char")) {
										if(arguments.get(i).equals("int") || types.get(i).equals("int")) {

										}
									}
									else
										if(!arguments.get(i).equals(types.get(i))) {
											error(t, "Called subroutine's arguments do not match arguments in declaration");

										}
								}
							}
						}
				}
				t = MyLexer.getNextToken();
				if(t.lexeme.equals(")")) {
					okay(t);
				}
				else
					error(t, ") expected");
			}
			else
				error(t, "( expected");
		}
		else
			error(t, "");

		if(method == true) {
			writeCode(code + " " + Integer.toString(arguments.size() + 1));
		}
		else
			writeCode(code + " " + Integer.toString(arguments.size()));
	}

	//Lists of expressions
	public ArrayList<String> expressionList() {
		 ArrayList<String> types = new ArrayList<String>();
		types.add(expression());
		Token t = MyLexer.peekNextToken();
		while(t.lexeme.equals(",")) {
			MyLexer.getNextToken();
			types.add(expression());
			t = MyLexer.peekNextToken();
		}
		return types;
	}

	//returns the type of the value to be returned
	//to help with semantic analysis
	public String returnStatement() {
		Token t = MyLexer.getNextToken();
		String type = " ";
		if(t.lexeme.equals("return")) {
			ret = true;
			okay(t);
			t = MyLexer.peekNextToken();
			if(!t.lexeme.equals(";")) {
				type = expression();
			}
			t = MyLexer.getNextToken();
			if(t.lexeme.equals(";")) {
				ret = false;
				okay(t);
			}
			else
				error(t, "; expected");
		}
		return type;
	}

	//adds the String s (a line of code) to the array containing of lines of code
	//for the given class
	public void writeCode(String s) {
		code.add(s);
	}

	//prints the code out to console as well as to a VM file in the directory
	//containing the classpath
	public void printCode() {
		for(int i = 0; i < code.size(); i++ )
			System.out.println(code.get(i));

		File f = new File(dir + "/" + className + ".vm");
		try (PrintWriter out = new PrintWriter(dir + "/" + className + ".vm")) {
			for(int i = 0; i < code.size(); i++ )
				out.println(code.get(i));
			out.close();
		}
		catch(FileNotFoundException e){
			System.out.print("FILE NOT FOUND");
		}

	}

	//adds the path of the jack directory in order to output the
	//vm file there
	public void addPath(String path){
		dir = path;
	}

	public static void main(String[] args) {
		String directory = args[0];
		ArrayList<String> Test = new  ArrayList<String>();
		System.out.println(directory);
		File folder = new File(args[0]);
		File[] listOfFiles = folder.listFiles();
		if(folder.isDirectory()){
		for (int i = 0; i < listOfFiles.length; i++) {
			  if ((listOfFiles[i].getName().charAt((int)(listOfFiles[i].getName().length()-1)) == 'k')
					  &&(listOfFiles[i].getName().charAt((int)listOfFiles[i].getName().length()-2) == 'c')
					  &&(listOfFiles[i].getName().charAt((int)listOfFiles[i].getName().length()-3) == 'a')
					  &&(listOfFiles[i].getName().charAt((int)listOfFiles[i].getName().length()-4) == 'j'))
					  Test.add(listOfFiles[i].getPath());
			}
			for(int i = 0; i < Test.size(); i ++)
				System.out.println(Test.get(i));
	        Parser l = new Parser();
					l.addPath(directory);
	        l.init(Test);
	        l.printCode();
		}
		else{
			System.out.println("ERROR, NOT A DIRECTORY");
		}


	}
}
