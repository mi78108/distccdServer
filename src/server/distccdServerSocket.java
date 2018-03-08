package server;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class distccdServerSocket {
    Socket socket;
    boolean isInUse = false;
    boolean isReady;

    public distccdServerSocket(Socket socket) {
        this.socket = socket;
    }

    boolean isready() {
        System.out.println("==验证 管道是否就绪");
        try {
            byte buff[] = new byte[1024];
            StringBuilder msg = new StringBuilder();
            InputStream in = socket.getInputStream();
            in.read(buff);
            for (int i = 0; buff[i] > 0; i++) {
                msg.append((char) buff[i]);
            }
            if (msg.toString().length() >= 0) {
                System.out.println(msg.toString());
                if (msg.toString().substring(0, 6).equals("CR:IAR")) {
                    isReady = true;
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("打开 通道错误！");
            return false;
        }
        return false;
    }

    void closeSocket() {
        try {
            this.socket.close();
        } catch (IOException e) {
            System.out.println(socket.getRemoteSocketAddress() + "关闭失败！");
        }
    }
}
