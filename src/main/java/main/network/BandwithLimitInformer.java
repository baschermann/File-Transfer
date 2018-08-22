package main.network;

import main.file.FileUtility;
import main.network.data.DataConnectedComputer;

import java.util.HashSet;
import java.util.Set;

public class BandwithLimitInformer {

    private volatile long limitPerSecond = 204800; // 200 KB
    private Set<DataConnectedComputer> computerRegistered = new HashSet<>();
    public volatile long timer = 25; // ms
    public volatile long limit = 5120;
    public volatile boolean unlimited = true;

    public synchronized void register(DataConnectedComputer connectedComputer) {
        computerRegistered.add(connectedComputer);
        calculate();
    }

    public synchronized void unregister(DataConnectedComputer connectedComputer) {
        computerRegistered.remove(connectedComputer);
        calculate();
    }

    public void setLimitPerSecond(long newLimit) {
        if(newLimit == 0) {
            unlimited = true;
            return;
        }

        limitPerSecond = newLimit;
        calculate();
        unlimited = false;
    }

    public void setTimer(long timer) {
        this.timer = timer;
        calculate();
    }

    private void calculate() {
        limit = (long) ((double) limitPerSecond / (1000f / (double) timer)) / Math.max(1, computerRegistered.size());

        if(unlimited) {
            System.out.println("New bandwidth limit - Unlimited");
        } else {
            System.out.println("New bandwidth limit - Per second: " + FileUtility.getPrintableSize(limitPerSecond) + ", every " + timer + "ms: " + FileUtility.getPrintableSize(limit));
        }
    }
}
