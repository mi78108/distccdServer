package server;

import java.util.ArrayList;
import java.util.List;

public class distccdRelay {
    String suuid;
    String cuuid;
    boolean isDone = false;
    boolean isRun = true;
    List<distccdServerSocket> distccdServerSockets = new ArrayList<>();
    List<distccdClientSocket> distccdClientSockets = new ArrayList<>();

    public distccdRelay(String suuid, String cuuid) {
        this.suuid = suuid;
        this.cuuid = cuuid;
        startRaley.start();
    }


    Thread startRaley = new Thread(new Runnable() {
        @Override
        public void run() {
            System.out.println("server 实例开始");
            while (isRun) {
                if (1 <= distccdClientSockets.size() && distccdClientSockets.size() <= distccdServerSockets.size()) {
                    for (distccdClientSocket dcs : distccdClientSockets) {
                        if (!dcs.isInUse) {
                            for (distccdServerSocket dsc : distccdServerSockets) {
                                if (!dsc.isInUse) {
                                    if (dcs.isReady || dcs.isready()) {
                                        if (dsc.isReady || dsc.isready()) {
                                            System.out.println("交驳 成功 开始通讯！");
                                            new Thread(new relay(dsc, dcs)).start();
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    });
}
