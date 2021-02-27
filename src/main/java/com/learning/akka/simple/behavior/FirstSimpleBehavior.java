package com.learning.akka.simple.behavior;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FirstSimpleBehavior extends AbstractBehavior<String> {

  public FirstSimpleBehavior(ActorContext<String> context) {
    super(context);
  }

  public static Behavior<String> create() {
    return Behaviors.setup(FirstSimpleBehavior::new);
  }

  @Override
  public Receive<String> createReceive() {
    return newReceiveBuilder().onAnyMessage(message -> {
      System.out.println("Received: " + message);
      return this;
    }).build();
  }

}
