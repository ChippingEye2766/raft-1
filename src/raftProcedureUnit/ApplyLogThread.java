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
import util.XML;

public class ApplyLogThread implements Runnable {
	private ApplyMethod applyMethod;
	
	public ApplyLogThread() {
		String className = new XML().get_applyMethod();
		try {
			Class<?> classObject = Class.forName(className);// ���ָ�����Class����
			applyMethod = (ApplyMethod)classObject.newInstance();// ����Ĭ�Ϲ��캯��
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (Node.get_instance().server.status == 2) {// ��ǰ�ڵ���leader
				int NodeNum = Node.get_instance().nodeAddrListSize;
				int leaderIdx = Node.get_instance().nodeId;
				int N = Node.get_instance().server.log.get_lastLogIndex();
				boolean findN = false;
				while (!findN) {// Ѱ��N��ʹ��N>CommitIndex�Ҵ󲿷�matchIndex>=N
					int counter = 1;
					for (int i = 0; i < NodeNum; ++i) {
						if (i != leaderIdx) {
							if (Node.get_instance().server.matchIndex[i] >= N) {
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
				Node.get_instance().server.log.commitIndex = Math.max(N, Node.get_instance().server.log.commitIndex);
			}

			// ��AppliedIndex֮��CommitIndex֮ǰ������CommitIndex�����ֵ�log�ύ�����ݿ⣬ͬʱ����CommandId�ظ���Ӧ��Client
			while (Node.get_instance().server.log.appliedIndex < Node.get_instance().server.log.commitIndex) {
				++Node.get_instance().server.log.appliedIndex;

				
				int term = Node.get_instance().server.log
						.get_logByIndex(Node.get_instance().server.log.appliedIndex).term;
				String command = Node.get_instance().server.log
						.get_logByIndex(Node.get_instance().server.log.appliedIndex).command;
				String commandId = Node.get_instance().server.log
						.get_logByIndex(Node.get_instance().server.log.appliedIndex).commandId;
				int logIndex = Node.get_instance().server.log
						.get_logByIndex(Node.get_instance().server.log.appliedIndex).index;

				// ���������Ϣû���ύ������ֹ�ظ��ύ��
				if (!Node.get_instance().server.log.checkAppliedBefore(commandId)) {
					String queryString = "insert into log(logIndex, term, command, commandId) values(" + logIndex + ","
							+ term + ",'" + command + "','" + commandId + "')";
					DBpool.getInstance().executeUpdate(queryString);// ������log�洢���־û��洢����
					applyMethod.apply(command);// �ύappliedIndexָ���log
				}

				if (Node.get_instance().server.status == 2) {
					// ���ύ��������logEntry�е�CommandId�ҵ���Ӧ��clientSocket����
					ConcurrentSocket socket = SocketList.get_instance().querySocket(commandId);
					if (socket != null) {
						List<Object> msg9 = new ArrayList<Object>();
						msg9.add(9);
						msg9.add("ok");

						String massage9 = JSON.ArrayToJSON(msg9);
						SendTask task = new SendTask(socket, massage9);
						ThreadPool.get_instance().add_tasks(task);
					}
				}
			}
		}
	}
}
