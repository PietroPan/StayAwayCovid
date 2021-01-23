import Client.Response;
import Client.ResponseInt;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection implements AutoCloseable {
    private ReentrantLock lock;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private String name;

    public TaggedConnection() throws IOException {
        this.socket = new Socket("localhost", 12345);
        this.lock = new ReentrantLock();
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public TaggedConnection(Socket s) throws IOException {
        this.socket = s;
        this.lock = new ReentrantLock();
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void login(String name, String pass) throws IOException, InterruptedException {
        lock.lock();
        try {
            out.writeInt(0); //Pedido com tag login
            out.writeUTF(name);
            out.writeUTF(pass);
            out.flush();
        }
        catch (IOException e) {

        }
        finally {
            lock.unlock();
        }
    }

    public void register(String name,String pass) {
        try {
            lock.lock();
            out.writeInt(1); //Pedido com tag register
            out.writeUTF(name);
            out.writeUTF(pass);
            out.flush();
        } catch (IOException e) {}
        finally {
            lock.unlock();
        }
    }

    public void setLocation(int x,int y) {
        try {
            lock.lock();
            out.writeInt(2);//Pedido com tag setLocation
            out.writeUTF(this.name);
            out.writeInt(x);
            out.writeInt(y);
            out.flush();
        } catch (IOException e) {}
        finally {
            lock.unlock();
        }
    }

    public void getNUsersLoc(int x,int y) {
        try {
            lock.lock();
            out.writeInt(3);//Pedido com tag getNUsersLoc
            out.writeInt(x);
            out.writeInt(y);
            out.flush();
        } catch (IOException e) {}
        finally {
            lock.unlock();
        }
    }

    public void waitLocation(int x,int y){
        try {
            lock.lock();
            out.writeInt(4);//Pedido com tag waitLocation
            out.writeInt(x);
            out.writeInt(y);
            out.flush();
        } catch (IOException e) {}
        finally {
            lock.unlock();
        }
    }

    public void isInfected(){
        try {
            lock.lock();
            out.writeInt(5);//Pedido com tag isInfected
            out.writeUTF(this.name);
            out.flush();
        }catch (IOException e) {}
        finally {
            lock.unlock();
        }
    }

    public void showMap(){
        try {
            lock.lock();
            out.writeInt(6);//Pedido com tag showMap
            out.writeUTF(this.name);
            out.flush();
        } catch (IOException e) {}
        finally {
            lock.unlock();
        }
    }


    public Response receive() throws IOException {
        Response res = null;

        lock.lock();
        try {
            switch (in.readInt()) {
                case 0:   // Login
                    res = new ResponseBool(0, in.readBoolean());
                    break;
                case 1:   //Register
                    res = new ResponseBool(1, in.readBoolean());
                    break;
                case 2:   //People at location
                    res = new ResponseInt(2, in.readInt());
                    break;
                case 3:   //Location available
                    res = new ResponsePair(3,in.readInt(), in.readInt());
                    break;
                case 4:   //Infected
                    res = new ResponseString(4, "Esteve com alguém infetado!!");
                    break;
                case 5:   //Map
                    if (in.readBoolean()) {
                        int [][][] matrix = new int [2][10][10];
                        for (int i=0;i<10;i++){
                            for (int j=0;j<10;j++){
                                matrix[0][i][j] = in.readInt();
                                matrix[1][i][j] = in.readInt();
                            }
                        }
                        res = new ResponseIntMatrix(5, matrix);
                    }
                    break;

                default:
                    break;


            }
            return res;
        }
        finally {
            lock.unlock();
        }
    }
    /*
        switch (in.readInt()) {
            case 0://Resposta recebida com tag 0
                if (in.readBoolean()) {
                    this.name = (in.readUTF());
                    System.out.println("Welcome " + this.name);
                            } else System.out.println("Name or Password are incorrect");
                            break;
                        case 1://Resposta recebida com tag 1
                            if (in.readBoolean()) {
                                this.name = (in.readUTF());
                                System.out.println("Welcome " + this.name);
                            } else System.out.println("Name is already in use");
                            break;
                        case 2://Resposta recebida com tag 2
                            System.out.println("Há " + in.readInt() + " pessoas na localização indicada");
                            break;
                        case 3://Resposta recebida com tag 3
                            System.out.println("Location ("+in.readInt()+","+in.readInt()+") is available!!");
                            break;
                        case 4://Resposta recebida com tag 4
                            System.out.println("Someone you've been with has been infected!!!");
                            break;
                        case 5://Resposta recebida com tag 5
                            if (!in.readBoolean()) System.out.println("You don't have permission");
                            else {
                                for (int i=0;i<10;i++){
                                    for (int j=0;j<10;j++){
                                        //System.out.println("("+i+","+j+") Current People: "+in.readInt());
                                        System.out.println("("+i+","+j+") Total People: "+in.readInt());
                                        System.out.println("("+i+","+j+") Infected People: "+in.readInt());
                                    }
                                }
                            }
                            break;
                    }
                }
            } catch (IOException e){}
        }).start();
    } */
    public void close() throws IOException {
        socket.close();
    }
}