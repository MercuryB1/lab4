import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Lexical {
    static String line;
    static int point;
    static boolean muxlineflag = false;
    static boolean muxlinestringflag = false;
    static boolean konghang = false;
    static StringBuilder result;

    static public ArrayList<String[]> lexical = new ArrayList<>();

    static private Map<String, String> category = new HashMap<String, String>(){{
        put("main", "MAINTK");
        put("const", "CONSTTK");
        put("int", "INTTK");
        put("break", "BREAKTK");
        put("continue", "CONTINUETK");
        put("if", "IFTK");
        put("else", "ELSETK");
        put("!", "NOT");
        put("&&", "AND");
        put("||", "OR");
        put("while", "WHILETK");
//        put("getint", "GETINTTK");
//        put("printf", "PRINTFTK");
        put("return", "RETURNTK");
        put("+", "PLUS");
        put("-", "MINU");
        put("void", "VOIDTK");
        put("*", "MULT");
        put("/", "DIV");
        put("%", "MOD");
        put("<", "LSS");
        put("<=", "LEQ");
        put(">", "GRE");
        put(">=", "GEQ");
        put("==", "EQL");
        put("!=", "NEQ");
        put("=", "ASSIGN");
        put(";", "SEMICN");
        put(",", "COMMA");
        put("(", "LPARENT");
        put(")", "RPARENT");
        put("[", "LBRACK");
        put("]", "RBRACK");
        put("{", "LBRACE");
        put("}", "RBRACE");
    }};

    public static void readtxts(BufferedReader bufferedReader) throws IOException {
        while ((line = bufferedReader.readLine()) != null) {
            point = 0;
            readtxt(line);
        }
    }

    private static void readtxt(String line) {
        try {
            // 一次读一个字符
            char c;
            char t;
//            konghang = true;
            while ((c = readone()) != '\0') {
//                if (muxlinestringflag) { //处理字符串多行
//                    result.append(c);
//                    if (c == '"') {
//                        lexical(result.toString(), pw);
//                        muxlinestringflag = false;
//                    }
//                    continue;
//                }
//                konghang = false;
                if (muxlineflag) {   //处理注释
                    if (c == '*' && (c = readone()) == '/') {
                        muxlineflag = false;
                    }
                    continue;
                } else if (c == '/') {
                    if ((t = readone()) == '/') {
                        break;
                    } else if (t == '*') {
                        muxlineflag = true;
                        continue;
                    } else {
                        unreadone();
                    }
                }

                result = new StringBuilder();
                if (contains(c)) { //处理单字符的特殊符号
                    char temp = c;
                    result.append(c);
                    if (temp == '>' || temp == '<' || temp == '!' || temp == '=') {
                        if ((c = readone()) != '\0' && c == '=') {
                            result.append(c);
                        } else {
                            unreadone();
                        }
                    }
                } else if(c == '&' || c == '|') { //处理&&，||
                    result.append(c);
                    result.append(readone());
                } else if(c == '"') { //处理FormatString  双引号可以在两行吗？？？？？？？？
                    result.append(c);
                    while((c = readone()) != '\0' && c != '"') {
                        result.append(c);
                    }
//                    if (c == '"') {
                    result.append(c);
//                    } else {
//                        muxlinestringflag = true;
//                        result.append('\n');
//                        continue;
//                    }
                } else if (Character.isDigit(c)) { //处理IntConst
                    result.append(c);
                    c = readone();
                    if(c == 'x' || c == 'X'){
                        result.append(c); //处理十六进制
                        while((c = readone()) != '\0' && Character.isLetterOrDigit(c)) {
                            result.append(c);
                        }
                    } else {
                        unreadone();
                        while((c = readone()) != '\0' && Character.isDigit(c)) {
                            result.append(c);
                        }
                    }
                    unreadone();
                } else if(Character.isLetter(c) || c == '_'){  //处理开头是字母
                    result.append(c);
                    while ((c = readone()) != '\0' && (Character.isLetterOrDigit(c) || c == '_')) {
                        result.append(c);
                    }
                    unreadone();
                }
                lexical(result.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if (konghang && muxlinestringflag) {
//            result.append('\n');
//        }
    }

    private static char readone() {
        if (point >= line.length()) {
            point++;
            return '\0';
        }
        else return line.charAt(point++);
    }

    private static void unreadone() {
        point--;
    }

    static private void lexical(String str){

        if (str.length() == 0) return;
        if (str.charAt(0) == '"') {
            String[] temp = new String[2];
            temp[0] = "STRCON";
            temp[1] = str;
            lexical.add(temp);
        } else if (Character.isDigit(str.charAt(0))) {
            String[] temp = new String[2];
            temp[0] = "INTCON";
            if(str.charAt(0) == '0'){
                if(str.length() == 1)
                    temp[1] = "" + 0;
                else if(str.charAt(1) == 'x' || str.charAt(1) == 'X')
                    temp[1] = Integer.parseInt(str.substring(2), 16) + "";
                else temp[1] = Integer.parseInt(str, 8) + "";
            }
            else temp[1] = str;
            lexical.add(temp);
        } else if (contains(str)) {
            String[] temp = new String[2];
            temp[0] = category.get(str);
            temp[1] = str;
            lexical.add(temp);
        } else {
            String[] temp = new String[2];
            temp[0] = "IDENFR";
            temp[1] = str;
            lexical.add(temp);
        }
    }

    static private boolean contains(char c){
        return category.containsKey(String.valueOf(c));
    }

    static private boolean contains(String c){
        return category.containsKey(c);
    }
}
