import java.util.ArrayList;

public class LLVM_IR {
    public AstNode root;
    public SymbolTable globalTable = new SymbolTable("globalTable");
    public ArrayList<SymbolTable> tables = new ArrayList<>();
    public int tableNum = 0;
    public String IRCodes = "";
    public int blockNum = 0;
    public int register = 1;
    public boolean isReturn = false;

    private boolean isDefiningConst = false;

    public LLVM_IR(AstNode root) {
        this.root = root;
        init();
        start(this.root);
//        System.out.println(IRCodes);
    }

    private void init(){
        globalTable.table.put("getint", new Symbol("getint", SymbolType.FUNC, ValueType.INT, new ArrayList<>(), globalTable));
        globalTable.table.put("getch", new Symbol("getch", SymbolType.FUNC, ValueType.INT, new ArrayList<>(), globalTable));
        ArrayList<String> params = new ArrayList<>();
        params.add("i32");
        globalTable.table.put("putint", new Symbol("putint", SymbolType.FUNC, ValueType.VOID, params, globalTable));
        globalTable.table.put("putch", new Symbol("putch", SymbolType.FUNC, ValueType.VOID, params, globalTable));
    }

    private void start(AstNode root){
        CompUnit(root);
    }
    public void CompUnit(AstNode root){
        MainFuncDef(root.children.get(0));
    }

    public void MainFuncDef(AstNode node){
        IRCodes += "define dso_local i32 @main(){\n";
        Block(node.children.get(4));  //Block

        IRCodes += "}\n";
    }

    public void Block(AstNode node){
        tables.add(new SymbolTable(tableNum++ + ""));
        for(AstNode child : node.children){
            if(child.type.equals("<BlockItem>"))
                BlockItem(child);
        }
        tables.remove(tables.size()-1);
    }

    public void BlockItem(AstNode node){
        String next_hop = node.children.get(0).type;
        switch (next_hop) {
            case "<VarDecl>" -> VarDecl(node.children.get(0));
            case "<ConstDecl>" -> ConstDecl(node.children.get(0));
            case "<Stmt>" -> Stmt(node.children.get(0));
        }
    }

    public void Stmt(AstNode node){
        String next_hop = node.children.get(0).type;
        switch (next_hop) {
            case "<LVal>":
                int regIdent = LVal(node.children.get(0));
                int regExp = Exp(node.children.get(2)).register;
                IRCodes += "\tstore i32 %" + regExp + ", i32* %" + regIdent + "\n";
                break;
            case "<Block>":
                Block(node.children.get(0));
                break;
            case "<Exp>":
                Exp(node.children.get(0));
                break;
            case "IFTK":
                int trueLabel = blockNum++;
                int falseLabel = blockNum++;
                Cond(node.children.get(2), trueLabel, falseLabel);
                if(node.children.size() == 5){
                    IRCodes += "\nblock" + trueLabel + ":\n";
//                    int trueHop = blockNum++;
//                    int outHop = blockNum++;
//                    IRCodes += "\tbr i1 %" + condReg + ",label %block" + trueHop + ", label %block" + outHop + "\n";
//                    IRCodes += "\nblock" + trueHop + ":\n";
                    Stmt(node.children.get(4));
                    if(!isReturn)
                        IRCodes += "\tbr %block" + falseLabel + "\n";
                    else isReturn = false;
                    IRCodes += "\nblock" + falseLabel + ":\n";
//                    IRCodes += "\tbr label %block" + outHop + "\n";
//                    IRCodes += "\nblock" + outHop + ":\n";
                } else if(node.children.size() == 7){
                    int nextLabel = blockNum++;
                    IRCodes += "\nblock" + trueLabel + ":\n";
                    Stmt(node.children.get(4));
                    if(!isReturn)
                        IRCodes += "\tbr %block" + falseLabel + "\n";
                    else isReturn = false;
                    IRCodes += "\nblock" + falseLabel + ":\n";
                    Stmt(node.children.get(6));
                    if(!isReturn)
                        IRCodes += "\tbr %block" + nextLabel + "\n";
                    else isReturn = false;
                    IRCodes += "\nblock" + nextLabel + ":\n";
                }
                break;
            case "RETURNTK":
                isReturn = true;
                int returnReg = Exp(node.children.get(1)).register;
                IRCodes += "\tret i32 %" + returnReg + "\n";
                break;
        }
    }

    public void Cond(AstNode node, int trueLabel, int falseLabel){
        LOrExp(node.children.get(0), trueLabel, falseLabel);
    }

    public void LOrExp(AstNode node, int trueIfLabel, int falseIfLabel){
        int nextLabel = -1;
        for(int i = 0; i <= node.children.size()-1; i++){
            AstNode child = node.children.get(i);
            if(child.type.equals("<LAndExp>")){
                if(i == (node.children.size() -1)){
                    if (nextLabel != -1) {
                        IRCodes += "\nblock" + nextLabel + ":\n";
                    }
                    LAndExp(child, trueIfLabel, falseIfLabel);
//                    IRCodes += "\tbr i1 %" + andReg + ", %label" + trueIfLabel + ", %label" + falseIfLabel + "\n";
                } else {
                    if(nextLabel != -1){
                        IRCodes += "\nblock" + nextLabel + ":\n";
                    }
                    nextLabel = blockNum++;
                    LAndExp(child, trueIfLabel, nextLabel);
//                    IRCodes += "\tbr i1 %" + andReg + ", %label" + trueIfLabel + ", %label" + nextLabel + "\n";
                }
            }
        }
    }

    public void LAndExp(AstNode node, int trueLabel, int falseLabel){
        int nextLabel = -1;
        for(int i = 0; i <= node.children.size()-1; i++){
            AstNode child = node.children.get(i);
            if(child.type.equals("<EqExp>")){
                if(i == (node.children.size() -1)){
                    if (nextLabel != -1) {
                        IRCodes += "\nblock" + nextLabel + ":\n";
                    }
                    int reg = EqExp(child);
//                    IRCodes += "%" + regNew +" = icmp eq i1 %" + reg + " 1" + "\n";
                    IRCodes += "\tbr i1 %" + reg + ", %block" + trueLabel + ", %block" + falseLabel + "\n";
                } else {
                    if(nextLabel != -1){
                        IRCodes += "\nblock" + nextLabel + ":\n";
                    }
                    nextLabel = blockNum++;
                    int reg = EqExp(child);
                    IRCodes += "\tbr i1 %" + reg + ", %block" + nextLabel + ", %block" + falseLabel + "\n";
                }
            }
        }
    }

    public int EqExp(AstNode node){
        ExpValue expValue;
        int expReg;
        int preReg = -1;
        int resultReg;
        String op = "";
        for(int i = 0; i <= node.children.size()-1; i++){
            AstNode child = node.children.get(i);
            switch (child.type) {
                case "<RelExp>" -> {
                     expValue = RelExp(child);
                     expReg = expValue.register;
                    if (op.equals("==")) {
                        if(expValue.valueType.equals("i1")){
                            int tmp = register++;
                            IRCodes += "%" + tmp + " = zext i1" + expReg + " to i32";
                            expReg = tmp;
                        }
                        resultReg = register++;
                        IRCodes += "%" + resultReg +" = icmp eq i32 %" + preReg + " %" + expReg + "\n";
                        preReg = resultReg;
                        if(i != node.children.size()-1){
                            preReg = register++;
                            IRCodes += "%" + preReg + " = zext i1 %" + resultReg + " to i32\n";
                        }
                    } else if (op.equals("!=")) {
                        if(expValue.valueType.equals("i1")){
                            int tmp = register++;
                            IRCodes += "%" + tmp + " = zext i1" + expReg + " to i32";
                            expReg = tmp;
                        }
                        resultReg = register++;
                        IRCodes += "%" + resultReg +" = icmp ne i32 %" + preReg + " %" + expReg + "\n";
                        preReg = resultReg;
                        if(i != node.children.size()-1){
                            preReg = register++;
                            IRCodes += "%" + preReg + " = zext i1 %" + resultReg + " to i32\n";
                        }
                    } else {
                        preReg = expReg;
                    }
                }
                case "EQL" -> op = "==";
                case "NEQ" -> op = "!=";
            }
        }
        return preReg;
    }

    public ExpValue RelExp(AstNode node){
        ExpValue expValue;
        ExpValue expValuePre = new ExpValue(-1, "i32");
        int preReg = -1;
        int resultReg;
        String op = "";
        if(node.children.size() == 1){
            int addReg = AddExp(node.children.get(0)).register;
            int newReg = register ++;
            IRCodes += "\t%" + newReg + " = icmp sgt i32 %" + addReg + ", 0\n";
            return new ExpValue(newReg, "i1");
        }
        for(int i = 0; i <= node.children.size()-1; i++){
            AstNode child = node.children.get(i);
            switch (child.type) {
                case "<AddExp>" -> {
                    int addReg = AddExp(child).register;
                    expValue = new ExpValue(addReg, "i32");
                    switch (op) {
                        case "<" -> {
                            resultReg = register++;
                            IRCodes += "%" + resultReg + " = icmp slt i32 %" + expValuePre.register + " %" + expValue.register + "\n";
                            expValuePre = new ExpValue(resultReg, "i1");
                            if (i != node.children.size() - 1) {
                                preReg = register++;
                                IRCodes += "%" + preReg + " = zext i1 %" + resultReg + " to i32\n";
                            }
                        }
                        case "<=" -> {
                            resultReg = register++;
                            IRCodes += "%" + resultReg + " = icmp sle i32 %" + expValuePre.register + " %" + expValue.register + "\n";
                            expValuePre = new ExpValue(resultReg, "i1");
                            if (i != node.children.size() - 1) {
                                preReg = register++;
                                IRCodes += "%" + preReg + " = zext i1 %" + resultReg + " to i32\n";
                            }
                        }
                        case ">" -> {
                            resultReg = register++;
                            IRCodes += "%" + resultReg + " = icmp sgt i32 %" + expValuePre.register + " %" + expValue.register + "\n";
                            expValuePre = new ExpValue(resultReg, "i1");
                            if (i != node.children.size() - 1) {
                                preReg = register++;
                                IRCodes += "%" + preReg + " = zext i1 %" + resultReg + " to i32\n";
                            }
                        }
                        case ">=" -> {
                            resultReg = register++;
                            IRCodes += "%" + resultReg + " = icmp sge i32 %" + expValuePre.register + " %" + expValue.register + "\n";
                            expValuePre = new ExpValue(resultReg, "i1");
                            if (i != node.children.size() - 1) {
                                preReg = register++;
                                IRCodes += "%" + preReg + " = zext i1 %" + resultReg + " to i32\n";
                            }
                        }
                        default -> expValuePre = expValue;
                    }
                }
                case "LSS" -> op = "<";
                case "LEQ" -> op = "<=";
                case "GRE" -> op = ">";
                case "GEQ" -> op = ">=";
            }
        }
        return expValuePre;
    }


    public void VarDecl(AstNode node){
//        String type = node.children.get(0).value;
        for(AstNode child: node.children){
            if (child.type.equals("<VarDef>")){
                VarDef(child);
            }
        }
    }

    public void VarDef(AstNode node){
        String Ident = node.children.get(0).value;
        if(isInTable(Ident));
        SymbolTable table = tables.get(tables.size()-1);
        Symbol newSym = new Symbol(Ident, SymbolType.VAR, ValueType.INT, register++, table);
        table.table.put(Ident, newSym);
        IRCodes += "\t" + newSym.registerString + " = alloca i32\n";
        if(node.children.size() > 1){
            int registerComing = InitVal(node.children.get(2));
            IRCodes += "\tstore i32 " + "%" + registerComing + ", i32* " + newSym.registerString + "\n";
        }
    }

    public void ConstDecl(AstNode node){
        for(AstNode child: node.children){
            if (child.type.equals("<ConstDef>")){
                ConstDef(child);
            }
        }
    }

    public void ConstDef(AstNode node){
        String Ident = node.children.get(0).value;
        if(isInTable(Ident));
        SymbolTable table = tables.get(tables.size()-1);
        Symbol newSym = new Symbol(Ident, SymbolType.CONST, ValueType.INT, register++, table);
        table.table.put(Ident, newSym);
        IRCodes += "\t" + newSym.registerString + " = alloca i32\n";
        isDefiningConst = true;
        if(node.children.size() > 1){
            int registerComing = ConstInitVal(node.children.get(2));
            IRCodes += "\tstore i32 " + "%" + registerComing + ", i32* " + newSym.registerString + "\n";
        }
        isDefiningConst = false;
    }

    // 只能是常量和const
    public int ConstInitVal(AstNode node){
        return Exp(node.children.get(0)).register;
    }

    public int InitVal(AstNode node){
        return Exp(node.children.get(0)).register;

    }

    public ExpValue Exp(AstNode node){

        return AddExp(node.children.get(0));
    }

    public ExpValue AddExp(AstNode node){
        int registerPre = -1;
        String op = "";
        boolean flag = true;
        for(AstNode child: node.children){
            switch (child.type) {
                case "<MulExp>" -> {
                    ExpValue registerValue = MulExp(child);
                    if(registerValue == null) return null;
                    int registerNow = registerValue.register;
                    if (flag) {
                        registerPre = registerNow;
                        flag = false;
                    }
                    if (op.equals("PLUS")) {
                        int newReg = register++;
                        IRCodes += "\t%" + newReg + " = add i32 %" + registerPre + ", i32 %" + registerNow + "\n";
                        registerPre = newReg;
                    } else if (op.equals("MINUS")) {
                        int newReg = register++;
                        IRCodes += "\t%" + newReg + " = sub i32 %" + registerPre + ", i32 %" + registerNow + "\n";
                        registerPre = newReg;
                    }
                }
                case "PLUS" -> op = "PLUS";
                case "MINU" -> op = "MINUS";
            }
        }
        return new ExpValue(registerPre, "i32");
    }

    public ExpValue MulExp(AstNode node){
        int registerPre = -1;
        String op = "";
        boolean flag = true;
        for(AstNode child: node.children){
            switch (child.type) {
                case "<UnaryExp>" -> {
                    ExpValue registerValue = UnaryExp(child);
                    if(registerValue == null) return null;
                    int registerNow = registerValue.register;
                    if (flag) {
                        registerPre = registerNow;
                        flag = false;
                    }
                    switch (op) {
                        case "MULT" -> {
                            int newReg = register++;
                            IRCodes += "\t%" + newReg + " = mul i32 %" + registerPre + ", i32 %" + registerNow + "\n";
                            registerPre = newReg;
                        }
                        case "DIV" -> {
                            int newReg = register++;
                            IRCodes += "\t%" + newReg + " = sdiv i32 %" + registerPre + ", i32 %" + registerNow + "\n";
                            registerPre = newReg;
                        }
                        case "MOD" -> {
                            int newReg = register++;
                            IRCodes += "\t%" + newReg + " = srem i32 %" + registerPre + ", i32 %" + registerNow + "\n";
                            registerPre = newReg;
                        }
                    }
                }
                case "MULT" -> op = "MULT";
                case "DIV" -> op = "DIV";
                case "MOD" -> op = "MOD";
            }
        }
        return new ExpValue(registerPre, "i32");
    }

    public ExpValue UnaryExp(AstNode node){
        String next_hop = node.children.get(0).type;
        switch (next_hop) {
            case "<PrimaryExp>":
                return PrimaryExp(node.children.get(0));
            case "IDENFR":
                if (isDefiningConst) {
                    throw new java.lang.Error("can not define const by func");
                }
                String ident = node.children.get(0).value;
                Symbol identSym = getFuncSym(ident);
                if (identSym == null) {
                    throw new java.lang.Error("function used before declaration");
                }
                ArrayList<String> params = new ArrayList<>();
                ArrayList<String> paramsType = identSym.funcParams;
                if (node.children.get(2).type.equals("<FuncRParams>")) {
                    params = FuncRParams(node.children.get(2));
                }
                if (params.size() != paramsType.size()) {
                    throw new java.lang.Error("func params are not consistent");
                }
                String out = "";
                String outDecl = "";
                for (int i = 0; i <= params.size() - 1; i++) {
                    if (i == 0) {
                        out += paramsType.get(i) + " " + params.get(i);
                        outDecl += paramsType.get(i);
                    } else {
                        out += ", " + paramsType.get(i) + " " + params.get(i);
                        outDecl += "," + paramsType.get(i);
                    }
                }
                if (identSym.valueType == ValueType.INT) {
                    int regNew = register++;
                    // 函数声明
                    IRCodes = "declare i32 @" + identSym.symbolName + "(" + outDecl + ")\n" + IRCodes;
                    // 函数调用
                    IRCodes += "\t%" + regNew + " = call i32 @" + identSym.symbolName + "(" + out + ")\n";
                    return new ExpValue(regNew, "i32");
                } else if (identSym.valueType == ValueType.VOID) {
                    IRCodes = "declare void @" + identSym.symbolName + "(" + outDecl + ")\n" + IRCodes;
                    IRCodes += "\tcall void @" + identSym.symbolName + "(" + out + ")\n";
                }

                break;
            case "<UnaryOp>":
                String sign = UnaryOp(node.children.get(0));
                System.out.println(sign);
                switch (sign) {
                    case "-" -> {
                        ExpValue reg = UnaryExp(node.children.get(1));
                        int regBefore = reg.register;
                        int regNew = register++;
                        IRCodes += "\t%" + regNew + " = sub i32 0, %" + regBefore + "\n";
                        return new ExpValue(regNew, "i32");
                    }
                    case "+" -> {
                        return UnaryExp(node.children.get(1));
                    }
                    case "!" -> {
                        ExpValue reg = UnaryExp(node.children.get(1));
                        int regBefore = reg.register;
//                        if(reg.valueType.equals("i1")){
//                            int regNew = register++;
//                            IRCodes += "\t%" + regNew + " = zext i1, %" + regBefore + " to i32\n";
//                            regBefore = regNew;
//                        }
                        int regNew = register++;
                        IRCodes += "\t%" + regNew + " = icmp eq i32 %" + regBefore + ", 0\n";
                        regBefore = regNew;
                        regNew = register++;
                        IRCodes += "\t%" + regNew + " = zext i1 %" + regBefore + " to i32\n";
                        return new ExpValue(regNew, "i32");
                    }
                }
                break;
        }
        return null;
    }

    public ArrayList<String> FuncRParams(AstNode node){
        ArrayList<String> params = new ArrayList<>();
        for(AstNode child: node.children){
            params.add("%" + Exp(child).register);
        }
        return params;
    }

    public String UnaryOp(AstNode node){
        return node.children.get(0).value;
    }

    public ExpValue PrimaryExp(AstNode node){
        String next_hop = node.children.get(0).type;
        switch (next_hop) {
            case "<Number>":
                return Number(node.children.get(0));
            case "<LVal>":
                int regBefore = LVal(node.children.get(0));
                int regNew = register++;
                IRCodes += "\t%" + regNew + " = load i32, i32* %" + regBefore + "\n";
                return new ExpValue(regNew, "i32");
            case "LPARENT":
                return Exp(node.children.get(1));
        }
        return null;
    }

    public int LVal(AstNode node){
        String Ident = node.children.get(0).value;
        if(isDefiningConst){
            if(!isConst(Ident))
                throw new java.lang.Error("can not define const by var");
        }
        return getSymReg(Ident);
    }

    public ExpValue Number(AstNode node){
        int number = Integer.parseInt(node.children.get(0).value);
        int newReg = register++;
        IRCodes += "\t%" + newReg + " = add i32 0, " + number + "\n";
        return new ExpValue(newReg, "i32");
    }





    public int getSymReg(String Ident){
        Symbol sym = globalTable.table.get(Ident);
        if(sym != null){
            return sym.register;
        } else {
            for(SymbolTable table: tables){
                sym = table.table.get(Ident);
                if (sym != null) return sym.register;
            }
        }
        throw new java.lang.Error("symbol used before declaration");
    }

    private boolean isConst(String Ident){
        Symbol sym = globalTable.table.get(Ident);
        if(sym != null){
            return sym.symbolType == SymbolType.CONST;
        } else {
            for(SymbolTable table: tables){
                sym = table.table.get(Ident);
                if (sym != null) return sym.symbolType == SymbolType.CONST;
            }
        }
        throw new java.lang.Error("symbol used before declaration");
    }

    private Symbol getFuncSym(String ident){
        Symbol sym = globalTable.table.get(ident);
        if(sym != null && sym.symbolType == SymbolType.FUNC){
            return sym;
        } else {
            for(SymbolTable table: tables){
                sym = table.table.get(ident);
                if (sym != null && sym.symbolType == SymbolType.FUNC) return sym;
            }
        }
        return null;
    }

    private boolean isInTable(String Ident){
        Symbol sym = globalTable.table.get(Ident);
        if(sym != null){
            throw new java.lang.Error("symbol has been defined");
        } else {
            for(SymbolTable table: tables){
                sym = table.table.get(Ident);
                if (sym != null) throw new java.lang.Error("symbol has been defined");
            }
        }
        return true;
    }
    private int i1Toi32(ExpValue value){
        if(value.valueType.equals("i1")){
            int regNew = register++;
            IRCodes += "%" + regNew + " = zext i1 %" + value.register + " to i32\n";
            return regNew;
        }
        return -1;
    }

}
