package com.learning.akka;

import java.math.BigInteger;
import java.time.Duration;
import java.util.SortedSet;
import java.util.concurrent.CompletionStage;

import com.learning.akka.bigprimes.behavior.ManagerBehavior;
import com.learning.akka.racing.behavior.RaceController;
import com.learning.akka.simple.behavior.FirstSimpleBehavior;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;

@SpringBootApplication
public class AkkaApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(AkkaApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		bigPrimesBehavior();
	}

	protected void racingBehavior() {
		ActorSystem<RaceController.Command> actorSystem = ActorSystem.create(RaceController.create(), "RacingSimulation");
		actorSystem.tell(new RaceController.StartCommand());
	}

	protected void bigPrimesBehavior() {
		ActorSystem<ManagerBehavior.Command> actorSystem = ActorSystem.create(ManagerBehavior.create(), "BigPrimes");

		CompletionStage<SortedSet<BigInteger>> result = AskPattern.ask(actorSystem, (me) -> new ManagerBehavior.InstructionCommand("start", me)
		, Duration.ofSeconds(30), actorSystem.scheduler());

		result.whenComplete((reply, failure) -> {
			if(reply != null) {
				reply.forEach(System.out::println);
			} else {
				System.out.println("The system didn't respond in time.");
			}
			actorSystem.terminate();
		});
	}

	protected void firstSimpleBehavior() {
		ActorSystem<String> actorSystem = ActorSystem.create(FirstSimpleBehavior.create(), "FirstActorSystem");
		actorSystem.tell("Testing message");
		actorSystem.tell("create a child");
	}

}
