package com.learning.akka.bigprimes.behavior;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.Duration;
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

  public interface Command extends Serializable {
  }

  @Getter
  @AllArgsConstructor
  public static class InstructionCommand implements Command {
    private static final long serialVersionUID = 1L;
    private String message;
    private ActorRef<SortedSet<BigInteger>> sender;
  }

  @Getter
  @AllArgsConstructor
  public static class ResultCommand implements Command {
    private static final long serialVersionUID = 1L;
    private BigInteger prime;
  }

  @Getter
  @AllArgsConstructor
  private class NoResponseReceivedCommand implements Command {
    private static final long serialVersionUID = 1L;
    private ActorRef<WorkerBehavior.Command> worker;
  }

  private SortedSet<BigInteger> primes = new TreeSet<>();
  private ActorRef<SortedSet<BigInteger>> sender;

  private ManagerBehavior(ActorContext<Command> context) {
    super(context);
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(ManagerBehavior::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder().onMessage(InstructionCommand.class, command -> {
      this.sender = command.getSender();
      for (int i = 0; i < 20; i++) {
        ActorRef<WorkerBehavior.Command> worker = getContext().spawn(WorkerBehavior.create(), "Worker" + i);
        askWorkerForAPrime(worker);
      }
      return this;
    }).onMessage(ResultCommand.class, command -> {
      primes.add(command.getPrime());
      System.out.println("I have received " + primes.size() + " prime numbers.");
      if(primes.size() == 20) {
        this.sender.tell(primes);
      }
      return this;
    }).onMessage(NoResponseReceivedCommand.class, command -> {
      System.out.println("Retrying with worker " + command.getWorker().path());
      askWorkerForAPrime(command.getWorker());
      return Behaviors.same();
    })
    .build();
  }

  private void askWorkerForAPrime(ActorRef<WorkerBehavior.Command> worker) {
    getContext().ask(Command.class, worker, Duration.ofSeconds(5), (me) -> new WorkerBehavior.Command("start", me),
        (response, throwable) -> {
          if (response != null) {
            return response;
          } else {
            System.out.println("Worker " + worker.path() + " failed to respond.");
            return new NoResponseReceivedCommand(worker);
          }
        });
  }

}
