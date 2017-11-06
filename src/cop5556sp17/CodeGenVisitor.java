package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
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
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

    /**
     * @param DEVEL          used as parameter to genPrint and genPrintTOS
     * @param GRADE          used as parameter to genPrint and genPrintTOS
     * @param sourceFileName name of source file, may be null.
     */
    public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
        super();
        this.DEVEL = DEVEL;
        this.GRADE = GRADE;
        this.sourceFileName = sourceFileName;
    }

    ClassWriter cw;
    String className;
    String classDesc;
    String sourceFileName;
    int slotNumber = 1;
    //List<localVariableValues> list = new ArrayList<localVariableValues>();
    Map<Dec,Label> startMap = new Hashtable<Dec, Label>();
    Map<Dec,Label> endMap = new Hashtable<Dec, Label>();
    int index = 0;
    MethodVisitor mv; // visitor of method currently under construction

    /**
     * Indicates whether genPrint and genPrintTOS should generate code.
     */
    final boolean DEVEL;
    final boolean GRADE;

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        className = program.getName();
        classDesc = "L" + className + ";";
        String sourceFileName = (String) arg;
        cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
                new String[]{"java/lang/Runnable"});
        cw.visitSource(sourceFileName, null);

        // generate constructor code
        // get a MethodVisitor
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
                null);
        mv.visitCode();
        // Create label at start of code
        Label constructorStart = new Label();
        mv.visitLabel(constructorStart);
        // this is for convenience during development--you can see that the code
        // is doing something.
        CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
        // generate code to call superclass constructor
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        // visit parameter decs to add each as field to the class
        // pass in mv so decs can add their initialization code to the
        // constructor.
        ArrayList<ParamDec> params = program.getParams();
        for (ParamDec dec : params)
            dec.visit(this, mv);
        mv.visitInsn(RETURN);
        // create label at end of code
        Label constructorEnd = new Label();
        mv.visitLabel(constructorEnd);
        // finish up by visiting local vars of constructor
        // the fourth and fifth arguments are the region of code where the local
        // variable is defined as represented by the labels we inserted.
        mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
        mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
        // indicates the max stack size for the method.
        // because we used the COMPUTE_FRAMES parameter in the classwriter
        // constructor, asm
        // will do this for us. The parameters to visitMaxs don't matter, but
        // the method must
        // be called.
        mv.visitMaxs(1, 1);
        // finish up code generation for this method.
        mv.visitEnd();
        // end of constructor

        // create main method which does the following
        // 1. instantiate an instance of the class being generated, passing the
        // String[] with command line arguments
        // 2. invoke the run method.
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
                null);
        mv.visitCode();
        Label mainStart = new Label();
        mv.visitLabel(mainStart);
        // this is for convenience during development--you can see that the code
        // is doing something.
        CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
        mv.visitTypeInsn(NEW, className);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
        mv.visitInsn(RETURN);
        Label mainEnd = new Label();
        mv.visitLabel(mainEnd);
        mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
        mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // create run method
        mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
        mv.visitCode();
        Label startRun = new Label();
        mv.visitLabel(startRun);
        CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
        program.getB().visit(this, null);
        mv.visitInsn(RETURN);
        Label endRun = new Label();
        mv.visitLabel(endRun);
        mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
        //TODO  visit the local variables
        List<Dec> list = program.getB().getDecs();
        for(Dec dec:list){
        		if(startMap.get(dec) == null || endMap.get(dec) == null ){
        			Label tempStartLabel = new Label();
        			Label tempEndLabel = new Label();
        			mv.visitLocalVariable(dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc(), null, tempStartLabel, tempEndLabel, dec.slot_number);
        		}else {
        			mv.visitLocalVariable(dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc(), null, startMap.get(dec), endMap.get(dec), dec.slot_number);
        		}
        	}
        mv.visitMaxs(1, 1);
        mv.visitEnd(); // end of run method


        cw.visitEnd();//end of class

        //generate classfile and return it
        return cw.toByteArray();
    }



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		if(assignStatement.getE().getTypeName().isType(Type.TypeName.IMAGE)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage", PLPRuntimeImageOps.copyImageSig, false);
		}
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypeName());
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		binaryChain.getE0().visit(this, true);
		if(binaryChain.getE0().getTypeName().isType(Type.TypeName.URL)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL", PLPRuntimeImageIO.readFromURLSig, false);
		}else if(binaryChain.getE0().getTypeName().isType(Type.TypeName.FILE)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile", PLPRuntimeImageIO.readFromFileDesc, false);
		}
		binaryChain.getE1().visit(this, false);
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
      //TODO  Implement this
		binaryExpression.getE0().visit(this, arg);
		binaryExpression.getE1().visit(this, arg);
		Label falseLabel = new Label();
		Label trueLabel = new Label();
		if(binaryExpression.getE0().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN) 
				&& binaryExpression.getE1().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN)
				&& binaryExpression.getOp().isKind(PLUS)){
			mv.visitInsn(IADD);
		}else if(binaryExpression.getE0().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN) 
				&& binaryExpression.getE1().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN)
				&& binaryExpression.getOp().isKind(MINUS)){
			mv.visitInsn(ISUB);
		}else if(binaryExpression.getE0().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN) 
				&& binaryExpression.getE1().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN)
				&& binaryExpression.getOp().isKind(TIMES)){
			mv.visitInsn(IMUL);
		}else if(binaryExpression.getE0().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN) 
				&& binaryExpression.getE1().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN)
				&&binaryExpression.getOp().isKind(DIV)){
			mv.visitInsn(IDIV);
		}else if(binaryExpression.getE0().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN) 
				&& binaryExpression.getE1().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN)
				&& binaryExpression.getOp().isKind(AND)){
			mv.visitInsn(IAND);
		}else if(binaryExpression.getE0().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN) 
				&& binaryExpression.getE1().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN)
				&& binaryExpression.getOp().isKind(MOD)){
			mv.visitInsn(IREM);
		}else if(binaryExpression.getE0().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN) 
				&& binaryExpression.getE1().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN)
				&& binaryExpression.getOp().isKind(OR)){
			mv.visitInsn(IOR);
		}else if(binaryExpression.getE0().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN) 
				&& binaryExpression.getE1().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN)
				&& binaryExpression.getOp().isKind(LT)){
			mv.visitJumpInsn(IF_ICMPGE, falseLabel);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, trueLabel);
			mv.visitLabel(falseLabel);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(trueLabel);
		}else if (binaryExpression.getE0().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN) 
				&& binaryExpression.getE1().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN)
				&& binaryExpression.getOp().isKind(GT)){
			mv.visitJumpInsn(IF_ICMPLE, falseLabel);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, trueLabel);
			mv.visitLabel(falseLabel);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(trueLabel);
		}else if(binaryExpression.getE0().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN) 
				&& binaryExpression.getE1().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN)
				&& binaryExpression.getOp().isKind(LE)){
			mv.visitJumpInsn(IF_ICMPGT, falseLabel);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, trueLabel);
			mv.visitLabel(falseLabel);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(trueLabel);
		}else if(binaryExpression.getE0().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN) 
				&& binaryExpression.getE1().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN)
				&& binaryExpression.getOp().isKind(GE)){
			mv.visitJumpInsn(IF_ICMPLT, falseLabel);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, trueLabel);	
			mv.visitLabel(falseLabel);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(trueLabel);
		}else if(binaryExpression.getE0().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN) 
				&& binaryExpression.getE1().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN)
				&& binaryExpression.getOp().isKind(EQUAL)){
			mv.visitJumpInsn(IF_ICMPNE, falseLabel);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, trueLabel);
			mv.visitLabel(falseLabel);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(trueLabel);
		}else if(binaryExpression.getE0().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN) 
				&& binaryExpression.getE1().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN)
				&& binaryExpression.getOp().isKind(NOTEQUAL)){
			mv.visitJumpInsn(IF_ICMPEQ, falseLabel);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, trueLabel);
			mv.visitLabel(falseLabel);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(trueLabel);
		}else if(binaryExpression.getE0().getTypeName().isType(Type.TypeName.IMAGE) 
				&& binaryExpression.getE1().getTypeName().isType(Type.TypeName.IMAGE)
				&& binaryExpression.getOp().isKind(PLUS)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add", PLPRuntimeImageOps.addSig, false);
		}else if(binaryExpression.getE0().getTypeName().isType(Type.TypeName.IMAGE) 
				&& binaryExpression.getE1().getTypeName().isType(Type.TypeName.IMAGE)
				&& binaryExpression.getOp().isKind(MINUS)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub", PLPRuntimeImageOps.subSig, false);
		}else if(binaryExpression.getE0().getTypeName().isType(Type.TypeName.IMAGE) 
				&& binaryExpression.getE1().getTypeName().isType(Type.TypeName.INTEGER)
				&& binaryExpression.getOp().isKind(TIMES)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
		}else if(binaryExpression.getE0().getTypeName().isType(Type.TypeName.INTEGER) 
				&& binaryExpression.getE1().getTypeName().isType(Type.TypeName.IMAGE)
				&& binaryExpression.getOp().isKind(TIMES)){
			mv.visitInsn(SWAP);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
		}else if(binaryExpression.getE0().getTypeName().isType(Type.TypeName.IMAGE) 
				&& binaryExpression.getE1().getTypeName().isType(Type.TypeName.INTEGER)
				&& binaryExpression.getOp().isKind(DIV)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);
		}else if(binaryExpression.getE0().getTypeName().isType(Type.TypeName.IMAGE) 
				&& binaryExpression.getE1().getTypeName().isType(Type.TypeName.INTEGER)
				&& binaryExpression.getOp().isKind(MOD)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
		}	
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//TODO  Implement this
		Label endLabel = new Label();
		Iterator<Dec> decIt = block.getDecs().iterator();
		while(decIt.hasNext()){
			Dec tempDec = decIt.next();
			tempDec.visit(this, arg);
			endMap.put(tempDec, endLabel);
		}
		Iterator<Statement> staIt = block.getStatements().iterator();
		while(staIt.hasNext()){
			Statement tempSta = staIt.next();
			tempSta.visit(this, arg);
			if(tempSta instanceof BinaryChain){
				mv.visitInsn(POP);
			}
		}
		mv.visitLabel(endLabel);
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		//TODO Implement this
		if(booleanLitExpression.getValue()){
			mv.visitInsn(ICONST_1);
		}else{
		    mv.visitInsn(ICONST_0);
		}
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
	    if(constantExpression.getFirstToken().isKind(KW_SCREENWIDTH)){
	    	    mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth", PLPRuntimeFrame.getScreenWidthSig, false);
	    }else if(constantExpression.getFirstToken().isKind(KW_SCREENHEIGHT)){
	    	    mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight", PLPRuntimeFrame.getScreenHeightSig, false);
	    }
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		//TODO Implement this
		declaration.slot_number = slotNumber;
		slotNumber++;
		if(declaration.getTypeName().isType(Type.TypeName.IMAGE,Type.TypeName.FRAME)){
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, declaration.slot_number);
		}
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		filterOpChain.getArg().visit(this, arg);
		if(filterOpChain.getFirstToken().isKind(OP_BLUR)){
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
		}else if(filterOpChain.getFirstToken().isKind(OP_GRAY)){
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
		}else if(filterOpChain.getFirstToken().isKind(OP_CONVOLVE)){
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig, false);
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		frameOpChain.getArg().visit(this, arg);
		if(frameOpChain.getFirstToken().isKind(KW_SHOW)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc, false);
		}else if(frameOpChain.getFirstToken().isKind(KW_HIDE)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc, false);
		}else if(frameOpChain.getFirstToken().isKind(KW_MOVE)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc, false);
		}else if(frameOpChain.getFirstToken().isKind(KW_XLOC)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc, false);
		}else if(frameOpChain.getFirstToken().isKind(KW_YLOC)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc, false);
		}
		return null;
	}

    @Override
    public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
    	    boolean side = (boolean)arg;
		if(side){
			if(identChain.getDec() instanceof ParamDec){
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, identChain.getFirstToken().getText(), identChain.getDec().getTypeName().getJVMTypeDesc());
			}else{
				if(identChain.getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN)){
					mv.visitVarInsn(ILOAD, identChain.getDec().slot_number);
				}else{
					mv.visitVarInsn(ALOAD, identChain.getDec().slot_number);
				}
			}
		}else if (!side) {
        	    if(identChain.getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN)){
				mv.visitInsn(DUP);
			    mv.visitVarInsn(ISTORE,identChain.getDec().slot_number);
			}else if(identChain.getTypeName().isType(Type.TypeName.IMAGE)){
				mv.visitInsn(DUP);
				mv.visitVarInsn(ASTORE, identChain.getDec().slot_number);
			}else if(identChain.getTypeName().isType(Type.TypeName.FILE)){
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(), identChain.getDec().getTypeName().getJVMTypeDesc());
			    mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write", PLPRuntimeImageIO.writeImageDesc, false);
			}else if(identChain.getTypeName().isType(Type.TypeName.FRAME)){
				mv.visitVarInsn(ALOAD, identChain.getDec().slot_number);
			    mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
			    mv.visitInsn(DUP);
			    mv.visitVarInsn(ASTORE, identChain.getDec().slot_number);
			}
        }
        return null;
    }

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		//TODO Implement this
		if(identExpression.getDec() instanceof ParamDec){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, identExpression.getFirstToken().getText(),identExpression.getDec().getTypeName().getJVMTypeDesc());
		}else{
			if(identExpression.getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN)){
				mv.visitVarInsn(ILOAD, identExpression.getDec().slot_number);
			}else {
				mv.visitVarInsn(ALOAD, identExpression.getDec().slot_number);
			}
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//TODO Implement this
		Label start = new Label();
		//startMap.put(identX.getDec(), start);
		if(identX.getDec() instanceof ParamDec){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(SWAP);
			mv.visitFieldInsn(PUTFIELD, className,identX.getText(),identX.getDec().getTypeName().getJVMTypeDesc());
		}else{
			if(identX.getDec().getTypeName().isType(Type.TypeName.INTEGER,Type.TypeName.BOOLEAN)){
				mv.visitVarInsn(ISTORE, identX.getDec().slot_number);
			}else{
				mv.visitVarInsn(ASTORE, identX.getDec().slot_number);
			}
		}
		mv.visitLabel(start);
		startMap.put(identX.getDec(), start);
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		//TODO Implement this
		ifStatement.getE().visit(this, arg);
		Label AFTER = new Label();
		mv.visitJumpInsn(IFEQ, AFTER);
		ifStatement.getB().visit(this, arg);
		mv.visitLabel(AFTER);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		imageOpChain.getArg().visit(this, arg);
		if(imageOpChain.getFirstToken().isKind(OP_WIDTH)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeImageIO.BufferedImageClassName, "getWidth", PLPRuntimeImageOps.getWidthSig, false);
		}else if(imageOpChain.getFirstToken().isKind(OP_HEIGHT)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeImageIO.BufferedImageClassName, "getHeight", PLPRuntimeImageOps.getHeightSig, false);
		}else if(imageOpChain.getFirstToken().isKind(KW_SCALE)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig,false);
		}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//TODO Implement this
		mv.visitLdcInsn(intLitExpression.value);
		return null;
	}

    
	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//TODO Implement this
		//For assignment 5, only needs to handle integers and booleans
		FieldVisitor fv;
		fv = cw.visitField(0, paramDec.getIdent().getText(), paramDec.getTypeName().getJVMTypeDesc(), null, null);
		fv.visitEnd();
		mv.visitVarInsn(ALOAD, 0);
		if(paramDec.getTypeName().isType(Type.TypeName.FILE)){
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
		}
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn(index++);
		if(!paramDec.getTypeName().isType(URL)){
			mv.visitInsn(AALOAD);
		}
		if(paramDec.getTypeName().isType(Type.TypeName.INTEGER)){
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
		}else if(paramDec.getTypeName().isType(Type.TypeName.BOOLEAN)){
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
		}else if(paramDec.getTypeName().isType(Type.TypeName.URL)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), paramDec.getTypeName().getJVMTypeDesc());
		}else if(paramDec.getTypeName().isType(Type.TypeName.FILE)){
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), paramDec.getTypeName().getJVMTypeDesc());
		}	
		return null;

	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.getE().visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		List<Expression> list = tuple.getExprList();
		Iterator<Expression> it = list.iterator();
		while(it.hasNext()){
			it.next().visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		//TODO Implement this
		Label guardStart = new Label();
		mv.visitJumpInsn(GOTO, guardStart);
		Label bodyStart = new Label();
		mv.visitLabel(bodyStart);
		whileStatement.getB().visit(this, arg);	
		mv.visitLabel(guardStart);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFNE, bodyStart);		
		return null;
	}

}
