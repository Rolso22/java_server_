import org.json.JSONObject;
import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

class ClientSock extends Thread {
    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;

    private static final String getIps = "get ips request";
    private static final String getKV  = "get kvs request";
    private static final String getHashIps = "get ips hash request";
    private static final String getHashKV  = "get kvs hash request";

    public ClientSock(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.start();
    }

    public void checkKV() throws IOException {
        JSONObject request = new JSONObject().put("Type", getHashKV).put("Ip", "127.0.0.1" + Server.State.techPort);
        out.write(request.toString());
        out.flush();
        String line = in.readLine();
        if (!line.equals(Server.State.hashKV)) {
            out.write(request.put("Type", getKV).toString());
            out.flush();
            line = in.readLine();
            updateKV(new JSONObject(line));
        }
    }

    public void updateKV(JSONObject data) {
        JSONObject copy = new JSONObject(Server.State.KV.toString());
        Iterator<String> itr = data.keys();
        while (itr.hasNext()) {
            String ind = itr.next();
            if (copy.opt(ind) != null) {
                if (!data.getJSONObject(ind).getString("value").equals(copy.getJSONObject(ind).getString("value"))) {
                    JSONObject x = copy.getJSONObject(ind);
                    JSONObject y = data.getJSONObject(ind);
                    Date t1 = (Date) x.get("time");
                    Date t2 = (Date) y.get("time");
                    if (t1.after(t2)) {
                        copy.put(ind, data.get(ind));
                    }
                }
            } else {
                copy.put(ind, data.get(ind));
            }
        }
        Server.State.KV = new JSONObject(copy);
    }

    public void checkIps() throws IOException {
        JSONObject request = new JSONObject().put("Type", getHashIps).put("Ip", "127.0.0.1" + Server.State.techPort);
        out.write(request.toString());
        out.flush();
        String line = in.readLine();
        if (!line.equals(Server.State.hashIps)) {
            out.write(request.put("Type", getIps).toString());
            out.flush();
            line = in.readLine();
            updateIps(new JSONObject(line));
        }
    }

    public void updateIps(JSONObject data) {
        JSONObject copy = new JSONObject(Server.State.Ips.toString());
        Iterator<String> itr = data.keys();
        while (itr.hasNext()) {
            String ind = itr.next();
            if (copy.opt(ind) != null) {
                if (!data.getJSONObject(ind).getString("status").equals(copy.getJSONObject(ind).getString("status"))) {
                    JSONObject x = copy.getJSONObject(ind);
                    JSONObject y = data.getJSONObject(ind);
                    Date t1 = (Date) x.get("time");
                    Date t2 = (Date) y.get("time");
                    if (t1.after(t2)) {
                        copy.put(ind, data.get(ind));
                    }
                }
            } else {
                copy.put(ind, data.get(ind));
            }
        }
        Server.State.Ips = new JSONObject(copy);
    }

    @Override
    public void run() {
        try {
            while (true) {
                checkIps();
                checkKV();
                Thread.sleep(3000);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}

public class WorkDispatcher extends Thread {

    public static LinkedList<ClientSock> clientList = new LinkedList<>();

    public static void addNode(JSONObject node) {
        Iterator<String> itr = node.keys();
        while (itr.hasNext()) {
            String nod = itr.next();
            try {
                clientList.add(new ClientSock(new Socket(nod.split(":")[0], Integer.parseInt(nod.split(":")[1]))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        addNode(Server.State.Ips);
    }
}
