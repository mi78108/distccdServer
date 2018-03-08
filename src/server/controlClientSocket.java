package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class controlClientSocket {

    Socket socket;

    String id;
    private String aid;
    boolean isOnline;
    boolean isDistccdServer;
    private boolean isNeedDistccdSuppost;
    boolean isUseingDistccdServer;
    boolean isSuppostingDistccd = false;
    private boolean isAskDistccdSuppost;
    boolean isFinished;
    private boolean isRun = true;

    public controlClientSocket(Socket socket) {
        this.socket = socket;
        this.id = UUID.randomUUID().toString();
    }


    private Thread recvFromClient = new Thread(new Runnable() {
        @Override
        public void run() {
            while (isRun && !socket.isClosed()) {
                byte buff[] = new byte[1024];
                StringBuilder msg = new StringBuilder();
                try {
                    InputStream in = socket.getInputStream();
                    in.read(buff);
                } catch (IOException e) {
                    System.out.println("读取客户端错误！！");
                }

                for (int i = 0; buff[i] > 0; i++) {
                    msg.append((char) buff[i]);
                }
                /*添加distccd 主机*/
                System.out.println(msg);
                System.out.println(msg.length());
                if (msg.length() >= 6) {
                    System.out.println("客户端消息：:" + msg);

                    if (msg.toString().substring(0, 6).equals("CR:IAS")) {//i am serevr
                        isDistccdServer = true;
                        //for (controlClientSocket ccs : distccServer.clients) {
                        //  if (ccs.id.equals(id)) {
                        //ccs.isDistccdServer = true;
                        // }
                        //}
                        System.out.println("IAS:被添加");
                    }

                    /*删除失效distccd 主机*/
                    if (msg.toString().substring(0, 6).equals("CR:IAL")) {
                        if (isDistccdServer) {
                            isDistccdServer = false;
                        }
                    }

                    /**/
                    if (msg.toString().substring(0, 6).equals("CQ:IND")) {//i need distccd
                        if (!isNeedDistccdSuppost && !isUseingDistccdServer) {
                            isNeedDistccdSuppost = true;
                        }
                    }
                    /*emerge完成*/
                    if (msg.toString().substring(0, 6).equals("CR:IAF")) {
                        if (isUseingDistccdServer) {
                            isUseingDistccdServer = false;
                            /*发送结束命令！*/
                            isFinished = true;
                            System.out.println("断开链接！");
                            if (distccServer.actions.get(id) != null) {
                                distccdRelay dr = distccServer.actions.get(id);
                               /* for (Socket sss : dr.distccdServerSockets) {
                                    try {
                                        sss.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                for (Socket sss : dr.distccdClientSockets) {
                                    try {
                                        sss.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }*/
                                dr.isDone = true;
                                dr.isRun = false;
                                distccServer.ATcs.remove(id);
                                distccServer.actions.remove(id);
                            }

                            System.out.println("All done!");
                            // distccServer.distccdServers.remove(msg.toString().substring(12, 48));
                            // distccServer.distccdServers.remove(distccServer.CTS.get(msg.toString().substring(12, 48)).toString());
                        }
                    }
                }

                try {
                    Thread.sleep(500);
                    Thread.yield();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    });

    private Thread sendToClient = new Thread(new Runnable() {
        @Override
        public void run() {
            String msg = null;
            while (isRun && !socket.isClosed()) {
                //  System.out.println("Server to Client runing whit:" + socket.getRemoteSocketAddress());

                if (isNeedDistccdSuppost && !isUseingDistccdServer) {
                    msg = "SR:ISY";
                    UUID cuuid = UUID.randomUUID();
                    UUID suuid = UUID.randomUUID();
                    //  UUID auuid = UUID.randomUUID();
                    //   aid = auuid.toString();


                    System.out.println("==UUID-生成:");
                    System.out.println("  --Server:" + suuid);
                    System.out.println("  --Client:" + cuuid);

                    Map<String, String> CTS = new HashMap<>();
                    CTS.put(cuuid.toString(), suuid.toString());
                    CTS.put(suuid.toString(), cuuid.toString());
                    distccServer.ATcs.put(id.toString(), CTS);

                    String num = "00";

                    //分配 CUUID
                    msg = msg + " " + num + " UUID=" + cuuid.toString()+"\n";
                    try {
                        OutputStream out = socket.getOutputStream();
                        out.write(msg.getBytes());
                        out.flush();
                        isNeedDistccdSuppost = false;
                        isUseingDistccdServer = true;
                    } catch (IOException e) {
                        System.out.println("回复" + socket.getRemoteSocketAddress() + "失败");
                    }

                    //
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    /*通知distccd主机*/
                    System.out.println("==distccd 通知！");
                    for (controlClientSocket cs : distccServer.clients) {
                        if (cs.isDistccdServer) {
                            System.out.println("Yes");
                            ///规则 补充
                              if (!cs.isSuppostingDistccd) {
                            //if (true) {
                                try {
                                    msg = "SQ:INS UUID=" + suuid + "\n";
                                    System.out.println("  --" + cs.socket.getRemoteSocketAddress() + "已通知！");
                                    cs.socket.getOutputStream().write(msg.getBytes());
                                    cs.socket.getOutputStream().flush();
                                } catch (IOException e) {
                                    System.out.println("??distccd通知失败！");
                                }
                            }
                        }
                    }

                    System.out.println("==创建实例！");
                    //    new Thread(new distccDeamon(cuuid.toString(), suuid.toString())).start();
                    // System.out.println(msg);
                    distccdRelay DR = new distccdRelay(suuid.toString(), cuuid.toString());
                    distccServer.actions.put(id, DR);
                }

                try {
                    Thread.sleep(500);
                    Thread.yield();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

    });


    Thread startControl = new Thread(new Runnable() {
        @Override
        public void run() {
            recvFromClient.start();
            sendToClient.start();
        }
    });


}
