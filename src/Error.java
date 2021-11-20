public class Error extends Exception {
    String message;

    public Error(String msg) {
        this.message = msg;
    }

    public String meg() {
        return message;
    }
}