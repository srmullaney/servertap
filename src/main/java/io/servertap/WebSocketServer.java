package io.servertap;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.EventInterceptor;
import com.corundumstudio.socketio.transport.NamespaceClient;
import org.bukkit.configuration.file.FileConfiguration;
import org.eclipse.jetty.util.log.Log;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class WebSocketServer {
    private final Configuration config;
    private final SocketIOServer io;
    private final Logger log;

    public WebSocketServer(ServerTapMain main, FileConfiguration bukkitConfig, Logger logger) {
        this.log = logger;

        this.config = getConfig();
        this.io = new SocketIOServer(config);
    }

    private Configuration getConfig() {
        Configuration c = new Configuration();
        c.setHostname("localhost");
        c.setPort(6969);

        return c;
    }
    public void on(String event, Consumer<Socket> callback) {
        log.info("Listener added!!!");
        io.addConnectListener(socket -> {
            log.info("Listener Run!");
            callback.accept(new Socket(socket));
        });
    }

    public void emit(String event, Object data) {
        io.getBroadcastOperations().sendEvent(event, data);
    }

    public void start() {
        log.info("[Socket.io] Starting Server!");
        io.start();
    }

    public void stop() {
        io.stop();
    }

    public class Socket {
        private final Socket instance;
        private final SocketIOClient defaultSocket;
        public Socket(SocketIOClient socketIOClient) {
            this.instance = this;
            defaultSocket = socketIOClient;
        }

        public void on(String event, Consumer<Socket> callback) {
            io.addEventListener(event, Class.class, new DataListener<Class>() {
                @Override
                public void onData(SocketIOClient socketIOClient, Class aClass, AckRequest ackRequest) throws Exception {
                    callback.accept(instance);
                }
            });
        }

        public void emit(String event, Object data) {
            defaultSocket.sendEvent(event, data);
        }
    }
}
