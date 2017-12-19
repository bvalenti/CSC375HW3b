import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Listener implements Runnable {
    InetAddress inetAddress;
    boolean listening = true;
    Main currentMain;
    String hostname;
    Thread mainThread;
    ServerSocket serverSocket;

    Listener(InetAddress inetAddress, Main main, Thread mainThread) {
        this.inetAddress = inetAddress;
        currentMain = main;
        this.mainThread = mainThread;
    }

    //================================================
    public void run() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException ex) {
            System.out.println("Hostname can not be resolved");
        }
        try {
            listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //===============================================
    public void listen() throws IOException {
        int port = currentMain.getPort(hostname);
        serverSocket = new ServerSocket(port);
        try {
            while(listening) {
                if (!mainThread.isAlive()) {
                    listening = false;
                }
                serverSocket.setSoTimeout(15000);
                try {
                    Socket clientSocket = serverSocket.accept();
                    new ListenerThread(clientSocket, currentMain).start();
                } catch (java.net.SocketTimeoutException e) {
                    System.out.println(hostname + ": server socket connection timeout.");
                    break;
                }
            }
        } finally {
            System.out.println(hostname + ": socket is closing now.");
            serverSocket.close();
        }
    }

    public void setMain(Main main) {
        this.currentMain = main;
    }
}
