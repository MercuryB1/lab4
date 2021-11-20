import java.util.ArrayList;

public class AstNode {
    public String type = "";
    public String value = "";
    public ArrayList<AstNode> children = new ArrayList<>();

    public AstNode(String type) {
        this.type = type;
    }

    public AstNode(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public void printTree(int dep){
//        for(int i = 0; i <= dep; i++){
//            System.out.print("-");
//        }
        System.out.print(dep + " " + type);
        if(!value.equals("")){
            System.out.println(" " + value);
        } else System.out.println("");
        for(AstNode node : children){
            node.printTree(dep+1);
        }
    }
}
