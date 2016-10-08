package communicationUnit;

import java.io.IOException;

public class RecvTask implements Runnable {
	private ConcurrentSocket socket;

	public RecvTask(ConcurrentSocket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			while (true) {
				String msg = socket.read();// �õ���Ϣ

				if (!msg.isEmpty()) {// ��Ϣ�ǿ�
					Massage massage = new Massage(this.socket, msg);
					MassageQueue.getInstance().add_massage(massage);// ���뵽��Ϣ����
				} else {// ��client�˵�socket�ر���Ϣ
					System.out.println("�ͻ������ӶϿ�");
					this.socket.close();// ɾ�����뿪����
					break;// ����ѭ�����߳̽������task
				}
			}
		} catch (IOException e) {
			SocketList.getInstance().remove_socket(socket);
		}
	}
}
