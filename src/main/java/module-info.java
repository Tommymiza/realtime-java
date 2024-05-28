module com.example.realtime {
    requires org.apache.commons.lang3;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.naming;
    requires netty.socketio;
    requires io.netty.transport.unix.common;
    requires socket.io.client;
    requires engine.io.client;
    requires mysql.connector.j;
    requires java.sql;
    requires com.fasterxml.jackson.annotation;
    requires json;
    requires org.apache.commons.io;


    opens com.example.realtime to javafx.fxml;
    exports com.example.realtime;
}