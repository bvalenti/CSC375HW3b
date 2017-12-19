import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    int size, lo, hi;
    int state = 0, clusterNum = 4, sum = 0;;
    double T, S;
    public boolean running = false;
    Solution A;
    Solution B;
    Composition composition;
    MyPainter painter;
    int count = 0, tlr;
    String hostname = "Unknown";
    Listener listener;
    String north; String south;
    InetAddress northIp; InetAddress southIp;
    int port;
    GUI gui;
    static String path = "/home/bvalenti/CSC375/HW3/HW3b/";
    PhaseLatch phaseLatch;
    PainterThread painterThread;
    Double convergence;

    public Main (double T, double S, double C1, double C2, double C3, int size) throws IOException, InterruptedException {
        //Get the name of the host computer.
        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException ex) {
            System.out.println("Hostname can not be resolved");
        }

        //Establish server socket:
        port = getPort(hostname);
        Thread mainThread = Thread.currentThread();
        InetAddress inetAddress = getIpAddress(hostname);
        phaseLatch = new PhaseLatch(clusterNum);
        listener = new Listener(inetAddress, this, mainThread);

        Thread runListener = new Thread(listener);
        runListener.start();

        //Initialize other variables:
        this.T = T; this.S = S;
        this.size = size;
        composition = new Composition(2*GUI.size,2+size);
        composition.C[0] = C1; composition.C[1] = C2; composition.C[2] = C3;
        A = new Solution(2*GUI.size, 2+size);
        B = new Solution(2*GUI.size, 2+size);
        writeEdges();
        writeSolution();
        painter = new MyPainter(A);

        for (int i = 0; i < A.width; i++) {
            for (int j = 0; j < A.height; j++) {
                A.grid[i][j] = 1;
                B.grid[i][j] = 1;
            }
        }
        if (T <= S) {
            painter.maxTemp = S;
        } else {
            painter.maxTemp = T;
        }

        getSolutionRange(hostname);
        north = getNorthName(hostname);
        south = getSouthName(hostname);
        northIp = getIpAddress(north);
        southIp = getIpAddress(south);
        if (hostname.equals("pi")) {
            running = true;
            painterThread = new PainterThread();
            painterThread.start();
        }
    }

    public static void main(String args[]) throws IOException, InterruptedException {
        Main main = new Main(2000,2000,1,1,1,250);
        main.run();
    }

    //======================================================
    public void run() throws IOException, InterruptedException {
        int Pn = 101, converged;
        A.grid[0][1] = S;
        A.grid[2 * size - 1][size - 1] = T;
        A.grid[500][125] = T;

        TemperatureSolver tp = new TemperatureSolver(1,size,lo,hi,composition,this);
        ForkJoinPool pool = new ForkJoinPool();
        do {
            convergence = pool.invoke(tp);
            converged = (convergence < 0.01)? 1:0;
            if (state == 0) {
//                tlr = ThreadLocalRandom.current().nextInt(1,100);
//                if (tlr < Pn) {
                    B.grid[0][1] = S;
                    B.grid[2 * size - 1][size - 1] = T;
                    B.grid[500][125] = T;
//                }
            } else {
//                tlr = ThreadLocalRandom.current().nextInt(1,100);
//                if (tlr < Pn) {
                    A.grid[0][1] = S;
                    A.grid[2 * size - 1][size - 1] = T;
                    A.grid[500][125] = T;
//                }
            }
            tp = new TemperatureSolver(1,size,lo,hi,composition,this);
            count++;
            if (hostname.equals("pi")) {
                System.out.println("Iteration: " + Integer.toString(count));
                System.out.println("Convergence: " + convergence.toString());
            }
            writeEdges();
            writeSolution();
            incrementPhaseLatchAcrossNodes(converged);
            int arr[] = phaseLatch.increment(converged);
            phaseLatch.incrementPhase();
            getEdges();

            //Change current working solution from A to B or vice versa.
            if (state == 0) {
                state = 1;
            } else {
                state = 0;
            }
            sum = 0;
            for (int i = 0; i < clusterNum; i++) {
                sum += arr[i];
            }
//            System.out.println("Convergence: " + Integer.toString(sum));
            if(sum > clusterNum && count > 100) { break; }
        } while (count < 2000);
        System.out.println("Program finished running");
        running = false;
        listener.listening = false;
    }

    //======================================================
    public InetAddress getIpAddress(String name) throws IOException {
        File file = new File(path + "registry.txt");
        Scanner sc = new Scanner(file);
        while(sc.hasNext()) {
            String line = sc.nextLine();
            String splitline[] = line.split(" ");
            if (splitline[0].equals(name)) {
                InetAddress inetAddress = InetAddress.getByName(splitline[1]);
                return inetAddress;
            }
        }
        return null;
    }

    //======================================================
    public int getPort(String name) throws IOException {
        File file = new File(path + "registry.txt");
        Scanner sc = new Scanner(file);
        while(sc.hasNext()) {
            String line = sc.nextLine();
            String splitline[] = line.split(" ");
            if (splitline[0].equals(name)) {
                int port = Integer.parseInt(splitline[6]);
                return port;
            }
        }
        return 0;
    }

    //======================================================
    public String getSouthName(String name) throws FileNotFoundException {
        File file = new File(path + "registry.txt");
        Scanner sc = new Scanner(file);
        while(sc.hasNext()) {
            String line = sc.nextLine();
            String splitline[] = line.split(" ");
            if (splitline[0].equals(name)) {
                return splitline[4].split(":")[1];
            }
        }
        return null;
    }
    public String getNorthName(String name) throws FileNotFoundException {
        File file = new File(path + "registry.txt");
        Scanner sc = new Scanner(file);
        while(sc.hasNext()) {
            String line = sc.nextLine();
            String splitline[] = line.split(" ");
            if (splitline[0].equals(name)) {
                return splitline[5].split(":")[1];
            }
        }
        return null;
    }

    //======================================================
    public void getSolutionRange(String name) throws FileNotFoundException {
        File file = new File(path + "registry.txt");
        Scanner sc = new Scanner(file);
        while(sc.hasNext()) {
            String line = sc.nextLine();
            String splitline[] = line.split(" ");
            if (splitline[0].equals(name)) {
                lo = Integer.parseInt(splitline[2]);
                hi = Integer.parseInt(splitline[3]);
                break;
            }
        }
    }

    //======================================================
    private void getEdges() throws IOException, InterruptedException {
        PrintWriter outWriter;
        String north = getNorthName(hostname);
        String south = getSouthName(hostname);
        InetAddress northIp = getIpAddress(north);
        InetAddress southIp = getIpAddress(south);
        int northPort = getPort(north);
        int southPort = getPort(south);
        if (northIp != null) {
            for (int j = 0; j < 2; j++) {
                try {
                    Socket socket = new Socket(northIp, northPort);
                    socket.setSoTimeout(5000);
                    OutputStream os = socket.getOutputStream();

                    String message = "north";
                    outWriter = new PrintWriter(os,true);
                    outWriter.println(message);

                    InputStream is = socket.getInputStream();
                    DataInputStream dis = new DataInputStream(is);

                    for (int i = 0; i < 2 * GUI.size; i++) {
                        if (state == 0) {
                            B.grid[i][0] = dis.readDouble();
                        } else {
                            A.grid[i][0] = dis.readDouble();
                        }
                    }
                    dis.close();
                    is.close();
                    os.close();
                } catch (java.net.ConnectException e) {
                    System.out.println(hostname + ": connection error with " + north);
                    Thread.sleep(3000);
                    continue;
                } catch (java.net.SocketTimeoutException e) {
                    System.out.println(hostname + ": connection to " + north + " timeout.");
                    continue;
                }
            }
        }

        if (southIp != null) {
            for (int j = 0; j < 2; j++) {
                try {
                    Socket socket = new Socket(southIp, southPort);
                    socket.setSoTimeout(5000);
                    OutputStream os = socket.getOutputStream();
                    String message = "south";
                    outWriter = new PrintWriter(os,true);
                    outWriter.println(message);
                    InputStream is = socket.getInputStream();
                    DataInputStream dis = new DataInputStream(is);
                        if (state == 0) {
                            for (int i = 0; i < 2 * GUI.size; i++) {
                                B.grid[i][251] = dis.readDouble();
                            }
                        } else {
                            for (int i = 0; i < 2 * GUI.size; i++) {
                                A.grid[i][251] = dis.readDouble();
                            }
                        }
                    dis.close();
                    is.close();
                    os.close();
                } catch (java.net.ConnectException e) {
                    System.out.println(hostname + " connection error with " + south);
                    Thread.sleep(3000);
                    continue;
                } catch (java.net.SocketTimeoutException e) {
                    System.out.println(hostname + ": connection to host" + south + " timeout.");
                    continue;
                }
            }
        }
    }

    //======================================================
    private Solution getSolution(String name) throws IOException {
        Solution out = new Solution(2*GUI.size, size);
        InetAddress inetAddress = getIpAddress(name);
        int port = getPort(name);
        Socket socket = new Socket(inetAddress,port);
        socket.setSoTimeout(5000);

        String message = "solution";

        OutputStream os = socket.getOutputStream();

        PrintWriter outWriter = new PrintWriter(os,true);
        outWriter.println(message);

        InputStream is = socket.getInputStream();
        DataInputStream dis = new DataInputStream(is);

        for (int j = 0; j < size; j++) {
            for (int i = 0; i < 2 * GUI.size; i++) {
                out.grid[i][j] = dis.readDouble();
            }
        }
        os.close();
        dis.close();
        is.close();
        return out;
    }

    //======================================================
    private void sendConvergenceValue(Double convergence) throws IOException, InterruptedException {
        File file = new File(path + "registry.txt");
        Scanner sc = new Scanner(file);
        Socket socket;
        while(sc.hasNext()) {
            String line = sc.nextLine();
            String splitline[] = line.split(" ");

            if (!splitline[0].equals(hostname)) {
                int port = Integer.parseInt(splitline[6]);
                InetAddress ip = InetAddress.getByName(splitline[1]);

                socket = getConnection(ip,port);
                socket.setSoTimeout(5000);
                OutputStream os = socket.getOutputStream();

                String message = "convergence: " + convergence.toString();
                PrintWriter outWriter = new PrintWriter(os, true);
                outWriter.println(message);
                os.close();
            }
        }
    }

    //======================================================
    private void writeEdges() throws IOException {
        File southFile = new File(path + hostname + "/south_edge.ser");
        File northFile = new File(path + hostname + "/north_edge.ser");

        RandomAccessFile southRaf = new RandomAccessFile(southFile,"rw");
        RandomAccessFile northRaf = new RandomAccessFile(northFile,"rw");

        FileChannel channelSouth = southRaf.getChannel();
        FileChannel channelNorth = northRaf.getChannel();

        ByteBuffer bSouth = ByteBuffer.allocate(2*GUI.size*8);
        ByteBuffer bNorth = ByteBuffer.allocate(2*GUI.size*8);

        if (state == 0) {
            for (int i = 0; i < 2*GUI.size; i++) {
                bSouth.putDouble(B.grid[i][250]);
                bNorth.putDouble(B.grid[i][1]);
            }
        } else {
            for (int i = 0; i < 2*GUI.size; i++) {
                bSouth.putDouble(A.grid[i][250]);
                bNorth.putDouble(A.grid[i][1]);
            }
        }
        bSouth.flip();
        bNorth.flip();

        while(bSouth.hasRemaining()) {
            channelSouth.write(bSouth);
        }
        while(bNorth.hasRemaining()) {
            channelNorth.write(bNorth);
        }
        channelNorth.close();
        channelSouth.close();
        northRaf.close();
        southRaf.close();
    }

    //======================================================
    private void writeSolution() throws IOException {
        File file = new File(path + hostname + "/solution.ser");
        RandomAccessFile raf = new RandomAccessFile(file,"rw");
        FileChannel f = raf.getChannel();
        ByteBuffer b = ByteBuffer.allocate(250*2*GUI.size*8);

        if (state == 0) {
            for (int j = 1; j < 1+size; j++) {
                for (int i = 0; i < 2*GUI.size; i++) {
                    b.putDouble(B.grid[i][j]);
                }
            }
        } else {
            for (int j = 1; j < 1+size; j++) {
                for (int i = 0; i < 2*GUI.size; i++) {
                    b.putDouble(A.grid[i][j]);
                }
            }
        }
        b.flip();
        while(b.hasRemaining()) {
            f.write(b);
        }
        f.close();
        raf.close();
    }

    //========================================================
    public void incrementPhaseLatchAcrossNodes(int conv) throws IOException, InterruptedException {
        Socket socket;

        File file = new File(path + "registry.txt");
        Scanner sc = new Scanner(file);
        while(sc.hasNext()) {
            String line = sc.nextLine();
            String splitline[] = line.split(" ");
            if (!splitline[0].equals(hostname)) {
                int port = Integer.parseInt(splitline[6]);
                InetAddress ip = InetAddress.getByName(splitline[1]);

                socket = getConnection(ip,port);
                socket.setSoTimeout(5000);
                OutputStream os = socket.getOutputStream();

                String message = "increment " + Integer.toString(conv);
                PrintWriter outWriter = new PrintWriter(os, true);
                outWriter.println(message);
                os.close();
            }
        }
    }
    private Socket getConnection(InetAddress ip, int port) throws IOException, InterruptedException {
        Socket socket;
        try {
            socket = new Socket(ip, port);
            return socket;
        } catch (java.net.ConnectException e) {
            System.out.println("Error connecting");
            Thread.sleep(500);
            return getConnection(ip,port);
        }
    }

    //========================================================
    public void paintSolution() throws IOException {
        Solution completeSolution = new Solution(2*GUI.size,GUI.size);
        for (int j = 1; j < 1+size; j++) {
            for (int i = 0; i < 2*GUI.size; i++) {
                completeSolution.grid[i][j-1] = A.grid[i][j];
            }
        }
        File file = new File(path + "registry.txt");
        Scanner sc = new Scanner(file);
        int count = 1;
        while(sc.hasNext()) {
            String line = sc.nextLine();
            String splitline[] = line.split(" ");
            if (!splitline[0].equals(hostname)) {
                Solution soln = getSolution(splitline[0]);
                for (int j = 0; j < size; j++) {
                    for (int i = 0; i < 2*GUI.size; i++) {
                        completeSolution.grid[i][j+250*count] = soln.grid[i][j];
                    }
                }
                count++;
            }
        }
        painter.paintSolution(completeSolution);
    }

    //============================================================
    public class PainterThread extends Thread {

        @Override
        public void run() {
            gui = new GUI(painter);
            gui.setVisible(true);

            while (running) {
                try {
                    Thread.sleep(100);
                    paintSolution();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
