package com.miskevich;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Server {

    public static void main(String[] args) throws IOException {
        List<SocketChannel> clients = new ArrayList<>();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(3000));
        serverSocketChannel.configureBlocking(false);

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        ByteBuffer byteBuffer = null;

        while (true) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    registerClient(clients, selector, key);

                } else if (key.isReadable()) {
                    byteBuffer = readClientMessage(selector, key);
                } else if (key.isWritable()) {
                    writeIntoClient(clients, selector, byteBuffer, key);
                }
                iterator.remove();
            }

        }
    }

    private static void writeIntoClient(List<SocketChannel> clients, Selector selector, ByteBuffer byteBuffer, SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        byte[] buffer;
        if (byteBuffer != null) {
            buffer = new byte[byteBuffer.remaining()];
            byteBuffer.get(buffer);

            for (SocketChannel client : clients) {
                client.write(ByteBuffer.wrap(buffer));
                System.out.println("Write message to client: " + client + " " + new String(byteBuffer.array()));
            }
        }
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private static ByteBuffer readClientMessage(Selector selector, SelectionKey key) throws IOException {
        ByteBuffer byteBuffer;
        SocketChannel socketChannel = (SocketChannel) key.channel();
        byteBuffer = ByteBuffer.allocate(20);
        socketChannel.read(byteBuffer);
        System.out.println("server read from client: " + new String(byteBuffer.array()));
        byteBuffer.flip();
        socketChannel.register(selector, SelectionKey.OP_WRITE);
        return byteBuffer;
    }

    private static void registerClient(List<SocketChannel> clients, Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("New client was registered");
        clients.add(clientChannel);
    }
}
