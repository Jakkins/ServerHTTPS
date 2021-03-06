import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAMultiPrimePrivateCrtKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.XECPrivateKey;
import java.security.interfaces.XECPublicKey;
import java.util.Base64;
import java.util.Base64.Decoder;

import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.interfaces.PBEKey;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class HTTPSServer {

    private static final char[] keyStorePassword = "ciaone".toCharArray();

    private int port = 9999;
    private boolean isServerDone = false;

    public static void main(String[] args) {
        HTTPSServer server = new HTTPSServer();
        server.run();
    }

    HTTPSServer() {}

    HTTPSServer(int port) {
        this.port = port;
    }

    // Create the and initialize the SSLContext
    private SSLContext createSSLContext() {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("server.jks"), keyStorePassword);

            log("Algorithm: " + keyStore.getKey("aliasbhoserver", "ciaone".toCharArray()).getAlgorithm());
            log("Format: " + keyStore.getKey("aliasbhoserver", "ciaone".toCharArray()).getFormat());
            keyStore.aliases().asIterator().forEachRemaining(s -> System.out.println(s));
            log("Is Certificate: " + keyStore.isCertificateEntry("aliasbhoserver"));
            log("Is Key Entry: " + keyStore.isKeyEntry("aliasbhoserver"));

            // log("");
            // log("");
            // // ...

            /*
                It's SunRsaSign.class, a proprietary class
                log("SunRsaSign? \t" + key.getClass().isInstance(SunRsaSign.class));
            */
            
            // // SunRsaSign RSA private CRT key, 2048 bits
            // Key key = keyStore.getKey("aliasbhoserver", "ciaone".toCharArray());

            // log("Key? \t" + key.getClass().isInstance(Key.class));
            // log("DHPrivateKey? \t" + key.getClass().isInstance(DHPrivateKey.class));
            // log("DHPublicKey? \t" + key.getClass().isInstance(DHPublicKey.class));
            // log("DSAPrivateKey? \t" + key.getClass().isInstance(DSAPrivateKey.class)); // 2 import
            // log("DSAPublicKey? \t" + key.getClass().isInstance(DSAPublicKey.class)); // 2 import

            // log("ECPrivateKey? \t" + key.getClass().isInstance(ECPrivateKey.class));
            // log("ECPublicKey? \t" + key.getClass().isInstance(ECPublicKey.class));
            // //log("EdECPrivateKey? \t" + key.getClass().isInstance(EdECPrivateKey.class));
            // //log("EdECPublicKey? \t" + key.getClass().isInstance(EdECPublicKey.class));
            // log("PBEKey? \t" + key.getClass().isInstance(PBEKey.class));
            // // https://docs.oracle.com/en/java/javase/15/docs/api/java.base/java/security/KeyStore.html
            // // I think I can use this to load private key on KeyStore
            // log("PrivateKey? \t" + key.getClass().isInstance(PrivateKey.class));

            // log("PublicKey? \t" + key.getClass().isInstance(PublicKey.class));
            // log("RSAMultiPrimePrivateCrtKey? \t" + key.getClass().isInstance(RSAMultiPrimePrivateCrtKey.class));
            // log("RSAPrivateCrtKey? \t" + key.getClass().isInstance(RSAPrivateCrtKey.class));
            // log("RSAPrivateKey? \t" + key.getClass().isInstance(RSAPrivateKey.class));
            // log("RSAPublicKey? \t" + key.getClass().isInstance(RSAPublicKey.class));

            // log("SecretKey? \t" + key.getClass().isInstance(SecretKey.class));
            // log("XECPrivateKey? \t" + key.getClass().isInstance(XECPrivateKey.class));
            // log("XECPublicKey? \t" + key.getClass().isInstance(XECPublicKey.class));

            // log( ( (PrivateKey) key));

            // log("");
            // log("");
            
            // Create key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keyStorePassword);
            KeyManager[] km = keyManagerFactory.getKeyManagers();
             
            // Create trust manager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();
             
            // Initialize SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLSv1");
            sslContext.init(km,  tm, null);
             
            return sslContext;
        } catch (Exception ex){
            ex.printStackTrace();
        }
         
        return null;
    }
     
    private void log(Object item) {
        System.out.println(item);
    }

    // Start to run the server
    public void run(){
        SSLContext sslContext = this.createSSLContext();
         
        try{
            // Create server socket factory
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
             
            // Create server socket
            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(this.port);
             
            System.out.println("SSL server started");
            while(!isServerDone){
                SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
                 
                // Start the server thread
                new ServerThread(sslSocket).start();
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
     
    // Thread handling the socket from client
    static class ServerThread extends Thread {
        private SSLSocket sslSocket = null;
         
        ServerThread(SSLSocket sslSocket){
            this.sslSocket = sslSocket;
        }
         
        public void run(){
            sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
             
            try{
                // Start handshake
                sslSocket.startHandshake();
                 
                // Get session after the connection is established
                SSLSession sslSession = sslSocket.getSession();
                 
                System.out.println("SSLSession :");
                System.out.println("\tProtocol : "+sslSession.getProtocol());
                System.out.println("\tCipher suite : "+sslSession.getCipherSuite());
                 
                // Start handling application content
                InputStream inputStream = sslSocket.getInputStream();
                OutputStream outputStream = sslSocket.getOutputStream();
                 
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream));
                 
                String line = null;
                while((line = bufferedReader.readLine()) != null){
                    System.out.println("Inut : "+line);
                     
                    if(line.trim().isEmpty()){
                        break;
                    }
                }
                 
                // Write data
                printWriter.print("HTTP/1.1 200\r\n");
                printWriter.flush();
                 
                sslSocket.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}