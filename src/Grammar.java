import java.io.PrintWriter;
import java.util.ArrayList;

public class Grammar {
    private static int pointer = 0;
    private static ArrayList<String[]> lexical;
    private static int printPointer = 0;
    public static ArrayList<String> grammar = new ArrayList<>();
    public static  AstNode root;

    public static boolean grammar(ArrayList<String[]> lexicals, PrintWriter pw) {
        lexical = lexicals;
        if (compUnit()) {
//            for (int i = 0; i < printPointer; i++) {
//                pw.print(grammar.get(i));
//                if (i != printPointer -1) {
//                    pw.print("\n");
//                }
//            }
            return true;
        }
        return false;
    }

    private static boolean compUnit() {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_compUnit = new AstNode("<CompUnit>"); // root node
        if (mainFuncDef(node_compUnit)) {
            addgrammar("<CompUnit>");
            root = node_compUnit;
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean decl(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        if (constDecl(parent) || varDecl(parent)) {
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean constDecl(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_constDecl = new AstNode("<ConstDecl>");
        if (symbol("CONSTTK", node_constDecl) && bType(node_constDecl) && constDef(node_constDecl)) {
            while (symbol("COMMA", node_constDecl) && constDef(node_constDecl));
            if (symbol("SEMICN", node_constDecl)) {
                parent.children.add(node_constDecl);
                addgrammar("<ConstDecl>");
                return true;
            }
        }
        return falsechu(now, nowPrint);
    }

    private static boolean bType(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        if (symbol("INTTK", parent)) {
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean constDef(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_constDef = new AstNode("<ConstDef>");
        if (symbol("IDENFR", node_constDef)) {
            if (symbol("ASSIGN", node_constDef) && constInitVal(node_constDef)) {
                parent.children.add(node_constDef);
                addgrammar("<ConstDef>");
                return true;
            }
        }
        return falsechu(now, nowPrint);
    }

    private static boolean constInitVal(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_constInitVal = new AstNode("<ConstInitVal>");
        if (constExp(node_constInitVal)) {
            parent.children.add(node_constInitVal);
            addgrammar("<ConstInitVal>");
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean varDecl(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_varDecl = new AstNode("<VarDecl>");
        if(bType(node_varDecl) && varDef(node_varDecl)) {
            while(symbol("COMMA", node_varDecl) && varDef(node_varDecl));
            if (symbol("SEMICN", node_varDecl)) {
                parent.children.add(node_varDecl);
                addgrammar("<VarDecl>");
                return true;
            }
        }
        return falsechu(now, nowPrint);
    }

    private static boolean varDef(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_varDef = new AstNode("<VarDef>");
        if (symbol("IDENFR", node_varDef)) {
            if (symbol("ASSIGN", node_varDef) && initVal(node_varDef));
            parent.children.add(node_varDef);
            addgrammar("<VarDef>");
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean initVal(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_initVal = new AstNode("<InitVal>");
        if (exp(node_initVal)) {
            addgrammar("<InitVal>");
            parent.children.add(node_initVal);
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean mainFuncDef(AstNode root) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_mainFuncDef = new AstNode("<MainFuncDef>");
        if (symbol("INTTK", node_mainFuncDef)
                && symbol("MAINTK", node_mainFuncDef)
                && symbol("LPARENT", node_mainFuncDef)
                && symbol("RPARENT", node_mainFuncDef)
                && block(node_mainFuncDef)) {
            addgrammar("<MainFuncDef>");
            root.children.add(node_mainFuncDef);
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean block(AstNode root) {
        int now = pointer;
        int nowprint = printPointer;
        AstNode node_block = new AstNode("<Block>");
        if (symbol("LBRACE", node_block)) {
            while (blockItem(node_block));
            if (symbol("RBRACE", node_block)) {
                root.children.add(node_block);
                addgrammar("<Block>");
                return true;
            }
        }
        return falsechu(now, nowprint);
    }

    private static boolean blockItem(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_blockItem = new AstNode("<BlockItem>");
        if (decl(node_blockItem) || stmt(node_blockItem)) {
            parent.children.add(node_blockItem);
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean stmt(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        boolean flag = false;
        AstNode node_stmt = new AstNode("<Stmt>");
        if (block(node_stmt)) {
            flag = true;
        } else if (symbol("IFTK", node_stmt)
                && symbol("LPARENT", node_stmt)
                && cond(node_stmt) && symbol("RPARENT", node_stmt) && stmt(node_stmt)) {
            while (symbol("ELSETK", node_stmt) && stmt(node_stmt));
            flag = true;
        } else if (symbol("RETURNTK", node_stmt)) {
            if (exp(node_stmt));
            if (symbol("SEMICN", node_stmt)) {
                flag = true;
            }
        } else if (lVal(node_stmt) && symbol("ASSIGN", node_stmt)) {
            if (exp(node_stmt) && symbol("SEMICN", node_stmt)) {
                flag = true;
            }
        }
        if (!flag){
            falsechu(now, nowPrint);
            if(node_stmt.children.size() > 0 && node_stmt.children.get(node_stmt.children.size()-1).type.equals("<LVal>")){
                node_stmt.children = new ArrayList<>();
            }
            if(exp(node_stmt));
            if(symbol("SEMICN", node_stmt)) {
                flag = true;
            }
        }
        if (flag) {
            addgrammar("<Stmt>");
            parent.children.add(node_stmt);
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean exp(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_exp = new AstNode("<Exp>");
        if (addExp(node_exp)) {
            parent.children.add(node_exp);
            addgrammar("<Exp>");
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean cond(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_cond = new AstNode("<Cond>");
        if (lOrExp(node_cond)) {
            parent.children.add(node_cond);
            addgrammar("<Cond>");
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean lVal(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_lVal = new AstNode("<LVal>");
        if (symbol("IDENFR", node_lVal)) {
            parent.children.add(node_lVal);
            addgrammar("<LVal>");
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean primaryExp(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_primaryExp = new AstNode("<PrimaryExp>");
        if (lVal(node_primaryExp) || number(node_primaryExp)
                || (symbol("LPARENT", node_primaryExp)
                        && exp(node_primaryExp)
                        && symbol("RPARENT", node_primaryExp))) {
            parent.children.add(node_primaryExp);
            addgrammar("<PrimaryExp>");
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean number(AstNode parent) {
        int now = pointer;
        int nowprint = printPointer;
        AstNode node_number = new AstNode("<Number>");
        if (symbol("INTCON", node_number)) {
            parent.children.add(node_number);
            addgrammar("<Number>");
            return true;
        }
        return falsechu(now, nowprint);
    }

    private static boolean unaryExp(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_unaryExp = new AstNode("<UnaryExp>");
        if (symbol("IDENFR", node_unaryExp) && symbol("LPARENT", node_unaryExp)) {
            if(funcRParams(node_unaryExp));
            if (symbol("RPARENT", node_unaryExp)) {
                parent.children.add(node_unaryExp);
                addgrammar("<UnaryExp>");
                return true;
            }
        }
        falsechu(now, nowPrint);
        // 去掉回溯造成的多余的IDENFR
        if(node_unaryExp.children.size() > 0 && node_unaryExp.children.get(node_unaryExp.children.size()-1).type.equals("IDENFR")){
            node_unaryExp.children.remove(node_unaryExp.children.size()-1);
        }
        if (primaryExp(node_unaryExp)) {
            parent.children.add(node_unaryExp);
            addgrammar("<UnaryExp>");
            return true;
        }
        else if (unaryOp(node_unaryExp) && unaryExp(node_unaryExp)) {
            parent.children.add(node_unaryExp);
            addgrammar("<UnaryExp>");
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean unaryOp(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_unaryOp = new AstNode("<UnaryOp>");
        ArrayList<String> temp = new ArrayList<>();
        temp.add("PLUS");
        temp.add("MINU");
        temp.add("NOT");
        if (symbol(temp, node_unaryOp)) {
            parent.children.add(node_unaryOp);
            addgrammar("<UnaryOp>");
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean funcRParams(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_funcRParams = new AstNode("<FuncRParams>");
        if (exp(node_funcRParams)) {
            while (symbol("COMMA", node_funcRParams) && exp(node_funcRParams));
            parent.children.add(node_funcRParams);
            addgrammar("<FuncRParams>");
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean mulExp(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_mulExp = new AstNode("<MulExp>");
        if(unaryExp(node_mulExp)) {
            parent.children.add(node_mulExp);
            addgrammar("<MulExp>");
            ArrayList<String> temp = new ArrayList<>();
            temp.add("MULT");
            temp.add("DIV");
            temp.add("MOD");
            AstNode node_mulExp_temp;
            while ((symbol(temp, node_mulExp)) && unaryExp(node_mulExp)) {
                node_mulExp_temp = new AstNode("<MulExp>");
                node_mulExp.children.add(node_mulExp_temp);
                node_mulExp = node_mulExp_temp;
                addgrammar("<MulExp>");
            }
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean addExp(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_addExp = new AstNode("<AddExp>");
        if(mulExp(node_addExp)) {
            parent.children.add(node_addExp);
            addgrammar("<AddExp>");
            ArrayList<String> temp = new ArrayList<>();
            temp.add("PLUS");
            temp.add("MINU");
            while ((symbol(temp, node_addExp)) && mulExp(node_addExp)) {
//                AstNode node_addExp_temp = new AstNode("<AddExp>");
//                node_addExp.children.add(node_addExp_temp);
//                node_addExp = node_addExp_temp;
//                addgrammar("<AddExp>");
            }
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean relExp(AstNode parent) {
        int now = pointer;
        int nowprint = printPointer;
        AstNode node_relExp = new AstNode("<RelExp>");
        if(addExp(node_relExp)) {
            parent.children.add(node_relExp);
            addgrammar("<RelExp>");
            ArrayList<String> temp = new ArrayList<>();
            temp.add("LSS");
            temp.add("LEQ");
            temp.add("GRE");
            temp.add("GEQ");
            AstNode node_relExp_temp;
            while ((symbol(temp, node_relExp)) && addExp(node_relExp)) {
//                node_relExp_temp = new AstNode("<RelExp>");
//                node_relExp.children.add(node_relExp_temp);
//                node_relExp = node_relExp_temp;
//                addgrammar("<RelExp>");
            }
            return true;
        }
        return falsechu(now, nowprint);
    }

    private static boolean eqExp(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_eqExp = new AstNode("<EqExp>");
        if(relExp(node_eqExp)) {
            parent.children.add(node_eqExp);
            addgrammar("<EqExp>");
            ArrayList<String> temp = new ArrayList<>();
            temp.add("EQL");
            temp.add("NEQ");
            AstNode node_eqExp_temp;
            while ((symbol(temp, node_eqExp)) && relExp(node_eqExp)) {
//                node_eqExp_temp = new AstNode("<EqExp>");
//                node_eqExp.children.add(node_eqExp_temp);
//                node_eqExp = node_eqExp_temp;
//                addgrammar("<EqExp>");
            }
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean lAndExp(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_lAndExp = new AstNode("<LAndExp>");
        if(eqExp(node_lAndExp)) {
            parent.children.add(node_lAndExp);
            addgrammar("<LAndExp>");
            AstNode node_lAngExp_temp;
            while ((symbol("AND", node_lAndExp)) && eqExp(node_lAndExp)) {
//                node_lAngExp_temp = new AstNode("<LAngExp>");
//                node_lAndExp.children.add(node_lAngExp_temp);
//                node_lAndExp = node_lAngExp_temp;
//                addgrammar("<LAndExp>");
            }
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean lOrExp(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_lOrExp = new AstNode("<LOrExp>");
        if(lAndExp(node_lOrExp)) {
            parent.children.add(node_lOrExp);
            addgrammar("<LOrExp>");
            AstNode node_lOrExp_temp;
            while ((symbol("OR", node_lOrExp)) && lAndExp(node_lOrExp)) {
//                node_lOrExp_temp = new AstNode("<LOrExp>");
//                node_lOrExp.children.add(node_lOrExp_temp);
//                node_lOrExp = node_lOrExp_temp;
//                addgrammar("<LOrExp>");
            }
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean constExp(AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        AstNode node_constExp = new AstNode("<ConstExp>");
        if(addExp(node_constExp)) {
            parent.children.add(node_constExp);
            addgrammar("<ConstExp>");
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean symbolWithoutAdd(String str) {
        int now = pointer;
        int nowPrint = printPointer;
        String[] temp = lexical.get(pointer++);
        if (str.equals(temp[0])) {
            addgrammar(temp[0] + " " + temp[1]);
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean symbol(String str, AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        String[] temp = lexical.get(pointer++);
        if (str.equals(temp[0])) {
            AstNode node_symbol = new AstNode(temp[0],  temp[1]);
            parent.children.add(node_symbol);
            addgrammar(temp[0] + " " + temp[1]);
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean symbol(ArrayList<String> str, AstNode parent) {
        int now = pointer;
        int nowPrint = printPointer;
        String[] temp = lexical.get(pointer++);
        if (str.contains(temp[0])) {
            AstNode node_symbol = new AstNode(temp[0], temp[1]);
            parent.children.add(node_symbol);
            addgrammar(temp[0] + " " + temp[1]);
            return true;
        }
        return falsechu(now, nowPrint);
    }

    private static boolean falsechu(int now, int nowprint) {
        pointer = now;
        printPointer = nowprint;
        return false;
    }

    private static void addgrammar(String str) {
        if (printPointer == grammar.size()) {
            grammar.add(str);
            printPointer++;
        } else{
            grammar.set(printPointer++,str);
        }
    }
}
