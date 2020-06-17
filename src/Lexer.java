import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;


public class Lexer {
	private BufferedReader reader;
	private int sizeOfLast;
	private List<Integer> contents = new ArrayList<Integer>();
	private int pos;
	private File file;
	private int lineNum;
	private char c = ' ';
	private String [] keywords = {"class", "constructor", "method", "function", "int"
			, "boolean", "char", "void", "var", "static", "field", "let", "do", "if"
			, "else", "while", "return", "true", "false", "null", "this"};
	
	//Opens the file and reads each byte into an integer array. 
	public boolean initialize(String fileName)
    {
		  try
	      {
			file = new File(fileName);
			if (!file.exists()) {
			      System.out.println( " does not exist.");
			      return false;
			    }
			    if (!(file.isFile() && file.canRead())) {
			      System.out.println(file.getName() + " cannot be read from.");
			      return false;
			    }
	        reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));
	        int x = 0;
	        while(x != -1) {
	        	x = reader.read();
	        	contents.add(x);
	        }
	      }
	      catch(Exception e) {
	        System.out.println("Unable to open file: " + fileName);
	        return false;
	      }
		  
		  lineNum = 1;
		  sizeOfLast = 0;
		  pos = 0;
		  return true;
    }
	
	public Token getNextToken() {
		sizeOfLast = 0;
		int data = 0;
		boolean end = false;
		
		data = read();
		c = (char) data;
		
		//loop to ignore comments, white space and end of line
		while(c == '/' || c == ' ' || c == '\n' || data == 13) { 
			if(c == '/') {
				data = read();
				if(c == '/') {
				while(c != '\n') {
					data = read();
				}
			}
			else
				if(c == '*') {
					end = false;
					data = read();
					if(c == '*') {
						data = read();
						if(c != '/')
							while(end == false) {
								data = read();
								if(c == '*') {
									data = read();
									if(c == '/')
										end = true;
								}
							}
					}
					else {
						while(c != '*' || end == false) {
							data = read();
							if(c == '*')
								end = true;
							if(c == '\n')
								throw new NullPointerException("Unexpected end of file in "
										+ "the middle of multi-line comment. Line: " 
										+ Integer.toString(lineNum));
						}
					}
					data = read();
				}
				else {
					Token t1 = symbol('/');
					return t1;
				}
			}
			if(c == '\n') 
				lineNum += 1;
				//count the linenum for tokens, to help with error messages 
			data = read();
					
		}
		
		//if end of file
		if(data == -1) 
	      {
	        Token t = new Token();
	        t.type = Token.TokenTypes.EOF;
	        t.lineNum = lineNum;
	        return t;
	      }
		
		
		//if identifier or keyword - read until not identifier or keyword
		if(Character.isLetter(c) || c == '_') { 
			Token t = new Token();
	        t.lexeme = "";
	        while(Character.isLetter(c) || c == '_' || Character.isDigit(c)) {
	        	t.lexeme += c;
	        	read();
	        }
	        pos --;
	        if(Arrays.asList(keywords).contains(t.lexeme))
	        	t.type = Token.TokenTypes.keyword;
	        else
	        	t.type = Token.TokenTypes.id;
	        t.lineNum = lineNum;
	        return t;
		}
		else 
			if(Character.isDigit(c)) {
				//if digit - read until not digit
				Token t = new Token();
				t.lexeme = "";
				while(Character.isDigit(c))
		        {
		          t.lexeme += c;
		          data = read();
		        }
				pos --;
		        t.type = Token.TokenTypes.number;
		        t.lineNum = lineNum;
		        return t;
		      }
			else {
				if(c == '"') {
					////if string_literal, read string
					Token t = new Token();
					t.lexeme = "";
					data = read();
					while(c != '"') {
						t.lexeme += c;
						data = read();
						if(data == -1)
							throw new NullPointerException("Unexpected end of file in "
									+ "the middle of string_literal. Line: " 
									+ Integer.toString(lineNum));
					}
					t.type = Token.TokenTypes.string_literal;
					t.lineNum = lineNum;
			        return t;
				}
			}
		
		  //only possible types left are tokens

		  Token t1 = symbol(c);
		  t1.lineNum = lineNum;
	      return t1;
			
		
		
	}
	
	//Checks the next token without removing it from the input
	public Token peekNextToken() {
		Token token;
		int tempLine = lineNum;
		token = getNextToken();
		pos -= sizeOfLast;
		if(token.type == Token.TokenTypes.number ||token.type == Token.TokenTypes.keyword || token.type == Token.TokenTypes.id)
			pos ++;
		lineNum = tempLine;
		return token;
		
	}
	
	//Returns the chosen character as a symbol token
	private Token symbol(char character) {
		Token t1 = new Token();
	     t1.lexeme = "";
	     t1.lexeme += character;
	     t1.type = Token.TokenTypes.symbol;
	     return t1;
	}
	
	//Reads the next character into 'c'
	private int read() {
		int data = 1;
		data = contents.get(pos);
		pos ++;
		sizeOfLast ++;
		c = (char) data;
		return data;
	} 
	
	//testing the Lexer
	public static void main(String[] args) {
        Lexer l = new Lexer();
        boolean test;
        Token testToken;
        test = l.initialize("Output.jack");
        if(test == true) 
        	System.out.println("SUCCESS");
        testToken = l.getNextToken();
        while(testToken.type != Token.TokenTypes.EOF) {
            System.out.println("< '" + testToken.lexeme + "', " + testToken.type + ", " 
            		+ Integer.toString(testToken.lineNum) + ">");
            testToken = l.peekNextToken();
            
        	System.out.println("< '" + testToken.lexeme + "', " + testToken.type + ", "
        			+ Integer.toString(testToken.lineNum) + ">");
        	testToken = l.getNextToken();
        }
        
        	
        
    } 
	
	
		
}
