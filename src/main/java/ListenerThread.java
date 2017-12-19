import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ListenerThread extends Thread {
    Socket clientSocket;
    String inputLine;
    Main currentMain;
    OutputStream os;
    String hostname;

    ListenerThread (Socket socket, Main main) {
        clientSocket = socket;
        currentMain = main;
        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException ex) {
            System.out.println("Hostname can not be resolved");
        }
    }

    public void run() {
        try {
            InputStream is = clientSocket.getInputStream();

            //Read client request
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            inputLine = in.readLine();
                if (inputLine.equals("north")) {
                    File northFile = new File(Main.path + hostname + "/south_edge.ser");
                    RandomAccessFile raf = new RandomAccessFile(northFile,"rw");
                    FileChannel f = raf.getChannel();
                    ByteBuffer buff = ByteBuffer.allocate(2*GUI.size*8);
                    f.read(buff);
                    buff.position(0);
                    os = clientSocket.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(os);
                    while(buff.hasRemaining()) {
                        dos.writeDouble(buff.getDouble());
                    }
                    raf.close();
                    f.close();
                    dos.close();
                    os.close();
                    is.close();
                } else if (inputLine.equals("south")) {
                    File southFile = new File(Main.path + hostname + "/north_edge.ser");
                    RandomAccessFile raf = new RandomAccessFile(southFile,"rw");
                    FileChannel f = raf.getChannel();
                    ByteBuffer buff = ByteBuffer.allocate(2*GUI.size*8);
                    f.read(buff);
                    buff.position(0);
                    os = clientSocket.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(os);
                    while(buff.hasRemaining()) {
                        dos.writeDouble(buff.getDouble());
                    }
                    raf.close();
                    f.close();
                    dos.close();
                    os.close();
                    is.close();
                } else if (inputLine.equals("solution")) {
                    File file = new File(Main.path + hostname + "/solution.ser");
                    RandomAccessFile raf = new RandomAccessFile(file,"rw");
                    FileChannel f = raf.getChannel();
                    ByteBuffer buff = ByteBuffer.allocate(2*GUI.size*8*250);
                    f.read(buff);
                    buff.position(0);

                    os = clientSocket.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(os);
                    while(buff.hasRemaining()) {
                        dos.writeDouble(buff.getDouble());
                    }
                    raf.close();
                    f.close();
                    dos.close();
                    os.close();
                    is.close();
                } else if (inputLine.contains("increment")) {
                    int c = Integer.parseInt(inputLine.split(" ")[1]);
                    currentMain.phaseLatch.incrementAndAdvance(c);
                }
//            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
