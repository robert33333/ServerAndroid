package com.example.rober.flashbox.date;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class DataBase {
    public static ObjectOutputStream oos;
    public static ObjectInputStream ois;
    public static Socket socket;
    public static DateUtilizator utilizatorCurent;
    public static ArrayList<String> listaSeriale;
    public static Serial serialCurent;
    public static boolean isChecked = false;
    public static ArrayList<Episod> episoadeFavorite;

    public static void initialize() {
        try {
            socket = new Socket("82.77.168.248", 9090);
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void episoadeFavorite() {
        try {
            oos.writeObject(new Comanda("get Episoade favorite",utilizatorCurent.getIdUtilizator()));
            episoadeFavorite = (ArrayList<Episod>) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
