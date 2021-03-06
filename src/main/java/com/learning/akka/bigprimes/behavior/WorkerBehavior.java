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
    return newReceiveBuilder()
    .onAnyMessage(command -> {
      if ("start".equals(command.getMessage())) {
        BigInteger bigInteger = new BigInteger(2000, new Random());
        command.getSender().tell(new ManagerBehavior.ResultCommand(bigInteger.nextProbablePrime()));
      }
      return this;
    }).build();
  }

}
