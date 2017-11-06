package cop5556sp17;


import cop5556sp17.AST.Dec;

import java.util.*;


public class SymbolTable {

	int current_scope;
	int next_scope;
	Stack<Integer> scope_stack = new Stack<>();
	HashMap<String,HashMap<Integer,Dec>> table = new HashMap<>();
	
	//TODO  add fields

	/** 
	 * to be called when block entered
	 */
	public void enterScope(){
		//TODO:  IMPLEMENT THIS
		current_scope = next_scope++;
		scope_stack.push(current_scope);
		
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		//TODO:  IMPLEMENT THIS
		if(scope_stack.size() > 0){
			scope_stack.pop();
		}
	}

	public boolean insert(String ident, Dec dec){
		//TODO:  IMPLEMENT THIS
		if(table.containsKey(ident)){
			HashMap<Integer,Dec> temp = table.get(ident);
			if(temp.containsKey(current_scope)){
				return false;
			}else{
				temp.put(current_scope, dec);
				table.put(ident, temp);
				return true;
			}
		}else{
			HashMap<Integer, Dec> temp = new HashMap<Integer, Dec>();
			temp.put(current_scope, dec);
			table.put(ident,temp);
			return true;
		}		
	}

	public Dec lookup(String ident){
		//TODO:  IMPLEMENT THIS
		if(!table.containsKey(ident)){
			return null;
		}
		HashMap<Integer,Dec> temp = table.get(ident);
		int index = 0;
		for(int i = scope_stack.size() - 1;i >= 0;i--){
			int scope = scope_stack.get(i);
			if(temp.get(scope) != null){
				index = scope;
				break;
			}
		}
		return temp.get(index);
	}

    public SymbolTable() {
        current_scope = 0;
        next_scope = 1;
        table = new HashMap<String, HashMap<Integer, Dec>>();
        scope_stack = new Stack<Integer>();

    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Symbol Table: ");
        sb.append("\n");
        Set Stringkey = table.keySet();
        Iterator<String> stringIterator = Stringkey.iterator();
        while (stringIterator.hasNext()) {
            String ident = stringIterator.next();
            sb.append("identifier " + ident + ", scope number = ");
            Map<Integer, Dec> entryMap = table.get(ident);
            Set decScope = entryMap.keySet();
            Iterator<Integer> integerIterator = decScope.iterator();
            while (integerIterator.hasNext()) {
                int scope = integerIterator.next();
                Dec dec = entryMap.get(scope);
                sb.append(scope + ", " + "type: " + dec.getTypeName());
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
