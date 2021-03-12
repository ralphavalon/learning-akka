package com.learning.akka.racing.behavior;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class RaceController extends AbstractBehavior<RaceController.Command> {

  private RaceController(ActorContext<Command> context) {
    super(context);
  }

  public interface Command extends Serializable {
  }

  @Getter
  @AllArgsConstructor
  public static class StartCommand implements Command {
    private static final long serialVersionUID = 1L;
  }

  @Getter
  @AllArgsConstructor
  public static class RacerUpdateCommand implements Command {
    private static final long serialVersionUID = 1L;
    private ActorRef<RacerBehavior.Command> racer;
    private int position;
  }

  @Getter
  @AllArgsConstructor
  public static class GetPositionsCommand implements Command {
    private static final long serialVersionUID = 1L;
  }

  @Getter
  @AllArgsConstructor
  public static class RacerFinishedCommand implements Command {
    private static final long serialVersionUID = 1L;
    private ActorRef<RacerBehavior.Command> racer;
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(RaceController::new);
  }

  private Map<ActorRef<RacerBehavior.Command>, Integer> currentPositions;
  private Map<ActorRef<RacerBehavior.Command>, Long> finishingTimes;
  private long start;
  private int raceLength = 50;
  private Object TIMER_KEY; // There's one timer per timer key

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder().onMessage(StartCommand.class, message -> {
      start = System.currentTimeMillis();
      currentPositions = new HashMap<>();
      finishingTimes = new HashMap<>();
      for (int i = 0; i < 10; i++) {
        ActorRef<RacerBehavior.Command> racer = getContext().spawn(RacerBehavior.create(), "racer" + i);
        currentPositions.put(racer, 0);
        racer.tell(new RacerBehavior.StartCommand(raceLength));
      }
      return Behaviors.withTimers(timer -> {
        timer.startTimerAtFixedRate(TIMER_KEY, new GetPositionsCommand(), Duration.ofSeconds(1));
        return Behaviors.same();
      });
    }).onMessage(GetPositionsCommand.class, message -> {
      for(ActorRef<RacerBehavior.Command> racer : currentPositions.keySet()) {
        racer.tell(new RacerBehavior.PositionCommand(getContext().getSelf()));
        displayRace();
      }
      return Behaviors.same();
    }).onMessage(RacerUpdateCommand.class, message -> {
      currentPositions.put(message.getRacer(), message.getPosition());
      return Behaviors.same();
    }).onMessage(RacerFinishedCommand.class, message -> {
      finishingTimes.put(message.getRacer(), System.currentTimeMillis());
      if (finishingTimes.size() == 10) {
        return raceCompleteMessageHandler();
      }
      return Behaviors.same();
    }).build();
  }

  public Receive<Command> raceCompleteMessageHandler() {
    return newReceiveBuilder()
      .onMessage(GetPositionsCommand.class, message -> {
        for(ActorRef<RacerBehavior.Command> racer : currentPositions.keySet()) {
          getContext().stop(racer);
        }
        displayResults();
        return Behaviors.withTimers(timers -> {
          timers.cancelAll();
          return Behaviors.stopped();
        });
      })
      .build();
  }

  private void displayRace() {
    int displayLength = 160;
		for (int i = 0; i < 50; ++i) System.out.println();
		System.out.println("Race has been running for " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
		System.out.println("    " + new String (new char[displayLength]).replace('\0', '='));
    int i = 0;
		for(ActorRef<RacerBehavior.Command> racer : currentPositions.keySet()) {
			System.out.println(i++ + " : "  + new String (new char[currentPositions.get(racer) * displayLength / 100]).replace('\0', '*'));
		}
	}

  private void displayResults() {
    System.out.println("Results");
    finishingTimes.values().stream().sorted().forEach(it -> {
      for(ActorRef<RacerBehavior.Command> key : finishingTimes.keySet()) {
        if(finishingTimes.get(key) == it) {
          String racerId = key.path().toString().substring(key.path().toString().length() - 1);
          System.out.println("Racer " + racerId + " finished in " + ( (double)it - start) / 1000 + " seconds");
        }
      }
    });
	}

}
