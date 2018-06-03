package launcher;


import com.example.rober.flashbox.date.DateUtilizator;
import com.example.rober.flashbox.date.Episod;
import com.example.rober.flashbox.date.EpisodFavorit;
import com.example.rober.flashbox.date.Serial;
import com.example.rober.flashbox.date.Sezon;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;

class Launcher {
    private static Connection con;

    public static DateUtilizator login(DateUtilizator d) {
        try (PreparedStatement ps = con.prepareStatement("select * from folks_utilizatori");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                if (rs.getString("nume").equals(d.getNume()))
                    if (rs.getString("parola").equals(d.getParola())) {
                        d.setIdUtilizator(rs.getInt("utilizator_id"));
                        d.setEmail(rs.getString("email"));
                        d.setDate(rs.getDate("data_inscriere"));
                        return d;
                    }
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    public static DateUtilizator register(DateUtilizator d) {
        try (PreparedStatement ps = con.prepareStatement("select * from folks_utilizatori");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                if (rs.getString("nume").equals(d.getNume()))
                    return null;
                if (rs.getString("email").equals(d.getEmail()))
                    return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        try (
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("select max(utilizator_id) from folks_utilizatori")) {
            rs.next();
            int id = rs.getInt(1) + 1;
            try (Statement stmt2 = con.createStatement();
                 ResultSet rs2 = stmt.executeQuery("select sysdate from dual")) {
                rs2.next();
                d.setDate(rs2.getDate(1));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            //rs.getInt(1) + 1;
            String sql_insert = "insert into folks_utilizatori" + "(utilizator_id, nume, parola, email, data_inscriere) values" +
                    "(?, ?, ?, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(sql_insert)) {
                ps.setInt(1, id);
                ps.setString(2, d.getNume());
                ps.setString(3, d.getParola());
                ps.setString(4, d.getEmail());
                ps.setDate(5, d.getDate());
                ps.executeUpdate();
                return d;

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<String> cautare(String s) {
        ArrayList<String> list = new ArrayList<String>();
        try (PreparedStatement ps = con.prepareStatement("Select nume from folks_seriale where lower(nume) like ?")) {
            ps.setString(1, "%" + s.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String aux = rs.getString(1);
                    list.add(aux);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ArrayList<Episod> getEpisoade(int sezon_id) {
        ArrayList<Episod> episoade = new ArrayList<>();
        try (PreparedStatement pstm = con.prepareStatement("select * from folks_episoade where sezon_id = ? order by numar")) {
            pstm.setInt(1, sezon_id);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    Episod episod = new Episod();
                    episod.setId(rs.getInt("episod_id"));
                    episod.setNumar(rs.getInt("numar"));
                    episod.setDescriere(rs.getString("descriere"));
                    episod.setNume(rs.getString("nume"));
                    episoade.add(episod);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return episoade;
    }

    public static ArrayList<Sezon> getSezoane(int serial_id) {
        ArrayList<Sezon> sezoane = new ArrayList<>();
        try (PreparedStatement pstm = con.prepareStatement("select * from folks_sezoane where serial_id = ? order by numar")) {
            pstm.setInt(1, serial_id);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    Sezon sezon = new Sezon();
                    sezon.setNumar(rs.getInt("numar"));
                    sezon.setId(rs.getInt("sezon_id"));
                    sezon.setListaEpisoade(Launcher.getEpisoade(sezon.getId()));
                    sezoane.add(sezon);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sezoane;
    }

    public static Serial getSerial(String obj) {
        Serial s = new Serial();
        try (PreparedStatement ps = con.prepareStatement("select * from folks_seriale where nume = ?")) {
            ps.setString(1, obj);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                s.setId(rs.getInt(1));
                s.setNume(rs.getString(2));
                s.setDescriere(rs.getString(3));
                s.setDataAparitie(rs.getDate(4));
                s.setLink(rs.getString(5));
                s.setLink_poza(rs.getString(6));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        s.setListaSezoane(Launcher.getSezoane(s.getId()));
        return s;
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        // Conectare si interogare baza de com.example.rober.flasbox.date.date
        Class.forName("oracle.jdbc.driver.OracleDriver");
        final String DB_URL = "jdbc:oracle:thin:@193.226.51.37:1521:o11g";
        final String DB_USER = "grupa42";
        final String DB_PASS = "grupa42";
        con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);


        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            serverSocket = new ServerSocket(9090);
        } catch (IOException e) {
            e.printStackTrace();

        }
        while (true) {
            try {
                assert serverSocket != null;
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            UserThread T = new UserThread(socket);
            T.start();
        }
    }

    public static ArrayList<EpisodFavorit> getFavorite(int id_utilizator) {
        ArrayList<EpisodFavorit> episoadeFavorite = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement("select episod_id, folks_seriale.nume, folks_episoade.numar, folks_sezoane.numar, folks_seriale.link_poza from folks_episoade join folks_sezoane using (sezon_id) join folks_seriale using(serial_id) where episod_id in (select episod_id from folks_episoade_favorite where utilizator_id = ?)")) {
            ps.setInt(1, id_utilizator);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EpisodFavorit e = new EpisodFavorit();
                    e.setIdEpisod(rs.getInt(1));
                    e.setIdUtilizator(id_utilizator);
                    e.setNumeSerial(rs.getString(2));
                    e.setNrEpisod(rs.getInt(3));
                    e.setNrSezon(rs.getInt(4));
                    e.setLink(rs.getString(5));
                    episoadeFavorite.add(e);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return episoadeFavorite;
    }

    public static void updateEpisodFavorit(EpisodFavorit ef) {
        int idUtilizator = ef.getIdUtilizator();
        int idEpisod = ef.getIdEpisod();
        int idSerial = 0;
        String sql = "select serial_id from folks_seriale join folks_sezoane using(serial_id) join folks_episoade using(sezon_id) where episod_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEpisod);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                idSerial = rs.getInt("serial_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sql = "select 1 from folks_episoade_favorite where serial_id = ? and utilizator_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSerial);
            ps.setInt(2, idUtilizator);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    sql = "update folks_episoade_favorite set episod_id = ? where utilizator_id = ? and serial_id = ?";
                }
                else {
                    sql = "insert into folks_episoade_favorite values ((select max(episod_favorit_id)+1 from folks_episoade_favorite), ?, ?, ?)";
                }
                try (PreparedStatement ps3 = con.prepareStatement(sql)) {
                    ps3.setInt(1, idUtilizator);
                    ps3.setInt(2, idSerial);
                    ps3.setInt(3, idEpisod);
                    ps3.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}