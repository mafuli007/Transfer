package BrazilCenter.transfer.repeatRequestService;

import java.util.*;
/**
  * */
public class RepeatRequestMsgList {

 	private Queue<RepeatRequestMsg> msgList ;

	public RepeatRequestMsgList() {
		this.msgList = new LinkedList<RepeatRequestMsg>();
	}
	public RepeatRequestMsg GetMsg() {
		synchronized (this) {
			RepeatRequestMsg msg = this.msgList.poll();
			return msg;
		}
	}
	public void AddMsg(RepeatRequestMsg msg) {
		synchronized (this) {
			this.msgList.add(msg);
		}
	}
}
