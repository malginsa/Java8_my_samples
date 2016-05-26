package metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

public class Metrics {
	
	
	public static void main(final String[] args) {
		
		Metrics metrics = new Metrics();
		
		Instant start = Instant.now();
		metrics.doWork();
		Instant stop = Instant.now();
		
		Duration d = Duration.between(start, stop);
		
		System.out.println("Elapsed: " + d.toMillis());
		
	}

	private void doWork() {
		
		Data data = new Data();
		ReferenceSequence sequence = new ReferenceSequence();
		
		long total = 0;

		List<CompletableFuture<Integer>> futures = new LinkedList<>();
		
		Semaphore sem = new Semaphore(1);
		
		for (Record record : data) {
			Reference ref = sequence.getRef(record);
			sem.acquireUninterruptibly();
			CompletableFuture<Integer> processTask = CompletableFuture.supplyAsync(() -> {
				int sum = process(record, ref);
				sem.release();
				return sum;}
			);
			futures.add(processTask);
		}
		
		for (CompletableFuture<Integer> completableFuture : futures) {
			total += completableFuture.join();
		}
		System.out.println(total);
		
	}

	private int process(final Record record, final Reference ref) {
		int sum = 0;
		for (int i = 0; i < record.read.length; i++) {
			sum += record.read[i];
			sum += ref.read[i];
		}
		return sum;
	}
	

}
