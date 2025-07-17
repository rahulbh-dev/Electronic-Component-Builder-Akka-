// Name: Rahul Bhardwaj, Matriculation No.: 237868

package org.example;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;

/**
 * Actor responsible for building components (basic or electronic).
 * Spawns other actors for subcomponents and reports completion.
 */
public class PartBuilder extends AbstractBehavior<BuildMessage> {

    // Delay in milliseconds to simulate processing time
    private static final int DELAY_MS = 1000;

    // Factory method to create a new PartBuilder actor
    public static Behavior<BuildMessage> create() {
        return Behaviors.setup(PartBuilder::new);
    }

    // Constructor takes the actor context
    private PartBuilder(ActorContext<BuildMessage> context) {
        super(context);
    }

    // Define how this actor reacts to incoming messages
    @Override
    public Receive<BuildMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(BuildMessage.class, this::onBuild)
                .build();
    }

    // Main handler for building a component
    private Behavior<BuildMessage> onBuild(BuildMessage msg) {
        ComponentType part = msg.component;

        // Case 1: Base component (BB1, BB2, BB3)
        if (part.name().startsWith("BB")) {
            getContext().getLog().info("Producing {}", part);

            try {
                Thread.sleep(DELAY_MS); // Simulate time taken to build a basic block
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // restore interrupt flag
            }

            getContext().getLog().info("Finished {}", part);
            msg.replyTo.tell(new DoneMessage(part));
            return this;
        }

        // Case 2: Electronic component (EB)
        // Determine which subcomponents are required
        ComponentType left, right;
        switch (part) {
            case EB1 -> { left = ComponentType.BB1; right = ComponentType.BB2; }
            case EB2 -> { left = ComponentType.EB1; right = ComponentType.BB2; }
            case EB3 -> { left = ComponentType.BB3; right = ComponentType.EB2; }
            case EB4 -> { left = ComponentType.EB1; right = ComponentType.EB3; }
            case EB5 -> { left = ComponentType.EB3; right = ComponentType.EB4; }
            default -> throw new IllegalArgumentException("Unknown component: " + part);
        }

        getContext().getLog().info("Producing {}", part);

        // Create a temporary helper actor to wait for both components to finish
        ActorRef<DoneMessage> combiner = getContext().spawnAnonymous(
                Combiner.create(part, msg.replyTo)
        );

        // Recursively spawn new builders for each subcomponent
        getContext().spawnAnonymous(PartBuilder.create()).tell(new BuildMessage(left, combiner));
        getContext().spawnAnonymous(PartBuilder.create()).tell(new BuildMessage(right, combiner));

        return this;
    }

    /**
     * Internal helper actor used to combine two DoneMessages for one component.
     * When both parts are ready, it notifies the original requester and stops itself.
     */
    public static class Combiner extends AbstractBehavior<DoneMessage> {
        private final ComponentType target;              // The component we're building
        private final ActorRef<DoneMessage> notify;      // Actor to notify when done
        private int received = 0;                        // Counter to track finished subcomponents

        // Factory method to create a Combiner actor
        public static Behavior<DoneMessage> create(ComponentType target, ActorRef<DoneMessage> notify) {
            return Behaviors.setup(ctx -> new Combiner(ctx, target, notify));
        }

        private Combiner(ActorContext<DoneMessage> context, ComponentType target, ActorRef<DoneMessage> notify) {
            super(context);
            this.target = target;
            this.notify = notify;
        }

        // Define how the Combiner handles DoneMessages
        @Override
        public Receive<DoneMessage> createReceive() {
            return newReceiveBuilder()
                    .onMessage(DoneMessage.class, this::onDone)
                    .build();
        }

        // Handle completion messages from subcomponents
        private Behavior<DoneMessage> onDone(DoneMessage msg) {
            received++;

            // Once both subcomponents are finished, simulate build time and log
            if (received == 2) {
                try {
                    Thread.sleep(DELAY_MS); // Simulate time taken to assemble electronic component
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                getContext().getLog().info("Finished {}", target);
                notify.tell(new DoneMessage(target));
                return Behaviors.stopped(); // Done: shut down this helper actor
            }

            return this; // Wait for the second message
        }
    }
}
