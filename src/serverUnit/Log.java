package serverUnit;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import util.DBpool;

public class Log {
	private LinkedList<LogEntry> log;
	private LinkedList<String> recentAppliedCmd;// ����ύ��������
	private boolean indexCacheDirty = false;
	private boolean termCacheDirty = false;
	private int lastLogIndexCache = -1;
	private int lastLogTermCache = -1;

	public int commitIndex;
	public int appliedIndex;

	public Log() {
		// ������ݿ��Ƿ���log�����û�оʹ���һ��
		String build_logTable = "CREATE TABLE IF NOT EXISTS `log` ( `logIndex` int(11) DEFAULT NULL, `term` int(11) DEFAULT NULL, `command` varchar(1024) DEFAULT NULL, `commandId` varchar(64) DEFAULT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8";
		DBpool.getInstance().executeUpdate(build_logTable);

		this.log = new LinkedList<LogEntry>();

		String queryString = "select max(logIndex) as lastLogIndex from log";
		Object o = DBpool.getInstance().executeQuery(queryString).get(0).get("lastLogIndex");
		lastLogIndexCache = o == null ? 0 : (Integer) o;

		queryString = "select term from log where logIndex = " + lastLogIndexCache;
		lastLogTermCache = DBpool.getInstance().executeQuery(queryString).isEmpty() ? 1
				: (Integer) DBpool.getInstance().executeQuery(queryString).get(0).get("term");

		this.commitIndex = get_lastLogIndex();
		this.appliedIndex = this.commitIndex;

		// ��������ύ����log��commandId
		this.recentAppliedCmd = new LinkedList<String>();
		int temp = lastLogIndexCache - 10;
		queryString = "select logIndex, commandId from log where logIndex > " + temp + " order by logIndex asc";
		List<Map<String, Object>> results = DBpool.getInstance().executeQuery(queryString);
		for (Map<String, Object> row : results) {
			this.recentAppliedCmd.add((String) row.get("commandId"));
		}
	}

	public synchronized int get_lastLogIndex() {// ���ܻ���ʳ־û��洢
		if (indexCacheDirty) {
			if (log.isEmpty()) {
				String queryString = "select max(logIndex) as lastLogIndex from log";
				Object o = DBpool.getInstance().executeQuery(queryString).get(0).get("lastLogIndex");
				lastLogIndexCache = o == null ? 0 : (Integer) o;
			} else {
				lastLogIndexCache = log.peekLast().index;
			}
			indexCacheDirty = false;
		}

		return lastLogIndexCache;
	}

	public synchronized int get_lastLogTerm() {// ���ܻ���ʳ־û��洢
		if (termCacheDirty) {
			if (log.isEmpty()) {
				String queryString = "select term from log where logIndex = " + get_lastLogIndex();
				lastLogTermCache = DBpool.getInstance().executeQuery(queryString).isEmpty() ? 1
						: (Integer) DBpool.getInstance().executeQuery(queryString).get(0).get("term");
			} else {
				lastLogTermCache = log.peekLast().term;
			}
			termCacheDirty = false;
		}

		return lastLogTermCache;
	}

	public synchronized LogEntry get_logByIndex(int index) {// ���ܻ���ʳ־û��洢
		if (index == 0) {
			return new LogEntry(0, null, null, 0);
		}
		if (log.isEmpty() || log.peekFirst().index > index) {
			// �����ݿ�log����Ѱ�������Ϣ
			String queryString = "select * from log where logIndex = " + index;
			List<Map<String, Object>> entryList = DBpool.getInstance().executeQuery(queryString);
			if (entryList.isEmpty()) {
				return null;
			} else {
				Map<String, Object> entry = entryList.get(0);
				return new LogEntry((Integer) entry.get("term"), (String) entry.get("command"),
						(String) entry.get("commandId"), (Integer) entry.get("logIndex"));
			}
		} else {
			for (LogEntry logEntry : log) {
				if (logEntry.index == index) {
					return logEntry;
				}
			}
		}
		return null;
	}

	public synchronized void delete_logEntry(int begin) {// ɾ��index����ڵ���begin��log��
		Iterator<LogEntry> it = log.iterator();
		while (it.hasNext()) {
			LogEntry log = it.next();
			if (log.index >= begin) {
				it.remove();
			}
		}
		indexCacheDirty = true;
		termCacheDirty = true;
	}

	public synchronized void add_logEntry(int term, String command, String commandId) {
		int logIndex = get_lastLogIndex() + 1;
		LogEntry logEntry = new LogEntry(term, command, commandId, logIndex);
		log.add(logEntry);
		indexCacheDirty = true;
		termCacheDirty = true;
	}

	public synchronized void logClear() {// �������Ҫ��log
		Iterator<LogEntry> it = log.iterator();
		while (it.hasNext()) {
			if (it.next().index < appliedIndex / 2) {
				it.remove();
			}
		}
	}

	public boolean checkAppliedBefore(String commandId) {
		for (String cmdId : this.recentAppliedCmd) {
			if (cmdId.equals(commandId)) {
				return true;// ֮ǰ�Ѿ��ύ��
			}
		}
		if(this.recentAppliedCmd.size() >= 50) {
			this.recentAppliedCmd.removeFirst();
		}
		this.recentAppliedCmd.add(commandId);
		return false;// ֮ǰû���ύ��
	}
}
