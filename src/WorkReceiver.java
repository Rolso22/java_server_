import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

class ServerSock extends Thread {
    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;

    private static final String getIps = "get ips request";
    private static final String getKV  = "get kvs request";
    private static final String getHashIps = "get ips hash request";
    private static final String getHashKV  = "get kvs hash request";

    private static String createAnswer(String line) {
        switch (line) {
            case getIps: return Server.State.Ips.toString() + "\n";
            case getKV: return Server.State.KV.toString() + "\n";
            case getHashKV: return CheckSum.md5(Server.State.KV.toString()) + "\n";
            case getHashIps: return CheckSum.md5(Server.State.Ips.toString()) + "\n";

        }
        return "invalid\n";
    }

    public ServerSock(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.println("жду запрос");
                JSONObject line = new JSONObject(in.readLine());
                System.out.println("запрос принят\n" + line);
                if (Server.State.Ips.opt(line.getString("Ip")) == null) {
                    System.out.println("добавление новой ноды, line: " + line);
                    Server.State.Ips.put(line.getString("Ip"), new JSONObject().put("Status", "activated").put("time", new SimpleDateFormat(Server.State.format).format(new Date())));
                    WorkDispatcher.addNode(new JSONObject().put(line.getString("Ip"), "{}"));
                    System.out.println("записал новую ноду");
                    System.out.println("ips: " + Server.State.Ips);
                }
                System.out.println("answer: " + createAnswer(line.getString("Type")));
                out.write(createAnswer(line.getString("Type")));
                out.flush();
                System.out.println("отправил ответ от сервера");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

public class WorkReceiver implements Runnable {

    public static LinkedList<ServerSock> serverList = new LinkedList<>();

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(Server.State.techPort);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("есть подключение");
                serverList.add(new ServerSock(socket));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}