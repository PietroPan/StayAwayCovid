package Connection;

import Util.Response;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Demultiplexer implements AutoCloseable {
    private TaggedConnectionClient connection;
    private ReentrantLock lock; // Porque temos que ter controlo de concorrência no acesso ao map
    private Map<Integer, Entry> entradas;

    public class Entry {
        Queue<Response> queue;
        Condition c;

        Entry() {
            this.queue = new ArrayDeque<>();
            this.c = lock.newCondition();
        }
    }

    public Demultiplexer() throws Exception {
        this.connection = new TaggedConnectionClient(new Socket("localhost", 12345));
        this.lock = new ReentrantLock();
        this.entradas = new HashMap<>();
    }


    public void start() {
        new Thread(() -> {
            Response r;
            int tag;
            boolean b = true;

            while(b) {
                try {
                    r = connection.receive();
                    tag = r.getTag();

                    lock.lock();
                    try {
                        if (!entradas.containsKey(tag)) {
                            entradas.put(tag, new Entry());
                        }
                        entradas.get(tag).queue.add(r);
                        entradas.get(tag).c.signal();
                    }

                    finally {
                        lock.unlock();
                    }

                }
                catch (IOException e) {
                    b = false;
                }
            }
        }).start();


    }


    public Response receive(int tag) throws IOException, InterruptedException {
        Response res;

        lock.lock();
        try {
            if (!entradas.containsKey(tag)) {
                entradas.put(tag, new Entry());
            }
            while (entradas.get(tag).queue.isEmpty()) {
                entradas.get(tag).c.await();
            }

            res = entradas.get(tag).queue.remove();

        }
        finally {
            lock.unlock();
        }

        return res;
    }

    public void close() throws Exception {
        connection.close();
    }

    ///////////////////////////////////////////// Envio de Requests ////////////////////////////////////////////////////

    public void login(String username, String password) throws IOException, InterruptedException {
        connection.login(username, password);
    }

    public void register(String username, String password, int x, int y) {
        connection.register(username, password,x,y);
    }

    public void setLocation(int x, int y) {
        connection.setLocation(x, y);
    }

    public void nrPessoasLocalizacao(int x, int y) {
        connection.getNUsersLoc(x, y);
    }

    public void waitLocation(int x, int y) {
        connection.waitLocation(x, y);
    }

    public void isInfected() {
        connection.isInfected();
    }

    public void showMap() {
        connection.showMap();
    }

    public void changeVip() {connection.changeVip();}

    public void waitInfected() {connection.waitInfected();}

    public void logout() {connection.logout();}

    public void quit() {
        connection.quit();
        try {
            connection.close();
        }
        catch (IOException e) {
            //...
        }

    }
}
