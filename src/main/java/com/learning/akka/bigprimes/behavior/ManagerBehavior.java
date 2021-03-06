package com.learning.akka.bigprimes.behavior;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.SortedSet;
import java.util.TreeSet;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

  public interface Command extends Serializable {}

  @Getter
  @AllArgsConstructor
  public static class InstructionCommand implements Command {
    private static final long serialVersionUID = 1L;
    private String message;
  }

  @Getter
  @AllArgsConstructor
  public static class ResultCommand implements Command {
    private static final long serialVersionUID = 1L;
    private BigInteger prime;
  }

  private SortedSet<BigInteger> primes = new TreeSet<>();

  private ManagerBehavior(ActorContext<Command> context) {
    super(context);
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(ManagerBehavior::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
    .onMessage(InstructionCommand.class, command -> {
      for (int i = 0; i < 20; i++) {
        ActorRef<WorkerBehavior.Command> worker = getContext().spawn(WorkerBehavior.create(), "Worker" + i);
        worker.tell(new WorkerBehavior.Command("start", getContext().getSelf()));
        worker.tell(new WorkerBehavior.Command("start", getContext().getSelf()));
      }
      return this;
    })
    .onMessage(ResultCommand.class, command -> {
      primes.add(command.getPrime());
      System.out.println("I have received " + primes.size() + " prime numbers.");
      if(primes.size() == 20) {
        primes.forEach(System.out::println);
      }
      return this;
    }).build();
  }

}
