package connectionMaintenanceUnit;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import communicationUnit.ConcurrentSocket;
import communicationUnit.RecvTask;
import communicationUnit.SendTask;
import communicationUnit.SocketList;
import communicationUnit.ThreadPool;
import serverUnit.Node;
import util.JSON;


public class HelloThread implements Runnable {

	@Override
	public void run() {
		List<String> nodeList = Node.getInstance().get_initiativeConnectAddress();
		for (String addr : nodeList) {
			String[] ip_port = addr.split(":");
			String ip = ip_port[0];
			int port = Integer.parseInt(ip_port[1]);
			try {
				Socket socket = new Socket(ip, port);// �ɹ�����Զ�˷�����
				ConcurrentSocket cs = new ConcurrentSocket(socket);
				SocketList.getInstance().add_helloSocket(cs, addr);// ��socket���뵽SocketList��ȥ
				
				// ��ȡ�Լ���ipport, �����������������ַ��Ϣ
				List<Object> msg5 = new ArrayList<Object>();
				int type = 5;
				String myIpport = Node.getInstance().get_myAddress();
				msg5.add(type);
				msg5.add(myIpport);
				String massage = JSON.ArrayToJSON(msg5);
				SendTask sendTask = new SendTask(cs, massage);
				ThreadPool.getInstance().addTasks(sendTask);// ��Զ�˷����������Լ��ĵ�ַ
				
				// ��cs������뵽�̳߳���
				RecvTask recvTask = new RecvTask(cs);
				ThreadPool.getInstance().addTasks(recvTask);
				
				System.out.println("socket����Զ�˳ɹ���address:" + addr);
			} catch (IOException e) {
				System.out.println("�޷������Ͻڵ㣬��ַΪ��" + addr);
				SocketList.getInstance().add_helloSocket(null, addr);
			}
		}
		while (true) {
			try {
				Thread.sleep(1000 * 10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			SocketList.getInstance().reborn_socket();
		}
	}

}
