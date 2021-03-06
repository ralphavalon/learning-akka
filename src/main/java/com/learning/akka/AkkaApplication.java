package com.learning.akka;

import com.learning.akka.bigprimes.behavior.ManagerBehavior;
import com.learning.akka.simple.behavior.FirstSimpleBehavior;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import akka.actor.typed.ActorSystem;

@SpringBootApplication
public class AkkaApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(AkkaApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		bigPrimesBehavior();
	}

	protected void bigPrimesBehavior() {
		ActorSystem<String> actorSystem = ActorSystem.create(ManagerBehavior.create(), "BigPrimes");
		actorSystem.tell("start");
	}

	protected void firstSimpleBehavior() {
		ActorSystem<String> actorSystem = ActorSystem.create(FirstSimpleBehavior.create(), "FirstActorSystem");
		actorSystem.tell("Testing message");
		actorSystem.tell("create a child");
	}

}
