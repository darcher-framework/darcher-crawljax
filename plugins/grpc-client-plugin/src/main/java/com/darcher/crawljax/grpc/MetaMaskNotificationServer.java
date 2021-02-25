package com.darcher.crawljax.grpc;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.openqa.selenium.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MetaMaskNotificationServer extends WebSocketServer {
    public interface UnapprovedTxListener {
        void onUnapprovedTx(UnapprovedTxMessage unapprovedTxMessage);
    }

    public interface UnconfirmedMessageListener {
        void onUnconfirmedMessage(UnconfirmedMessage unconfirmedMessage);
    }

    public interface PermissionRequestListener {
        void onPermissionRequest(PermissionRequestMessage permissionRequestMessage);
    }

    public interface UnlockRequestListener {
        void onUnlockRequestListener(UnlockRequestMessage unlockRequestMessage);
    }

    public interface StackTraceMessageListener {
        void onStackTraceMessage(StackTraceMessage stackTraceMessage);
    }

    private UnapprovedTxListener unapprovedTxListener;
    private UnconfirmedMessageListener unconfirmedMessageListener;
    private PermissionRequestListener permissionRequestListener;
    private UnlockRequestListener unlockRequestListener;
    private StackTraceMessageListener stackTraceMessageListener;

    private final Logger logger;

    private final MessageDecoder decoder = new MessageDecoder();

    public MetaMaskNotificationServer(InetSocketAddress address) {
        this(address, null, null, null, null, null);
    }

    public MetaMaskNotificationServer(InetSocketAddress address,
                                      UnapprovedTxListener unapprovedTxListener,
                                      UnconfirmedMessageListener unconfirmedMessageListener,
                                      PermissionRequestListener permissionRequestListener,
                                      UnlockRequestListener unlockRequestListener,
                                      StackTraceMessageListener stackTraceMessageListener) {
        super(address);
        this.unapprovedTxListener = unapprovedTxListener;
        this.unconfirmedMessageListener = unconfirmedMessageListener;
        this.permissionRequestListener = permissionRequestListener;
        this.unlockRequestListener = unlockRequestListener;
        this.stackTraceMessageListener = stackTraceMessageListener;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    public MetaMaskNotificationServer(InetSocketAddress address,
                                      UnapprovedTxListener unapprovedTxListener,
                                      StackTraceMessageListener stackTraceMessageListener) {
        this(address, unapprovedTxListener, null, null, null, stackTraceMessageListener);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.info("MetaMask Notification connection opened");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Message msg;
        try {
            msg = this.decoder.decode(message);
        } catch (JsonSyntaxException e) {
            logger.error("Invalid JSON: " + e.getMessage());
            return;
        }
        // Handle new messages
        switch (msg.getType()) {
            case Message.UNAPPROVED_TX:
                logger.debug("Receive Unapproved Tx message");
                if (unapprovedTxListener != null) {
                    unapprovedTxListener.onUnapprovedTx((UnapprovedTxMessage) msg);
                }
                break;
            case Message.PERMISSION_REQUEST:
                logger.debug("Receive permission request message");
                if (permissionRequestListener != null) {
                    permissionRequestListener.onPermissionRequest((PermissionRequestMessage) msg);
                }
                break;
            case Message.UNLOCK_REQUEST:
                logger.debug("Receive unlock request message");
                if (unlockRequestListener != null) {
                    unlockRequestListener.onUnlockRequestListener((UnlockRequestMessage) msg);
                }
                break;
            case Message.UNCONFIRMED_MESSAGE:
                logger.debug("Receive unconfirmed message");
                if (unconfirmedMessageListener != null) {
                    unconfirmedMessageListener.onUnconfirmedMessage((UnconfirmedMessage) msg);
                }
                break;
            case Message.FETCH_STACK_TRACE:
                logger.debug("Receive stack trace message");
                if (stackTraceMessageListener != null) {
                    stackTraceMessageListener.onStackTraceMessage((StackTraceMessage) msg);
                }
                break;
            default:
                logger.warn("Unknown message with type: " + msg.getType());
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // WebSocket connection closes
        logger.info("MetaMask Notification connection closed");
    }

    @Override
    public void onStart() {
        logger.info("WebSocket server started at port " + getPort());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        // Do error handling here
        logger.error("MetaMask Notification connection error: " + ex);
    }

    public void stop() throws IOException, InterruptedException {
        logger.info("Stopping WebSocket server...");
        this.getConnections().forEach(WebSocket::close);
        super.stop();
    }

    public void setUnapprovedTxListener(UnapprovedTxListener unapprovedTxListener) {
        this.unapprovedTxListener = unapprovedTxListener;
    }

    public void setUnconfirmedMessageListener(UnconfirmedMessageListener unconfirmedMessageListener) {
        this.unconfirmedMessageListener = unconfirmedMessageListener;
    }

    public void setPermissionRequestListener(PermissionRequestListener permissionRequestListener) {
        this.permissionRequestListener = permissionRequestListener;
    }

    public void setUnlockRequestListener(UnlockRequestListener unlockRequestListener) {
        this.unlockRequestListener = unlockRequestListener;
    }

    public void setStackTraceMessageListener(StackTraceMessageListener stackTraceMessageListener) {
        this.stackTraceMessageListener = stackTraceMessageListener;
    }

    public static void main(String[] args) {
        MetaMaskNotificationServer server = new MetaMaskNotificationServer(new InetSocketAddress("localhost", 1237),
                unapprovedTxMessage -> {
                    System.out.println(unapprovedTxMessage);
                },
                stackTraceMessage -> {
                    System.out.println(stackTraceMessage);
                });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }));
        server.start();
    }
}


class Message {
    public static final String UNAPPROVED_TX = "UnapprovedTx";
    public static final String UNCONFIRMED_MESSAGE = "UnconfirmedMessage";
    public static final String UNLOCK_REQUEST = "UnlockRequest";
    public static final String PERMISSION_REQUEST = "PermissionRequest";
    public static final String FETCH_STACK_TRACE = "FetchStackTrace";

    private String type;

    public Message(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

interface Decoder<T> {
    T decode(String s);

    boolean willDecode(String s);
}

class StackTraceMessage extends Message {
    private String[] stack;

    public StackTraceMessage(String type, String[] stack) {
        super(type);
        this.stack = stack;
        if (!type.equals(Message.FETCH_STACK_TRACE)) {
            throw new IllegalArgumentException("Invalid type '" + type + "' for StackTraceMessage");
        }
    }

    public StackTraceMessage(String[] stack) {
        this(Message.FETCH_STACK_TRACE, stack);
    }

    public String[] getStack() {
        return stack;
    }

    public void setStack(String[] stack) {
        this.stack = stack;
    }
}

class UnapprovedTxMessage extends Message {
    private String from;
    private String to;
    private String gas;
    private String gasPrice;
    private String value;

    public UnapprovedTxMessage(String type, String from, String to, String gas, String gasPrice, String value) {
        super(type);
        if (!type.equals(Message.UNAPPROVED_TX)) {
            throw new IllegalArgumentException("Invalid type '" + type + "' for UnapprovedTxMessage");
        }
        this.from = from;
        this.to = to;
        this.gas = gas;
        this.gasPrice = gasPrice;
        this.value = value;
    }

    public UnapprovedTxMessage(String from, String to, String gas, String gasPrice, String value) {
        super(Message.UNAPPROVED_TX);
        this.from = from;
        this.to = to;
        this.gas = gas;
        this.gasPrice = gasPrice;
        this.value = value;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getGas() {
        return gas;
    }

    public void setGas(String gas) {
        this.gas = gas;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(String gasPrice) {
        this.gasPrice = gasPrice;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

class UnlockRequestMessage extends Message {
    public UnlockRequestMessage(String type) {
        super(type);
        if (!type.equals(Message.UNLOCK_REQUEST)) {
            throw new IllegalArgumentException("Invalid type '" + type + "' for UnlockRequestMessage");
        }
    }

    public UnlockRequestMessage() {
        super(Message.UNLOCK_REQUEST);
    }
}

class UnconfirmedMessage extends Message {
    public UnconfirmedMessage(String type) {
        super(type);
        if (!type.equals(Message.UNCONFIRMED_MESSAGE)) {
            throw new IllegalArgumentException("Invalid type '" + type + "' for UnconfirmedMessage");
        }
    }

    public UnconfirmedMessage() {
        super(Message.UNCONFIRMED_MESSAGE);
    }
}

class PermissionRequestMessage extends Message {
    public PermissionRequestMessage(String type) {
        super(type);
        if (!type.equals(Message.PERMISSION_REQUEST)) {
            throw new IllegalArgumentException("Invalid type '" + type + "' for PermissionRequestMessage");
        }
    }

    public PermissionRequestMessage() {
        super(Message.PERMISSION_REQUEST);
    }
}

class MessageDecoder implements Decoder<Message> {

    private static final Gson gson = new Gson();

    @Override
    public Message decode(String s) throws JsonSyntaxException {
        Message msg = gson.fromJson(s, Message.class);
        switch (msg.getType()) {
            case Message.UNAPPROVED_TX:
                return gson.fromJson(s, UnapprovedTxMessage.class);
            case Message.UNLOCK_REQUEST:
                return gson.fromJson(s, UnlockRequestMessage.class);
            case Message.PERMISSION_REQUEST:
                return gson.fromJson(s, PermissionRequestMessage.class);
            case Message.UNCONFIRMED_MESSAGE:
                return gson.fromJson(s, UnconfirmedMessage.class);
            case Message.FETCH_STACK_TRACE:
                return gson.fromJson(s, StackTraceMessage.class);
            default:
                throw new InvalidArgumentException("Invalid type '" + msg.getType() + "'");
        }
    }

    @Override
    public boolean willDecode(String s) {
        return s != null;
    }

}