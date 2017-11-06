package cop5556sp17;

import static cop5556sp17.Scanner.Kind.PLUS;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Kind.*;
import cop5556sp17.AST.*;

public class ASTTest {

	static final boolean doPrint = true;
	static void show(Object s){
		if(doPrint){System.out.println(s);}
	}
	

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testFactor0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IdentExpression.class, ast.getClass());
	}

	@Test
	public void testFactor1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "123";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IntLitExpression.class, ast.getClass());
	}



	@Test
	public void testBinaryExpr0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "1+abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
		BinaryExpression be = (BinaryExpression) ast;
		assertEquals(IntLitExpression.class, be.getE0().getClass());
		assertEquals(IdentExpression.class, be.getE1().getClass());
		assertEquals(PLUS, be.getOp().kind);
	}
    
	
	  @Test
	    public void testparse() throws IllegalCharException, IllegalNumberException, SyntaxException {
	        String input = "a url a, url a {image a}";
	        Scanner scanner = new Scanner(input);
	        scanner.scan();
	        Parser parser = new Parser(scanner);
	        ASTNode ast = parser.parse();
	        assertEquals(Program.class, ast.getClass());
	        Program p = (Program) ast;
	        assertEquals("a", p.getName());
	        ArrayList<ParamDec> list = new ArrayList<ParamDec>();
	        ParamDec pd1 = new ParamDec(scanner.new Token(Kind.KW_URL, 2, 3), scanner.new Token(Kind.IDENT, 6, 1));
	        list.add(pd1);
	        ParamDec pd2 = new ParamDec(scanner.new Token(Kind.KW_URL, 9, 3), scanner.new Token(Kind.IDENT, 13, 1));
	        list.add(pd2);
	        assertEquals(list, p.getParams());
	    }
	  
	  @Test
		public void FilterOp() throws IllegalCharException, IllegalNumberException, SyntaxException {
			String input = "x -> show;";
			Scanner scanner = new Scanner(input);
			scanner.scan();
			Parser parser = new Parser(scanner);
			ASTNode ast = parser.chain();
			assertEquals(BinaryChain.class, ast.getClass());
			BinaryChain ch = (BinaryChain) ast;
			assertEquals(BinaryChain.class, ch.getE0().getClass());
			assertEquals(BinaryChain.class, ch.getE1().getClass());
			assertEquals(Kind.ARROW, ch.getArrow().kind);
		}
}
