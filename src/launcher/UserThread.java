package launcher;

import com.example.rober.flashbox.date.Comanda;
import com.example.rober.flashbox.date.DateUtilizator;
import com.example.rober.flashbox.date.EpisodFavorit;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


class UserThread extends Thread {
    private final Socket socket;

    public UserThread(Socket clientSocket) {
        this.socket = clientSocket;
    }

    public void run() {

        try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            while (true) {
                try {

                    Comanda comanda = (Comanda) ois.readObject();

                    switch (comanda.getOptiune()) {
                        case "login":
                            oos.writeObject(Launcher.login((DateUtilizator) comanda.getObj()));
                            System.out.println("login");
                            break;
                        case "register":
                            oos.writeObject(Launcher.register((DateUtilizator) comanda.getObj()));
                            System.out.println("register");
                            break;
                        case "cautare":
                            oos.writeObject(Launcher.cautare((String) comanda.getObj()));
                            System.out.println("cautare");
                            break;
                        case "get serial":
                            oos.writeObject(Launcher.getSerial((String) comanda.getObj()));
                            System.out.println("get serial");
                            break;
                        case "get Episoade favorite":
                            oos.writeObject(Launcher.getFavorite((int) comanda.getObj()));
                            System.out.println("get favorite");
                            break;
                        case "update episod favorit":
                            Launcher.updateEpisodFavorit((EpisodFavorit) comanda.getObj());
                            System.out.println(Launcher.getFavorite(1));
                        case "delete favorite":

                    }
                    int i=0;
                    //    System.out.println(msj);
                }
                catch (EOFException e) {
                    return;
                }
                catch (IOException | ClassNotFoundException  e ) {
                    e.printStackTrace();
                    return;
                }

            }

        } catch (IOException e) {
            return;
        }


    }
}