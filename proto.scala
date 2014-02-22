import java.net._
import java.util.Random
import java.lang.Thread

import javax.transaction._
import org.infinispan._
import org.infinispan.manager._

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props

val MAX_TASK_EXECUTION_TIME = 1000

case class TaskActor(val syncTaskCache:Cache[String,String],val name:String) extends Actor {

  val SUCESS_STATUS = "SUCCESS"
  val random = new Random(System.currentTimeMillis())
  val tm = syncTaskCache.getAdvancedCache().getTransactionManager()

  def transacBlock(taskId:String) = {
    val nodeId = InetAddress.getLocalHost.toString

    if ( syncTaskCache.get(taskId) != SUCESS_STATUS ) {// if the task is already executed, ends here
      println("locking")
      syncTaskCache.put(taskId,nodeId) // lock on the taskId, will "block" other instance to run the
      if ( syncTaskCache.get(taskId) != SUCESS_STATUS ) { // in case other instance ran the task before the lock
        print("[" + name + "] Run task for ")
        runTask()
        syncTaskCache.put(taskId, SUCESS_STATUS)
        println("Done")
     }
     println("unlocking")
    }
  }

  def runTask() = {
    val delay = random.nextInt(MAX_TASK_EXECUTION_TIME)
    print(delay + " ... ")
    Thread.sleep(delay)
  }

  def testCase(taskId:String) = {
    tm.begin()
    transacBlock(taskId)
    tm.commit()
  }

  def receive = {
    case _ => testCase("task")
  }
}

object Proto {
  def main(args: Array[String]) {
    println("Starting QuickSell App...")
    val manager = new DefaultCacheManager("configurations.xml")
    val syncTaskCache = manager.getCache[String, String]("lockTaskTable")
    val actorSystem = ActorSystem("TaskSystem")

      val taskId = "TASK_ID"
      val nodeId = InetAddress.getLocalHost.toString
      val NB_CONCURRENT_THREAD = 15
      for (i <- 0 until NB_CONCURRENT_THREAD) {
        val actorname = "thread-" + i
        val taskActor = actorSystem.actorOf(Props(new TaskActor(syncTaskCache, actorname)), name = actorname)
        taskActor ! actorname
        println(actorname)
      }
      println("All actors created and running...")
      Thread.sleep(NB_CONCURRENT_THREAD * MAX_TASK_EXECUTION_TIME)
      manager.stop()
      println("App shutdown.")
    }
}
