import java.util.HashMap;

public class SymbolTable {
    public String SymbolTableName;
    public HashMap<String, Symbol> table = new HashMap<>();

    public HashMap<String, Symbol> getTable() {
        return table;
    }

    public SymbolTable(String symbolTableName) {
        SymbolTableName = symbolTableName;
    }
}
