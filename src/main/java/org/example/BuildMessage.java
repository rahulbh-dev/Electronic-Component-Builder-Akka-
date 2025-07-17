// Name: Rahul Bhardwaj, Matriculation No.: 237868
package org.example;

import akka.actor.typed.ActorRef;

public class BuildMessage {
    public final ComponentType component;
    public final ActorRef<DoneMessage> replyTo;

    public BuildMessage(ComponentType component, ActorRef<DoneMessage> replyTo) {
        this.component = component;
        this.replyTo = replyTo;
    }
}
