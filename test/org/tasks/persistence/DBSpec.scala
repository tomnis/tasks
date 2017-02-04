package org.tasks.persistence

import java.sql.Date

import org.junit.{Before, Test}
import org.scalatest.Matchers
import org.scalatest.junit.JUnitSuite

/**
  * Created by jayonhuh on 1/30/17.
  */
class DBSpec extends JUnitSuite with Matchers {

  @Before
  def createSchema(): Unit = {
    DBConnection.createSchema()
  }

  @Test
  def writeTask(): Unit = {
    // TODO topicId  should be untitled if not given a topic

    // getting current date in a value to use
    val date = new Date(System.currentTimeMillis())

    // Creating two tasks to add to db
    val task: Task = Task(0, title = "to do", note = "some note", dueDate = Option(date))
    val anotherTask: Task = Task(0, title = "to do", note = "some other note")
    val insertedTask: Task = DBWriter.putTask(task).get
    val anotherInsertedTask: Task = DBWriter.putTask(anotherTask).get

    // verify that the IDs of the tasks are not equal.
    insertedTask.id should not be anotherInsertedTask.id

    // creating an identical task to 'task' should fail
    intercept[Exception] { DBWriter.putTask(task).get }
  }

  @Test
  def writeTaskTopic(): Unit = {

    // Create two taskTopics to add to db
    val taskTopic: TaskTopic = TaskTopic(0, description = "test")
    val taskTopic2: TaskTopic = TaskTopic(0, description = "test2")

    // insert both to DB
    val insertedTopic: TaskTopic = DBWriter.putTaskTopic(taskTopic).get
    val insertedTopic2: TaskTopic = DBWriter.putTaskTopic(taskTopic2).get

    // confirm that the ID are different
    insertedTopic.id should not be insertedTopic2.id



  }

  @Test
  def readTask(): Unit = {

    // Try to read all tasks
    val allTasks: List[Task] = DBReader.getAllTasks()

    // verify that the list is not empty
    allTasks.isEmpty should not be true

    // Create a task with a topic ID
    val task: Task = Task(0, title = "to do", topicID = Option(3), note = "some note")
    val insertedTask: Task = DBWriter.putTask(task).get

    // verify that we can return the singular task and that it is the one created above
    val listWithTask: List[Task] = DBReader.getTasksForTopicID(insertedTask.topicID.get)
    listWithTask foreach {task: Task => task.id should be (insertedTask.id) }

  }

  @Test
  def Dependencies(): Unit = {

    //Add two tasks and add a dependency between one another
    val task: Task = Task(0, title = "to do", topicID = Option(3), note = "some note")
    val anotherTask: Task = Task(0, title = "to do 2", topicID = Option(3), note = "some note again")
    val insertedTask: Task = DBWriter.putTask(task).get
    val anotherInsertedTask: Task = DBWriter.putTask(anotherTask).get

    val insertedDependency: Dependency = DBWriter.putDependency(insertedTask, anotherInsertedTask).get

    println("DEPEDNENCY HERE" + insertedDependency.id)

    // Verify that the dependency was created correctly
    insertedDependency.taskID should be (insertedTask.id)
    insertedDependency.dependencyTaskID should be (anotherInsertedTask.id)

    // Test getting the dependency task and verify it is the correct task
    val dependencyTask: Task = DBReader.getDependencyTasks(insertedTask.id).head
    dependencyTask.id should be (anotherInsertedTask.id)

    // Test getting the dependent task and verify it is the correct task
    val dependentTask: Task = DBReader.getDependentTasks(anotherInsertedTask.id).head
    dependentTask.id should be (insertedTask.id )

    // add a dependency to the one that is current a dependent
    val anotherDependency: Task = Task(0, title = "another one - dj khaled", topicID = Option(3), note = " whatever")
    val insertDependencyTask: Task = DBWriter.putTask(anotherDependency).get

    val secondDep: Dependency = DBWriter.putDependency(anotherInsertedTask, insertDependencyTask).get


    println("tasks id is" + insertedTask.id)
    // get a list of tasks by calling the transitive dependency task function
    val transitiveTasks: List[Task] = DBReader.getTransitiveDependencyTasks(insertedTask.id)

    transitiveTasks map{ _.id } equals List(anotherInsertedTask.id, insertDependencyTask.id) should be (true)

    //transitiveTasks.equals(List(anotherInsertedTask, insertDependencyTask) ) should be (true)









  }


}
