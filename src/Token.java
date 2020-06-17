public class Token{
	enum TokenTypes {
	keyword,
	id ,
	number,
	symbol,
	string_literal,
	EOF
	;
    }
	public String lexeme;
	public TokenTypes type;
	public int lineNum;
	
}
