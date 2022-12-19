package streams

/*
  This is simply generating a hello world actor to show the boilerplate
  involved when creating an actor
 */

import akka.actor.{Actor, ActorSystem, Props}

class HelloActor extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case s: String => println(s + "!")
    case _ => println("what am I even supposed to do here?")
  }
}

object Program extends App {

    val system = ActorSystem("hello-world")
    val actor = system.actorOf(Props[HelloActor], name = "helloactor")
    actor ! "Hello World"
    system.terminate()
}