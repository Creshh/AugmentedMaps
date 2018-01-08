package de.tu_chemnitz.tomkr.augmentedmaps.core;


/**
 * Created by Tom Kretzschmar on 21.12.2017.
 *
 */
public abstract class LooperThread extends Thread{
    private static final String TAG = LooperThread.class.getName();
    private final Object pauseLock = new Object();
    private boolean pause = false;
    private boolean stop = false;
    private int targetFrametime;
    private int fps;
    private long frametime;

    protected abstract void loop();

    public LooperThread(int targetFrametime){
        this.targetFrametime = targetFrametime;
    }

    @Override
    public void run(){
        while (!stop) {
            long starttime = System.currentTimeMillis();

            synchronized (pauseLock) {
                while (pause) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            loop();

            frametime = (System.currentTimeMillis() - starttime);
            fps = (frametime > targetFrametime) ? (int) (1000 / frametime) : targetFrametime;
            if (frametime < targetFrametime) {
                try {
                    sleep(targetFrametime - frametime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startLooper(){
        synchronized (pauseLock) {
            pause = false;
            pauseLock.notifyAll();
        }
        if (!this.isAlive()) {
            this.start();
        }
        onStart();
    }

    public void pause(){
        synchronized (pauseLock) {
            pause = true;
        }
        onPause();
    }

    protected void onPause(){

    }
    protected void onStart(){

    }

    protected void onQuit(){

    }

    public void quit(){
        stop = true;
        onQuit();
    }

    public int getFPS(){
        return fps;
    }

    public long getFrametime() {
        return frametime;
    }
}
