package org.example;

import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Logging;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Main {
	public static void main(String[] args) throws InterruptedException {

		var driver = GraphDatabase.driver("bolt://localhost:7687",
				AuthTokens.basic("neo4j", "your_password"), Config.builder()
						.withMaxConnectionPoolSize(2)
						.withLogging(Logging.none()).build());

		driver.verifyConnectivity();

		var noOfThreads = 2;
		var noToExecPerThread = 200000;
		var totalReqs = noToExecPerThread * noOfThreads;

		var threadPool = Executors.newFixedThreadPool(noOfThreads);


		var start = System.currentTimeMillis();
		for (int i = 0; i<noOfThreads; i++){
			threadPool.submit(() -> {
				var count = 0;

				while (count!=noToExecPerThread) {
					var result = driver.session().run("RETURN 1");
					result.consume();
					count++;
				}
			});
		}

		threadPool.shutdown();
		threadPool.awaitTermination(1000, TimeUnit.SECONDS);
		var end = System.currentTimeMillis();
		var totalTime = end - start;
		System.out.println(String.format("%s requests executed in %s milis", totalReqs, totalTime));
		System.out.println(String.format("%s reqs per mili", totalReqs/totalTime));
	}
}