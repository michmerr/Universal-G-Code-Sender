package com.willwinder.universalgcodesender;

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: MerrellM
 * Date: 1/13/14
 * Time: 11:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class StdIOConnection extends SerialConnection {

    PrintStream outStream;
    Process pseudoPort;
    String command;
    Listener listener;
    Thread listenerThread;

    public StdIOConnection(String command) {
        super();
        this.command = command;
    }

    @Override
    public boolean supports(String portname) {
        List<String> supportedNames = CommUtils.getStdIOConnections();
        boolean result = false;
        if (portname == null || portname.isEmpty())
            return result;

        for (String name : supportedNames) {
            if (name.equalsIgnoreCase(portname)) {
                result = true;
                break;
            }
        }

        return result;
    }

    @Override
    public boolean openPort(String name, int baud) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException, TooManyListenersException, Exception {
        if (pseudoPort != null)
            throw new PortInUseException();

        listener = new Listener(this);
        listenerThread = new Thread(listener);


        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        pseudoPort = pb.start();
        in = pseudoPort.getInputStream();
        out = pseudoPort.getOutputStream();
        outStream = new PrintStream(out, true);

        listenerThread.start();



        return true;
    }

    @Override
    public void closePort() {

        if (pseudoPort == null)
            return;

        try {
            listener.stop();
            listenerThread.join(1000);
        } catch (InterruptedException ie) {
            Logger.getLogger(StdIOConnection.class.getName()).log(Level.SEVERE, null, ie);
        }

        try {
            out.close();
            in.close();
        } catch (IOException e) {
            Logger.getLogger(StdIOConnection.class.getName()).log(Level.SEVERE, null, e);
        }


        out = null;
        in = null;
        pseudoPort.destroy();
        pseudoPort = null;

        listener = null;

    }

    @Override
    public void sendStringToComm(String command) {
        outStream.print(command);
        outStream.flush();
    }

    @Override
    public void sendByteImmediately(byte b) throws IOException {
        outStream.write(b);
        outStream.flush();
    }

    protected class Listener implements Runnable {

        StdIOConnection port;
        boolean stop;

        public Listener(StdIOConnection port) {
            this.port = port;
            this.stop = false;
        }

        public void stop() {
            this.stop = true;
        }

        @Override
        public void run() {
            int bufferSize = 8096;

            byte[] buffer = new byte[bufferSize];
            int bytesRead = 0;
            int eolIndex = -1;
            StringBuilder sb = new StringBuilder();
            String eol = comm.getLineTerminator();
            int eolLength = eol.length();

            while (!stop) {
                if (this.port == null || this.port.in == null) continue;

                try {
                    bytesRead = this.port.in.read(buffer, 0, bufferSize);
                } catch (IOException e) {
                    Logger.getLogger(StdIOConnection.class.getName()).log(Level.SEVERE, null, e);
                    bytesRead = 0;
                }

                if (bytesRead > 0) {
                    sb.append(new String(buffer, 0, bytesRead));

                    while ((eolIndex = sb.indexOf(eol)) > -1) {
                        comm.responseMessage(sb.substring(0, eolIndex));
                        sb.delete(0, eolIndex + eolLength);
                    }
                }
            }

            if (sb.length() > 0)
                comm.responseMessage(sb.toString());
        }
    }
}
