// Name: Rahul Bhardwaj, Matriculation No.: 237868

package org.example;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.Behaviors;
import org.slf4j.Logger;

/**
 * Entry point of the ZuliefererSystem.
 * Initializes the actor system and starts the build process for EB5.
 */
public class Main {
    public static void main(String[] args) {

        // Created here the top-level ActorSystem that manages the whole process
        ActorSystem<BuildMessage> system = ActorSystem.create(
                Behaviors.setup(ctx -> {

                    // Logger for initial output
                    Logger log = ctx.getLog();

                    // Log the start of the top-level component build
                    log.info("Producing {}", ComponentType.EB5);

                    // This actor will receive a DoneMessage when EB5 is complete
                    ActorRef<DoneMessage> terminator = ctx.spawn(
                            Behaviors.setup(innerCtx -> {
                                Logger innerLog = innerCtx.getLog();

                                // Handle the DoneMessage and shut down the system
                                return Behaviors.receive(DoneMessage.class)
                                        .onMessage(DoneMessage.class, msg -> {
                                            innerLog.info("Build completed: {}", msg.component);
                                            innerCtx.getSystem().terminate(); //  shutdown
                                            return Behaviors.stopped();       // stop the terminator
                                        }).build();
                            }),
                            "terminator" // explicit name, since this actor is unique
                    );

                    // Spawn a PartBuilder to begin constructing EB5
                    ctx.spawnAnonymous(PartBuilder.create())
                            .tell(new BuildMessage(ComponentType.EB5, terminator));

                    // This behavior doesn’t respond to messages itself
                    return Behaviors.ignore();
                }),
                "ZuliefererSystem"
        );

        // Block the main thread until the actor system shuts down
        system.getWhenTerminated().toCompletableFuture().join();
    }
}
