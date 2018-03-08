package server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;

public class distccServer {
    /*存放在线主机*/
    static List<controlClientSocket> clients = new ArrayList<>();
    /*存放活动主机uuid和主机对应*/
    static Map<String, Map> ATcs = new HashMap<>();
    /*存放uuid对应的服务活动*/
    static Map<String, distccdRelay> actions = new HashMap<>();


    public static void main(String[] args) throws IOException {
        boolean isRun = true;
        ServerSocket serverControlSocket = new ServerSocket(3237);
        ServerSocket serverDistccdSocket = new ServerSocket(3236);

        Thread controlServer = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        /*监听端口*/
                        System.out.println("等待远程连接，端口号为：" + serverControlSocket.getLocalPort() + "...");
                        Socket clientS = serverControlSocket.accept();
                        clientS.setReceiveBufferSize(1024);
                        clientS.setSendBufferSize(1024);
                        controlClientSocket ccs = new controlClientSocket(clientS);
                        clients.add(ccs);
                        ccs.startControl.start();
                        // new Thread(new Alived()).start();
                        // new Thread(new controlWithClient(clientS)).start();
                    } catch (SocketTimeoutException e) {
                        System.out.println("Socket timed out!");
                        break;
                    } catch (IOException e) {
                        System.out.println("服务器异常！！！");
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        /*处理链接主机*/
        Thread distccdServer = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String uuid = null;
                    try {
                        // System.out.println("等待远程distccd连接，端口号为：" + serverControlSocket.getLocalPort() + "...");
                        Socket clientD = serverDistccdSocket.accept();
                        clientD.setReceiveBufferSize(1024);
                        clientD.setSendBufferSize(1024);
                        //new Thread(new distccDeamon(clientD)).start();
                        System.out.println("==Distccd 主机活动！");
                        InputStream in = clientD.getInputStream();
                        byte buff[] = new byte[1024];
                        StringBuilder msg = new StringBuilder();
                        try {

                            in.read(buff);
                        } catch (IOException e) {
                            System.out.println("读取服务器错误！！");
                        }
                        for (int i = 0; buff[i] > 0; i++) {
                            msg.append((char) buff[i]);
                        }
                        if (msg.toString().length() > 1) {
                            if (msg.toString().length() > 5 && msg.toString().substring(0, 5).equals("UUID=")) {
                                uuid = msg.toString().substring(5, 41);
                            }
                        }

                        System.out.println("  --Connect-uuid:" + uuid);
                        /*按照uuid 确定属于某个活动 并存放到特定的map中*/
                        if (uuid != null) {
                            for (Map.Entry<String, Map> entry : ATcs.entrySet()) {
                                for (Object str : entry.getValue().values()) {
                                    if (str.toString().equals(uuid)) {
                                        distccdRelay dr = actions.get(entry.getKey());
                                        if (uuid.equals(dr.suuid)) {
                                            dr.distccdServerSockets.add(new distccdServerSocket(clientD));
                                            System.out.println("  --distccd服务端" + dr.distccdServerSockets.size() + "活动连接成功！");
                                            continue;
                                        } else if (uuid.equals(dr.cuuid)) {
                                            dr.distccdClientSockets.add(new distccdClientSocket(clientD));
                                            System.out.println("  --distccd客户端" + dr.distccdClientSockets.size() + "活动连接成功！");
                                            continue;
                                        } else {
                                            System.out.println("  --distccd活动连接失败！");
                                            try {
                                                clientD.close();
                                            } catch (IOException e) {
                                                System.out.println("服务器 错误！");
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                        }

                    } catch (SocketException e) {
                        System.out.println("服务器 故障！");
                    } catch (IOException e) {
                        System.out.println("服务器accpect err");
                    }
                }
            }
        });

        controlServer.start();
        distccdServer.start();
        new Thread(new Alived()).start();

        while (isRun)

        {
            System.out.println("distcc集群服务器端：");
            System.out.println("选择你的操作：");
            System.out.println("1.查看在线主机");
            System.out.println("2.查看在线distcc服务器！");
            System.out.println("3.");

            Scanner input = new Scanner(System.in);
            System.out.println(":");
            int cho = input.nextInt();
            System.out.println("你选择了：" + cho);
            switch (cho) {
                case 1:
                    System.out.print("目前有:" + clients.size());
                    System.out.println(clients.size());
                    for (controlClientSocket ccs : clients) {
                        System.out.println(ccs.socket.getRemoteSocketAddress());
                    }
                    break;
                case 2:
                    System.out.println("目前有:");
                    for (controlClientSocket ccs : clients) {
                        if (ccs.isDistccdServer) {
                            System.out.println(ccs.socket.getRemoteSocketAddress());
                        }
                    }
                    break;
                case 3:
                    System.out.println("distccds-action：");
                    System.out.println("The action:" + actions.size());
                    System.out.println("Connect:");
                    for (distccdRelay dr : actions.values()) {
                        System.out.println("C-S: " + dr.cuuid + " " + dr.suuid);
                        System.out.println("Cs-Ss: " + dr.distccdClientSockets.size() + " " + dr.distccdServerSockets.size());
                        System.out.println("\n");
                    }
                    break;
            }
        }
        System.out.println("\n\n");

    }

}

