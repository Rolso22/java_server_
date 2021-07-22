import java.io.*;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server {

    static class State {
        public static JSONObject KV = new JSONObject();
        public static JSONObject Ips = new JSONObject();

        public static String clusterName;
        public static Integer clientPort;
        public static Integer techPort;
        public static String discoveryIp = null;

        public static String nodeName;
        public static String statePath;

        public static String hash;
        public static String hashKV;
        public static String hashIps;
        public static String format = "yyyy-MM-dd HH:mm:ss z";
    }

    public static void updateHash(String hash) {
        if ("hashKV".equals(hash)) {
            State.hashKV = CheckSum.md5(State.KV.toString());
        } else if ("hashIps".equals(hash)) {
            State.hashIps = CheckSum.md5(State.Ips.toString());
        }
        State.hash = CheckSum.md5(new JSONObject().put("KV", State.KV).put("Ips", State.Ips).toString());
        dumpToDisk();
    }

    public static String getValue(String key) {
        JSONObject data;
        try {
            data = (JSONObject) State.KV.get(key);
        } catch (JSONException e) {
            System.out.println(e.getMessage());
            return key + " is not found";
        }
        return data.get("value").toString();
    }

    public static void putValue(String key, String value, String time) {
        JSONObject data = new JSONObject();
        data.put("value", value);
        data.put("time", time);
        State.KV.put(key, data);
        updateHash("hashKV");
        dumpToDisk();
    }

    public static void main(String[] args) throws IOException {

        Init(args);

        System.out.println(new SimpleDateFormat(State.format).format(new Date()));

/*        Thread receiverThread = new Thread(new WorkReceiver());
        receiverThread.start();

        Thread dispatcherThread = new WorkDispatcher();
        dispatcherThread.start();

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", State.clientPort), 0);
        server.createContext("/", new HomeHandler());
        server.setExecutor(null);
        server.start();*/
    }

    public static void Init(String[] args) {

        State.clusterName = args[0]; // myCluster
        State.nodeName = args[1]; // myNode
        State.techPort = Integer.parseInt(args[2]); // 8080
        State.clientPort = Integer.parseInt(args[3]); // 9080
        if (args.length == 5) {
            State.discoveryIp = args[4];
        }

        boolean isExistFile = Recorder.createFile();
        if (isExistFile) {
            try {
                Recorder.loadFromDisk();
                String hash = CheckSum.md5(new JSONObject().put("KV", State.KV).put("Ips", State.Ips).toString());
                if (!hash.equals(State.hash)) {
                    System.out.println("FILE FROM DISK IS INCORRECT");
                    State.hash = CheckSum.md5(new JSONObject().put("KV", State.KV).put("Ips", State.Ips).toString());
                    Recorder.writeToDisk();
                }
                State.hashKV = CheckSum.md5(State.KV.toString());
                State.hashIps = CheckSum.md5(State.Ips.toString());
                State.hash = CheckSum.md5(new JSONObject().put("KV", State.KV).put("Ips", State.Ips).toString());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            State.Ips.put(State.discoveryIp, new JSONObject().put("Status", "activated").put("time", new Date()));
            State.hash = CheckSum.md5(new JSONObject().put("KV", State.KV).put("Ips", State.Ips).toString());
        }

        System.out.println(State.KV);
        System.out.println(State.Ips);
        System.out.println(State.hash);
    }

    public static void dumpToDisk() {
        try {
            Recorder.writeToDisk();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}