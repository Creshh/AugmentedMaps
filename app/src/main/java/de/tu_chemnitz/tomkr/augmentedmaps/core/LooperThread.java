package de.tu_chemnitz.tomkr.augmentedmaps.core;

/**
 * Created by Tom Kretzschmar on 21.12.2017.<br>
 * <br>
 * A abtract class extending {@link Thread} to run in a pausable loop. This class is used to rerun code continously in a loop while being able to pause and restart the procedure.<br>
 * Implementations must override the {@link #loop()} method and implement their code in this method.
 */
public abstract class LooperThread extends Thread{
    /**
     * Tag for logging
     */
    private static final String TAG = LooperThread.class.getName();

    /**
     * Synchronization object.
     */
    private final Object pauseLock = new Object();

    /**
     * Flag for pausing the loop.
     */
    private boolean pause = false;

    /**
     * Flag for finally stopping the loop and corresponding thread.
     */
    private boolean stop = false;

    /**
     * Targeted minimal frametime. If processing done in loop() is heavy, time spent per frame could be much longer then this value.
     */
    private int targetFrametime;

    /**
     * FPS calculated from {@link #frametime} for conveniance reading access to these values.
     */
    private int fps;

    /**
     * Actual frametime. Can exceed the {@link #targetFrametime}.
     */
    private long frametime;

    /**
     * Method which must be overriden by implementations. This method is called in a loop. The system tries to invoke this method as often as through {@link #targetFrametime} defined.<br>
     * If the processing done in this method is heavy, it might exceed the targeted frametime. If it is much lower, the Thread will go to sleep for the amount of time left.
     */
    protected abstract void loop();

    /**
     * Full constructor.
     * @param targetFrametime Targeted minimal frametime. If processing done in loop() is heavy, time spent per frame could be much longer then this value.
     */
    public LooperThread(int targetFrametime){
        this.targetFrametime = targetFrametime;
    }

    /**
     * Runs the loop and accounts for the frametime handling. Must not be called, instead invoke {@link #startLooper()}
     */
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

    /**
     * Start LooperThread after initialization with {@link #LooperThread(int)} or after it was paused with {@link #pause()}.
     */
    public void startLooper(){
        if(stop){
            throw new IllegalThreadStateException("LooperThread was already stopped. Can not be restarted. Maybe use LooperThread.pause() instead of LooperThread.quit()");
        }
        synchronized (pauseLock) {
            pause = false;
            pauseLock.notifyAll();
        }
        if (!this.isAlive()) {
            this.start();
        }
        onStart();
    }

    /**
     * Pause LooperThread. Thread will wait until {@link #startLooper()} is called again. Use this instead of {@link #quit()} if it should be restarted in the future.
     */
    public void pause(){
        synchronized (pauseLock) {
            pause = true;
        }
        onPause();
    }

    /**
     * Callback method which is invoked when {@link #pause()} is called. Can be overriden by subclasses.
     */
    protected void onPause(){

    }

    /**
     * Callback method which is invoked when {@link #startLooper()} is called. Can be overriden by subclasses.
     */
    protected void onStart(){

    }

    /**
     * Callback method which is invoked when {@link #quit()} is called. Can be overriden by subclasses.
     */
    protected void onQuit(){

    }

    /**
     * Quit and stop LooperThread. It must not be restarted after calling this function or it will throw a {@link IllegalThreadStateException}.
     */
    public void quit(){
        stop = true;
        onQuit();
    }

    /**
     * @return current FPS value.
     */
    public int getFPS(){
        return fps;
    }

    /**
     * @return current frametime.
     */
    public long getFrametime() {
        return frametime;
    }
}
