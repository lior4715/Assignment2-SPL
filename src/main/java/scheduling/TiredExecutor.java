package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        // TODO
        workers = new TiredThread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            workers[i] = new TiredThread(i, 0.5 + Math.random());
            workers[i].start();
            idleMinHeap.add(workers[i]);
        }

    }

    public void submit(Runnable task) {
        // TODO
        while (true) {
            TiredThread worker;
            try {
                worker = idleMinHeap.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            try {
                worker.newTask(task);
                inFlight.incrementAndGet(); 
                return;
            } catch (IllegalStateException e) {
                idleMinHeap.put(worker);
            }
        }

    }

    public void submitAll(Iterable<Runnable> tasks) {
        // TODO: submit tasks one by one and wait until all finish
        for (Runnable task : tasks) {
            submit(task);
        }
        while (inFlight.get() > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() throws InterruptedException {
        // TODO
        for (TiredThread worker : workers) {
            worker.interrupt();
            worker.join();
        }
    }

    public synchronized String getWorkerReport() {
        // TODO: return readable statistics for each worker
        String report = "";
        for (TiredThread worker : workers) {
            report += String.format("Worker %d: Time Used = %d ns, Time Idle = %d ns, Fatigue = %.2f, Busy = %b\n",
                    worker.getWorkerId(), worker.getTimeUsed(), worker.getTimeIdle(), worker.getFatigue(), worker.isBusy());
        }   
        return report;
    }
}
