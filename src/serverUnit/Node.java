package serverUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;

import communicationUnit.ThreadPool;
import connectionMaintenanceUnit.HelloThread;
import connectionMaintenanceUnit.WelcomeThread;
import raftProcedureUnit.ApplyLogThread;
import raftProcedureUnit.MassageProcessThread;
import timerUnit.TimerThread;
import util.DBpool;
import util.XML;

public class Node {
	// 单例
	private static Node instance = new Node();

	@SuppressWarnings("unchecked")
	private Node() {
		Map<String, Object> conf = new XML().nodeConf();

		this.nodeAddrListSize = ((List<String>) (conf.get("ipport"))).size();
		this.nodeAddrList = new ArrayList<String>();
		for (String ipport : (List<String>) (conf.get("ipport"))) {
			this.nodeAddrList.add(ipport);
		}
		this.nodeId = Integer.parseInt((String) conf.get("nodeId"));
		this.server = new Server(nodeAddrListSize);
	}

	public static Node get_instance() {
		return instance;
	}// 单例

	public final int nodeAddrListSize;
	public final List<String> nodeAddrList;
	public final int nodeId;
	public final Server server;

	public void start() {
		PropertyConfigurator.configure("conf/log4j.properties");
		DBpool.getInstance();// 启动连接池
		ThreadPool.get_instance();// 启动线程池
		new Thread(new HelloThread()).start();
		new Thread(new WelcomeThread()).start();
		new Thread(TimerThread.get_instance()).start();
		new Thread(new MassageProcessThread()).start();
		new Thread(new ApplyLogThread()).start();
	}

	public List<String> get_initiativeConnectAddress() {
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < nodeId; ++i) {
			result.add(nodeAddrList.get(i));
		}
		return result;
	}

	public String get_myAddress() {
		return nodeAddrList.get(nodeId);
	}

	public String get_address(int idx) {
		return nodeAddrList.get(idx);
	}
}
