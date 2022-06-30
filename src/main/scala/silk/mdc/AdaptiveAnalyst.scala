package jesperan.silk
package mdc

import akka.actor.ActorLogging
import akka.actor.Actor


object AdaptiveAnalyst {

}

class AdaptiveAnalyst extends Actor with ActorLogging {

    override def preStart(): Unit = {
        super.preStart()
        log.info("Adaptive Analyst actor started")
	}

    def receive: Receive = {
        case (msg) => log.info("Adaptive Analyst received: " + msg)
    }
}
