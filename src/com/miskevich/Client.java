package com.miskevich;

import java.io.*;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 3000);
            OutputStream outputStream = socket.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(outputStream));

            BufferedReader consoleReader = new BufferedReader
                    (new InputStreamReader(System.in));
            BufferedReader serverReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            while (true) {
                if (consoleReader.ready()) {
                    bufferedWriter.write(consoleReader.readLine());
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
                if (serverReader.ready()) {
                    System.out.println("I read this " + serverReader.readLine());
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
