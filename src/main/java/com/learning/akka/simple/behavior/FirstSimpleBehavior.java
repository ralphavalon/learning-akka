package com.learning.akka.simple.behavior;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FirstSimpleBehavior extends AbstractBehavior<String> {

  private FirstSimpleBehavior(ActorContext<String> context) {
    super(context);
  }

  public static Behavior<String> create() {
    return Behaviors.setup(FirstSimpleBehavior::new);
  }

  @Override
  public Receive<String> createReceive() {
    return newReceiveBuilder()
    .onMessageEquals("create a child", () -> {
      System.out.println("Current path: " + getContext().getSelf().path());
      ActorRef<String> secondActor = getContext().spawn(FirstSimpleBehavior.create(), "secondActor");
      System.out.println("Second Actor current path: " + secondActor.path());
      return this;
    })
    .onAnyMessage(message -> {
      System.out.println("Received: " + message);
      return this;
    }).build();
  }

}
