package com.infoledger.crypto.enclave.app.server;

import com.infoledger.vsockj.VSock;
import com.infoledger.vsockj.VSockAddress;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

/** Proxy server to communicate with vsock-proxy in order to proxy requests to aws kms. */
public class SocketVSockProxy implements Runnable {

  private final Socket clientSocket;
  private final int serverPort;
  private final String uid = UUID.randomUUID().toString();

  /**
   * Input and output stream share the Socket under the hood
   * thus closing either input or output stream may lead to
   * exceptions on other side due to not all the data read yet.
   */
  private volatile boolean closing = false;
  private volatile boolean canCloseClientSocketInputStream = false;
  private volatile boolean canCloseClientSocketOutputStream = false;

  private static final int BUFFER_SIZE = 4096;

  public SocketVSockProxy(Socket clientSocket, int serverPort) {
    this.serverPort = serverPort;
    this.clientSocket = clientSocket;
  }

  @Override
  public void run() {
    try (final VSock server =
             new VSock(new VSockAddress(VSockAddress.VMADDR_CID_PARENT, serverPort))) {
      handleTrafficToServer(server);
      handleTrafficFromServer(server);
    } catch (IOException e) {
      if (!closing) {
        e.printStackTrace();
      }
    } finally {
      try {
        clientSocket.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  private void handleTrafficFromServer(VSock server) throws IOException {
    final byte[] reply = new byte[BUFFER_SIZE];

    int readBytes;
    try (final OutputStream outToClient = clientSocket.getOutputStream()) {
      final InputStream inFromServer = server.getInputStream();
      while ((readBytes = inFromServer.read(reply)) != -1) {
        if (readBytes == 0 || closing) {
          break;
        }
        outToClient.write(reply, 0, readBytes);
        outToClient.flush();
      }

      closing = true;
      canCloseClientSocketOutputStream = true;
      while (!canCloseClientSocketInputStream) {
        Thread.sleep(100);
      }
    } catch (IOException | InterruptedException e) {
      if (!closing) {
        e.printStackTrace();
      }
    }
  }

  private void handleTrafficToServer(VSock server) throws IOException {
    new Thread(
        () -> {
          final byte[] request = new byte[BUFFER_SIZE];
          int bytesRead;
          try(final InputStream inFromClient = clientSocket.getInputStream()) {
            final OutputStream outToServer = server.getOutputStream();
            while ((bytesRead = inFromClient.read(request)) != -1) {
              if (bytesRead == 0 || closing) {
                break;
              }
              outToServer.write(request, 0, bytesRead);
              outToServer.flush();
            }
            closing = true;
            canCloseClientSocketInputStream = true;
            while (!canCloseClientSocketOutputStream) {
              Thread.sleep(100);
            }
          } catch (IOException | InterruptedException e) {
            if (!closing) {
              e.printStackTrace();
            }
          }
        })
        .start();
  }
}
