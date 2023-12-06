package Online;

public class ServerStarter {
    public static void main(String[] args) {
        Server s = new Server(4567);
        s.start();

        //Printer.toggleDebugosity();
        //Printer.toggleVerbosity();
    }
}
