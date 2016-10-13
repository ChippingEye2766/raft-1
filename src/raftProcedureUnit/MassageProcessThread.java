package raftProcedureUnit;

import java.util.ArrayList;
import java.util.List;
import communicationUnit.ConcurrentSocket;
import communicationUnit.Massage;
import communicationUnit.MassageQueue;
import communicationUnit.SendTask;
import communicationUnit.SocketList;
import communicationUnit.ThreadPool;
import serverUnit.Node;
import timerUnit.TimerThread;
import util.JSON;
import util.QueryTask;

public class MassageProcessThread implements Runnable {

	@Override
	public void run() {
		while (true) {
			// TODO Auto-generated method stub
			Massage massage = MassageQueue.getInstance().get_massage();
			System.out.println(massage.massage);
			List<Object> msg = JSON.JSONToArray(massage.massage);

			int status = Node.getInstance().server.status;
			int msgType = (int) msg.get(0);
			ConcurrentSocket msgSocket = massage.socket;// �����Ϣ��Դ��socket

			if (msgType == 5) {
				String ipport = (String) msg.get(1);
				SocketList.getInstance().move_welcomeSocket(msgSocket, ipport);
			}

			if (status == 0) {// ��ǰ��follower
				if (msgType == 0) {
					int term = (int) msg.get(1);
					int lastLogIndex = (int) msg.get(2);
					int lastLogTerm = (int) msg.get(3);

					int myTerm = Node.getInstance().server.currentTerm;
					int myLastLogIndex = Node.getInstance().server.log.get_lastLogIndex();
					int myLastLogTerm = Node.getInstance().server.log.get_lastLogTerm();

					List<Object> msg1 = new ArrayList<Object>();// ѡƱ��ִ
					if (term > myTerm) {
						if (lastLogTerm > myLastLogTerm
								|| lastLogTerm == myLastLogTerm && lastLogIndex >= myLastLogIndex) {
							Node.getInstance().server.currentTerm = term;// �޸��Լ���term
							TimerThread.getInstance().reset_timer();// ���ü�ʱ��
							// ����ѡƱ��ִ
							msg1.add(1);
							msg1.add(term);
							msg1.add(true);
						} else {
							// ����ѡƱ��ִ
							msg1.add(1);
							msg1.add(term);
							msg1.add(false);
						}
					} else {
						// ����ѡƱ��ִ
						msg1.add(1);
						msg1.add(myTerm);
						msg1.add(false);
					}
					String massage1 = JSON.ArrayToJSON(msg1);
					SendTask task = new SendTask(msgSocket, massage1);
					ThreadPool.getInstance().addTasks(task);// ��ִѡƱ
				} else if (msgType == 1) {
					// ��������
				} else if (msgType == 2) {
					// log����
					int term = (int) msg.get(1);
					int myTerm = Node.getInstance().server.currentTerm;

					List<Object> msg3 = new ArrayList<Object>();
					if (term >= myTerm) {
						Node.getInstance().server.currentTerm = term;// �޸��Լ���term
						TimerThread.getInstance().reset_timer();// ���ü�ʱ��
						// ��ʼlog���ƹ���
						logCopy(msg, msgSocket);
					} else {
						// �ظ��ĳ�����
						msg3.add(3);
						msg3.add(myTerm);
						msg3.add(false);
						msg3.add(null);
						msg3.add("agingTerm");
						msg3.add(Node.getInstance().nodeId);
						msg3.add(null);
						String massage3 = JSON.ArrayToJSON(msg3);
						SendTask task = new SendTask(msgSocket, massage3);
						ThreadPool.getInstance().addTasks(task);// �ظ�log������Ϣ
					}

				} else if (msgType == 3) {
					// ��������
				} else if (msgType == 4) {
					TimerThread.getInstance().reset_timer();// ���ü�ʱ��
					++Node.getInstance().server.currentTerm;// �����Լ���term
					Node.getInstance().server.status = 1;// �Լ�����Ϊcandidate
					Node.getInstance().server.grantNum = 1;// ����grantNum��׼��ѡ��

					int myTerm = Node.getInstance().server.currentTerm;
					int myLastLogIndex = Node.getInstance().server.log.get_lastLogIndex();
					int myLastLogTerm = Node.getInstance().server.log.get_lastLogTerm();

					List<Object> msg0 = new ArrayList<Object>();
					msg0.add(0);
					msg0.add(myTerm);
					msg0.add(myLastLogIndex);
					msg0.add(myLastLogTerm);
					String massage0 = JSON.ArrayToJSON(msg0);
					SocketList.getInstance().broadcast(massage0);// �㲥ѡ����Ϣ
				} else if (msgType == 6) {
					List<Object> msg7 = new ArrayList<Object>();
					msg7.add(7);
					msg7.add(false);
					String massage7 = JSON.ArrayToJSON(msg7);
					SendTask task = new SendTask(msgSocket, massage7);
					ThreadPool.getInstance().addTasks(task);// �ظ��ͻ��ˣ��Ҵ���
				} else if (msgType == 8) {
					List<Object> msg7 = new ArrayList<Object>();
					msg7.add(7);
					msg7.add(false);
					String massage7 = JSON.ArrayToJSON(msg7);
					SendTask task = new SendTask(msgSocket, massage7);
					ThreadPool.getInstance().addTasks(task);// �ظ��ͻ��ˣ��Ҵ���
				}
			} else if (status == 1) {// ��ǰ��candidate
				if (msgType == 0) {
					int term = (int) msg.get(1);
					int lastLogIndex = (int) msg.get(2);
					int lastLogTerm = (int) msg.get(3);

					int myTerm = Node.getInstance().server.currentTerm;
					int myLastLogIndex = Node.getInstance().server.log.get_lastLogIndex();
					int myLastLogTerm = Node.getInstance().server.log.get_lastLogTerm();

					List<Object> msg1 = new ArrayList<Object>();// ѡƱ��ִ
					if (term > myTerm) {
						if (lastLogTerm > myLastLogTerm
								|| lastLogTerm == myLastLogTerm && lastLogIndex >= myLastLogIndex) {
							Node.getInstance().server.status = 0;// �Լ�����Ϊfollower
							Node.getInstance().server.currentTerm = term;// �޸��Լ���term
							TimerThread.getInstance().reset_timer();// ���ü�ʱ��
							// ����ѡƱ��ִ
							msg1.add(1);
							msg1.add(term);
							msg1.add(true);
						} else {
							// ����ѡƱ��ִ
							msg1.add(1);
							msg1.add(term);
							msg1.add(false);
						}
					} else {
						// ����ѡƱ��ִ
						msg1.add(1);
						msg1.add(myTerm);
						msg1.add(false);
					}
					String massage1 = JSON.ArrayToJSON(msg1);
					SendTask task = new SendTask(msgSocket, massage1);
					ThreadPool.getInstance().addTasks(task);// ��ִѡƱ
				} else if (msgType == 1) {
					int term = (int) msg.get(1);
					boolean grant = (boolean) msg.get(2);
					if (grant) {// ͬ���Լ���leader
						++Node.getInstance().server.grantNum;
						if (Node.getInstance().server.grantNum > Node.getInstance().nodeAddrListSize / 2) {
							Node.getInstance().server.status = 2;// �Լ���Ϊleader
							TimerThread.getInstance().reset_leaderTimer();// ����Leader��ʱ��
							for (int i = 0; i < Node.getInstance().nodeAddrListSize; ++i) {// ��ʼ��nextIndex[]
								Node.getInstance().server.nextIndex[i] = Node.getInstance().server.log
										.get_lastLogIndex() + 1;
							}
							System.out.println("i am leader");
						}
					} else {
						if (term > Node.getInstance().server.currentTerm) {
							Node.getInstance().server.status = 0;// �Լ�����Ϊfollower
							Node.getInstance().server.currentTerm = term;
						}
					}
				} else if (msgType == 2) {
					// log����
					int term = (int) msg.get(1);
					int myTerm = Node.getInstance().server.currentTerm;

					List<Object> msg3 = new ArrayList<Object>();
					if (term >= myTerm) {
						Node.getInstance().server.status = 0;// �Լ�����Ϊfollower
						Node.getInstance().server.currentTerm = term;// �޸��Լ���term
						TimerThread.getInstance().reset_timer();// ���ü�ʱ��
						// ��ʼlog���ƹ���
						logCopy(msg, msgSocket);
					} else {
						// �ظ��ĳ�����
						msg3.add(3);
						msg3.add(myTerm);
						msg3.add(false);
						msg3.add(null);
						msg3.add("agingTerm");
						msg3.add(Node.getInstance().nodeId);
						msg3.add(null);
						String massage3 = JSON.ArrayToJSON(msg3);
						SendTask task = new SendTask(msgSocket, massage3);
						ThreadPool.getInstance().addTasks(task);// �ظ�log������Ϣ
					}

				} else if (msgType == 3) {
					// ��������
				} else if (msgType == 4) {
					TimerThread.getInstance().set_timer();// �����ʱ��
					++Node.getInstance().server.currentTerm;// ����term
					Node.getInstance().server.grantNum = 1;// ����grantNum��׼������ѡ��

					int myTerm = Node.getInstance().server.currentTerm;
					int myLastLogIndex = Node.getInstance().server.log.get_lastLogIndex();
					int myLastLogTerm = Node.getInstance().server.log.get_lastLogTerm();

					List<Object> msg0 = new ArrayList<Object>();
					msg0.add(0);
					msg0.add(myTerm);
					msg0.add(myLastLogIndex);
					msg0.add(myLastLogTerm);
					String massage0 = JSON.ArrayToJSON(msg0);
					SocketList.getInstance().broadcast(massage0);// �㲥ѡ����Ϣ
				} else if (msgType == 6) {
					List<Object> msg7 = new ArrayList<Object>();
					msg7.add(7);
					msg7.add(false);
					String massage7 = JSON.ArrayToJSON(msg7);
					SendTask task = new SendTask(msgSocket, massage7);
					ThreadPool.getInstance().addTasks(task);// �ظ��ͻ��ˣ��Ҵ���
				} else if (msgType == 8) {
					List<Object> msg7 = new ArrayList<Object>();
					msg7.add(7);
					msg7.add(false);
					String massage7 = JSON.ArrayToJSON(msg7);
					SendTask task = new SendTask(msgSocket, massage7);
					ThreadPool.getInstance().addTasks(task);// �ظ��ͻ��ˣ��Ҵ���
				}
			} else {// ��ǰ��leader
				if (msgType == 0) {
					int term = (int) msg.get(1);
					int lastLogIndex = (int) msg.get(2);
					int lastLogTerm = (int) msg.get(3);

					int myTerm = Node.getInstance().server.currentTerm;
					int myLastLogIndex = Node.getInstance().server.log.get_lastLogIndex();
					int myLastLogTerm = Node.getInstance().server.log.get_lastLogTerm();

					List<Object> msg1 = new ArrayList<Object>();// ѡƱ��ִ
					if (term > myTerm) {
						if (lastLogTerm > myLastLogTerm
								|| lastLogTerm == myLastLogTerm && lastLogIndex >= myLastLogIndex) {
							Node.getInstance().server.status = 0;// �Լ�����Ϊfollower
							SocketList.getInstance().informClientClientClose();
							Node.getInstance().server.currentTerm = term;// �޸��Լ���term
							TimerThread.getInstance().reset_timer();// ���ü�ʱ��
							// ����ѡƱ��ִ
							msg1.add(1);
							msg1.add(term);
							msg1.add(true);
						} else {
							// ����ѡƱ��ִ
							msg1.add(1);
							msg1.add(term);
							msg1.add(false);
						}
					} else {
						// ����ѡƱ��ִ
						msg1.add(1);
						msg1.add(myTerm);
						msg1.add(false);
					}
					String massage1 = JSON.ArrayToJSON(msg1);
					SendTask task = new SendTask(msgSocket, massage1);
					ThreadPool.getInstance().addTasks(task);// ��ִѡƱ
				} else if (msgType == 1) {
					// ��������
				} else if (msgType == 2) {
					// log����
					int term = (int) msg.get(1);
					int myTerm = Node.getInstance().server.currentTerm;

					List<Object> msg3 = new ArrayList<Object>();
					if (term >= myTerm) {
						Node.getInstance().server.status = 0;// �Լ�����Ϊfollower
						SocketList.getInstance().informClientClientClose();
						Node.getInstance().server.currentTerm = term;// �޸��Լ���term
						TimerThread.getInstance().reset_timer();// ���ü�ʱ��
						// ��ʼlog���ƹ���
						logCopy(msg, msgSocket);
					} else {
						// �ظ��ĳ�����
						msg3.add(3);
						msg3.add(myTerm);
						msg3.add(false);
						msg3.add(null);
						msg3.add("agingTerm");
						msg3.add(Node.getInstance().nodeId);
						msg3.add(null);
						String massage3 = JSON.ArrayToJSON(msg3);
						SendTask task = new SendTask(msgSocket, massage3);
						ThreadPool.getInstance().addTasks(task);// �ظ�log������Ϣ
					}

				} else if (msgType == 3) {
					// ����log���ƵĻظ�
					int term = (int) msg.get(1);
					boolean success = (boolean) msg.get(2);
					int followerIdx = (int) msg.get(5);
					if (success) {
						int matchIndex = (int) msg.get(3);
						Node.getInstance().server.nextIndex[followerIdx] = matchIndex + 1;
						Node.getInstance().server.matchIndex[followerIdx] = matchIndex;
					} else {
						String failReason = (String) msg.get(4);
						if (failReason.equals("agingTerm")) {// �ĳ�������
							Node.getInstance().server.status = 0;// �Լ�����Ϊfollower
							SocketList.getInstance().informClientClientClose();
							Node.getInstance().server.currentTerm = term;// �޸��Լ���term
						} else {
							int suggestNextIndex = (int) msg.get(6);
							Node.getInstance().server.nextIndex[followerIdx] = suggestNextIndex;
						}
					}
				} else if (msgType == 4) {
					TimerThread.getInstance().reset_leaderTimer();// ����leader��ʱ��
					// ����log������Ϣ
					int myNodeIdx = Node.getInstance().nodeId;
					for (int i = 0; i < Node.getInstance().nodeAddrListSize; ++i) {
						if (i != myNodeIdx) {
							List<Object> msg2 = new ArrayList<Object>();
							int nextIndex = Node.getInstance().server.nextIndex[i];
							int myLastLogIndex = Node.getInstance().server.log.get_lastLogIndex();

							int myTerm = Node.getInstance().server.currentTerm;
							int prevLogIndex = nextIndex - 1;
							int prevLogTerm = Node.getInstance().server.log.get_logByIndex(prevLogIndex).term;
							int leaderCommit = Node.getInstance().server.log.commitIndex;

							int endIndex = myLastLogIndex - nextIndex > 50 ? nextIndex + 50 : myLastLogIndex;
							int entriesNum = endIndex - nextIndex + 1 > 0 ? endIndex - nextIndex + 1 : 0;
							msg2.add(2);
							msg2.add(myTerm);
							msg2.add(prevLogIndex);
							msg2.add(prevLogTerm);
							msg2.add(leaderCommit);
							msg2.add(entriesNum);
							for (int j = nextIndex; j <= endIndex; ++j) {
								msg2.add(Node.getInstance().server.log.get_logByIndex(j).command);
							}
							for (int j = nextIndex; j <= endIndex; ++j) {
								msg2.add(Node.getInstance().server.log.get_logByIndex(j).commandId);
							}
							for (int j = nextIndex; j <= endIndex; ++j) {
								msg2.add(Node.getInstance().server.log.get_logByIndex(j).term);
							}

							String massage2 = JSON.ArrayToJSON(msg2);
							String remoteAddr = Node.getInstance().get_address(i);
							ConcurrentSocket cs = SocketList.getInstance().querySocket(remoteAddr);
							if (cs != null) {
								SendTask task = new SendTask(cs, massage2);
								ThreadPool.getInstance().addTasks(task);// ����log������Ϣ
							}
						}
					}
				} else if (msgType == 6) {
					List<Object> msg7 = new ArrayList<Object>();
					msg7.add(7);
					msg7.add(true);
					String massage7 = JSON.ArrayToJSON(msg7);
					SendTask task = new SendTask(msgSocket, massage7);
					ThreadPool.getInstance().addTasks(task);// �ظ��ͻ��ˣ��Ҷ���
				} else if (msgType == 8) {
					int myTerm = Node.getInstance().server.currentTerm;
					boolean read = (boolean) msg.get(1);
					String command = (String) msg.get(2);
					String commandId = (String) msg.get(3);
					SocketList.getInstance().move_clientSocket(msgSocket, commandId);
					if (!read) {// ����read��Ϣ
						Node.getInstance().server.log.add_logEntry(myTerm, command, commandId);// ��log�м����µ�logEntry
					} else {
						QueryTask task = new QueryTask(command, commandId);
						ThreadPool.getInstance().addTasks(task);// ��ѯ
					}
				}
			}
		}
	}

	private void logCopy(List<Object> msg, ConcurrentSocket msgSocket) {
		int term = (int) msg.get(1);
		int prevLogIndex = (int) msg.get(2);
		int prevLogTerm = (int) msg.get(3);
		int leaderCommit = (int) msg.get(4);
		int entriesNum = (int) msg.get(5);

		if (Node.getInstance().server.log.get_logByIndex(prevLogIndex) != null
				&& Node.getInstance().server.log.get_logByIndex(prevLogIndex).term == prevLogTerm) {

			Node.getInstance().server.log.delete_logEntry(prevLogIndex + 1);// ���Լ�log��prevLogIndex֮�������ɾ����

			for (int entriesIdx = 6; entriesIdx <= 5 + entriesNum; ++entriesIdx) {
				String command = (String) msg.get(entriesIdx);
				String commandId = (String) msg.get(entriesIdx + entriesNum);
				int logTerm = (int) msg.get(entriesIdx + 2 * entriesNum);

				Node.getInstance().server.log.add_logEntry(logTerm, command, commandId);
			} // ���Լ���log������µ�entries

			// ���ͳɹ���Ϣ
			List<Object> msg3 = new ArrayList<Object>();
			msg3.add(3);
			msg3.add(term);
			msg3.add(true);
			msg3.add(Node.getInstance().server.log.get_lastLogIndex());
			msg3.add(null);
			msg3.add(Node.getInstance().nodeId);
			msg3.add(null);
			String massage3 = JSON.ArrayToJSON(msg3);
			SendTask task = new SendTask(msgSocket, massage3);
			ThreadPool.getInstance().addTasks(task);

			if (leaderCommit > Node.getInstance().server.log.commitIndex) {
				Node.getInstance().server.log.commitIndex = Math.min(leaderCommit,
						Node.getInstance().server.log.get_lastLogIndex());
			}
		} else {
			// �ظ�log��ƥ��
			List<Object> msg3 = new ArrayList<Object>();
			msg3.add(3);
			msg3.add(term);
			msg3.add(false);
			msg3.add(null);
			msg3.add("noMatching");
			msg3.add(Node.getInstance().nodeId);

			// ����leader��һ�γ���ʹ�õ�nextIndex
			if (Node.getInstance().server.log.get_logByIndex(prevLogIndex) == null) {
				msg3.add(Node.getInstance().server.log.get_lastLogIndex() + 1);
			} else {
				msg3.add(prevLogIndex);
			}

			String massage3 = JSON.ArrayToJSON(msg3);
			SendTask task = new SendTask(msgSocket, massage3);
			ThreadPool.getInstance().addTasks(task);
		}
	}

}
