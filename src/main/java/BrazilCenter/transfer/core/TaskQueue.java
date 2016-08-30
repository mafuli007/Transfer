package BrazilCenter.transfer.core;

import java.util.*;

import BrazilCenter.models.Task;


/**
 * */
public class TaskQueue {

	private Queue<Task> tasklist;

	public TaskQueue() {
		this.tasklist = new LinkedList<Task>();
	}
	public Task GetTask() {
		synchronized (this) {
			Task task = this.tasklist.poll();
			return task;
		}
	}
	public void AddTask(Task task) {
		synchronized (this) {
			this.tasklist.add(task);
		}
	}
}
