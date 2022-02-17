package com.infoledger.crypto.enclave.app.server;

import com.infoledger.vsockj.ServerVSock;
import com.infoledger.vsockj.VSock;
import com.infoledger.vsockj.VSockAddress;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("InfiniteLoopStatement")
public class EnclaveServer implements Closeable {
  private static final Logger LOG = LogManager.getLogger(EnclaveServer.class);

  private static final AtomicInteger RERUN_COUNT = new AtomicInteger(0);

  private final InetAddress localServerAddress;
  private final int port;
  private final int reattemptsNumber;
  private ServerSocket serverSocket;

  public EnclaveServer(InetAddress localServerAddress, int port, int reattemptsNumber) {
    this.localServerAddress = localServerAddress;
    this.port = port;
    this.reattemptsNumber = reattemptsNumber;
  }

  public void runServer(Consumer<VSock> requestConsumer) {
    new Thread(
            () -> {
              try {
                this.runServerThreaded(requestConsumer);
              } catch (IOException e) {
                // todo: [m.lushchytski] : add restart logic with attempts count
                LOG.warn(e.getMessage(), e);
              }
            })
        .start();
  }

  public void runProxyServer(int port) throws IOException {
    serverSocket = new ServerSocket(port, 50, localServerAddress);
    new Thread(
            () -> {
              try {
                LOG.info(
                    "Running proxy server on {}:{}",
                    localServerAddress,
                    serverSocket.getLocalPort());
                while (true) {
                  Socket clientSocket = serverSocket.accept();
                  new Thread(new SocketVSockProxy(clientSocket, port)).start();
                }
              } catch (IOException e) {
                LOG.warn(
                    "Rerunning proxy server. Attempt number: {}", RERUN_COUNT.incrementAndGet());
                if (RERUN_COUNT.get() <= reattemptsNumber) {
                  try {
                    runProxyServer(port);
                  } catch (IOException ioException) {
                    LOG.warn(e.getMessage(), e);
                  }
                }
              }
            })
        .start();
  }

  private void runServerThreaded(Consumer<VSock> requestConsumer) throws IOException {
    try (ServerVSock server = new ServerVSock()) {
      server.bind(new VSockAddress(VSockAddress.VMADDR_CID_ANY, port));
      LOG.info("Bound on Cid: {}", server.getLocalCid());

      while (true) {
        try (VSock peerVSock = server.accept()) {
          requestConsumer.accept(peerVSock);
        } catch (Exception e) {
          LOG.warn(e.getMessage(), e);
        }
      }
    }
  }

  public InetAddress getLocalServerAddress() {
    return localServerAddress;
  }

  @Override
  public void close() throws IOException {
    if (serverSocket != null) {
      serverSocket.close();
    }
  }
}
