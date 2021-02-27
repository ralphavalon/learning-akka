package com.learning.akka;

import com.learning.akka.bigprimes.behavior.FirstSimpleBehavior;

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
		ActorSystem<String> actorSystem = ActorSystem.create(FirstSimpleBehavior.create(), "FirstActorSystem");
		actorSystem.tell("Testing message");
	}

}
