import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/*class sss extends Thread {
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(8081);
            Socket sSocket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(sSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sSocket.getOutputStream()));
            while (true) {
                String line = in.readLine();

                out.write("me get: " + line + "\n");
                out.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}*/

public class client
{
    public static void main(String[] args) throws Exception
    {
/*        Thread thr = new sss();
        thr.start();*/

        Socket socket = new Socket("127.0.0.1", 8080);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        while (true) {
            out.write("get kvs request\n");
            out.flush();
            String line = in.readLine();
            System.out.println(line);
            Thread.sleep(3000);
        }
/*
        socket.close();
        in.close();
        out.close();*/
    }
}