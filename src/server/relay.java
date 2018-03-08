package server;


import java.io.IOException;
import java.net.Socket;

public class relay implements Runnable {
    private Socket ssocket;
    private Socket csocket;
    private distccdClientSocket dcsocket;
    private distccdServerSocket dssocket;

    public relay(distccdServerSocket dssocket, distccdClientSocket dcsocket) {
        this.ssocket = dssocket.socket;
        this.csocket = dcsocket.socket;
        this.dcsocket = dcsocket;
        this.dssocket = dssocket;

    }


    Thread cTs = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                while (!ssocket.isClosed() && !csocket.isClosed()) {
                    ssocket.getOutputStream().write(csocket.getInputStream().read());
                }
            } catch (IOException e) {
                try {
                    ssocket.close();
                    csocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.out.println("cTs err!!!");
                return;
            }
        }
    });

    Thread sTc = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                while (!ssocket.isClosed() && !csocket.isClosed()) {
                    csocket.getOutputStream().write(ssocket.getInputStream().read());
                }
            } catch (IOException e) {
                System.out.println("sTc err!!!");
                try {
                    csocket.close();
                    ssocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return;
            }
        }
    });

    @Override
    public void run() {
        boolean isRun = true;

        dssocket.isInUse = true;
        dcsocket.isInUse = true;

        sTc.start();
        cTs.start();

        while (isRun) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println("replay延时失败！");
            }
        }
    }
}
