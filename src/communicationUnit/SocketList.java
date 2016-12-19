package communicationUnit;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import serverUnit.Node;
import util.JSON;

public class SocketList {
	private Logger log = Logger.getLogger(SocketList.class);
	// ����
	private static SocketList instance = new SocketList();

	private SocketList() {
		this.helloSocketMap = new HashMap<String, ConcurrentSocket>();
		this.welcomeSocketMap = new HashMap<String, ConcurrentSocket>();
		this.clientSocketMap = new HashMap<String, ConcurrentSocket>();
	}

	public static SocketList get_instance() {
		return instance;
	}

	private final Map<String, ConcurrentSocket> helloSocketMap;// Զ��ip:port-socket�ԣ��������ӵ�socket��
	private final Map<String, ConcurrentSocket> welcomeSocketMap;// Զ��ip:port-socket�ԣ����������socket��
	private final Map<String, ConcurrentSocket> clientSocketMap;// clientָ��id-socket��

	public synchronized ConcurrentSocket querySocket(String key) {// ����key��ȡsocket
		if (helloSocketMap.get(key) != null)
			return helloSocketMap.get(key);
		if (welcomeSocketMap.get(key) != null)
			return welcomeSocketMap.get(key);
		return clientSocketMap.get(key);
	}

	public synchronized String queryAddr(ConcurrentSocket socket) {// ����socketѰ��Զ�˷�������ַ
		for (String ipport : helloSocketMap.keySet()) {
			if (helloSocketMap.get(ipport).equals(socket)) {
				return ipport;
			}
		}
		for (String ipport : welcomeSocketMap.keySet()) {
			if (welcomeSocketMap.get(ipport).equals(socket)) {
				return ipport;
			}
		}
		return null;
	}

	public synchronized void add_helloSocket(ConcurrentSocket socket, String ipport) {
		helloSocketMap.put(ipport, socket);
	}

	public synchronized void remove_socket(ConcurrentSocket socket) {
		for (String ipport : helloSocketMap.keySet()) {
			if (helloSocketMap.get(ipport) != null && helloSocketMap.get(ipport).equals(socket)) {
				helloSocketMap.put(ipport, null);
				socket.close();
				return;
			}
		}
		for (String ipport : welcomeSocketMap.keySet()) {
			if (welcomeSocketMap.get(ipport) != null && welcomeSocketMap.get(ipport).equals(socket)) {
				welcomeSocketMap.put(ipport, null);
				socket.close();
				return;
			}
		}
		for (String cmdId : clientSocketMap.keySet()) {
			if (clientSocketMap.get(cmdId) != null && clientSocketMap.get(cmdId).equals(socket)) {
				clientSocketMap.remove(cmdId);
				socket.close();
				return;
			}
		}
	}

	public synchronized void move_welcomeSocket(ConcurrentSocket socket, String ipport) {// ��welcomeSocket����welcomeSocketMap��
		welcomeSocketMap.put(ipport, socket);
	}

	public synchronized void move_clientSocket(ConcurrentSocket socket, String cmdId) {// ��clientSocket���뵽clientSocketMap��
		clientSocketMap.put(cmdId, socket);
	}

	public synchronized void broadcast(String msg) {// �㲥��Ϣ
		for (String ipport : helloSocketMap.keySet()) {
			ConcurrentSocket socket = helloSocketMap.get(ipport);
			if (socket != null) {
				SendTask task = new SendTask(socket, msg);
				ThreadPool.get_instance().add_tasks(task);
			}
		}
		for (String ipport : welcomeSocketMap.keySet()) {
			ConcurrentSocket socket = welcomeSocketMap.get(ipport);
			if (socket != null) {
				SendTask task = new SendTask(socket, msg);
				ThreadPool.get_instance().add_tasks(task);
			}
		}
	}

	public synchronized void reborn_socket() {
		for (String ipport : helloSocketMap.keySet()) {
			if (helloSocketMap.get(ipport) == null) {
				String ip = ipport.split(":")[0];
				int port = Integer.parseInt(ipport.split(":")[1]);

				try {
					Socket socket = new Socket(ip, port);// ��������Զ��
					ConcurrentSocket cs = new ConcurrentSocket(socket);
					add_helloSocket(cs, ipport);

					// ��ȡ�Լ���ipport, �����������������ַ��Ϣ
					List<Object> msg5 = new ArrayList<Object>();
					int type = 5;
					String myIpport = Node.get_instance().get_myAddress();
					msg5.add(type);
					msg5.add(myIpport);
					String massage = JSON.ArrayToJSON(msg5);
					SendTask sendTask = new SendTask(cs, massage);
					ThreadPool.get_instance().add_tasks(sendTask);// ��Զ�˷����������Լ��ĵ�ַ

					// ��cs������뵽�̳߳���
					RecvTask recvTask = new RecvTask(cs);
					ThreadPool.get_instance().add_tasks(recvTask);
				} catch (IOException e) {
					log.info("����socketʧ�ܣ�Զ��host�� " + ip + ":" + port);
				}
			}
		}
	}
	
	public synchronized void informClientClientClose() {// ֪ͨ���е�client�ر�socket
		for (String key : clientSocketMap.keySet()) {
			ConcurrentSocket socket = clientSocketMap.get(key);
			List<Object> msg7 = new ArrayList<Object>();
			msg7.add(7);
			msg7.add(false);
			String massage7 = JSON.ArrayToJSON(msg7);
			SendTask task = new SendTask(socket, massage7);
			ThreadPool.get_instance().add_tasks(task);// �ظ��ͻ��ˣ��Ҵ���
		}
	}
}
