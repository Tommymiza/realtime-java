package com.example.realtime;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.*;
import java.util.Arrays;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.json.JSONObject;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class HelloController {
    private Socket socket;

    @FXML
    private VBox rootVbox;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TableView<Etudiant> tableView;
    @FXML
    private TableColumn<Etudiant, Integer> columnId;
    @FXML
    private TableColumn<Etudiant, String> columnNom;
    @FXML
    private TableColumn<Etudiant, String> columnAdresse;
    @FXML
    private TableColumn<Etudiant, Double> columnBourse;
    @FXML
    private TableColumn<Etudiant, Void> columnAction;

    @FXML
    private TextField nom;

    @FXML
    private TextField adresse;

    @FXML
    private TextField bourse;

    @FXML
    private Button submit_btn;

    @FXML
    private Button reset_btn;

    @FXML
    private Circle status;

    @FXML
    private TextField id;

    private ObservableList<Etudiant> data;

    private Document document;

    private DocumentBuilder builder;

    @FXML
    void initialize() {
        try {
            socket = IO.socket("http://localhost:42350");
            socket.on("connect", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    status.setFill(Paint.valueOf("#1fff76"));
                    System.out.println("connected");
                }
            });
            socket.on("reconnect", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    status.setFill(Paint.valueOf("#1fff76"));
                    System.out.println("connected");
                }
            });
            socket.on("disconnect", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    status.setFill(Paint.valueOf("#5c5c5b"));
                    System.out.println("disconnected");
                }
            });
            socket.open();

            columnId.setCellValueFactory(new PropertyValueFactory<>("id"));
            columnNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
            columnAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
            columnBourse.setCellValueFactory(new PropertyValueFactory<>("bourse"));
            columnAction.setCellFactory(cellFactory);

            data = FXCollections.observableArrayList();
            tableView.setItems(data);

            loadDataFromDatabase();

            builder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();

        }catch (URISyntaxException e){
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        Platform.runLater(()->{
            Stage stage = (Stage) rootVbox.getScene().getWindow();
            stage.setOnCloseRequest(event->{
                if(socket != null && socket.connected()){
                    socket.close();
                }
            });
        });
    }
    Callback<TableColumn<Etudiant, Void>, TableCell<Etudiant, Void>> cellFactory = new Callback<TableColumn<Etudiant, Void>, TableCell<Etudiant, Void>>() {
        @Override
        public TableCell<Etudiant, Void> call(final TableColumn<Etudiant, Void> param) {
            final TableCell<Etudiant, Void> cell = new TableCell<>() {
                private final Button editButton = new Button("Modifier");
                private final Button deleteButton = new Button("Supprimer");

                {
                    // Action pour le bouton "Modifier"
                    editButton.setOnAction(event -> {
                        // Récupérer l'étudiant correspondant à cette ligne
                        Etudiant etudiant = getTableView().getItems().get(getIndex());

                        id.setText(String.valueOf(etudiant.getId()));
                        nom.setText(etudiant.getNom());
                        adresse.setText(etudiant.getAdresse());
                        bourse.setText(String.valueOf(etudiant.getBourse()));
                    });

//                     Action pour le bouton "Supprimer"
                    deleteButton.setOnAction(event -> {
                        // Récupérer l'étudiant correspondant à cette ligne
                        Etudiant etudiant = getTableView().getItems().get(getIndex());
                        // Insérez ici le code pour supprimer l'étudiant
                        socket.emit("delete", String.valueOf(etudiant.getId()), new Ack() {
                            @Override
                            public void call(Object... args) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        showAlert("Succès", "Opération réussie");
                                        loadDataFromDatabase();
                                    }
                                });
                            }
                        });
                    });
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox hbox = new HBox(editButton, deleteButton);
                        setGraphic(hbox);
                    }
                }
            };
            return cell;
        }
    };

    private void loadDataFromDatabase() {
        data.clear();
        String url = "jdbc:mysql://localhost:3306/javap";
        String user = "root";
        String password = "";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM etudiant");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nom = resultSet.getString("nom");
                String adresse = resultSet.getString("adresse");
                int bourse = resultSet.getInt("bourse");

                Etudiant etudiant = new Etudiant(id, nom, adresse, bourse);
                data.add(etudiant);
            }
            if(new File("src/main/temp.xml").exists()){
                builder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
                document = builder.parse("src/main/temp.xml");
                NodeList etudiants = document.getElementsByTagName("etudiant");
                for(int i = 0; i < etudiants.getLength(); i++){
                    Node current = etudiants.item(i);
                    Element element = (Element) current;
                    Etudiant etudiant = new Etudiant(element.getElementsByTagName("type").item(0).getTextContent().equals("CREATE") ? ThreadLocalRandom.current().nextInt(999,  9999+ 1) : Integer.parseInt(element.getElementsByTagName("id").item(0).getTextContent()), element.getElementsByTagName("nom").item(0).getTextContent(), element.getElementsByTagName("adresse").item(0).getTextContent(), Integer.valueOf(element.getElementsByTagName("bourse").item(0).getTextContent()));
                    data.add(etudiant);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }



    public void onReset(){
        id.setText("");
        nom.setText("");
        adresse.setText("");
        bourse.setText("");
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void onSubmit() throws Exception {

        System.out.println("Submit button clicked!");
        String nom = this.nom.getText();
        String adresse = this.adresse.getText();
        int bourse = Integer.parseInt(this.bourse.getText());
        Object id = this.id.getText().isEmpty() ? null : Integer.parseInt(this.id.getText());
        JSONObject etudiant = new JSONObject();
        if(id != null){
            etudiant.put("id", id);
            etudiant.put("type", "UPDATE");
        }else{
            etudiant.put("type", "CREATE");
        }
        etudiant.put("nom", nom);
        etudiant.put("adresse", adresse);
        etudiant.put("bourse", bourse);
        if(socket.connected()){
            socket.emit("create", etudiant, new Ack() {
                @Override
                public void call(Object... args) {
                    if(Arrays.toString(args).equals("[Finished!]")){
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                showAlert("Succès", "Opération réussie");
                                loadDataFromDatabase();
                            }
                        });
                    }
                }
            });
        }else{
            if(new File("src/main/temp.xml").exists()){
                document = builder.parse(new File("src/main/temp.xml"));
                document.normalizeDocument();
                Node root = document.getElementsByTagName("etudiants").item(0);
                Node child = document.createElement("etudiant");

                Node n = document.createElement("nom");
                n.appendChild(document.createTextNode(nom));
                Node a = document.createElement("adresse");
                a.appendChild(document.createTextNode(adresse));
                Node b = document.createElement("bourse");
                b.appendChild(document.createTextNode(String.valueOf(bourse)));
                Node t = document.createElement("type");
                t.appendChild(document.createTextNode(id != null ? "UPDATE" :"CREATE"));
                child.appendChild(n);
                child.appendChild(a);
                child.appendChild(b);
                child.appendChild(t);
                if(id != null){
                    Node i = document.createElement("id");
                    i.appendChild(document.createTextNode(id.toString()));
                    child.appendChild(i);
                }
                root.appendChild(child);

                saveDomToFile(document, "src/main/temp.xml");
            }else{
                document = builder.newDocument();
                Node root = document.createElement("etudiants");
                document.appendChild(root);
                Node child = document.createElement("etudiant");
                Node n = document.createElement("nom");
                n.appendChild(document.createTextNode(nom));
                Node a = document.createElement("adresse");
                a.appendChild(document.createTextNode(adresse));
                Node b = document.createElement("bourse");
                b.appendChild(document.createTextNode(String.valueOf(bourse)));
                Node t = document.createElement("type");
                t.appendChild(document.createTextNode(id != null ? "UPDATE" : "CREATE"));
                child.appendChild(n);
                child.appendChild(a);
                child.appendChild(b);
                child.appendChild(t);
                if(id != null){
                    Node i = document.createElement("id");
                    i.appendChild(document.createTextNode(id.toString()));
                    child.appendChild(i);
                }
                root.appendChild(child);
                saveDomToFile(document, "src/main/temp.xml");
            }
            loadDataFromDatabase();
        }
    }
    private void saveDomToFile(Document document,String fileName)
            throws Exception {
        DOMSource dom = new DOMSource(document);
        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer();

        StreamResult result = new StreamResult(new File(fileName));
        transformer.transform(dom, result);
    }

}