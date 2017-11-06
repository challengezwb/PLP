package cop5556sp17;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.sound.sampled.Line;

import org.omg.PortableServer.POAManagerPackage.State;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.Kind;

public class Scanner {
	/**
	 * Kind enum
	 */
	
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}
	public static enum State{
		START,IN_DIGIT,IN_IDENT,AFTER_EQ,AFTER_LT,AFTER_OR,AFTER_GT,AFTER_MINUS,AFTER_TIMES,AFTER_DIV,AFTER_NOT,IN_COMMENTS,END_COMMENTS;
		
	}
/**
 * Thrown by Scanner when an illegal character is encountered
 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}
		

	

	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;
		public boolean isKeyWord ; 
		

		//returns the text of this Token
		public String getText() {
			//TODO IMPLEMENT THI
			if(this.kind == Kind.IDENT|| this.kind == Kind.INT_LIT){
           return chars.substring(pos, pos+length);
			}else
				return this.kind.getText();
		}
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			
			//TODO IMPLEMENT THIS
			   int line=0;
			   int posInLine=0;
			   for(int i=0;i<pos;i++) {
			    posInLine++;
			    if(chars.charAt(i)=='\n') {
			     line++;
			     posInLine=0;
			    }
			   }
			   return new LinePos(line, posInLine);
		}
        

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException{
			//TODO IMPLEMENT THIS
		    
			return Integer.parseInt(chars.substring(pos, pos + length));
		}

		public boolean isKind(Kind kind) {
			// TODO Auto-generated method stub
			if(this.kind == kind){
				return true;
			}
			return false;
		}
				
		 @Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }

		 

		  private Scanner getOuterType() {
		   return Scanner.this;
		  }

		public boolean isKind(Kind... kinds) throws SyntaxException {
			// TODO Auto-generated method stub
			for(Kind temp : kinds){
				if(this.kind == temp){
					return true;
				}
			}
			return false;	
		}
		
	}

	 


	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();


	}
     
	public List<Integer> list = new ArrayList<Integer>();
    public static HashMap<String, Kind> keyword = new HashMap<String, Kind>();
    static{
        	for(Kind k: Kind.values()){
	    	String str = k.getText();
	    	if(str.matches("^[a-z]+$")){
	    		keyword.put(str, k);
	    	}
    }
    }
    
    public boolean isKeyWord(String s){
	  
		  return keyword.containsKey(s);
    }
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0; 
		//TODO IMPLEMENT THIS!!!!
		
		int length =chars.length();
		State state = State.START;
		int startPos = 0;
		list.add(0);
		int ch;
		while(pos <= length){
	        ch = pos < length ? chars.charAt(pos) : -1;
            switch(state){
            case START:{
                pos = skipWhiteSpace(pos);
                ch = pos < length ? chars.charAt(pos) : -1;
                startPos = pos;
                switch (ch) {
                    case -1 : {tokens.add(new Token(Kind.EOF, pos, 0)); pos++;}  break;
                    
                    case '+': {tokens.add(new Token(Kind.PLUS, startPos, 1));pos++;} break;
                    
                    case '-': {state = state.AFTER_MINUS;pos++;} break;
                    
                    case '*': {tokens.add(new Token(Kind.TIMES, startPos, 1));pos++;} break;
                        	
                    case '/': {state = state.AFTER_DIV;pos++;} break;
                      	
                    case '<': {state = state.AFTER_LT;pos++;}break;
                    	                               
                    case '>': {state = state.AFTER_GT;pos++;}break;
                    
                    case '!': {state = state.AFTER_NOT;pos++;}break;
                     
                    case '|': {state = state.AFTER_OR;pos++;}break;
                  
                    case '=': {state = State.AFTER_EQ;pos++;}break;
                    
                    case '&': {tokens.add(new Token(Kind.AND, startPos, 1));pos++;} break;
                    
                    case '%': {tokens.add(new Token(Kind.MOD, startPos, 1));pos++;} break;
                    
                    case ',': {tokens.add(new Token(Kind.COMMA, startPos, 1));pos++;} break;
                    
                    case ';': {tokens.add(new Token(Kind.SEMI, startPos, 1));pos++;} break;
                    
                    case '(': {tokens.add(new Token(Kind.LPAREN, startPos, 1));pos++;} break;
                    
                    case ')': {tokens.add(new Token(Kind.RPAREN, startPos, 1));pos++;} break;
                    
                    case '{': {tokens.add(new Token(Kind.LBRACE, startPos, 1));pos++;} break;
                    
                    case '}': {tokens.add(new Token(Kind.RBRACE, startPos, 1));pos++;} break;
                    
                    case '0': {tokens.add(new Token(Kind.INT_LIT,startPos, 1));pos++;}break;
                    case '\n':{pos++;list.add(pos);}break;
                    default: {
                        if (Character.isDigit(ch)) {state = State.IN_DIGIT;pos++;} 
                        else if (Character.isJavaIdentifierStart(ch)) {
                             state = State.IN_IDENT;pos++;
                         } 
                         else {throw new IllegalCharException(
                                    "illegal char " +ch+" at pos "+pos);
                         }
                      }
                } 
            }break;
            
           case IN_DIGIT:{
             	if (Character.isDigit(ch)) {
                    pos++;
                 } else {
                	      Token temp = new Token(Kind.INT_LIT, startPos, pos - startPos);
                	         try{
                	        	     temp.intVal();
                             tokens.add(temp);                  
                      state = State.START;           
                 }catch(Exception IllegalNumberException){
            		throw new IllegalNumberException("not in the range of Int !");  
            	        } 
                 }
            }  break;
           /* case IN_DIGIT:{
            	   try{
            		   if(Character.isDigit(ch)){
            			   pos++;
            		   }else {
            			   Token temp = new Token(Kind.INT_LIT, startPos, pos - startPos);
            			   temp.intVal();
            			   tokens.add(temp);
            			   state = State.START;
            		   }
            	   }catch(Exception IllegalNumberException){
            		   throw new IllegalNumberException("not in the range of Int !");  
            	   }
            }break;*/
            case IN_IDENT: {
            	   if (Character.isJavaIdentifierPart(ch)) {
                      pos++;
                } else {
                	        Token temp =new Token(Kind.IDENT, startPos, pos - startPos);
                	        String s = chars.substring(startPos, pos);
                	        if(isKeyWord(s) == true){
                	        	tokens.add(new Token(keyword.get(s), startPos, pos - startPos));
                	        	state = State.START;
                	        }else{
                	              tokens.add(new Token(Kind.IDENT, startPos, pos - startPos));
                              state = State.START;
                	        }
                }
            }  break;

            case AFTER_EQ: {
            	   if (ch == '='){
            		   tokens.add(new Token(Kind.EQUAL, startPos, 2));pos++;;
            	       state = State.START;
            	   }else {throw new IllegalCharException(
                           "illegal char " +ch+" at pos "+pos);
                }
            }  break;
    
            case AFTER_LT: {
            	   if(ch == '-'){        	 
 	        	       tokens.add(new Token(Kind.ASSIGN, startPos, 2));pos++;
 	        	       state = State.START;
 	           }else if(ch == '='){
 	        	       tokens.add(new Token(Kind.LE, startPos, 2));pos++;
 	        	       state = State.START;
 	           }else
 	        	       tokens.add(new Token(Kind.LT, startPos, 1));
 	        	       state = State.START;
            }break;
            
            case AFTER_OR: {
            	   if(ch == '-'){
     	            pos++;
     	            ch = chars.charAt(pos);
     	            if(ch == '>'){
     	            	tokens.add(new Token(Kind.BARARROW, startPos, 3));pos++;
     	            	state = State.START;
     	            }else{
     	            	      tokens.add(new Token(Kind.OR, startPos, 1));
     	            	      tokens.add(new Token(Kind.MINUS, startPos+1, 1));
     	            	      state = State.START;
                       }
     	         }else{
     	     	      tokens.add(new Token(Kind.OR, startPos, 1));
     	     	      state = State.START;
     	          }
            }break;
            
            case AFTER_GT: {
            	if(ch == '='){
 	        	   tokens.add(new Token(Kind.GE, startPos, 2));pos++;
 	        	   state = State.START;
 	          }else{
 	        	        tokens.add(new Token(Kind.GT, startPos, 1));
	        	        state = State.START;
 	          }
            }break;
            
            case AFTER_MINUS: {
            	if(ch == '>'){
            		tokens.add(new Token(Kind.ARROW, startPos, 2));pos++;
 	        	    state = State.START;
            	}else{
            		tokens.add(new Token(Kind.MINUS, startPos, 1));
            		state = State.START;
            	}
            }break;
            
            case AFTER_DIV: {
            	if(ch == '*'){
  	        	  state = state.IN_COMMENTS;
  	        	  pos++;
  	         }else {
  	          tokens.add(new Token(Kind.DIV, startPos, 1));
              state = State.START;
  	         }
            }break;
            
            case AFTER_NOT: {
 	           if(ch == '='){
 	        	    	 tokens.add(new Token(Kind.NOTEQUAL, startPos, 2));pos++;
 	        	    	 state = State.START;
 	           }else
 	        	     tokens.add(new Token(Kind.NOT, startPos, 1));
 	             state = State.START;
            }break;
            
            case IN_COMMENTS: {
            	 if(ch == '*'){
            		 state = State.END_COMMENTS;
            		 pos++;
            	 }else if (ch == -1){
            		 state =State.START;
            	 }else if (ch == '\n'){
            		 pos++;
            		 list.add(ch);
            	 }else
            	 pos++;
            }break;
            
            case END_COMMENTS: {
            	if(ch == '/'){
            		pos++;
            		state = State.START;
            	}else{
            		//pos++;
            	   state =State.IN_COMMENTS;
            	}
            }break;
            default:  assert false;
            }
		}
		tokens.add(new Token(Kind.EOF,pos,0));
		return this;  
	}



	private int  skipWhiteSpace(int pos) {
		// TODO Auto-generated method stub
		if(pos < chars.length()){
			
		while(Character.isWhitespace(chars.charAt(pos))){
               pos++;
			if(pos==chars.length())
			break;
		}
		}
		return pos;
	}



	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	 /*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */public Token peek() {
	    if (tokenNum >= tokens.size())
	        return null;
	    return tokens.get(tokenNum);
	}


	

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		//TODO IMPLEMENT THIS
		return t.getLinePos();
	}


}
