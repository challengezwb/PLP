package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		binaryChain.getE0().visit(this, arg);
		binaryChain.getE1().visit(this, arg);
		TypeName type0 = binaryChain.getE0().getTypeName();
		TypeName type = null;
		if(type0.equals(URL) && binaryChain.getE1().getTypeName().equals(IMAGE) && binaryChain.getArrow().isKind(ARROW)){
			type = IMAGE;
			binaryChain.setTypeName(type);
			return null;
		}else if(type0.equals(FILE) && binaryChain.getE1().getTypeName().equals(IMAGE) && binaryChain.getArrow().isKind(ARROW)){
			type = IMAGE;
			binaryChain.setTypeName(type);
			return null;
		}else if(type0.equals(FRAME) && binaryChain.getE1().firstToken.isKind(KW_XLOC,KW_YLOC) && binaryChain.getArrow().isKind(ARROW) && 
				binaryChain.getE1() instanceof FrameOpChain){
			
			type = INTEGER;
			binaryChain.setTypeName(type);
			return null;
		}else if(type0.equals(FRAME) && binaryChain.getE1().firstToken.isKind(KW_SHOW,KW_HIDE,KW_MOVE) && 
				binaryChain.getArrow().isKind(ARROW) && binaryChain.getE1() instanceof FrameOpChain){
			
			type = FRAME;
			binaryChain.setTypeName(type);
			return null;
		}else if(type0.equals(IMAGE) && binaryChain.getE1().getTypeName().equals(FRAME) && binaryChain.getArrow().isKind(ARROW)){
			
			type = FRAME;
			binaryChain.setTypeName(type);
			return null;
		}else if(type0.equals(IMAGE) &&binaryChain.getE1().getTypeName().equals(FILE) && binaryChain.getArrow().isKind(ARROW)){

			type = NONE;
			binaryChain.setTypeName(type);
			return null;
		}else if(type0.equals(IMAGE) && binaryChain.getE1().getFirstToken().isKind(OP_WIDTH,OP_HEIGHT) && 
				binaryChain.getArrow().isKind(ARROW)  && binaryChain.getE1() instanceof ImageOpChain){
			
			type = INTEGER;
			binaryChain.setTypeName(type);
			return null;
		}else if(type0.equals(IMAGE) && binaryChain.getE1().getFirstToken().isKind(KW_SCALE) && 
				binaryChain.getArrow().isKind(ARROW) && binaryChain.getE1() instanceof ImageOpChain){
			
			type = IMAGE;
			binaryChain.setTypeName(type);
			return null;
		}else if(type0.equals(IMAGE) && binaryChain.getE1().getFirstToken().isKind(OP_GRAY,OP_BLUR,OP_CONVOLVE) 
				&& binaryChain.getArrow().isKind(ARROW,BARARROW) && binaryChain.getE1() instanceof FilterOpChain){
			
			type = IMAGE;
			binaryChain.setTypeName(type);
			return null;
		}else if(type0.equals(IMAGE) && binaryChain.getArrow().isKind(ARROW) && binaryChain.getE1() instanceof IdentChain 
				&& binaryChain.getE1().getTypeName().isType(IMAGE)){
			
			type = IMAGE;
			binaryChain.setTypeName(type);
			return null;
		}else if(type0.equals(INTEGER) && binaryChain.getArrow().isKind(ARROW) && binaryChain.getE1() instanceof IdentChain 
				&& binaryChain.getE1().getTypeName().isType(INTEGER)){
			
			type = INTEGER;
			binaryChain.setTypeName(type);
			return null;
		}else{
			throw new TypeCheckException("Illegal Chain" + binaryChain.getE0().getFirstToken().pos);
		}
			
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
	    TypeName type = null;
	    binaryExpression.getE0().visit(this, arg);
	    TypeName type0 = binaryExpression.getE0().getTypeName();
	    binaryExpression.getE1().visit(this, arg);
	    TypeName type1 = binaryExpression.getE1().getTypeName();
   
	    if(type0.equals(INTEGER) && type1.equals(INTEGER) && binaryExpression.getOp().isKind(PLUS,MINUS)){
	    	  type = INTEGER;
	    	  binaryExpression.setTypeName(type);
	    	  return null;
	    }else if(type0.equals(IMAGE) && type1.equals(IMAGE) && binaryExpression.getOp().isKind(PLUS,MINUS)){
	          type = IMAGE;
	    	  binaryExpression.setTypeName(type);
	    	  return null;
	    }else if(type0.equals(INTEGER) && type1.equals(INTEGER) && binaryExpression.getOp().isKind(TIMES,DIV,MOD)){
	    	  type = INTEGER;
	    	  binaryExpression.setTypeName(type);
	    	  return null;
	    }else if(type0.equals(INTEGER) && type1.equals(IMAGE) && binaryExpression.getOp().isKind(TIMES)){
	          type = IMAGE;
	    	  binaryExpression.setTypeName(type);
	    	  return null;
	    }else if(type0.equals(IMAGE) && type1.equals(INTEGER) && binaryExpression.getOp().isKind(TIMES)){
	          type = IMAGE;
	    	  binaryExpression.setTypeName(type);
	    	  return null;
	    }else if(type0.equals(INTEGER) && type1.equals(INTEGER) && binaryExpression.getOp().isKind(LT,GT,LE,GE)){
	          type = BOOLEAN;
	    	  binaryExpression.setTypeName(type);
	    	  return null;
	    }else if(type0.equals(BOOLEAN) && type1.equals(BOOLEAN) && binaryExpression.getOp().isKind(LT,GT,LE,GE,AND,OR)){
	    	  type = BOOLEAN;
	    	  binaryExpression.setTypeName(type);
	    	  return null;
	    }else if(binaryExpression.getOp().isKind(EQUAL,NOTEQUAL) && type0.isType(type1)){
	          type = BOOLEAN;
	          binaryExpression.setTypeName(type);
	    	  return null;
	    }else if(type0.equals(IMAGE) && type1.equals(IMAGE) && binaryExpression.getOp().isKind(PLUS,MINUS)){
	    	  type = IMAGE;
	    	  binaryExpression.setTypeName(type);
	    	  return null;
	    }else if(type0.equals(IMAGE) && type1.equals(INTEGER) && binaryExpression.getOp().isKind(TIMES,DIV)){
	    	  type = IMAGE;
	    	  binaryExpression.setTypeName(type);
	    	  return null;
	    }else if(type0.equals(INTEGER) && type1.equals(IMAGE) && binaryExpression.getOp().isKind(TIMES)){
	    	  type = IMAGE;
	    	  binaryExpression.setTypeName(type);
	    	  return null;
	    }else if(type0.equals(IMAGE) && type1.equals(INTEGER) && binaryExpression.getOp().isKind(MOD)){
	    	  type = IMAGE;
	    	  binaryExpression.setTypeName(type);
	    	  return null;
	    }else{
	      throw new TypeCheckException("Illegal type of binaryExpression" + type0 +binaryExpression.getE0().getFirstToken().pos + type1 + binaryExpression.getE1().getFirstToken().pos);
	    }
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		symtab.enterScope();
		List<Dec> declist = block.getDecs();
		List<Statement> stalist = block.getStatements();
		for(Dec dec : declist){
			dec.visit(this, arg);
	
		}
		for(Statement sta : stalist){
			sta.visit(this, arg);
		}
		symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		booleanLitExpression.setTypeName(BOOLEAN);
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		TypeName type = null;
		if(filterOpChain.getFirstToken().isKind(OP_BLUR) || filterOpChain.getFirstToken().isKind(OP_GRAY) 
		  || filterOpChain.getFirstToken().isKind(OP_CONVOLVE)){
			if(filterOpChain.getArg().getExprList().size() == 0){
				type = IMAGE;
			}
		}else{
			throw new TypeCheckException("Illegal type of FilterOpChain" + filterOpChain.getFirstToken().pos);
		}
		filterOpChain.setTypeName(type);
		visitTuple(filterOpChain.getArg(),arg);
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		TypeName type = null;
		frameOpChain.setKind(frameOpChain.getFirstToken().kind);
		if(frameOpChain.getFirstToken().isKind(KW_SHOW) || frameOpChain.getFirstToken().isKind(KW_HIDE)){
			if(frameOpChain.getArg().getExprList().size() == 0){
				type = NONE;
			}
		}else if(frameOpChain.getFirstToken().isKind(KW_XLOC) || frameOpChain.getFirstToken().isKind(KW_YLOC)){
			if(frameOpChain.getArg().getExprList().size() == 0){
				type = INTEGER;
			}
		}else if(frameOpChain.getFirstToken().isKind(KW_MOVE)){
			if(frameOpChain.getArg().getExprList().size() == 2){
				type = NONE;
			}
		}else{
			throw new TypeCheckException("There is a bug in your parser" + frameOpChain.getFirstToken().pos);
		}
		frameOpChain.setTypeName(type);
		visitTuple(frameOpChain.getArg(),arg);
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Dec dec = symtab.lookup(identChain.getFirstToken().getText());
		if(symtab.lookup(identChain.getFirstToken().getText()) != null){
			identChain.setTypeName(Type.getTypeName(dec.getFirstToken()));
			identChain.setDec(dec);
			identChain.getDec().setTypeName(Type.getTypeName(dec.getFirstToken()));
		}else{
			throw new TypeCheckException("Illegal type of IdentChain" + symtab.lookup(identChain.getFirstToken().getText()).getTypeName());
		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		TypeName type = null;
		Dec dec = symtab.lookup(identExpression.getFirstToken().getText());
		if(dec != null){
			type = Type.getTypeName(dec.getFirstToken());
			identExpression.setDec(dec);
			identExpression.setTypeName(type);
		}else{
			throw new TypeCheckException("Illegal IdentExpression" + identExpression.getFirstToken().pos);
		}
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		ifStatement.getE().visit(this, arg);
		ifStatement.getB().visit(this, arg);
		if(ifStatement.getE().getTypeName().equals(BOOLEAN)){
			return null;
		}else{
			throw new TypeCheckException("Illegal type of IfStatement" + ifStatement.getE().getTypeName());
		}
		
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		intLitExpression.setTypeName(INTEGER);
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		if(sleepStatement.getE().getTypeName().equals(INTEGER)){
			sleepStatement.getE().visit(this, arg);
			return null;
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		whileStatement.getE().visit(this, arg);
		whileStatement.getB().visit(this, arg);
		if(whileStatement.getE().getTypeName().equals(BOOLEAN)){
			return null;
		}else{
			throw new TypeCheckException("Illegal type of WhileStatement" + whileStatement.getE().getTypeName());
		}
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		if(symtab.insert(declaration.getIdent().getText(), declaration)){
			Dec dec = symtab.lookup(declaration.getIdent().getText());
			declaration.setTypeName(Type.getTypeName(dec.getFirstToken()));
			return null;
		}else{
			throw new TypeCheckException("Illegal Dec" + declaration.getIdent().pos + declaration.getIdent().getText());
		}
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO Auto-generated method stub
		List<ParamDec> par = program.getParams();
		for(ParamDec pardec : par){
			pardec.visit(this, arg);
		}
		program.getB().visit(this, arg);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//assignStatement.getVar().visit(this, arg);
		assignStatement.getE().visit(this, arg);
		assignStatement.getVar().visit(this, arg);
		TypeName typeE = assignStatement.getE().getTypeName();
		TypeName typeIdentval = assignStatement.getVar().getDec().getTypeName();
		if(typeE.isType(typeIdentval)){
			//assignStatement.getE().visit(this, arg);
			return null;
		}else{
			throw new TypeCheckException("illegal type of AssignmentStatement" + assignStatement.getFirstToken().pos);
		}
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Dec dec = null;
		//identX.visit(this, arg);
		if(symtab.lookup(identX.getFirstToken().getText()) != null ){
			dec = symtab.lookup(identX.getFirstToken().getText());
		}else{
			throw new TypeCheckException("Illegal dec of IdentValue" + identX.getFirstToken().pos);
		}
		identX.setDec(dec);
		identX.getDec().setTypeName(Type.getTypeName(identX.getDec().getFirstToken()));
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		if(symtab.insert(paramDec.getIdent().getText(), paramDec)){
			Dec pdec = symtab.lookup(paramDec.getIdent().getText());
			paramDec.setTypeName(Type.getTypeName(pdec.getFirstToken()));
			return null;
		}else{
			throw new TypeCheckException("Illegal ParamDec" + paramDec.getFirstToken().pos);
		}
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg){
		// TODO Auto-generated method stub
		constantExpression.setTypeName(INTEGER);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		TypeName type = null;
		if(imageOpChain.firstToken.isKind(OP_WIDTH) || imageOpChain.getFirstToken().isKind(OP_HEIGHT)){
			if(imageOpChain.getArg().getExprList().size() == 0){
				type =INTEGER;
			}
		}else if(imageOpChain.getFirstToken().isKind(KW_SCALE)){
			if(imageOpChain.getArg().getExprList().size() == 1){
				type = IMAGE;
			}
		}else{
			throw new TypeCheckException("Illegal type of ImageOpChain" + imageOpChain.getFirstToken().pos);
		}
		imageOpChain.setTypeName(type);
		imageOpChain.getArg().visit(this, arg);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Iterator<Expression> it = tuple.getExprList().iterator();
		while(it.hasNext()){
			Expression e = it.next();
			e.visit(this, arg);
			if(e.getTypeName().equals(INTEGER)){
					continue;
			}else{	
				throw new TypeCheckException("Illegal type of Tuple" + it.next().getFirstToken().pos);
			}
		}
		return null;
	}


}
