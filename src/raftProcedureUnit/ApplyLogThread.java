package raftProcedureUnit;

import java.util.ArrayList;
import java.util.List;

import communicationUnit.ConcurrentSocket;
import communicationUnit.SendTask;
import communicationUnit.SocketList;
import communicationUnit.ThreadPool;
import serverUnit.Node;
import util.DBpool;
import util.JSON;

public class ApplyLogThread implements Runnable {

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (Node.getInstance().server.status == 2) {// ��ǰ�ڵ���leader
				int NodeNum = Node.getInstance().nodeAddrListSize;
				int leaderIdx = Node.getInstance().nodeId;
				int N = Node.getInstance().server.log.get_lastLogIndex();
				boolean findN = false;
				while (!findN) {// Ѱ��N��ʹ��N>CommitIndex�Ҵ󲿷�matchIndex>=N
					int counter = 1;
					for (int i = 0; i < NodeNum; ++i) {
						if (i != leaderIdx) {
							if (Node.getInstance().server.matchIndex[i] >= N) {
								++counter;
							}
						}
					}
					if (counter > NodeNum / 2) {
						findN = true;
					} else {
						--N;
					}
				}
				Node.getInstance().server.log.commitIndex = Math.max(N, Node.getInstance().server.log.commitIndex);
			}

			// ��AppliedIndex֮��CommitIndex֮ǰ������CommitIndex�����ֵ�log�ύ�����ݿ⣬ͬʱ����CommandId�ظ���Ӧ��Client
			while (Node.getInstance().server.log.appliedIndex < Node.getInstance().server.log.commitIndex) {
				++Node.getInstance().server.log.appliedIndex;

				// ������log�洢���־û��洢����
				int term = Node.getInstance().server.log
						.get_logByIndex(Node.getInstance().server.log.appliedIndex).term;
				String command = Node.getInstance().server.log
						.get_logByIndex(Node.getInstance().server.log.appliedIndex).command;
				String commandId = Node.getInstance().server.log
						.get_logByIndex(Node.getInstance().server.log.appliedIndex).commandId;
				int logIndex = Node.getInstance().server.log
						.get_logByIndex(Node.getInstance().server.log.appliedIndex).index;

				// ���������Ϣû���ύ������ֹ�ظ��ύ��
				if (!Node.getInstance().server.log.checkAppliedBefore(commandId)) {
					String queryString = "insert into log(logIndex, term, command, commandId) values(" + logIndex + ","
							+ term + ",'" + command + "','" + commandId + "')";
					DBpool.getInstance().executeUpdate(queryString);
					// �ύappliedIndexָ���log
					System.out.println("apply: " + command);
				}

				if (Node.getInstance().server.status == 2) {
					// ���ύ��������logEntry�е�CommandId�ҵ���Ӧ��clientSocket����
					ConcurrentSocket socket = SocketList.getInstance().querySocket(commandId);
					if (socket != null) {
						List<Object> msg9 = new ArrayList<Object>();
						msg9.add(9);
						msg9.add("ok");

						String massage9 = JSON.ArrayToJSON(msg9);
						SendTask task = new SendTask(socket, massage9);
						ThreadPool.getInstance().addTasks(task);
					}
				}
			}
		}
	}
}
