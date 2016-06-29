/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 * @author klockner
 */
public class ServerProxy {
    ServerSocket serverSocket = null;
    boolean listening = true;
    int port = 7000;
    
    public ServerProxy() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Proxy na porta " + port);
        
            while (listening) {
                new ProxyThread(serverSocket.accept()).start();
            }
            serverSocket.close();
        } catch (IOException ex) {
            System.out.println("Erro ao iniciar o server proxy");
        }
    }

}
