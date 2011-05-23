package sandbox;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import benchmarks.benchmarks.mappinglooping.ComplexLoopingStringArray;
import junit.data.Data;
import static net.jcores.CoreKeeper.$;

/**
 * Java parallelization right out of hell.
 * 
 * Try running this, change n from 1 to ... #CPUs, and see how it does not scale.
 *  
 */
public class ParallelizationTest {
	
	public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
		int n = 2;
		final String in[] = Data.strings(1000000);
		final String out[] = new String[in.length];
		final CyclicBarrier barrier = new CyclicBarrier(n + 1);

		
		long start = System.currentTimeMillis();
		
		$.execute(new Runnable() {
			@Override
			public void run() {
				System.out.println("Runner " + Thread.currentThread());
				final String[] iin = Data.strings(1000000);// Arrays.copyOf(in, in.length);
				final String[] oout = new String[iin.length]; //Arrays.copyOf(out, in.length);
				
				for(int i=0; i<iin.length; i++) {
					oout[i] = ComplexLoopingStringArray.ff(iin[i]);
				}
				
				try {
					barrier.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (BrokenBarrierException e) {
					e.printStackTrace();
				}			
			}
		}, n);
		
		barrier.await();
		
		long stop = System.currentTimeMillis();
		
		System.out.println(out[new Random().nextInt(in.length)]);
		System.out.println("n=" + n + ": " + (stop-start) + "ms");
		
		/* 
		
		Results on an Intel i5, 4GB RAM, Windows XP, Java 6.25 (same for Java 7 Beta) 
		
		Plain
		n=1: 6203ms
		n=2: 9359ms
		n=4: 32422ms


		Oout only
		n=4: 33703ms

		Iin only
		n=4: 33391ms

		Both
		n=4: 33360ms
		*/
 	}
}
