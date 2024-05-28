package com.example.realtime;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.commons.io.*;

public class Server {
    private static SocketIOServer socketIOServer;
    public static void main(String[] args) {
        try {
            Configuration config = new Configuration();
            config.setPort(42350);
            socketIOServer = new SocketIOServer(config);

            socketIOServer.addConnectListener(new ConnectListener() {
                @Override
                public void onConnect(SocketIOClient socketIOClient) {
                    System.out.println("Client connected!");
                    Document document;

                    try {
                        DocumentBuilder builder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
                        if(new File("src/main/temp.xml").exists()){
                            document = builder.parse("src/main/temp.xml");
                            NodeList etudiants = document.getElementsByTagName("etudiant");
                            Connection conn = Db.initialize();
                            String sql;
                            assert conn != null;
                            for(int i = 0; i < etudiants.getLength(); i++){
                                Node current = etudiants.item(i);
                                Element element = (Element) current;
                                if(element.getElementsByTagName("type").item(0).getTextContent().equals("CREATE")){
                                    sql = "INSERT INTO etudiant (nom, adresse, bourse) VALUES (?, ?, ?)";
                                    PreparedStatement pstmt = conn.prepareStatement(sql);
                                    pstmt.setString(1, element.getElementsByTagName("nom").item(0).getTextContent());
                                    pstmt.setString(2, element.getElementsByTagName("adresse").item(0).getTextContent());
                                    pstmt.setDouble(3, Integer.parseInt(element.getElementsByTagName("bourse").item(0).getTextContent()));
                                    int rowsAffected = pstmt.executeUpdate();
                                }else{
                                    sql = "UPDATE etudiant set nom = ?, adresse = ?, bourse = ? WHERE id = ?";
                                    PreparedStatement pstmt = conn.prepareStatement(sql);
                                    pstmt.setString(1, element.getElementsByTagName("nom").item(0).getTextContent());
                                    pstmt.setString(2, element.getElementsByTagName("adresse").item(0).getTextContent());
                                    pstmt.setDouble(3, Integer.parseInt(element.getElementsByTagName("bourse").item(0).getTextContent()));
                                    pstmt.setDouble(4, Integer.parseInt(element.getElementsByTagName("id").item(0).getTextContent()));
                                    int rowsAffected = pstmt.executeUpdate();
                                }
                            }
                            File file = new File("src/main/temp.xml");
                            try {
                                FileDeleteStrategy.FORCE.delete(file);
                            }catch (Exception exx){
                                exx.printStackTrace();
                            }
                        }
                    } catch (ParserConfigurationException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (SAXException e) {
                        throw new RuntimeException(e);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                }
            });

            socketIOServer.addEventListener("create", Etudiant.class, (socketIOClient, e, ackRequest) -> {
                try {
                    Connection conn = Db.initialize();
                    String sql;
                    assert conn != null;
                    if(e.getType().equals("CREATE")){
                        sql = "INSERT INTO etudiant (nom, adresse, bourse) VALUES (?, ?, ?)";
                        PreparedStatement pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, e.getNom());
                        pstmt.setString(2, e.getAdresse());
                        pstmt.setDouble(3, e.getBourse());
                        int rowsAffected = pstmt.executeUpdate();
                    }else{
                        sql = "UPDATE etudiant set nom = ?, adresse = ?, bourse = ? WHERE id = ?";
                        PreparedStatement pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, e.getNom());
                        pstmt.setString(2, e.getAdresse());
                        pstmt.setDouble(3, e.getBourse());
                        pstmt.setDouble(4, e.getId());
                        int rowsAffected = pstmt.executeUpdate();
                    }
                    ackRequest.sendAckData("Finished!");
                }catch (Exception ex){
                    ex.printStackTrace();
                    ackRequest.sendAckData("Failed!");
                }
            });

            socketIOServer.addEventListener("delete", String.class, (socketIOClient, e, ackRequest)->{
                Connection conn = Db.initialize();
                assert conn != null;
                String sql = "DELETE FROM etudiant WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setDouble(1, Integer.parseInt(e));
                int rowsAffected = pstmt.executeUpdate();
            });

            socketIOServer.addDisconnectListener(new DisconnectListener() {
                @Override
                public void onDisconnect(SocketIOClient socketIOClient) {
                    System.out.println("Client disconnected!");
                }
            });


            socketIOServer.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
