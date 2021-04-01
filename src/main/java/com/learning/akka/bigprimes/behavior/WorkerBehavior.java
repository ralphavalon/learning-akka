package com.learning.akka.bigprimes.behavior;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {

  @Getter
  @AllArgsConstructor
  public static class Command implements Serializable {
    private static final long serialVersionUID = 1L;
    private String message;
    private ActorRef<ManagerBehavior.Command> sender;
  }

  private WorkerBehavior(ActorContext<WorkerBehavior.Command> context) {
    super(context);
  }

  public static Behavior<WorkerBehavior.Command> create() {
    return Behaviors.setup(WorkerBehavior::new);
  }

  @Override
  public Receive<WorkerBehavior.Command> createReceive() {
    return handleMessagesWhenWeDontYetHaveAPrimeNumber();
  }

  public Receive<WorkerBehavior.Command> handleMessagesWhenWeDontYetHaveAPrimeNumber() {
    return newReceiveBuilder()
    .onAnyMessage(command -> {
      BigInteger bigInteger = new BigInteger(2000, new Random());
      BigInteger prime = bigInteger.nextProbablePrime();
      Random r = new Random();
      if(r.nextInt(5) < 2) {
        command.getSender().tell(new ManagerBehavior.ResultCommand(prime));
      }
      return handleMessagesWhenWeAlreadyHaveAPrimeNumber(prime);
    }).build();
  }

  public Receive<WorkerBehavior.Command> handleMessagesWhenWeAlreadyHaveAPrimeNumber(BigInteger prime) {
    return newReceiveBuilder()
    .onAnyMessage(command -> {
      Random r = new Random();
      if(r.nextInt(5) < 2) {
        command.getSender().tell(new ManagerBehavior.ResultCommand(prime));
      }
      return Behaviors.same();
    }).build();
  }

}
