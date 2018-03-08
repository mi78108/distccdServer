package server;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/*distccd请求 对象*/
public class distccdClientSocket {
    Socket socket;
    boolean isReady = false;
    boolean isInUse = false;

    void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(socket.getRemoteSocketAddress() + "关闭失败！");
        }
    }

    /*验证客户端准备就绪！*/
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
            if (msg.toString().length() > 0) {
                System.out.println(msg.toString());
                if (msg.toString().substring(0, 6).equals("CR:IAG")) {
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

    public distccdClientSocket(Socket socket) {
        this.socket = socket;
    }
}
