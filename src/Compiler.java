import java.io.*;

public class Compiler {
    //    static File outfile = new File("output.txt");
    static PrintWriter pw;

    public static void main(String[] args) throws IOException {
        String stream = args[0];
        File file = new File(stream);
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String output = args[1];
        File outFile = new File(output);
        FileWriter fw = new FileWriter(outFile,true);



//        FileReader fileReader = new FileReader("testfile.txt");
//        BufferedReader bufferedReader = new BufferedReader(fileReader);
//        if (!outfile.exists()) {
//            outfile.createNewFile();
//        }
//        FileWriter fw = new FileWriter(outfile);
//        pw = new PrintWriter(fw, true);


        Lexical.readtxts(bufferedReader);


//        for (String[] temp : Lexical.lexical) {
//            System.out.println(temp[0] + " " + temp[1]);
//        }

        if(!Grammar.grammar(Lexical.lexical, pw)){
            System.exit(-1);
        }
//        Grammar.root.printTree(0);
        LLVM_IR llvm_ir = new LLVM_IR(Grammar.root);
        pw.write(llvm_ir.IRCodes);
        pw.flush();

//        System.out.println(llvm_ir.IRCodes);
        pw.close();
        fw.close();
        bufferedReader.close();
        fileReader.close();
    }
}
