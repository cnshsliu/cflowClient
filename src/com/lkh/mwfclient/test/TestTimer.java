package com.lkh.mwfclient.test;

import junit.framework.TestCase;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;

import com.lkh.mwfclient.AWEClient;

public class TestTimer extends TestCase {
	// private static String hostname = "www.myworldflow.com";
	private static String hostname = "localhost:8080";
	private static Logger logger = Logger.getLogger(TestTimer.class);
	private static AWEClient client = AWEClient.newInstance("tester1", "tester1");

	@Before
	public void setUp() throws Exception {
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender appender = new ConsoleAppender(layout);
		logger.addAppender(appender);

		// String userJSON = getUserInfo().toString();
	}

	@After
	public void tearDown() throws Exception {
	}


	public void test_timer() throws Exception {
		cleanHouse();
		loadTestUsers();

		// 流程启动后首先到一个Timer, 再到id_2, 再到id_end; id_2由T004执行
		String wft = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
				+ "<node id=\"id_start\" type=\"start\" title=\"Start\" name=\"Start\" x=\"27\" y=\"213\">\n "
				+ "<next targetID=\"id_timer\"/></node>"
				+ "<node id=\"id_end\" type=\"end\" title=\"End\" name=\"End\" x=\"738\" y=\"213\">\n <next targetID=\"id_end\"/></node>"
				+ "<node id=\"id_afterTimer\" type=\"task\" title=\"Approve Leaving\" name=\"Approve Leaving\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n "
				+ "<taskto type=\"person\" whom=\"T004\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_end\"/>\n </node>"
				+ "<node id=\"id_timer\" type=\"timer\" title=\"Timer\" name=\"Timer\" yy=\"0\" mm=\"0\" dd=\"0\" hh=\"0\" mi=\"1\">"
				+ "<next targetID=\"id_afterTimer\"/>"
				+ "</node>"
				+ "</cf:workflow>";
		String wftid = client.uploadWft( wft, "testProcess_1");
		assertTrue(wftid != null);

		String teamid = client.createTeam( "test_team2",
				"test_team2_memo");
		JSONObject members = new JSONObject();
		members.put("T002", "Approver");
		// 启动进程
		String prcid = null;
		try {
			prcid = client.startWorkflow( "T001", wftid, teamid,
					"testProcess_1", null);

			// 取该进程的内容（JSON格式）
			JSONObject prcJson = client.getPrcInfo( prcid);
			assertEquals("running", prcJson.get("STATUS"));
			String thePrcId = (String) prcJson.get("PRCID");
			assertEquals(prcid, thePrcId);

			// 取得worklist, 找到属于该进程的那个work
			JSONArray wlist = null;
			for (int i = 0;; i++) {
				wlist = client.getWorklist( "T004", prcid);
				logger.info("T004 worklist item number [" + i + "]: "
						+ wlist.size());
				if (wlist.size() > 0)
					break;
				Thread.sleep(10000L);
			}
			JSONObject wii = getWorkitem(wlist, prcid, "id_afterTimer");

			client.doTask( "T004", prcid, (String) wii.get("NODEID"),
					(String) wii.get("SESSID"), null, null);

			// 再次看这个进程的状态，应该是完成状态
			prcJson = client.getPrcInfo( prcid);
			assertEquals("finished", prcJson.get("STATUS"));

		} finally {
			client.deleteProcess( prcid);
			client.deleteWft( wftid);
		}

		cleanHouse();

	}

	private void cleanHouse() {
		try {
			unloadTestUsers();

			String[] status = new String[3];
			status[0] = "running";
			status[1] = "finished";
			status[2] = "canceled";
			for (int s = 0; s < 3; s++) {
				JSONArray tmp = client.getProcessesByStatus( status[s]);
				for (int i = 0; i < tmp.size(); i++) {
					JSONObject prcInfo = (JSONObject) tmp.get(i);
					String tmpPrcId = (String) prcInfo.get("PRCID");
					String tmpWftId = (String) prcInfo.get("WFTID");
					client.deleteProcess( tmpPrcId);
					client.deleteWft( tmpWftId);
				}
			}

			JSONArray tmp = client.getWfts();
			for (int i = 0; i < tmp.size(); i++) {
				JSONObject info = (JSONObject) tmp.get(i);
				String tmpWftId = (String) info.get("WFTID");
				client.deleteWft( tmpWftId);
			}

			tmp = client.getTeams();
			for (int i = 0; i < tmp.size(); i++) {
				JSONObject team = (JSONObject) tmp.get(i);
				client.deleteTeamById( (String) team.get("ID"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadTestUsers() throws Exception {
		client.addUser( "T001", "T001", "T001@null.com", "GMT+08:00",
				"zh-CN", "E");
		client.addUser( "T002", "T002", "T002@null.com", "GMT+08:00",
				"zh-CN", "E");
		client.addUser( "T003", "T003", "T003@null.com", "GMT+08:00",
				"zh-CN", "E");
		client.addUser( "T004", "T004", "T004@null.com", "GMT+08:00",
				"zh-CN", "E");
		client.addUser( "T005", "T005", "T005@null.com", "GMT+08:00",
				"zh-CN", "E");
	}

	private void unloadTestUsers() throws Exception {
		client.deleteUser( "T001");
		client.deleteUser( "T002");
		client.deleteUser( "T003");
		client.deleteUser( "T004");
		client.deleteUser( "T005");
	}

	private JSONObject getWorkitem(JSONArray wlist, String prcId, String nodeId) {
		JSONObject wii = null;
		for (int i = 0; i < wlist.size(); i++) {
			JSONObject tmp = (JSONObject) wlist.get(i);
			if (tmp.get("PRCID").equals(prcId)
					&& tmp.get("NODEID").equals(nodeId)) {
				wii = tmp;
				break;
			}
		}
		return wii;
	}
}
