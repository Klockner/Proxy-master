/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

/**
 *
 * @author klockner
 */
public class ProxyThread extends Thread {

    private Socket socket = null;
    private static final int BUFFER_SIZE = 32768;

    public ProxyThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            int contador = 0;
            String inputLine, outputLine;
            String urlString = "";
            int cnt = 0;
            String urlToCall = "";

            //////////////////////////////////////////////////////
            //Iniciando get request from client
            while ((inputLine = in.readLine()) != null) {
                try {
                    StringTokenizer tok = new StringTokenizer(inputLine);
                    tok.nextToken();
                } catch (Exception e) {
                    break;
                }
                //parseia a primeira linha do request para encontrar a url
                if (cnt == 0) {
                    String[] tokens = inputLine.split(" ");
                    urlToCall = tokens[1];
                    urlString = tokens[2];
                    
                    //pode redirecionar para um log de saida
                    System.out.println("Request: " + urlToCall);
                }
                cnt++;
            }
            //Finalizando get request from client
            ///////////////////////////////////////////////////////

            BufferedReader rd = null;
            try 
            {
                ///////////////////////////////////////////////////////
                //begin send request to server
                URL url = new URL(urlToCall);
                String host = url.getHost();
                InetAddress address = InetAddress.getByName(host);
                String ip = address.getHostAddress();
                
                System.out.println(" *****  "+ip);
                URLConnection conn = url.openConnection();
                
                conn.setDoInput(true);
                //not doint HTTP posts
                conn.setDoOutput(false);
                if(conn.getContentType() != null)
                System.out.println("Content type: "
                        + conn.getContentType());
                System.out.println("Permissão: " 
                        + conn.getPermission());
                String chave = "monitorando";
                
                //get the response
                InputStream is = null;
                HttpURLConnection huc = (HttpURLConnection) conn;
                try {
                    is = conn.getInputStream();
                    rd = new BufferedReader(new InputStreamReader(is));
                } catch (IOException e) {
                    System.out.println("Exception em get the response");
                }
                //end send request to server, get response from server
                //////////////////////////////////////////////////////////////

                /////////////////////////////////////////////////////////////
                //begin send response to client
                byte by[] = new byte[BUFFER_SIZE];
                
                try {
                    int index = is.read(by, 0, BUFFER_SIZE);
                    while (index != -1) {
                        if(urlToCall.contains(chave) && contador < 1) {
                            contador++;
                            //erro 401;
                            System.out.println("Erro 401");
                            String htmlDenied = "<HTML> <HEAD><TITLE>Acesso não autorizado!</TITLE></HEAD><BODY> <H1>Acesso não autorizado!</H1> </BODY> </HTML>";

                            out.writeBytes(htmlDenied);
                            index = is.read(by, 0, BUFFER_SIZE);
                            break;
                        } else {
                            out.write(by, 0, index);
                            index = is.read(by, 0, BUFFER_SIZE);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Exception na response pro client");
                }
                out.flush();
                //end send response to client
                ///////////////////////////////////////////////////////////
            } catch (IOException e) {
                System.err.println("Exception 2: " + e.getMessage());
                out.writeBytes("");
            }

            if (contador > 0) {
                contador = 0;
            }
            
            //close out all resources
            if (rd != null) {
                rd.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
