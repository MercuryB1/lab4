import java.util.ArrayList;


public class Symbol {
    public String symbolName;  //符号的名字
    public SymbolType symbolType;  //符号的类型，常量、变量、函数
    public ValueType valueType;    //值的类型， INT
    public int register;   //存储的寄存器地址
    public String registerString; //字符串形式
    public ArrayList<String> funcParams;   //函数的参数
    public int value;  //常量、变量的值
    public boolean is_assigned; //是否只被定义

    public SymbolTable tableBelonged;

    // 函数
    public Symbol(String symbolName, SymbolType symbolType, ValueType valueType, ArrayList<String> funcParams, SymbolTable tableBelonged) {
        this.symbolName = symbolName;
        this.symbolType = symbolType;
        this.valueType = valueType;
        this.funcParams = funcParams;
        this.tableBelonged = tableBelonged;
    }

    // 定义变量
    public Symbol(String symbolName, SymbolType symbolType, ValueType valueType, int register,  SymbolTable tableBelonged) {
        this.symbolName = symbolName;
        this.symbolType = symbolType;
        this.valueType = valueType;
        this.register = register;
        this.registerString = "%"+this.register;
        this.is_assigned = false;
        this.tableBelonged = tableBelonged;
    }
    // 定义变量同时赋值
    public Symbol(String symbolName, SymbolType symbolType, ValueType valueType, int value, int register,  SymbolTable tableBelonged) {
        this.symbolName = symbolName;
        this.symbolType = symbolType;
        this.valueType = valueType;
        this.register = register;
        this.registerString = "%"+this.register;
        this.value = value;
        this.is_assigned = true;
        this.tableBelonged = tableBelonged;
    }
}
