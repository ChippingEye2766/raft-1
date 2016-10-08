package raftProcedureUnit;

import java.util.ArrayList;
import java.util.List;

import communicationUnit.ConcurrentSocket;
import communicationUnit.SendTask;
import communicationUnit.SocketList;
import communicationUnit.ThreadPool;
import serverUnit.Node;
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
				int N = Node.getInstance().server.log.size() - 1;
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
				Node.getInstance().server.commitIndex = Math.max(N, Node.getInstance().server.commitIndex);
			}

			// ��AppliedIndex֮��CommitIndex֮ǰ������CommitIndex�����ֵ�log�ύ�����ݿ⣬ͬʱ����CommandId�ظ���Ӧ��Client
			while (Node.getInstance().server.appliedIndex < Node.getInstance().server.commitIndex) {
				++Node.getInstance().server.appliedIndex;
				// �ύappliedIndexָ���log����Ҫ�鿴�Ƿ��ظ��ύ��
				System.out.println(
						"apply: " + Node.getInstance().server.log.get(Node.getInstance().server.appliedIndex).command);
				if (Node.getInstance().server.status == 2) {
					// ���ύ��������logEntry�е�CommandId�ҵ���Ӧ��clientSocket����
					String commandId = Node.getInstance().server.log
							.get(Node.getInstance().server.appliedIndex).commandId;
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
