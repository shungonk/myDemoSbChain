package com.myexample;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.Security;

import com.myexample.blockchain.SBChain;
import com.myexample.common.utils.PropertyUtil;
import com.myexample.httphandler.TransactionHandler;
import com.sun.net.httpserver.HttpServer;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class ApplicationServer {
    public void run() {
        try {
            var host = PropertyUtil.getProperty("serverhost", "localhost");
            var port = Integer.parseInt(PropertyUtil.getProperty("serverport", "8080"));
            var server = HttpServer.create(new InetSocketAddress(host, port), 0);
            server.createContext("/transaction", new TransactionHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("Server started on port " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // add provider for security
        Security.addProvider(new BouncyCastleProvider());
        // demo initialization
        SBChain.addGenesisTransaction(SBChain.MINER_ADDRESS, 1000f);

        new ApplicationServer().run();
    }
}
