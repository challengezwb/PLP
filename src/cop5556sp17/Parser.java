package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;
public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}
    
	public Expression expression() throws SyntaxException {
		//TODO
		Token firsttoken = t;
		Expression e0 = null;
		Expression e1 = null;
		e0 = term();
		while(t.isKind(LT) || t.isKind(GT) || t.isKind(LE) ||t.isKind(GE) || t.isKind(EQUAL) || t.isKind(NOTEQUAL)){
			Token relOp = t;
			match(LT,GT,LE,GE,EQUAL,NOTEQUAL);
			e1 = term();
			e0 = new BinaryExpression(firsttoken, e0, relOp,e1);
		}
		
		return e0;
		//throw new UnimplementedFeatureException();
	}

	public Expression term() throws SyntaxException {
		//TODO
		Token firsttoken = t;
		Expression e0 = null;
		Expression e1 = null;
		e0 = elem();
		while(t.isKind(PLUS,MINUS,OR)){
			Token weakOp = t;
			match(PLUS,MINUS,OR);
			e1 = elem();
			e0 = new BinaryExpression(firsttoken, e0, weakOp,e1);
		}
		//throw new UnimplementedFeatureException();
		return e0;
	}

	public Expression elem() throws SyntaxException {
		//TODO
		Token firsttoken = t;
		Expression e0 = null;
		Expression e1 = null;
		e0 = factor();
		while(t.isKind(TIMES,DIV,AND,MOD)){
			Token strongOp = t;
			match(TIMES,DIV,AND,MOD);
			e1 = factor();
			e0 = new BinaryExpression(firsttoken, e0, strongOp,e1);
		}
		//throw new UnimplementedFeatureException();
		return e0;
	}

    public Expression factor() throws SyntaxException {
		Kind kind = t.kind;
		Expression e = null;
		switch (kind) {
		case IDENT: {
			e = new IdentExpression(t);
			match(IDENT);
			return e;
		}

		case INT_LIT: {
			e = new IntLitExpression(t);
			match(INT_LIT);
			return e;
		}
		
		case KW_TRUE: {
			e = new BooleanLitExpression(t);
			match(KW_TRUE);
			return e;
		}
		
		case KW_FALSE: {
			e = new BooleanLitExpression(t);
			match(KW_FALSE);
			return e;
		}
		
		case KW_SCREENWIDTH: {
			e = new ConstantExpression(t);
			match(KW_SCREENWIDTH);
			return e;
		}
		
		case KW_SCREENHEIGHT: {
			e = new ConstantExpression(t);
			match(KW_SCREENHEIGHT);
			return e;
		}
		
		case LPAREN: {
			match(LPAREN);
			e = expression();
			match(RPAREN);
			return e;
		}
		
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor " + t.getLinePos().toString());
		}
	}

	public Block block() throws SyntaxException {
		//TODO
	    Token firsttoken = t;
		ArrayList<Dec> declist = new ArrayList<Dec>();
		ArrayList<Statement> stalist = new ArrayList<Statement>();
		Dec dec = null;
		Statement statement = null;
		Block block = null;
		if(t.isKind(LBRACE)){
			match(LBRACE);
			while(true){
				if(t.kind == KW_INTEGER || t.kind == KW_BOOLEAN || t.kind == KW_IMAGE || t.kind == KW_FRAME){
					declist.add(dec());
				}else if(t.isKind(RBRACE)){
					break;
				}else{
					stalist.add(statement());
				}
			}
		}
	    
	    match(RBRACE);
	    block = new Block(firsttoken,declist,stalist);
		//throw new UnimplementedFeatureException();
	    return block;
	}

	public Program program() throws SyntaxException {
		//TODO
		Program program = null;
		ArrayList<ParamDec> parlist = new ArrayList<ParamDec>();
		Token firsttoken = t;
		match(IDENT);
		if(t.isKind(KW_INTEGER,KW_IMAGE,KW_FILE,KW_FRAME,KW_URL,KW_BOOLEAN)){
			parlist.add(paramDec());
		    while(t.isKind(COMMA)){
			   match(COMMA);
			   parlist.add(paramDec());
		      }
		   program = new Program(firsttoken,parlist,block());
		   return program;
		}else{
			program = new Program(firsttoken,parlist,block());
			return program;
		}
		//throw new UnimplementedFeatureException();
	}

	public ParamDec paramDec() throws SyntaxException {
		//TODO
		ParamDec paramdec = null;
	    Token firsttoken = match(KW_INTEGER,KW_IMAGE,KW_FILE,KW_FRAME,KW_URL,KW_BOOLEAN);
		Token secondtoken = match(IDENT);
		paramdec = new ParamDec(firsttoken,secondtoken);
		return paramdec;
	}

	public Dec dec() throws SyntaxException {
		//TODO 
		Dec dec = null;
	    Token firsttoken = t;
		match(KW_INTEGER,KW_IMAGE,KW_FILE,KW_FRAME,KW_URL,KW_BOOLEAN);
		Token secondtoken = t;
		match(IDENT);
		dec = new Dec(firsttoken,secondtoken);
		return dec;
			
	}

    public Statement statement() throws SyntaxException {
		//TODO
		Kind kind = t.kind;
		Statement statement = null;
		Expression e = null;
		switch(kind){
		case OP_SLEEP: {
			Token firsttoken = t;
			match(OP_SLEEP);
			e = expression();
			match(SEMI);
			statement = new  SleepStatement(firsttoken, e);
			return statement;
		}
		
		case KW_WHILE: {
			Token firsttoken = t;
			match(KW_WHILE);
			match(LPAREN);
			e = expression();
			match(RPAREN);
            statement = new WhileStatement(firsttoken,e,block());
            return statement;
		}
		
		case KW_IF: {
			Token firsttoken = t;
			match(KW_IF);
			match(LPAREN);
			e = expression();
			match(RPAREN);
            statement = new IfStatement(firsttoken,e,block());
            return statement;
		}
		
		case IDENT: {
			Token firsttoken = t;
			if(scanner.peek().isKind(ASSIGN)){
				IdentLValue indentvalue = new IdentLValue(t);
				match(IDENT);
				match(ASSIGN);
				e = expression();
				match(SEMI);
				statement = new AssignmentStatement(firsttoken,indentvalue, e);
				return statement;
			}else{
		    statement = chain();
			match(SEMI);
			return statement;
			}
		}
		
		case OP_BLUR: {  
		//match(OP_BLUR);
	    statement = chain();
	    match(SEMI);
	    return statement;
        }
    
        case OP_GRAY: {
        //	match(OP_GRAY);
        	statement = chain();
        	match(SEMI);
    	    return statement;
        }
    
        case OP_CONVOLVE: {
        	//match(OP_CONVOLVE);
        	statement = chain();
        	match(SEMI);
    	    return statement;
        }
    
        case KW_SHOW: {
        //	match(KW_SHOW);
        	statement = chain();
        	match(SEMI);
    	    return statement;
        }
    
        case KW_HIDE: {
        	//match(KW_HIDE);
        	statement = chain();
        	match(SEMI);
    	    return statement;
        }
    
        case KW_MOVE: {
        	//match(KW_MOVE);
        	statement = chain();
        	match(SEMI);
    	    return statement;
        }
    
        case KW_XLOC: {
        //	match(KW_XLOC);
        	statement = chain();
        	match(SEMI);
    	    return statement;
        }
    
        case KW_YLOC: {
        	//match(KW_YLOC);
        	statement = chain();
        	match(SEMI);
    	    return statement;
        }
    
        case OP_WIDTH: {
        //	match(OP_WIDTH);
        	statement = chain();
        	match(SEMI);
    	    return statement;
        }
    
        case OP_HEIGHT: {
        	//match(OP_HEIGHT);
        	statement = chain();
        	match(SEMI);
    	    return statement;
        }
    
        case KW_SCALE: {
        //match(KW_SCALE);
        	statement = chain();
        	match(SEMI);
    	    return statement;
        }
        
		default:
			throw new SyntaxException("illegal statement " + kind + t.getLinePos().toString());
		}
	}

	public Chain chain() throws SyntaxException {
		//TODO
		Token firsttoken = t;
		Chain chain = null;
		ChainElem chainelem = null;
		chain = chainElem();
		Token arrow = t;
		match(ARROW,BARARROW);
		chainelem = chainElem();
		chain = new BinaryChain(firsttoken, chain,arrow ,chainelem);
		while(t.kind == ARROW || t.kind == BARARROW){
			Token arrow1 = t;
			match(ARROW,BARARROW);
			chainelem = chainElem();
			chain = new BinaryChain(firsttoken, chain,arrow1 ,chainelem);
				}
		return chain;		
	}

  public ChainElem chainElem() throws SyntaxException {
		//TODO
	    Kind kind = t.kind;
	    FilterOpChain filteroc = null;
	    FrameOpChain  frameoc = null;
	    ImageOpChain  imageoc = null;
	    switch(kind){
	    case IDENT: {
	    	   Token firsttoken = t;
	    	   match(IDENT);
	    	   return new IdentChain(firsttoken);
	    }
	    
	    case OP_BLUR: {    	    
	    	    Token filterOp = t;
	      	match(OP_BLUR);	      	
	      	filteroc = new FilterOpChain(filterOp,arg());
	    	    return filteroc;
	    }
	    
	    case OP_GRAY: {
	    	    Token filterOp = t;
	      	match(OP_GRAY);	      	
	      	filteroc = new FilterOpChain(filterOp,arg());
	    	    return filteroc;
        }
	    
	    case OP_CONVOLVE: {
	    	    Token filterOp = t;
	      	match(OP_CONVOLVE);	      	
	      	filteroc = new FilterOpChain(filterOp,arg());
	    	    return filteroc;
        }
	    
	    case KW_SHOW: {
	    	    Token frameOp = t;
	      	match(KW_SHOW);	      	
	      	frameoc = new FrameOpChain(frameOp,arg());
	    	    return frameoc;
        }
	    
	    case KW_HIDE: {
	    	    Token frameOp = t;
	      	match(KW_HIDE);	      	
	      	frameoc = new FrameOpChain(frameOp,arg());
	    	    return frameoc;
        }
	    
	    case KW_MOVE: {
	    	    Token frameOp = t;
	      	match(KW_MOVE);	      	
	      	frameoc = new FrameOpChain(frameOp,arg());
	    	    return frameoc;        
	    }
	    
	    case KW_XLOC: {
	    	    Token frameOp = t;
	      	match(KW_XLOC);	      	
	      	frameoc = new FrameOpChain(frameOp,arg());
	    	    return frameoc;
        }
	    
	    case KW_YLOC: {
	    	    Token frameOp = t;
	      	match(KW_YLOC);	      	
	      	frameoc = new FrameOpChain(frameOp,arg());
	    	    return frameoc;
        }
	    
	    case OP_WIDTH: {
	    	    Token imageop = t;
	      	match(OP_WIDTH);	      	
	      	imageoc = new ImageOpChain(imageop,arg());
	    	    return imageoc;
        }
	    
	    case OP_HEIGHT: {
	       	Token imageop = t;
	      	match(OP_HEIGHT);	      	
	      	imageoc = new ImageOpChain(imageop,arg());
	    	    return imageoc;
        }
	    
	    case KW_SCALE: {
	    	    Token imageop = t;
	      	match(KW_SCALE);	      	
	      	imageoc = new ImageOpChain(imageop,arg());
	    	    return imageoc;
        }
	    
	    default:
	    	throw new SyntaxException("illegal chainElem" + kind + t.getLinePos().toString());
	    }
	    
	}
	
	public Tuple arg() throws SyntaxException {
		//TODO
		    Expression e = null;
		    Token firsttoken = t;
		    List<Expression> arglist = new ArrayList<Expression>();
		    if(t.kind == LPAREN){
		    		match(LPAREN);
		    		e = expression();
		    		arglist.add(e);
		    		while(t.kind == COMMA){
				  match(COMMA);
				  arglist.add(expression());
		    		}
		    		match(RPAREN);
		    }
		    return new Tuple(firsttoken,arglist);
	}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF" + " see" + t.kind + t.getLinePos() );
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind + t.getLinePos().toString());
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		for(Kind temp : kinds){
			if(t.isKind(temp)){
				return consume();
			}
		}
			throw new SyntaxException("illegal kind" + "saw " + t.kind + "expected " + kinds + t.getLinePos().toString());
			//replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
