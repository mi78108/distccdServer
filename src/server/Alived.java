package server;

import java.io.IOException;
import java.net.Socket;

public class Alived implements Runnable {
    boolean isRun = true;

    @Override
    public void run() {
        System.out.println("Alived 线程开始！");
        while (isRun) {
            // System.out.println("存活测试：runing");
            // System.out.println("联机主机：" + distccServer.clients.size());
            for (controlClientSocket ccs : distccServer.clients) {
                //   System.out.println(csocket.getRemoteSocketAddress() + " port:" + csocket.getPort());
                try {
                    ccs.socket.sendUrgentData(0xff);
                } catch (IOException e) {
                    System.out.println("control-Client:服务器 断线！");
                    distccServer.clients.remove(ccs);
                    try {
                        ccs.socket.close();
                    } catch (IOException e1) {
                        System.out.println("失效socket 关闭失败");
                    }
                    break;
                }
            }

            for (distccdRelay dr : distccServer.actions.values()) {
                for (distccdServerSocket dss : dr.distccdServerSockets) {
                    //   System.out.println(csocket.getRemoteSocketAddress() + " port:" + csocket.getPort());
                    try {
                        dss.socket.sendUrgentData(0xff);
                    } catch (IOException e) {
                        System.out.println("distccd server服务器 断线！");
                        dss.closeSocket();
                        dr.distccdServerSockets.remove(dr);
                        try {
                            dss.socket.close();
                        } catch (IOException e1) {
                            System.out.println("失效socket 关闭失败");
                        }
                        break;
                    }

                    for (distccdClientSocket dcs : dr.distccdClientSockets) {
                        //   System.out.println(csocket.getRemoteSocketAddress() + " port:" + csocket.getPort());
                        try {
                            dcs.socket.sendUrgentData(0xff);
                        } catch (IOException e) {
                            System.out.println("distccd client服务器 断线！");
                            dcs.closeSocket();
                            dr.distccdServerSockets.remove(dr);
                            try {
                                dcs.socket.close();
                            } catch (IOException e1) {
                                System.out.println("失效socket 关闭失败");
                            }
                            break;
                        }
                    }
                }
            }
            //  System.out.println("done");
            try {
                //    System.out.println("sleep");
                Thread.sleep(5000);
                Thread.yield();
            } catch (InterruptedException e) {
                System.out.println("存活测试睡眠错误");
                isRun = false;
            }

        }
        System.out.println("Alive 线程结束");
    }
}
