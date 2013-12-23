package com.lkh.mwfclient.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.HttpStatus;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;

import com.aliyun.openservices.ClientConfiguration;
import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.model.ObjectMetadata;
import com.lkh.mwfclient.AWEClient;
import com.lkh.mwfclient.MwfClientException;

public class UT1 extends TestCase {
	private static Logger logger = Logger.getLogger(UT1.class);
	private static AWEClient client = null;
	private static AWEClient client2 = null;

	@Before
	public void setUp() throws Exception {
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender appender = new ConsoleAppender(layout);
		logger.addAppender(appender);

	}

	@After
	public void tearDown() throws Exception {
	}

	public void test_user() throws Exception {

		// 登录两个开发者；
		client = AWEClient.newInstance("tester1", "tester1");
		client2 = AWEClient.newInstance("tester2", "tester2");

		JSONArray users = client.getAllUsers();
		int count = users.size();
		// 分别载入用户；

		loadTestUsers(client);
		loadTestUsers(client2);
		users = client.getAllUsers();
		assertTrue(users.size() >= count);
		// tester修改一个用户，取用户信息检查是否成功修改
		client.updateUser("T001", "T001_newName", "T001_newemail@null.com", "GMT+08:00", "zh_CN", "E");
		JSONObject tmp = client.getUserInfo("T001");
		assertEquals("T001_newName", tmp.get("NAME"));
		client.updateUser("T001", "T001", "T001_newemail@null.com", "GMT+08:00", "zh_CN", "E");
		tmp = client.getUserInfo("T001");
		assertEquals("T001", tmp.get("NAME"));
		// liukehong没有修改过，
		tmp = client.getUserInfo("T001");
		assertEquals("T001", tmp.get("NAME"));
		// liukehong删除所有测试用户
		unloadTestUsers(client);
		unloadTestUsers(client2);

	}

	public void ytest_oss_get() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FileOutputStream oups = new FileOutputStream(new File("C:\\cflow\\hello.xml"));
		String entityString = "";
		try {
			MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

			HttpClient httpClient = new HttpClient(connectionManager);
			String key = "vault/tester1/wft1.xml";
			String url = "http://oss.aliyuncs.com/cflow/" + key;
			GetMethod get = new GetMethod(url);
			get.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
			try {
				int statusCode = httpClient.executeMethod(get);
				if (statusCode != HttpStatus.SC_OK) {
					logger.error("Method failed: " + get.getStatusLine());
				}
				InputStream inps = get.getResponseBodyAsStream();
				inpsToOups(inps, oups);
				inps.close();
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
			} finally {
				get.releaseConnection();
			}
			entityString = baos.toString("UTF-8");
			oups.close();
		}// end try
		catch (Exception e) {
			e.printStackTrace();
		}// end catch

	}

	public void ytest_oss_put() throws Exception {
		FileOutputStream oups = new FileOutputStream(new File("C:\\cflow\\hello.xml"));
		File aFile = new File("C:\\cflow\\test.html");
		FileInputStream inps = new FileInputStream(aFile);
		String entityString = "";
		try {

			ObjectMetadata objMeta = new ObjectMetadata();
			objMeta.setContentLength(aFile.length());
			objMeta.setContentType("text/html");
			String path = "vault/tester1/test.html";
			ClientConfiguration config = new ClientConfiguration();
			OSSClient ossClient = new OSSClient("http://oss.aliyuncs.com/", "3mkbjle9tr4it3spn6wv06ki", "tC3bar7CuhEL97bIJ5g0jSScH5Y=", config);
			ossClient.putObject("cflow", path, inps, objMeta);

		}// end try
		catch (Exception e) {
			e.printStackTrace();
		}// end catch

	}

	public void test_wft() throws Exception {
		client = AWEClient.newInstance("tester1", "tester1");

		String wft = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
				+ "<node id=\"58DE1222-4446-C2DE-1F8B-CA9EBE43C965\" type=\"start\" title=\"Start\" name=\"Start\" x=\"27\" y=\"213\">\n <next targetID=\"5094DF39-F83A-BE01-3FE0-CA9EC768D9B7\"/></node>" + "<node id=\"7E40C95C-333D-964E-2EA3-CA9EBE457AE1\" type=\"end\" title=\"End\" name=\"End\" x=\"738\" y=\"213\">\n <next targetID=\"7E40C95C-333D-964E-2EA3-CA9EBE457AE1\"/></node>"
				+ "<node id=\"5094DF39-F83A-BE01-3FE0-CA9EC768D9B7\" type=\"task\" title=\"Task\" name=\"Task\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n <taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"7E40C95C-333D-964E-2EA3-CA9EBE457AE1\"/>\n </node>" + "</cf:workflow>";
		String wftname = "rstest1";

		String wftid = client.uploadWft(wft, wftname);
		logger.info(wftid);
		assertFalse(wftid.startsWith("ERROR"));

		String theWft = client.getWftDoc(wftid);
		assertEquals(theWft, wft);

		JSONArray wfts = client.getWfts();
		assertTrue(wfts.size() > 0);

		// 依次删除新建的这些模板
		for (int i = 0; i < wfts.size(); i++) {
			JSONObject aWft = (JSONObject) wfts.get(i);
			if (aWft.get("WFTNAME").equals(wftname)) {
				String tmp = (String) aWft.get("WFTID");
				assertFalse(client.deleteWft(tmp).startsWith("ERROR"));
			}
		}
		wfts = client.getWfts();
		// 此时，未必>0, 所以用>=0
		assertTrue(wfts.size() >= 0);
		// 依次检查每个模板，并统计所以名字为测试名字的模板个数
		int numberOfTestWft = 0;
		for (int i = 0; i < wfts.size(); i++) {
			JSONObject aWft = (JSONObject) wfts.get(i);
			if (aWft.get("WFTNAME").equals(wftname)) {
				numberOfTestWft++;
			}
		}
		// 这个数字应该全部为零。
		assertTrue(numberOfTestWft == 0);

		// 测试取Example模板
		int numberOfWft = client.getWfts().size();
		String newWftId = client.getWftExample("Demo_AND");
		assertFalse(newWftId == null);
		JSONObject aWft = client.getWftInfo(newWftId);
		assertEquals(newWftId, aWft.get("WFTID"));
		assertTrue(client.getWfts().size() == numberOfWft + 1);
		client.deleteWft(newWftId);
		assertTrue(client.getWfts().size() == numberOfWft);

	}

	public void test_admin_teams() throws Exception {
		client = AWEClient.newInstance("tester1", "tester1");
		loadTestUsers(client);
		try {

			// 取得已有的Teams

			int oldTeamCount = client.getTeams().size();
			String teamid = client.createTeam("test_team1", "test_team1_memo");
			assertTrue(client.getTeams().size() >= oldTeamCount);

			// 根据ID取得Team对象
			JSONObject theTeam = client.getTeamById(teamid);
			assertTrue(theTeam != null);
			assertEquals(theTeam.get("NAME"), "test_team1");
			assertEquals(theTeam.get("ID"), teamid);

			assertFalse(client.deleteTeamById(teamid) == null);

			// 再次尝试取这个Team， 应失败
			theTeam = null;
			try {
				theTeam = client.getTeamById(teamid);
			} catch (Exception mwfex) {
				theTeam = null;
			}
			assertEquals(null, theTeam);

			teamid = client.createTeam("myTeam", "myTeam_Memo");
			JSONObject members = new JSONObject();
			members.put("T001", "Approver");
			members.put("T002", "Auditor");
			members.put("T003", "Approver");
			members.put("T004", "Auditor");
			client.addTeamMembers(teamid, members.toString());
			assertEquals(2, client.getTeamMembersByRole(teamid, "Approver").size());
			assertEquals(2, client.getTeamMembersByRole(teamid, "Auditor").size());
			assertEquals(0, client.getTeamMembersByRole(teamid, "Reviewer").size());
			int oldTeamSize = client.getTeamMembers(teamid).size();
			client.removeTeamMember(teamid, "T002");
			assertEquals(1, client.getTeamMembersByRole(teamid, "Auditor").size());
			assertTrue(oldTeamSize - 1 == client.getTeamMembers(teamid).size());
			client.removeTeamMember(teamid, "T001");
			client.removeTeamMember(teamid, "T002");
			client.removeTeamMember(teamid, "T003");
			client.removeTeamMember(teamid, "T004");
			client.removeTeamMember(teamid, "T005");
			assertTrue(0 == client.getTeamMembers(teamid).size());

			client.deleteTeamById(teamid);

			boolean found = true;
			try {
				client.getTeamById(teamid);
			} catch (Exception mwfex) {
				found = false;
			}
			assertEquals(false, found);
		} finally {
			unloadTestUsers(client);
		}

	}

	public void test_admin_VT() throws Exception {
		client = AWEClient.newInstance("tester1", "tester1");
		loadTestUsers(client);
		try {

			String content = "Hello World!";
			client.deleteVt("vt1");

			int oldVtCount = client.getVts().size();
			client.uploadVt(content, "vt1");
			assertEquals(oldVtCount, client.getVts().size() - 1);
			client.deleteVt("vt1");
			assertEquals(oldVtCount, client.getVts().size());
		} finally {
			unloadTestUsers(client);
		}
	}

	public void test_OR() throws Exception {
		client = AWEClient.newInstance("tester1", "tester1");
		loadTestUsers(client);
		try {

			String wft_OR = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
					+ "<node id=\"id_start\" type=\"start\" title=\"Start\" name=\"Start\" x=\"27\" y=\"213\">\n <next targetID=\"id_1\"/><next targetID=\"id_2\"/><next targetID=\"id_3\"/></node>" + "<node id=\"id_end\" type=\"end\" title=\"End\" name=\"End\" x=\"738\" y=\"213\">\n <next targetID=\"id_end\"/></node>"

					+ "<node id=\"id_1\" type=\"task\" title=\"ID_1\" name=\"ID_1\"  acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n " + "<taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n " + "<next targetID=\"id_OR\"/></node>"

					+ "<node id=\"id_2\" type=\"task\" title=\"ID_2\" name=\"ID_2\"  acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n " + "<taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n " + "<next targetID=\"id_OR\"/></node>"

					+ "<node id=\"id_3\" type=\"task\" title=\"ID_3\" name=\"ID_3\"  acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n " + "<taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n " + "<next targetID=\"id_OR\"/></node>"

					+ "<node id='id_OR' type='or'>" + "<next targetID='id_NULL'/>" + "</node>"

					+ "<node id=\"id_NULL\" type=\"task\" title=\"ID_NULL\" name=\"ID_NULL\"  acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n " + "<taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n " + "<next targetID=\"id_end\"/></node>"

					+ "</cf:workflow>";

			String wftid_OR = client.uploadWft(wft_OR, "testProcess_OR");
			assertFalse(wftid_OR.startsWith("ERROR"));

			JSONArray wlist = client.getWorklist("T001");
			int old_wlist_count = wlist.size();

			JSONObject ctx = new JSONObject();
			ctx.put("author", "liukehong");
			String prcid = null;
			try {
				prcid = client.startWorkflow("T001", wftid_OR, null, "instnacename_testProcess_OR", ctx);
				assertFalse(prcid.startsWith("ERROR"));
				try {

					// id_apply_leaving
					wlist = client.getWorklist("T001");
					assertTrue(wlist.size() == old_wlist_count + 3);
					JSONObject theWii_1 = getWorkitem(wlist, prcid, "id_1");
					assertTrue(theWii_1 != null);
					JSONObject theWii_2 = getWorkitem(wlist, prcid, "id_2");
					assertTrue(theWii_2 != null);
					JSONObject theWii_3 = getWorkitem(wlist, prcid, "id_3");
					assertTrue(theWii_3 != null);
					client.doTask("T001", prcid, (String) theWii_1.get("NODEID"), (String) theWii_1.get("SESSID"), null, null);
					wlist = client.getWorklist("T001");
					assertTrue(wlist.size() == old_wlist_count + 1);
					JSONObject prcJson = client.getPrcInfo(prcid);
					assertEquals("running", prcJson.get("STATUS"));
					JSONObject theWii_NULL = getWorkitem(wlist, prcid, "id_NULL");
					client.doTask("T001", prcid, (String) theWii_NULL.get("NODEID"), (String) theWii_NULL.get("SESSID"), null, null);
					wlist = client.getWorklist("T001");
					assertTrue(wlist.size() == old_wlist_count);

					prcJson = client.getPrcInfo(prcid);
					assertEquals("finished", prcJson.get("STATUS"));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} finally {
				if (prcid != null)
					client.deleteProcess(prcid);
				client.deleteWft(wftid_OR);
			}

			// do some house clean work.
			wlist = client.getWorklist("T001");
			while (wlist.size() > 0) {
				JSONObject wii = (JSONObject) wlist.get(0);
				String aprcid = (String) wii.get("PRCID");
				client.deleteProcess(aprcid);
				wlist = client.getWorklist("T001");
			}

			return;
		} finally {
			unloadTestUsers(client);
		}
	}

	private JSONArray myGetWL(AWEClient client, String doer) {
		JSONArray ret = null;
		try {
			for (int i = 0; i < 100; i++) {
				ret = client.getWorklist("T001");
				if (ret != null) {
					break;
				}
				if (ret.size() > 0)
					break;
				Thread.sleep(1000);
			}
		} catch (Exception ex) {

		}
		return ret;
	}

	private String myStartWorkflow(String token, String startBy, String wftid, String teamId, String instanceName, JSONObject ctx) throws Exception {
		return client.startWorkflow(startBy, wftid, teamId, instanceName, ctx);
	}

	public void test_AND() throws Exception {
		client = AWEClient.newInstance("tester1", "tester1");
		loadTestUsers(client);
		try {
			cleanHouse(client);

			String wft_AND = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
					+ "<node id=\"id_start\" type=\"start\" title=\"Start\" name=\"Start\" x=\"27\" y=\"213\">\n <next targetID=\"id_1\"/><next targetID=\"id_2\"/><next targetID=\"id_3\"/></node>" + "<node id=\"id_end\" type=\"end\" title=\"End\" name=\"End\" x=\"738\" y=\"213\">\n <next targetID=\"id_end\"/></node>"

					+ "<node id=\"id_1\" type=\"task\" title=\"ID_1\" name=\"ID_1\"  acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n " + "<taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n " + "<next targetID=\"id_AND\"/></node>"

					+ "<node id=\"id_2\" type=\"task\" title=\"ID_2\" name=\"ID_2\"  acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n " + "<taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n " + "<next targetID=\"id_AND\"/></node>"

					+ "<node id=\"id_3\" type=\"task\" title=\"ID_3\" name=\"ID_3\"  acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n " + "<taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n " + "<next targetID=\"id_AND\"/></node>"

					+ "<node id='id_AND' type='and'>" + "<next targetID='id_NULL'/>" + "</node>"

					+ "<node id=\"id_NULL\" type=\"task\" title=\"ID_NULL\" name=\"ID_NULL\"  acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n " + "<taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n " + "<next targetID=\"id_end\"/></node>"

					+ "</cf:workflow>";

			String wftid_AND = client.uploadWft(wft_AND, "testProcess_AND");
			JSONArray wlist = client.getWorklist("T001");
			int old_wlist_count = wlist.size();

			JSONObject ctx = new JSONObject();
			ctx.put("author", "liukehong");

			String teamId = null;
			String tmp = null;
			String prcid = null;
			try {
				prcid = client.startWorkflow("T001", wftid_AND, teamId, "testProcess_AND", ctx);
				assertFalse(prcid.startsWith("ERROR"));
				try {

					// id_apply_leaving
					wlist = myGetWL(client, "T001");
					assertTrue(wlist.size() == old_wlist_count + 3);
					JSONObject theWii_1 = getWorkitem(wlist, prcid, "id_1");
					assertTrue(theWii_1 != null);
					JSONObject theWii_2 = getWorkitem(wlist, prcid, "id_2");
					assertTrue(theWii_2 != null);
					JSONObject theWii_3 = getWorkitem(wlist, prcid, "id_3");
					assertTrue(theWii_3 != null);
					JSONObject theWii_NULL = getWorkitem(wlist, prcid, "id_NULL");
					assertTrue(theWii_NULL == null);
					tmp = client.doTask("T001", prcid, (String) theWii_1.get("NODEID"), (String) theWii_1.get("SESSID"), null, null);
					assertFalse(tmp.startsWith("ERROR"));
					wlist = myGetWL(client, "T001");
					assertTrue(wlist.size() == old_wlist_count + 2);
					tmp = client.doTask("T001", prcid, (String) theWii_2.get("NODEID"), (String) theWii_2.get("SESSID"), null, null);
					assertFalse(tmp.startsWith("ERROR"));
					wlist = myGetWL(client, "T001");
					assertTrue(wlist.size() == old_wlist_count + 1);
					tmp = client.doTask("T001", prcid, (String) theWii_3.get("NODEID"), (String) theWii_3.get("SESSID"), null, null);
					assertFalse(tmp.startsWith("ERROR"));
					wlist = myGetWL(client, "T001");
					assertTrue(wlist.size() == old_wlist_count + 1);
					theWii_NULL = getWorkitem(wlist, prcid, "id_NULL");
					assertTrue(theWii_NULL != null);

					tmp = client.doTask("T001", prcid, (String) theWii_NULL.get("NODEID"), (String) theWii_NULL.get("SESSID"), null, null);
					assertFalse(tmp.startsWith("ERROR"));
					wlist = myGetWL(client, "T001");
					assertTrue(wlist.size() == old_wlist_count);

					JSONObject prcJson = client.getPrcInfo(prcid);
					assertEquals("finished", prcJson.get("STATUS"));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} finally {

				if (prcid != null)
					client.deleteProcess(prcid);

				client.deleteWft(wftid_AND);
			}

			wlist = client.getWorklist("T001");
			while (wlist.size() > 0) {
				JSONObject wii = (JSONObject) wlist.get(0);
				String aprcid = (String) wii.get("PRCID");
				client.deleteProcess(aprcid);
				wlist = client.getWorklist("T001");
			}
		} finally {
			unloadTestUsers(client);
		}
	}

	public void test_mpsm_anydone() throws Exception {
		client = AWEClient.newInstance("tester1", "tester1");
		loadTestUsers(client);
		try {

			// 新建一个模板， 里面有一个活动，给到流程启动者
			String wft = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + "<node id=\"id_start\" type=\"start\" title=\"Start\" name=\"Start\" x=\"27\" y=\"213\">\n <next targetID=\"id_1\"/></node>"
					+ "<node id=\"id_end\" type=\"end\" title=\"End\" name=\"End\" x=\"738\" y=\"213\">\n <next targetID=\"id_end\"/></node>" + "<node id=\"id_1\" type=\"task\" title=\"mpsm_1\" name=\"Task\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n <taskto type=\"role\" whom=\"Approver\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_end\"/>\n </node>"
					+ "</cf:workflow>";
			String wftid = client.uploadWft(wft, "mpsm_1");

			JSONObject team = null;
			try {
				team = client.getTeamByName("test_team2");
				client.deleteTeamById((String) team.get("ID"));
			} catch (MwfClientException ex) {

			}
			String teamid = client.createTeam("test_team2", "test_team2_memo");
			JSONObject members = new JSONObject();
			members.put("T002", "Approver");
			members.put("T003", "Approver");
			members.put("T004", "Approver");
			members.put("T005", "Approver");

			client.addTeamMembers(teamid, members);

			JSONObject ctx = new JSONObject();
			ctx.put("author", "liukehong");
			String prcid = null;
			try {
				prcid = client.startWorkflow("T001", wftid, teamid, "testMpsm_1", ctx);
				assertFalse(prcid.startsWith("ERROR"));

				JSONObject prcJson = client.getPrcInfo(prcid);
				assertEquals("running", prcJson.get("STATUS"));
				String thePrcId = (String) prcJson.get("PRCID");
				assertEquals(prcid, thePrcId);

				JSONArray wlist_07 = client.getWorklist("T002", prcid);
				JSONArray wlist_08 = client.getWorklist("T003", prcid);
				JSONArray wlist_09 = client.getWorklist("T004", prcid);
				JSONArray wlist_10 = client.getWorklist("T005", prcid);
				assertTrue(wlist_07.size() > 0);
				assertTrue(wlist_08.size() > 0);
				assertTrue(wlist_09.size() > 0);
				assertTrue(wlist_10.size() > 0);
				JSONObject theWii_07 = getWorkitem(wlist_07, prcid, "id_1");
				assertTrue(theWii_07 != null);
				JSONObject theWii_08 = getWorkitem(wlist_08, prcid, "id_1");
				assertTrue(theWii_08 != null);
				JSONObject theWii_09 = getWorkitem(wlist_09, prcid, "id_1");
				assertTrue(theWii_09 != null);
				JSONObject theWii_10 = getWorkitem(wlist_10, prcid, "id_1");
				assertTrue(theWii_10 != null);

				client.doTask("T002", prcid, (String) theWii_08.get("NODEID"), (String) theWii_08.get("SESSID"), null, null);

				wlist_07 = client.getWorklist("T002", prcid);
				wlist_08 = client.getWorklist("T003", prcid);
				wlist_09 = client.getWorklist("T004", prcid);
				wlist_10 = client.getWorklist("T005", prcid);
				assertTrue(wlist_07.size() == 0);
				assertTrue(wlist_08.size() == 0);
				assertTrue(wlist_09.size() == 0);
				assertTrue(wlist_10.size() == 0);

				prcJson = client.getPrcInfo(prcid);
				assertEquals("finished", prcJson.get("STATUS"));

				assertEquals(teamid, client.deleteTeamById(teamid));

			} finally {
				if (prcid != null)
					client.deleteProcess(prcid);
				client.deleteWft(wftid);
			}
		} finally {
			unloadTestUsers(client);
		}
	}

	public void test_mpsm_alldone() throws Exception {
		client = AWEClient.newInstance("tester1", "tester1");
		loadTestUsers(client);
		try {

			// 新建一个模板， 里面有一个活动，给到流程启动者
			String wft = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + "<node id=\"id_start\" type=\"start\" title=\"Start\" name=\"Start\" x=\"27\" y=\"213\">\n <next targetID=\"id_1\"/></node>"
					+ "<node id=\"id_end\" type=\"end\" title=\"End\" name=\"End\" x=\"738\" y=\"213\">\n <next targetID=\"id_end\"/></node>"
					+ "<node id=\"id_1\" type=\"task\" title=\"mpsm_2\" name=\"Task\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"2\">\n <taskto type=\"role\" whom=\"Approver\"/>\n <mpcdc/>\n <oec>if(total==4) return \"4\"; else return \"DEFAULT\";</oec>\n <next option=\"4\" targetID=\"id_4\"/>\n <next option=\"DEFAULT\" targetID=\"id_end\"/>\n </node>"
					+ "<node id=\"id_4\" type=\"task\" title=\"totalequalsto4\" name=\"Task\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n <taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_end\"/>\n  </node>" + "</cf:workflow>";
			String wftid = client.uploadWft(wft, "mpsm_2");

			String teamid = client.createTeam("test_team2", "test_team2_memo");
			JSONObject members = new JSONObject();
			members.put("T002", "Approver");
			members.put("T003", "Approver");
			members.put("T004", "Approver");
			members.put("T005", "Approver");

			client.addTeamMembers(teamid, members);

			JSONObject ctx = new JSONObject();
			ctx.put("author", "liukehong");
			String prcid = null;
			try {
				prcid = client.startWorkflow("T001", wftid, teamid, "testMpsm_2", ctx);
				assertFalse(prcid.startsWith("ERROR"));
				JSONObject prcJson = client.getPrcInfo(prcid);
				assertEquals("running", prcJson.get("STATUS"));
				String thePrcId = (String) prcJson.get("PRCID");
				assertEquals(prcid, thePrcId);

				JSONArray wlist;
				JSONObject theWii;
				JSONArray wlist_07 = client.getWorklist("T002", prcid);
				JSONArray wlist_08 = client.getWorklist("T003", prcid);
				JSONArray wlist_09 = client.getWorklist("T004", prcid);
				JSONArray wlist_10 = client.getWorklist("T005", prcid);
				assertTrue(wlist_07.size() > 0);
				assertTrue(wlist_08.size() > 0);
				assertTrue(wlist_09.size() > 0);
				assertTrue(wlist_10.size() > 0);
				JSONObject theWii_07 = getWorkitem(wlist_07, prcid, "id_1");
				assertTrue(theWii_07 != null);
				JSONObject theWii_08 = getWorkitem(wlist_08, prcid, "id_1");
				assertTrue(theWii_08 != null);
				JSONObject theWii_09 = getWorkitem(wlist_09, prcid, "id_1");
				assertTrue(theWii_09 != null);
				JSONObject theWii_10 = getWorkitem(wlist_10, prcid, "id_1");
				assertTrue(theWii_10 != null);

				client.doTask("T003", prcid, (String) theWii_08.get("NODEID"), (String) theWii_08.get("SESSID"), null, null);

				wlist_07 = client.getWorklist("T002", prcid);
				wlist_08 = client.getWorklist("T003", prcid);
				wlist_09 = client.getWorklist("T004", prcid);
				wlist_10 = client.getWorklist("T005", prcid);
				assertTrue(wlist_07.size() > 0);
				assertTrue(wlist_08.size() == 0);
				assertTrue(wlist_09.size() > 0);
				assertTrue(wlist_10.size() > 0);

				theWii_07 = getWorkitem(wlist_07, prcid, "id_1");
				assertTrue(theWii_07 != null);
				client.doTask("T002", prcid, (String) theWii_07.get("NODEID"), (String) theWii_07.get("SESSID"), null, null);

				wlist_07 = client.getWorklist("T002", prcid);
				wlist_08 = client.getWorklist("T003", prcid);
				wlist_09 = client.getWorklist("T004", prcid);
				wlist_10 = client.getWorklist("T005", prcid);
				assertTrue(wlist_07.size() == 0);
				assertTrue(wlist_08.size() == 0);
				assertTrue(wlist_09.size() > 0);
				assertTrue(wlist_10.size() > 0);

				theWii_09 = getWorkitem(wlist_09, prcid, "id_1");
				assertTrue(theWii_09 != null);
				client.doTask("T004", prcid, (String) theWii_09.get("NODEID"), (String) theWii_09.get("SESSID"), null, null);

				wlist_07 = client.getWorklist("T002", prcid);
				wlist_08 = client.getWorklist("T003", prcid);
				wlist_09 = client.getWorklist("T004", prcid);
				wlist_10 = client.getWorklist("T005", prcid);
				assertTrue(wlist_07.size() == 0);
				assertTrue(wlist_08.size() == 0);
				assertTrue(wlist_09.size() == 0);
				assertTrue(wlist_10.size() > 0);

				theWii_10 = getWorkitem(wlist_10, prcid, "id_1");
				assertTrue(theWii_10 != null);
				client.doTask("T005", prcid, (String) theWii_10.get("NODEID"), (String) theWii_10.get("SESSID"), null, null);

				wlist_07 = client.getWorklist("T002", prcid);
				wlist_08 = client.getWorklist("T003", prcid);
				wlist_09 = client.getWorklist("T004", prcid);
				wlist_10 = client.getWorklist("T005", prcid);
				assertTrue(wlist_07.size() == 0);
				assertTrue(wlist_08.size() == 0);
				assertTrue(wlist_09.size() == 0);
				assertTrue(wlist_10.size() == 0);

				wlist = client.getWorklist("T001");
				assertTrue(wlist.size() > 0);
				theWii = getWorkitem(wlist, prcid, "id_4");
				assertTrue(theWii != null);
				client.doTask("T001", prcid, (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, null);

				prcJson = client.getPrcInfo(prcid);
				assertEquals("finished", prcJson.get("STATUS"));

				assertEquals(teamid, client.deleteTeamById(teamid));

			} finally {
				if (prcid != null)
					client.deleteProcess(prcid);
				client.deleteWft(wftid);
			}
		} finally {
			unloadTestUsers(client);
		}
	}

	public void test_mpsm_variabledone() throws Exception {
		client = AWEClient.newInstance("tester1", "tester1");
		loadTestUsers(client);
		try {

			// 新建一个模板， 里面有一个活动，给到流程启动者
			String wft = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
					+ "<node id=\"id_start\" type=\"start\" title=\"Start\" name=\"Start\" x=\"27\" y=\"213\">\n <next targetID=\"id_1\"/></node>"
					+ "<node id=\"id_end\" type=\"end\" title=\"End\" name=\"End\" x=\"738\" y=\"213\">\n <next targetID=\"id_end\"/></node>"
					+ "<node id=\"id_1\" type=\"task\" title=\"mpsm_2\" name=\"Task\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"3\">\n <taskto type=\"role\" whom=\"Approver\"/>\n <mpcdc>if(finished>=2) return \"true\"; else return \"false\";</mpcdc>\n <oec>if(finished>=2) return \"4\"; else return \"DEFAULT\";</oec>\n <next option=\"4\" targetID=\"id_4\"/>\n <next option=\"DEFAULT\" targetID=\"id_end\"/>\n </node>"
					+ "<node id=\"id_4\" type=\"task\" title=\"totalequalsto4\" name=\"Task\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n <taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_end\"/>\n  </node>" + "</cf:workflow>";
			String wftid = client.uploadWft(wft, "mpsm_3");

			String teamid = client.createTeam("test_team2", "test_team2_memo");
			JSONObject members = new JSONObject();
			members.put("T002", "Approver");
			members.put("T003", "Approver");
			members.put("T004", "Approver");
			members.put("T005", "Approver");

			client.addTeamMembers(teamid, members.toString());

			// T001启动流程
			String prcid = null;
			try {
				prcid = client.startWorkflow("T001", wftid, teamid, "testMpsm_3", null);
				assertFalse(prcid.startsWith("ERROR"));

				JSONObject prcJson = client.getPrcInfo(prcid);
				assertEquals("running", prcJson.get("STATUS"));
				String thePrcId = (String) prcJson.get("PRCID");
				assertEquals(prcid, thePrcId);

				// 四个用户都应该收到工作项
				JSONArray wlist;
				JSONObject theWii;
				JSONArray wlist_07 = client.getWorklist("T002", prcid);
				JSONArray wlist_08 = client.getWorklist("T003", prcid);
				JSONArray wlist_09 = client.getWorklist("T004", prcid);
				JSONArray wlist_10 = client.getWorklist("T005", prcid);
				assertTrue(wlist_07.size() > 0);
				assertTrue(wlist_08.size() > 0);
				assertTrue(wlist_09.size() > 0);
				assertTrue(wlist_10.size() > 0);
				JSONObject theWii_07 = getWorkitem(wlist_07, prcid, "id_1");
				assertTrue(theWii_07 != null);
				JSONObject theWii_08 = getWorkitem(wlist_08, prcid, "id_1");
				assertTrue(theWii_08 != null);
				JSONObject theWii_09 = getWorkitem(wlist_09, prcid, "id_1");
				assertTrue(theWii_09 != null);
				JSONObject theWii_10 = getWorkitem(wlist_10, prcid, "id_1");
				assertTrue(theWii_10 != null);

				// T003完成id_1
				client.doTask("T003", prcid, (String) theWii_08.get("NODEID"), (String) theWii_08.get("SESSID"), null, null);

				wlist_07 = client.getWorklist("T002", prcid);
				wlist_08 = client.getWorklist("T003", prcid);
				wlist_09 = client.getWorklist("T004", prcid);
				wlist_10 = client.getWorklist("T005", prcid);
				assertTrue(wlist_07.size() > 0);
				assertTrue(wlist_08.size() == 0);
				assertTrue(wlist_09.size() > 0);
				assertTrue(wlist_10.size() > 0);

				// T002完成id_1
				theWii_07 = getWorkitem(wlist_07, prcid, "id_1");
				assertTrue(theWii_07 != null);
				client.doTask("T002", prcid, (String) theWii_07.get("NODEID"), (String) theWii_07.get("SESSID"), null, null);

				// 流向id_4;
				// 所有id_1上的工作取消
				wlist_07 = client.getWorklist("T002", prcid);
				wlist_08 = client.getWorklist("T003", prcid);
				wlist_09 = client.getWorklist("T004", prcid);
				wlist_10 = client.getWorklist("T005", prcid);
				assertTrue(wlist_07.size() == 0);
				assertTrue(wlist_08.size() == 0);
				assertTrue(wlist_09.size() == 0);
				assertTrue(wlist_10.size() == 0);

				wlist = client.getWorklist("T001");
				assertTrue(wlist.size() > 0);
				theWii = getWorkitem(wlist, prcid, "id_4");
				assertTrue(theWii != null);
				client.doTask("T001", prcid, (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, null);

				prcJson = client.getPrcInfo(prcid);
				assertEquals("finished", prcJson.get("STATUS"));

				assertEquals(teamid, client.deleteTeamById(teamid));
			} finally {
				if (prcid != null)
					client.deleteProcess(prcid);
				client.deleteWft(wftid);
			}
		} finally {
			unloadTestUsers(client);
		}
	}

	public void test_admin_process_admin() throws Exception {
		client = AWEClient.newInstance("tester1", "tester1");
		loadTestUsers(client);
		try {

			// 新建一个模板， 里面有一个活动，给到流程启动者
			String wft = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
					+ "<node id=\"58DE1222-4446-C2DE-1F8B-CA9EBE43C965\" type=\"start\" title=\"Start\" name=\"Start\" x=\"27\" y=\"213\">\n <next targetID=\"id_1\"/></node>" + "<node id=\"7E40C95C-333D-964E-2EA3-CA9EBE457AE1\" type=\"end\" title=\"End\" name=\"End\" x=\"738\" y=\"213\">\n <next targetID=\"7E40C95C-333D-964E-2EA3-CA9EBE457AE1\"/></node>"
					+ "<node id=\"id_1\" type=\"task\" title=\"Task\" name=\"Task\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n <taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"7E40C95C-333D-964E-2EA3-CA9EBE457AE1\"/>\n </node>" + "</cf:workflow>";
			String wftname = "rstest1";

			String wftid = client.uploadWft(wft, wftname);

			// 新建一个Team
			String teamid = client.createTeam("test_team2", "test_team2_memo");

			// 取正在运行的进程
			JSONArray runningProcs = client.getProcessesByStatus("running");
			int running_processes_number = runningProcs.size();

			// 启动进程
			JSONObject ctx = new JSONObject();
			ctx.put("author", "liukehong");
			String prcid = null;
			try {
				prcid = client.startWorkflow("T001", wftid, teamid, "test_process2", ctx);
				assertFalse(prcid.startsWith("ERROR"));

				// 重新取出正在运行的进程，其个数应该增加了1
				runningProcs = client.getProcessesByStatus("running");
				int new_running_processes_number = runningProcs.size();
				assertEquals(running_processes_number + 1, new_running_processes_number);

				// 取该进程的内容（JSON格式）
				JSONObject prcJson = client.getPrcInfo(prcid);
				assertEquals("running", prcJson.get("STATUS"));
				assertEquals(prcid, prcJson.get("PRCID"));

				// 取得worklist, 找到属于该进程的那个work
				JSONArray wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				JSONObject theWii = getWorkitem(wlist, prcid, "id_1");
				assertTrue(theWii != null);

				// Suspend该进程
				String tt = client.suspendProcess(prcid);
				assertFalse(tt == null);

				// 再取worklist, 应该找不到前面那个work
				wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() == 0);
				theWii = getWorkitem(wlist, prcid, "id_1");
				assertTrue(theWii == null);

				// Resume该进程
				tt = client.resumeProcess(prcid);
				assertFalse(tt == null);

				// 再取worklist, 应该再次找到前面那个work
				wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				theWii = getWorkitem(wlist, prcid, "id_1");
				assertTrue(theWii != null);
				String theNodeId = (String) theWii.get("NODEID");

				// 取得当前活动节点
				// 挂起这个节点
				client.suspendWork(prcid, theNodeId);

				// 再取worklist, 应该找不到前面那个work
				wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() >= 0);
				theWii = getWorkitem(wlist, prcid, "id_1");
				assertTrue(theWii == null);

				// 恢复这个节点
				client.resumeWork(prcid, theNodeId);

				// 再取worklist, 应该再次找到前面那个work
				wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				theWii = getWorkitem(wlist, prcid, "id_1");
				assertTrue(theWii != null);

				JSONArray T3307_wlist = client.getWorklist("T002", prcid);

				// 测试Delegation.
				// LKH将工作委托给T002
				String ret = client.delegate((String) theWii.get("PRCID"), (String) theWii.get("SESSID"), "T001", "T002");
				assertTrue(ret != null);

				JSONArray tmpWL = client.getWorklist("T001", prcid);
				assertTrue(tmpWL.size() == 0);

				// 再取T002的Worklist， T002的工作列表中应该多了一个。
				JSONArray T3307_new_wlist = client.getWorklist("T002", prcid);
				assertEquals(T3307_wlist.size() + 1, T3307_new_wlist.size());
				JSONObject T3307_theWii = getWorkitem(T3307_new_wlist, prcid, "id_1");

				// 完成这个work
				client.doTask("T002", (String) T3307_theWii.get("PRCID"), (String) T3307_theWii.get("NODEID"), (String) T3307_theWii.get("SESSID"), null, null);

				tmpWL = client.getWorklist("T002", prcid);
				assertTrue(tmpWL.size() == 0);

				// 再次看这个进程的状态，应该是完成状态
				prcJson = client.getPrcInfo((String) T3307_theWii.get("PRCID"));
				assertEquals("finished", prcJson.get("STATUS"));
			} finally {

				// 删除这个新进程
				if (prcid != null)
					client.deleteProcess(prcid);

				// 删除前面测试创建的Team
				client.deleteTeamById(teamid);

				// 删除这个模板
				client.deleteWft(wftid);
			}
		} finally {
			unloadTestUsers(client);
		}

	}

	public void test_delegation() throws Exception {
		client = AWEClient.newInstance("tester1", "tester1");
		loadTestUsers(client);
		try {

			// 新建一个模板， 里面有一个活动，给到流程启动者
			String wft = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
					+ "<node id=\"58DE1222-4446-C2DE-1F8B-CA9EBE43C965\" type=\"start\" title=\"Start\" name=\"Start\" x=\"27\" y=\"213\">\n <next targetID=\"id_1\"/></node>" + "<node id=\"7E40C95C-333D-964E-2EA3-CA9EBE457AE1\" type=\"end\" title=\"End\" name=\"End\" x=\"738\" y=\"213\">\n <next targetID=\"7E40C95C-333D-964E-2EA3-CA9EBE457AE1\"/></node>"
					+ "<node id=\"id_1\" type=\"task\" title=\"Task\" name=\"Task\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n <taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"7E40C95C-333D-964E-2EA3-CA9EBE457AE1\"/>\n </node>" + "</cf:workflow>";
			String wftname = "rstest1";

			String wftid = client.uploadWft(wft, wftname);

			// 新建一个Team
			String teamid = client.createTeam("test_team2", "test_team2_memo");

			// 取正在运行的进程
			JSONArray runningProcs = client.getProcessesByStatus("running");
			int running_processes_number = runningProcs.size();

			// 启动进程
			JSONObject ctx = new JSONObject();
			ctx.put("author", "liukehong");
			String prcid = null;
			try {
				prcid = client.startWorkflow("T001", wftid, teamid, "test_process2", ctx);
				assertFalse(prcid.startsWith("ERROR"));

				// 重新取出正在运行的进程，其个数应该增加了1
				runningProcs = client.getProcessesByStatus("running");
				int new_running_processes_number = runningProcs.size();
				assertEquals(running_processes_number + 1, new_running_processes_number);

				// 取该进程的内容（JSON格式）
				JSONObject prcJson = client.getPrcInfo(prcid);
				assertEquals("running", prcJson.get("STATUS"));
				assertEquals(prcid, prcJson.get("PRCID"));

				// 取得worklist, 找到属于该进程的那个work
				JSONArray wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				JSONObject theWii = getWorkitem(wlist, prcid, "id_1");
				assertTrue(theWii != null);

				JSONArray T3307_wlist = client.getWorklist("T002", prcid);
				assertTrue(T3307_wlist.size() == 0);

				// 测试Delegation.
				// LKH将工作委托给T002
				String ret = client.delegate((String) theWii.get("PRCID"), (String) theWii.get("SESSID"), "T001", "T002");
				assertTrue(ret != null);

				JSONArray tmpWL = client.getWorklist("T001", prcid);
				assertTrue(tmpWL.size() == 0);

				// 再取T002的Worklist， T002的工作列表中应该多了一个。
				JSONArray T3307_new_wlist = client.getWorklist("T002", prcid);
				assertEquals(T3307_wlist.size() + 1, T3307_new_wlist.size());
				JSONObject T3307_theWii = getWorkitem(T3307_new_wlist, prcid, "id_1");

				// 完成这个work
				client.doTask("T002", (String) T3307_theWii.get("PRCID"), (String) T3307_theWii.get("NODEID"), (String) T3307_theWii.get("SESSID"), null, null);

				tmpWL = client.getWorklist("T002", prcid);
				assertTrue(tmpWL.size() == 0);

				// 再次看这个进程的状态，应该是完成状态
				prcJson = client.getPrcInfo((String) T3307_theWii.get("PRCID"));
				assertEquals("finished", prcJson.get("STATUS"));
			} finally {
				// 删除这个新进程
				if (prcid != null)
					client.deleteProcess(prcid);

				// 删除前面测试创建的Team
				client.deleteTeamById(teamid);

				// 删除这个模板
				client.deleteWft(wftid);
			}
		} finally {
			unloadTestUsers(client);
		}

	}

	// 测试工作流模板中制定活动给特定的用户名 <taskto type=\"person\" whom=\"T3307\"/>\
	public void test_taskto_user() throws Exception {
		client = AWEClient.newInstance("tester1", "tester1");
		loadTestUsers(client);
		try {

			// 新建一个模板， 活动id_1，给到流程启动者
			// 活动 id_2, 制定给到T004
			String wft = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + "<node id=\"id_start\" type=\"start\" title=\"Start\" name=\"Start\" x=\"27\" y=\"213\">\n <next targetID=\"id_1\"/></node>"
					+ "<node id=\"id_end\" type=\"end\" title=\"End\" name=\"End\" x=\"738\" y=\"213\">\n <next targetID=\"id_end\"/></node>" + "<node id=\"id_1\" type=\"task\" title=\"Apply Leaving\" name=\"Apply Leaving\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n <taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_2\"/>\n </node>"
					+ "<node id=\"id_2\" type=\"task\" title=\"Approve Leaving\" name=\"Approve Leaving\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n <taskto type=\"person\" whom=\"T004\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_end\"/>\n </node>" + "</cf:workflow>";
			String wftid = client.uploadWft(wft, "testProcess_1");

			// 启动进程
			String prcid = null;
			try {
				prcid = client.startWorkflow("T001", wftid, null, "testProcess_1", null);
				assertFalse(prcid.startsWith("ERROR"));

				// 取该进程的内容（JSON格式）
				JSONObject prcJson = client.getPrcInfo(prcid);
				assertEquals("running", prcJson.get("STATUS"));
				String thePrcId = (String) prcJson.get("PRCID");
				assertEquals(prcid, thePrcId);

				// 取得worklist, 找到属于该进程的那个work
				JSONArray wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				JSONObject theWii = getWorkitem(wlist, prcid, "id_1");
				assertTrue(theWii != null);

				// 完成这个work
				client.doTask("T001", prcid, (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, null);

				JSONArray T004_wlist = client.getWorklist("T004");

				JSONObject T004_theWii = getWorkitem(T004_wlist, prcid, "id_2");
				assertTrue(T004_theWii != null);

				client.doTask("T004", (String) T004_theWii.get("PRCID"), (String) T004_theWii.get("NODEID"), (String) T004_theWii.get("SESSID"), null, null);

				// 再次看这个进程的状态，应该是完成状态
				prcJson = client.getPrcInfo(prcid);
				assertEquals("finished", prcJson.get("STATUS"));

				// T004 再取worklist, 应该找不到前面那个work
				T004_wlist = client.getWorklist("T004");
				T004_theWii = getWorkitem(T004_wlist, prcid, "id_5");
				assertTrue(T004_theWii == null);
			} finally {
				if (prcid != null)
					client.deleteProcess(prcid);
				client.deleteWft(wftid);

				cleanHouse(client);
			}
		} finally {
			unloadTestUsers(client);
		}
	}

	// 测试工作流模板中制定活动给特定的角色和reference
	public void test_taskto_role_reference() throws Exception {
		client = AWEClient.newInstance("tester1", "tester1");
		loadTestUsers(client);
		try {

			// 新建一个模板， 活动id_1，给到流程启动者
			// 活动 id_2, 给到 role Approver
			// 活动 id_3, 给到 role Auditor
			// 活动 id_4, 给到 与id_2一样
			// 活动 id_5, 给到 与id_1一样
			// 活动 id_6, 给到 person T005
			String wft = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + "<node id=\"id_start\" type=\"start\" title=\"Start\" name=\"Start\" x=\"27\" y=\"213\">\n <next targetID=\"id_1\"/></node>"
					+ "<node id=\"id_end\" type=\"end\" title=\"End\" name=\"End\" x=\"738\" y=\"213\">\n <next targetID=\"id_end\"/></node>" + "<node id=\"id_1\" type=\"task\" title=\"Apply Leaving\" name=\"Apply Leaving\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n <taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_2\"/>\n </node>"
					+ "<node id=\"id_2\" type=\"task\" title=\"Approve Leaving\" name=\"Approve Leaving\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n <taskto type=\"role\" whom=\"Approver\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_3\"/>\n </node>"
					+ "<node id=\"id_3\" type=\"task\" title=\"Auditor Leaving\" name=\"Auditor Leaving\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n <taskto type=\"role\" whom=\"Auditor\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_4\"/>\n </node>"
					+ "<node id=\"id_4\" type=\"task\" title=\"Approver Check Result\" name=\"Approver Check Result\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n <taskto type=\"RefertoNode\" whom=\"id_2\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_5\"/>\n </node>"
					+ "<node id=\"id_5\" type=\"task\" title=\"Starter Check Result\" name=\"Starter Check Result\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n <taskto type=\"RefertoNode\" whom=\"id_1\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_6\"/>\n </node>"
					+ "<node id=\"id_6\" type=\"task\" title=\"Approve Leaving\" name=\"Approve Leaving\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n <taskto type=\"person\" whom=\"T005\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_end\"/>\n </node>" + "</cf:workflow>";

			String wftid = client.uploadWft(wft, "testProcess_2");

			// 新建一个Team
			String teamid = client.createTeam("test_team2", "test_team2_memo");
			// 指派团队成员
			JSONObject members = new JSONObject();
			members.put("T003", "Approver");
			members.put("T004", "Auditor");
			client.addTeamMembers(teamid, members);

			// 启动进程
			String prcid = null;
			try {
				prcid = client.startWorkflow("T001", wftid, teamid, "testProcess_2", null);
				assertFalse(prcid.startsWith("ERROR"));

				// 取该进程的内容（JSON格式）
				JSONObject prcJson = client.getPrcInfo(prcid);
				assertEquals("running", prcJson.get("STATUS"));
				String thePrcId = (String) prcJson.get("PRCID");
				assertEquals(prcid, thePrcId);

				// 取得worklist, 找到属于该进程的那个work
				JSONArray wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				JSONObject theWii = getWorkitem(wlist, prcid, "id_1");
				assertTrue(theWii != null);

				// 完成这个work
				client.doTask("T001", prcid, (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, null);

				JSONArray wlist_tmp = client.getWorklist("T003", prcid);
				assertTrue(wlist_tmp.size() > 0);
				JSONObject theWii_tmp = getWorkitem(wlist_tmp, prcid, "id_2");
				assertTrue(theWii_tmp != null);

				client.doTask("T003", (String) theWii_tmp.get("PRCID"), (String) theWii_tmp.get("NODEID"), (String) theWii_tmp.get("SESSID"), null, null);

				wlist_tmp = client.getWorklist("T004", prcid);
				assertTrue(wlist_tmp.size() > 0);
				theWii_tmp = getWorkitem(wlist_tmp, prcid, "id_3");
				assertTrue(theWii_tmp != null);
				client.doTask("T004", (String) theWii_tmp.get("PRCID"), (String) theWii_tmp.get("NODEID"), (String) theWii_tmp.get("SESSID"), null, null);

				wlist_tmp = client.getWorklist("T003", prcid);
				assertTrue(wlist_tmp.size() > 0);
				theWii_tmp = getWorkitem(wlist_tmp, prcid, "id_4");
				assertTrue(theWii_tmp != null);
				client.doTask("T003", (String) theWii_tmp.get("PRCID"), (String) theWii_tmp.get("NODEID"), (String) theWii_tmp.get("SESSID"), null, null);

				wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				theWii = getWorkitem(wlist, prcid, "id_5");
				assertTrue(theWii != null);
				client.doTask("T001", (String) theWii.get("PRCID"), (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, null);

				wlist_tmp = client.getWorklist("T005", prcid);
				assertTrue(wlist_tmp.size() > 0);
				theWii_tmp = getWorkitem(wlist_tmp, prcid, "id_6");
				assertTrue(theWii_tmp != null);
				client.doTask("T005", (String) theWii_tmp.get("PRCID"), (String) theWii_tmp.get("NODEID"), (String) theWii_tmp.get("SESSID"), null, null);

				// 再次看这个进程的状态，应该是完成状态
				prcJson = client.getPrcInfo(prcid);
				assertEquals("finished", prcJson.get("STATUS"));
			} finally {
				if (prcid != null)
					client.deleteProcess(prcid);

				client.deleteTeamById(teamid);

				client.deleteWft(wftid);

				cleanHouse(client);
			}
		} finally {
			unloadTestUsers(client);
		}
	}

	// 测试工作流模板中指定活动给特定的角色和reference
	public void test_taskto_differentTeam() throws Exception {
		client = AWEClient.newInstance("tester1", "tester1");
		loadTestUsers(client);
		try {

			String wft = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + "<node id=\"id_start\" type=\"start\" title=\"Start\" name=\"Start\" x=\"27\" y=\"213\">\n <next targetID=\"id_1\"/></node>"
					+ "<node id=\"id_end\" type=\"end\" title=\"End\" name=\"End\" x=\"738\" y=\"213\">\n <next targetID=\"id_end\"/></node>" + "<node id=\"id_1\" type=\"task\" title=\"Apply Leaving\" name=\"Apply Leaving\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n " + "<taskto type=\"role\" whom=\"Approver\"/>\n"
					+ "<mpcdc/>\n <oec/>\n <next targetID=\"id_end\"/>\n </node>" + "</cf:workflow>";

			String wftid = client.uploadWft(wft, "testProcess_2");
			assertTrue(wftid != null);

			// 新建一个Team
			String team1 = client.createTeam("team1", "team1Memo");
			String team2 = client.createTeam("team2", "team2Memo");
			// 指派团队成员
			JSONObject members = new JSONObject();
			members.put("T003", "Approver");
			members.put("T004", "Auditor");
			client.addTeamMembers(team1, members);
			members.clear();
			members.put("T002", "Approver");
			client.addTeamMembers(team2, members);

			String prcid = null;
			JSONArray wlist = null;
			JSONObject wii = null;
			JSONObject prcJson = null;
			// 启动进程
			try {
				prcid = client.startWorkflow("T001", wftid, team1, "testProcess_2", null);
				assertFalse(prcid.startsWith("ERROR"));

				wlist = client.getWorklist("T003", prcid);
				assertTrue(wlist.size() > 0);
				wii = getWorkitem(wlist, prcid, "id_1");
				assertTrue(wii != null);

				// 完成这个work
				client.doTask("T003", prcid, (String) wii.get("NODEID"), (String) wii.get("SESSID"), null, null);

				prcJson = client.getPrcInfo(prcid);
				assertEquals("finished", prcJson.get("STATUS"));
				if (prcid != null)
					client.deleteProcess(prcid);

				//
				//
				// Now, start with another team
				prcid = client.startWorkflow("T001", wftid, team2, "testProcess_2", null);
				assertFalse(prcid.startsWith("ERROR"));

				wlist = client.getWorklist("T002", prcid);
				assertTrue(wlist.size() > 0);
				wii = getWorkitem(wlist, prcid, "id_1");
				assertTrue(wii != null);

				client.doTask("T002", prcid, (String) wii.get("NODEID"), (String) wii.get("SESSID"), null, null);

				prcJson = client.getPrcInfo(prcid);
				assertEquals("finished", prcJson.get("STATUS"));
			} finally {

				if (prcid != null)
					client.deleteProcess(prcid);

				client.deleteTeamById(team1);
				client.deleteTeamById(team2);

				client.deleteWft(wftid);

				cleanHouse(client);
			}
		} finally {
			unloadTestUsers(client);
		}
	}

	// 测试选择
	public void test_option() throws Exception {
		client = AWEClient.newInstance("tester1", "tester1");
		loadTestUsers(client);
		try {

			String wft = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + "<node id=\"id_start\" type=\"start\" title=\"Start\" name=\"Start\" x=\"27\" y=\"213\">\n <next targetID=\"id_apply_leaving\"/></node>"
					+ "<node id=\"id_end\" type=\"end\" title=\"End\" name=\"End\" x=\"738\" y=\"213\">\n <next targetID=\"id_end\"/></node>"
					+ "<node id=\"id_apply_leaving\" type=\"task\" title=\"Apply Leaving\" name=\"Apply Leaving\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n <taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_approve_leaving\"/>\n </node>"
					+ "<node id=\"id_approve_leaving\" type=\"task\" title=\"Approve Leaving\" name=\"Approve Leaving\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n <taskto type=\"role\" whom=\"Approver\"/>\n <mpcdc/>\n <oec/>\n <next option=\"Approve\" targetID=\"id_approved\"/><next option=\"Reject\" targetID=\"id_rejected\"/>\n </node>"
					+ "<node id=\"id_approved\" type=\"task\" title=\"Approved\" name=\"Approved\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n <taskto type=\"RefertoNode\" whom=\"id_apply_leaving\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_end\"/>\n </node>"
					+ "<node id=\"id_rejected\" type=\"task\" title=\"Rejected\" name=\"Rejected\" x=\"356\" y=\"203\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n <taskto type=\"RefertoNode\" whom=\"id_apply_leaving\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_end\"/>\n </node>" + "</cf:workflow>";

			String wftid = client.uploadWft(wft, "testProcess_3");

			// 新建一个Team
			String teamid = client.createTeam("test_team2", "test_team2_memo");
			// 指派团队成员
			JSONObject members = new JSONObject();
			members.put("T002", "Approver");
			members.put("T003", "Auditor");
			client.addTeamMembers(teamid, members);
			JSONArray wlist = null;
			// 启动进程
			String prcid = null;
			JSONObject theWii = null;
			JSONObject prcJson = null;
			try {
				prcid = client.startWorkflow("T001", wftid, teamid, "testProcess_3", null);
				assertFalse(prcid.startsWith("ERROR"));

				// 取得worklist, 找到属于该进程的那个work
				wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				theWii = getWorkitem(wlist, prcid, "id_apply_leaving");
				assertTrue(theWii != null);

				// 完成这个work
				client.doTask("T001", prcid, (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, null);

				wlist = client.getWorklist("T002", prcid);
				assertTrue(wlist.size() > 0);
				theWii = getWorkitem(wlist, prcid, "id_approve_leaving");
				assertTrue(theWii != null);

				client.doTask("T002", (String) theWii.get("PRCID"), (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), "Approve", null);

				wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				theWii = getWorkitem(wlist, prcid, "id_rejected");
				assertTrue(theWii == null);
				theWii = getWorkitem(wlist, prcid, "id_approved");
				assertTrue(theWii != null);
				assertEquals(theWii.get("WORKNAME"), "Approved");
				client.doTask("T001", (String) theWii.get("PRCID"), (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, null);

				// 再次看这个进程的状态，应该是完成状态
				prcJson = client.getPrcInfo(prcid);
				assertEquals("finished", prcJson.get("STATUS"));
			} finally {

				// 删除这个新进程
				if (prcid != null)
					client.deleteProcess(prcid);
			}

			// 启动进程
			prcid = null;
			try {
				prcid = client.startWorkflow("T001", wftid, teamid, "testProcess_3", null);
				assertFalse(prcid.startsWith("ERROR"));

				// 取得worklist, 找到属于该进程的那个work
				wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				theWii = getWorkitem(wlist, prcid, "id_apply_leaving");
				assertTrue(theWii != null);

				// 完成这个work
				client.doTask("T001", prcid, (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, null);

				// 取T3307的Worklist
				wlist = client.getWorklist("T002", prcid);
				assertTrue(wlist.size() > 0);
				theWii = getWorkitem(wlist, prcid, "id_approve_leaving");
				assertTrue(theWii != null);

				client.doTask("T002", (String) theWii.get("PRCID"), (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), "Reject", null);

				wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				theWii = getWorkitem(wlist, prcid, "id_approved");
				assertTrue(theWii == null);
				theWii = getWorkitem(wlist, prcid, "id_rejected");
				assertTrue(theWii != null);
				assertEquals(theWii.get("WORKNAME"), "Rejected");
				client.doTask("T001", (String) theWii.get("PRCID"), (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, null);

				// 再次看这个进程的状态，应该是完成状态
				prcJson = client.getPrcInfo(prcid);
				assertEquals("finished", prcJson.get("STATUS"));
			} finally {
				// 删除这个新进程
				if (prcid != null)
					client.deleteProcess(prcid);

				// 删除前面测试创建的Team
				client.deleteTeamById(teamid);

				// 删除这个模板
				client.deleteWft(wftid);

				cleanHouse(client);
			}

		} finally {
			unloadTestUsers(client);
		}
	}

	// 测试AND_OR
	public void test_andor() throws Exception {
		client = AWEClient.newInstance("tester1", "tester1");
		loadTestUsers(client);
		try {

			String wft = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + "<node id=\"id_start\" type=\"start\" title=\"Start\" name=\"Start\" x=\"27\" y=\"213\">\n <next targetID=\"id_apply_leaving\"/></node>"
					+ "<node id=\"id_end\" type=\"end\" title=\"End\" name=\"End\" x=\"738\" y=\"213\">\n <next targetID=\"id_end\"/></node>" + "<node id=\"id_apply_leaving\" type=\"task\" title=\"Apply Leaving\" name=\"Apply Leaving\"  acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n "
					+ "<taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_approve_leaving\"/>\n </node>" + "<node id=\"id_approve_leaving\" type=\"task\" title=\"Approve Leaving\" name=\"Approve Leaving\"  acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n "
					+ "<taskto type=\"role\" whom=\"Approver\"/>\n <mpcdc/>\n <oec/>\n <next option=\"Approve\" targetID=\"id_approved\"/><next option=\"Approve\" targetID=\"id_approved2\"/><next option=\"Reject\" targetID=\"id_rejected\"/><next option=\"Reject\" targetID=\"id_rejected2\"/>\n </node>"
					+ "<node id=\"id_approved\" type=\"task\" title=\"Approved\" name=\"Approved\"  acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n " + "<taskto type=\"RefertoNode\" whom=\"id_apply_leaving\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_AND\"/>\n </node>"
					+ "<node id=\"id_approved2\" type=\"task\" title=\"Approved2\" name=\"Approved2\"  acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n " + "<taskto type=\"RefertoNode\" whom=\"id_approve_leaving\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_AND\"/>\n </node>" + "<node id=\"id_AND\" type=\"and\" ><next targetID=\"id_end\"/></node>\n "
					+ "<node id=\"id_rejected\" type=\"task\" title=\"Rejected\" name=\"Rejected\"  acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n " + "<taskto type=\"RefertoNode\" whom=\"id_apply_leaving\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_OR\"/>\n </node>"
					+ "<node id=\"id_rejected2\" type=\"task\" title=\"Rejected2\" name=\"Rejected2\"  acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n " + "<taskto type=\"RefertoNode\" whom=\"id_approve_leaving\"/>\n <mpcdc/>\n <oec/>\n <next targetID=\"id_OR\"/>\n </node>" + "<node id=\"id_OR\" type=\"or\" ><next targetID=\"id_end\"/></node>\n " + "</cf:workflow>";

			String wftid = client.uploadWft(wft, "testProcess_4");

			// 新建一个Team
			String teamid = client.createTeam("test_team2", "test_team2_memo");
			// 指派团队成员
			JSONObject members = new JSONObject();
			members.put("T002", "Approver");
			members.put("T003", "Auditor");
			client.addTeamMembers(teamid, members);

			// 启动进程
			String prcid = null;
			JSONArray wlist = null;
			JSONObject wii = null;
			JSONObject prcJson = null;
			try {
				prcid = client.startWorkflow("T001", wftid, teamid, "testProcess_4", null);
				assertFalse(prcid.startsWith("ERROR"));

				// id_apply_leaving
				wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				wii = getWorkitem(wlist, prcid, "id_apply_leaving");
				assertTrue(wii != null);
				client.doTask("T001", prcid, (String) wii.get("NODEID"), (String) wii.get("SESSID"), null, null);

				wlist = client.getWorklist("T002", prcid);
				assertTrue(wlist.size() > 0);
				wii = getWorkitem(wlist, prcid, "id_approve_leaving");
				assertTrue(wii != null);

				client.doTask("T002", (String) wii.get("PRCID"), (String) wii.get("NODEID"), (String) wii.get("SESSID"), "Approve", null);

				// id_approved refer to id_apply_leaving
				wlist = client.getWorklist("T001", prcid);
				wii = getWorkitem(wlist, prcid, "id_approved");
				assertTrue(wii != null);
				assertEquals(wii.get("WORKNAME"), "Approved");
				client.doTask("T001", (String) wii.get("PRCID"), (String) wii.get("NODEID"), (String) wii.get("SESSID"), null, null);

				prcJson = client.getPrcInfo(prcid);
				assertEquals("running", prcJson.get("STATUS"));

				// id_approved2 refer to id_approve_leaving
				wlist = client.getWorklist("T002", prcid);
				wii = getWorkitem(wlist, prcid, "id_approved2");
				assertTrue(wii != null);
				assertEquals(wii.get("WORKNAME"), "Approved2");
				client.doTask("T002", (String) wii.get("PRCID"), (String) wii.get("NODEID"), (String) wii.get("SESSID"), null, null);

				// 再次看这个进程的状态，应该是完成状态
				prcJson = client.getPrcInfo(prcid);
				assertEquals("finished", prcJson.get("STATUS"));
			} finally {
				// 删除这个新进程
				if (prcid != null)
					client.deleteProcess(prcid);
			}

			prcid = null;
			try {
				// 启动进程
				prcid = client.startWorkflow("T001", wftid, teamid, "testProcess_4", null);
				assertFalse(prcid.startsWith("ERROR"));

				wlist = client.getWorklist("T001", prcid);
				wii = getWorkitem(wlist, prcid, "id_apply_leaving");
				assertTrue(wlist.size() > 0);
				assertTrue(wii != null);

				// 完成这个work
				client.doTask("T001", prcid, (String) wii.get("NODEID"), (String) wii.get("SESSID"), null, null);

				wlist = client.getWorklist("T002", prcid);
				assertTrue(wlist.size() > 0);
				wii = getWorkitem(wlist, prcid, "id_approve_leaving");
				assertTrue(wii != null);

				client.doTask("T002", (String) wii.get("PRCID"), (String) wii.get("NODEID"), (String) wii.get("SESSID"), "Reject", null);

				wlist = client.getWorklist("T001");
				wii = getWorkitem(wlist, prcid, "id_rejected");
				assertTrue(wii != null);
				assertEquals(wii.get("WORKNAME"), "Rejected");
				client.doTask("T001", (String) wii.get("PRCID"), (String) wii.get("NODEID"), (String) wii.get("SESSID"), null, null);

				wlist = client.getWorklist("T002");
				wii = getWorkitem(wlist, prcid, "id_rejected2");
				assertTrue(wii == null);

				// 再次看这个进程的状态，应该是完成状态
				prcJson = client.getPrcInfo(prcid);
				assertEquals("finished", prcJson.get("STATUS"));
			} finally {

				if (prcid != null)
					client.deleteProcess(prcid);
				client.deleteTeamById(teamid);
				client.deleteWft(wftid);

				cleanHouse(client);
			}

		} finally {
			unloadTestUsers(client);
		}
	}

	// 测试 Option, AND_OR, Variables, Script执行，以及script回写参数值
	// script回写的例子是com.lkh.cflow.test.MyLiner, 通过 java类型的script来调用
	public void test_script() throws Exception {
		client = AWEClient.newInstance("tester1", "tester1");
		loadTestUsers(client);
		try {

			String wft_script_java = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
					+ "<node id=\"id_start\" type=\"start\" title=\"Start\" name=\"Start\" x=\"27\" y=\"213\">\n <next targetID=\"id_apply_leaving\"/></node>" + "<node id=\"id_end\" type=\"end\" title=\"End\" name=\"End\" x=\"738\" y=\"213\">\n <next targetID=\"id_end\"/></node>"

					+ "<node id=\"id_apply_leaving\" type=\"task\" title=\"Apply Leaving\" name=\"Apply Leaving\"  acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n " + "<attachment type=\"int\" label=\"Leave days\" attname=\"days\" value=\"\"/>" + "<attachment type=\"String\" label=\"Leave reason\" attname=\"reason\" value=\"\"/>"
					+ "<taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n " + "<next targetID=\"id_reason\"/></node>"

					+ "<node id=\"id_reason\" type=\"task\" title=\"Give Reason\" name=\"Give Reason\">" + "<attachment type=\"String\" label=\"Leave reason\" attname=\"reason\" value=\"\"/>" + "<taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n " + "<next targetID=\"id_script\"/>\n " + "</node>"

					+ "<node id='id_script' type='script'><script>JAVA:com.lkh.cflow.test.MyJavaAdapter</script>" + "<next option='long' targetID='id_long'/>" + "<next option='short' targetID='id_short'/>" + "</node>"

					+ "<node id='id_long' type='task' name='LONG'>" + "<taskto type=\"RefertoNode\" whom=\"id_apply_leaving\"/>" + "<next targetID='id_end'/>" + "</node>"

					+ "<node id='id_short' type='task' name='SHORT'>" + "<taskto type=\"RefertoNode\" whom=\"id_apply_leaving\"/>" + "<next targetID='id_end'/>" + "</node>"

					+ "</cf:workflow>";

			String wft_script_web = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
					+ "<node id=\"id_start\" type=\"start\" title=\"Start\" name=\"Start\" x=\"27\" y=\"213\">\n <next targetID=\"id_apply_leaving\"/></node>" + "<node id=\"id_end\" type=\"end\" title=\"End\" name=\"End\" x=\"738\" y=\"213\">\n <next targetID=\"id_end\"/></node>"

					+ "<node id=\"id_apply_leaving\" type=\"task\" title=\"Apply Leaving\" name=\"Apply Leaving\"  acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n " + "<attachment type=\"int\" label=\"Leave days\" attname=\"days\" value=\"\"/>" + "<attachment type=\"String\" label=\"Leave Reason\" attname=\"reason\" value=\"\"/>"
					+ "<taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n " + "<next targetID=\"id_script\"/>\n " + "</node>"

					+ "<node id='id_script' type='script'><script>URL:http://" + client.getServer() + "/cflow/TestScriptWeb</script>" + "<next option='long' targetID='id_long'/>" + "<next option='short' targetID='id_short'/>" + "</node>"

					+ "<node id='id_long' type='task' name='LONG'>" + "<taskto type=\"RefertoNode\" whom=\"id_apply_leaving\"/>" + "<next targetID='id_end'/>" + "</node>"

					+ "<node id='id_short' type='task' name='SHORT'>" + "<taskto type=\"RefertoNode\" whom=\"id_apply_leaving\"/>" + "<next targetID='id_end'/>" + "</node>"

					+ "</cf:workflow>";
			String wft_script_javascript = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
					+ "<node id=\"id_start\" type=\"start\" title=\"Start\" name=\"Start\" x=\"27\" y=\"213\">\n <next targetID=\"id_apply_leaving\"/></node>" + "<node id=\"id_end\" type=\"end\" title=\"End\" name=\"End\" x=\"738\" y=\"213\">\n <next targetID=\"id_end\"/></node>"

					+ "<node id=\"id_apply_leaving\" type=\"task\" title=\"Apply Leaving\" name=\"Apply Leaving\"  acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">\n " + "<attachment type=\"int\" label=\"Leave days\" attname=\"days\" value=\"\"/>" + "<taskto type=\"role\" whom=\"starter\"/>\n <mpcdc/>\n <oec/>\n " + "<next targetID=\"id_script\"/>\n " + "</node>"

					+ "<node id='id_script' type='script'><script>if(days>10) data.RETURN=\"long\"; else data.RETURN=\"short\"; </script>" + "<next option='long' targetID='id_long'/>" + "<next option='short' targetID='id_short'/>" + "</node>"

					+ "<node id='id_long' type='task' name='LONG'>" + "<taskto type=\"RefertoNode\" whom=\"id_apply_leaving\"/>" + "<next targetID='id_end'/>" + "</node>"

					+ "<node id='id_short' type='task' name='SHORT'>" + "<taskto type=\"RefertoNode\" whom=\"id_apply_leaving\"/>" + "<next targetID='id_end'/>" + "</node>"

					+ "</cf:workflow>";

			String wftid_script_java = client.uploadWft(wft_script_java, "testProcess_5_script_java");
			String wftid_script_web = client.uploadWft(wft_script_web, "testProcess_5_script_web");
			String wftid_script_javascript = client.uploadWft(wft_script_javascript, "testProcess_5_script_JS");

			// 新建一个Team
			String teamid = client.createTeam("test_team2", "test_team2_memo");
			// 指派团队成员
			JSONObject members = new JSONObject();
			members.put("T002", "Approver");
			members.put("T003", "Auditor");
			client.addTeamMembers(teamid, members);
			JSONObject prcJson = null;
			JSONArray wlist = null;
			JSONObject theWii = null;
			// 启动进程
			String prcid = null;
			try {
				prcid = client.startWorkflow("T001", wftid_script_java, teamid, "testProcess_5_inst", null);
				assertFalse(prcid.startsWith("ERROR"));

				// id_apply_leaving
				wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				theWii = getWorkitem(wlist, prcid, "id_apply_leaving");
				assertTrue(theWii != null);
				client.doTask("T001", prcid, (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, "{\"days\":\"11\", \"reason\":\"gohome\", \"var3\":\"value3\"}");

				wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				theWii = getWorkitem(wlist, prcid, "id_reason");
				assertTrue(theWii != null);
				client.doTask("T001", prcid, (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, "{ \"reason\":\"gohome2\"}");

				wlist = client.getWorklist("T001", prcid);
				theWii = getWorkitem(wlist, prcid, "id_long");
				assertTrue(theWii != null);
				assertEquals(theWii.get("WORKNAME"), "LONG");
				client.doTask("T001", (String) theWii.get("PRCID"), (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, null);

				// 再次看这个进程的状态，应该是完成状态
				prcJson = client.getPrcInfo(prcid);
				assertEquals("finished", prcJson.get("STATUS"));
			} finally {
				// 删除这个新进程
				if (prcid != null)
					client.deleteProcess(prcid);
			}

			prcid = null;
			try {
				// 启动进程
				prcid = client.startWorkflow("T001", wftid_script_java, teamid, "testProcess_5", null);
				assertFalse(prcid.startsWith("ERROR"));

				// id_apply_leaving
				wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				theWii = getWorkitem(wlist, prcid, "id_apply_leaving");
				assertTrue(theWii != null);
				client.doTask("T001", prcid, (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, "{\"days\":\"11\", \"reason\":\"gohome\"}");

				wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				theWii = getWorkitem(wlist, prcid, "id_reason");
				assertTrue(theWii != null);

				JSONObject beforeScript = client.getPrcVariables(prcid);
				// 下面一个task完成后，将自动调用script活动
				client.doTask("T001", prcid, (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, "{\"days\":\"9\", \"reason\":\"gohome2\"}");
				// Here, the script node will be executed.
				// com.lkh.cflow.test.MyLinker.
				// will write "test value to change back to process attachment" back
				// to
				// “reason”
				JSONObject afterScript = client.getPrcVariables(prcid);
				assertTrue(beforeScript.get("reason").equals("gohome"));
				assertTrue(afterScript.get("reason").equals("test value to change back to process attachment."));
				assertTrue(afterScript.get("ignored") != null);

				wlist = client.getWorklist("T001", prcid);
				theWii = getWorkitem(wlist, prcid, "id_short");
				assertTrue(theWii != null);
				assertEquals(theWii.get("WORKNAME"), "SHORT");
				client.doTask("T001", (String) theWii.get("PRCID"), (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, null);

				// 再次看这个进程的状态，应该是完成状态
				prcJson = client.getPrcInfo(prcid);
				assertEquals("finished", prcJson.get("STATUS"));
			} finally {
				// 删除这个新进程
				if (prcid != null)
					client.deleteProcess(prcid);
			}

			prcid = null;
			try {
				// 启动进程
				prcid = client.startWorkflow("T001", wftid_script_web, teamid, "testProcess_5", null);
				assertFalse(prcid.startsWith("ERROR"));

				// id_apply_leaving
				wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				theWii = getWorkitem(wlist, prcid, "id_apply_leaving");
				assertTrue(theWii != null);
				client.doTask("T001", prcid, (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, "{\"days\":\"11\", \"reason\":\"gohome\"}");

				wlist = client.getWorklist("T001", prcid);
				theWii = getWorkitem(wlist, prcid, "id_long");
				assertTrue(theWii != null);
				assertEquals(theWii.get("WORKNAME"), "LONG");
				client.doTask("T001", (String) theWii.get("PRCID"), (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, null);

				// 再次看这个进程的状态，应该是完成状态
				prcJson = client.getPrcInfo(prcid);
				assertEquals("finished", prcJson.get("STATUS"));
			} finally {
				// 删除这个新进程
				if (prcid != null)
					client.deleteProcess(prcid);
			}
			prcid = null;
			try {

				// 启动进程
				prcid = client.startWorkflow("T001", wftid_script_web, teamid, "testProcess_5", null);
				assertFalse(prcid.startsWith("ERROR"));

				// id_apply_leaving
				wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				theWii = getWorkitem(wlist, prcid, "id_apply_leaving");
				assertTrue(theWii != null);
				client.doTask("T001", prcid, (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, "{\"days\":\"6\", \"reason\":\"gohome\"}");

				wlist = client.getWorklist("T001", prcid);
				theWii = getWorkitem(wlist, prcid, "id_short");
				assertTrue(theWii != null);
				assertEquals(theWii.get("WORKNAME"), "SHORT");
				client.doTask("T001", (String) theWii.get("PRCID"), (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, null);

				// 再次看这个进程的状态，应该是完成状态
				prcJson = client.getPrcInfo(prcid);
				assertEquals("finished", prcJson.get("STATUS"));
			} finally {
				// 删除这个新进程
				if (prcid != null)
					client.deleteProcess(prcid);
			}
			prcid = null;
			try {

				// 启动进程
				prcid = client.startWorkflow("T001", wftid_script_javascript, teamid, "testProcess_5", null);
				assertFalse(prcid.startsWith("ERROR"));

				// id_apply_leaving
				wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				theWii = getWorkitem(wlist, prcid, "id_apply_leaving");
				assertTrue(theWii != null);
				client.doTask("T001", prcid, (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, "{\"days\":\"12\", \"reason\":\"gohome\"}");

				wlist = client.getWorklist("T001", prcid);
				theWii = getWorkitem(wlist, prcid, "id_long");
				assertTrue(theWii != null);
				assertEquals(theWii.get("WORKNAME"), "LONG");
				client.doTask("T001", (String) theWii.get("PRCID"), (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, null);

				// 再次看这个进程的状态，应该是完成状态
				prcJson = client.getPrcInfo(prcid);
				assertEquals("finished", prcJson.get("STATUS"));
			} finally {
				// 删除这个新进程
				if (prcid != null)
					client.deleteProcess(prcid);
			}
			prcid = null;
			try {

				// 启动进程
				prcid = client.startWorkflow("T001", wftid_script_javascript, teamid, "testProcess_5", null);
				assertFalse(prcid.startsWith("ERROR"));

				// id_apply_leaving
				wlist = client.getWorklist("T001", prcid);
				assertTrue(wlist.size() > 0);
				theWii = getWorkitem(wlist, prcid, "id_apply_leaving");
				assertTrue(theWii != null);
				client.doTask("T001", prcid, (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, "{\"days\":\"6\", \"reason\":\"gohome\"}");

				wlist = client.getWorklist("T001");
				theWii = getWorkitem(wlist, prcid, "id_short");
				assertTrue(theWii != null);
				assertEquals(theWii.get("WORKNAME"), "SHORT");
				client.doTask("T001", (String) theWii.get("PRCID"), (String) theWii.get("NODEID"), (String) theWii.get("SESSID"), null, null);

				// 再次看这个进程的状态，应该是完成状态
				prcJson = client.getPrcInfo(prcid);
				assertEquals("finished", prcJson.get("STATUS"));
			} finally {
				// 删除这个新进程
				if (prcid != null)
					client.deleteProcess(prcid);

				// 删除前面测试创建的Team
				client.deleteTeamById(teamid);

				// 删除这个模板
				client.deleteWft(wftid_script_java);
				client.deleteWft(wftid_script_web);
			}

		} finally {
			unloadTestUsers(client);
		}
	}

	public void test_child() throws Exception {
		client = AWEClient.newInstance("tester1", "tester1");
		loadTestUsers(client);
		try {
			cleanHouse(client);
			String wft_child = null;
			String wftid_child = null;
			String wft_parent_wrong = null;
			String wftid_wrong = null;
			String wftid_right = null;
			String teamid = null;
			String prcid_wrong = null;
			String prcid_right = null;
			String prcid_child = null;
			try {
				// 进程中一个人工活动，一个javascript, 这个JavaScript中返回了RETURN_TO_PARENT,
				// 将影响父进程中的流程走向
				wft_child = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + " <node id=\"id_start\" type=\"start\" title=\"Start\" name=\"Start\" >" + " <next targetID=\"id_1\"/>" + " </node>"
						+ " <node id=\"id_end\" type=\"end\" title=\"End\" name=\"End\" >" + " <next targetID=\"id_end\"/>" + " </node>" + " <node id=\"id_1\" type=\"task\" title=\"Task\" name=\"Task\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">" + " <taskto type=\"role\" whom=\"starter\"/>" + " <mpcdc/>" + " <oec/>" + " <next targetID=\"id_script\"/>" + " </node>"
						+ " <node id=\"id_script\" type=\"script\" title=\"Script\" name=\"Script\" >" + " <script>data.RETURN=\"long\"; data.RETURN_TO_PARENT=\"good\";</script>" + " <next targetID=\"id_end\"/>" + " </node>"

						+ "</cf:workflow>";

				wftid_child = client.uploadWft(wft_child, "childwft");
				assertTrue(wftid_child != null);

				// 这个父亲工作流，其中的子流程ID编号错误。CFLOW自动根据onerror确定该子流程节点后的流程走向
				wft_parent_wrong = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + "<node id=\"E8D8B454-699A-4050-293A-E0830EEB4B8D\" type=\"start\" title=\"Start\" name=\"Start\" x=\"28\" y=\"228\">"
						+ "<next targetID=\"id_node_script\"/>" + "</node>" + "<node id=\"id_end\" type=\"end\" title=\"End\" name=\"End\" x=\"708\" y=\"228\">" + "<next targetID=\"id_end\"/>" + "</node>" + "<node id=\"id_node_script\" type=\"sub\" title=\"abcd\" name=\"Sub\" x=\"171\" y=\"225\" subWftUID=\"" + "WRONGCHILDWFTID" + "\" subWftWG=\"\">" + "<next option=\"good\" targetID=\"id_good\"/>" + "<next option=\"bad\" targetID=\"id_bad\"/>"
						+ "<next option=\"onerror\" targetID=\"id_child_fail\"/>" + "<next option=\"DEFAULT\" targetID=\"id_default\"/>" + "</node>" + "<node id=\"id_good\" type=\"task\" title=\"Task\" name=\"GOOD\" x=\"316\" y=\"164\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">" + "<taskto type=\"role\" whom=\"starter\"/>" + "<mpcdc/>" + "<oec/>" + "<next targetID=\"id_end\"/>"
						+ "</node>" + "<node id=\"id_bad\" type=\"task\" title=\"Task\" name=\"BAD\" x=\"327\" y=\"333\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">" + "<taskto type=\"role\" whom=\"starter\"/>" + "<mpcdc/>" + "<oec/>" + "<next targetID=\"id_end\"/>" + "</node>"
						+ "<node id=\"id_child_fail\" type=\"task\" title=\"Task\" name=\"child fail\" x=\"327\" y=\"333\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">" + "<taskto type=\"role\" whom=\"starter\"/>" + "<mpcdc/>" + "<oec/>" + "<next targetID=\"id_end\"/>" + "</node>"
						+ "<node id=\"id_default\" type=\"task\" title=\"Task\" name=\"DEFAULT\" x=\"327\" y=\"333\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">" + "<taskto type=\"role\" whom=\"starter\"/>" + "<mpcdc/>" + "<oec/>" + "<next targetID=\"id_end\"/>" + "</node>" + "</cf:workflow>";

				// 这个流程是一个正确的父流程。因前面子流程RETURN_TO_PARENT=good. 所以，将执行后面的id_good人工活动
				String wft_parent_right = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cf:workflow xsi:schemaLocation=\"http://lkh.com/cflow ../schemas/wft.xsd\" name=\"tobedelete\" owner=\"LKH\" acl=\"private\" lastModified=\"2011-03-19T04:18:58\" created=\"2011-03-19T04:18:58\" xmlns:cf=\"http://lkh.com/cflow\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + "<node id=\"E8D8B454-699A-4050-293A-E0830EEB4B8D\" type=\"start\" title=\"Start\" name=\"Start\" x=\"28\" y=\"228\">"
						+ "<next targetID=\"id_node_script\"/>" + "</node>" + "<node id=\"id_end\" type=\"end\" title=\"End\" name=\"End\" x=\"708\" y=\"228\">" + "<next targetID=\"id_end\"/>" + "</node>" + "<node id=\"id_node_script\" type=\"sub\" title=\"abcd\" name=\"Sub\" x=\"171\" y=\"225\" subWftUID=\""
						+ wftid_child
						+ "\" subWftWG=\"\">"
						+ "<next option=\"good\" targetID=\"id_good\"/>"
						+ "<next option=\"bad\" targetID=\"id_bad\"/>"
						+ "<next option=\"onerror\" targetID=\"id_child_fail\"/>"
						+ "<next option=\"DEFAULT\" targetID=\"id_default\"/>"
						+ "</node>"
						+ "<node id=\"id_good\" type=\"task\" title=\"Task\" name=\"GOOD\" x=\"316\" y=\"164\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">"
						+ "<taskto type=\"role\" whom=\"starter\"/>"
						+ "<mpcdc/>"
						+ "<oec/>"
						+ "<next targetID=\"id_end\"/>"
						+ "</node>"
						+ "<node id=\"id_bad\" type=\"task\" title=\"Task\" name=\"BAD\" x=\"327\" y=\"333\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">"
						+ "<taskto type=\"role\" whom=\"starter\"/>"
						+ "<mpcdc/>"
						+ "<oec/>"
						+ "<next targetID=\"id_end\"/>"
						+ "</node>"
						+ "<node id=\"id_child_fail\" type=\"task\" title=\"Task\" name=\"child fail\" x=\"327\" y=\"333\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">"
						+ "<taskto type=\"role\" whom=\"starter\"/>"
						+ "<mpcdc/>"
						+ "<oec/>"
						+ "<next targetID=\"id_end\"/>"
						+ "</node>"
						+ "<node id=\"id_default\" type=\"task\" title=\"Task\" name=\"DEFAULT\" x=\"327\" y=\"333\" acquirable=\"false\" acqThreshold=\"1\" allowRoleChange=\"false\" allowDelegate=\"false\" allowAdhoc=\"false\" roleToChange=\"all\" form=\"\" mpsm=\"1\">" + "<taskto type=\"role\" whom=\"starter\"/>" + "<mpcdc/>" + "<oec/>" + "<next targetID=\"id_end\"/>" + "</node>" + "</cf:workflow>";

				wftid_wrong = client.uploadWft(wft_parent_wrong, "parentwft");
				assertTrue(wftid_wrong != null);
				wftid_right = client.uploadWft(wft_parent_right, "parentwft");
				assertTrue(wftid_right != null);

				// 新建一个Team
				teamid = client.createTeam("test_team2", "test_team2_memo");
				// 指派团队成员
				JSONObject members = new JSONObject();
				members.put("T002", "Approver");
				members.put("T003", "Auditor");
				client.addTeamMembers(teamid, members);

				JSONObject wii = null;
				JSONArray wlist = null;
				prcid_wrong = null;
				JSONObject prcJson = null;
				try {
					// 启动进程
					prcid_wrong = client.startWorkflow("T001", wftid_wrong, teamid, "testProcess_5_inst", null);
					assertFalse(prcid_wrong.startsWith("ERROR"));

					wlist = client.getWorklist("T001", prcid_wrong);
					assertTrue(wlist.size() > 0);
					wii = getWorkitem(wlist, prcid_wrong, "id_child_fail");
					client.doTask("T001", prcid_wrong, (String) wii.get("NODEID"), (String) wii.get("SESSID"), null, null);
					// 再次看这个进程的状态，应该是完成状态
					prcJson = client.getPrcInfo(prcid_wrong);
					assertEquals("finished", prcJson.get("STATUS"));
				} finally {
					if (prcid_wrong != null)
						client.deleteProcess(prcid_wrong);
				}
				//
				//
				//
				//
				//

				// 启动进程
				prcid_right = null;
				try {
					prcid_right = client.startWorkflow("T001", wftid_right, teamid, "testProcess_5_inst", null);
					assertFalse(prcid_right.startsWith("ERROR"));
					wlist = client.getWorklist("T001");
					wii = null;
					for (int i = 0; i < wlist.size(); i++) {
						JSONObject tmp = (JSONObject) wlist.get(i);
						if (tmp.get("NODEID").equals("id_1") && tmp.get("PPID").equals(prcid_right)) {
							wii = tmp;
							prcid_child = (String) tmp.get("PRCID");
							break;
						}
					}
					assertTrue(wii != null);
					client.doTask("T001", prcid_child, (String) wii.get("NODEID"), (String) wii.get("SESSID"), null, null);

					wlist = client.getWorklist("T001", prcid_right);
					assertTrue(wlist.size() > 0);
					wii = getWorkitem(wlist, prcid_right, "id_good");
					assertTrue(wii != null);
					client.doTask("T001", prcid_right, (String) wii.get("NODEID"), (String) wii.get("SESSID"), null, null);

					// 再次看这个进程的状态，应该是完成状态
					prcJson = client.getPrcInfo(prcid_right);
					assertEquals("finished", prcJson.get("STATUS"));
				} finally {
					if (prcid_right != null)
						client.deleteProcess(prcid_right);

					if (prcid_child != null)
						client.deleteProcess(prcid_child);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {

				// 删除前面测试创建的Team
				client.deleteTeamById(teamid);

				// 删除这个模板
				client.deleteWft(wftid_child);
				client.deleteWft(wftid_right);
				client.deleteWft(wftid_wrong);

				cleanHouse(client);
			}

		} finally {
			unloadTestUsers(client);
		}
	}

	private void assertAcsk(String token) {
		assertFalse(token == null);
	}

	private void cleanHouse(AWEClient client) {
		try {

			String[] status = new String[3];
			status[0] = "running";
			status[1] = "finished";
			status[2] = "canceled";
			for (int s = 0; s < 3; s++) {
				JSONArray tmp = client.getProcessesByStatus(status[s]);
				for (int i = 0; i < tmp.size(); i++) {
					JSONObject prcInfo = (JSONObject) tmp.get(i);
					String tmpPrcId = (String) prcInfo.get("PRCID");
					String tmpWftId = (String) prcInfo.get("WFTID");
					client.deleteProcess(tmpPrcId);
					client.deleteWft(tmpWftId);
				}
			}

			JSONArray tmp = client.getWfts();
			for (int i = 0; i < tmp.size(); i++) {
				JSONObject info = (JSONObject) tmp.get(i);
				String tmpWftId = (String) info.get("WFTID");
				client.deleteWft(tmpWftId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadTestUsers(AWEClient client) throws Exception {
		client.addUser("T001", "T001", "T001@null.com", "GMT+08:00", "zh-CN", "E");
		client.addUser("T002", "T002", "T002@null.com", "GMT+08:00", "zh-CN", "E");
		client.addUser("T003", "T003", "T003@null.com", "GMT+08:00", "zh-CN", "E");
		client.addUser("T004", "T004", "T004@null.com", "GMT+08:00", "zh-CN", "E");
		client.addUser("T005", "T005", "T005@null.com", "GMT+08:00", "zh-CN", "E");
	}

	private void unloadTestUsers(AWEClient client) throws Exception {
		client.deleteUser("T001");
		client.deleteUser("T002");
		client.deleteUser("T003");
		client.deleteUser("T004");
		client.deleteUser("T005");
	}

	private void inpsToOups(InputStream inps, OutputStream oups) {
		byte[] buffer = new byte[1024];
		int i = 0;
		try {
			while ((i = inps.read(buffer)) != -1) {
				oups.write(buffer, 0, i);
			}// end while
		} catch (Exception ex) {
			logger.error(ex.getLocalizedMessage());
		} finally {
			try {
				inps.close();
				oups.flush();
				oups.close();
			} catch (IOException ex) {
				logger.error(ex.getLocalizedMessage());
				ex.printStackTrace();
			}
		}
	}

	private JSONObject getWorkitem(JSONArray wlist, String prcId, String nodeId) {
		JSONObject wii = null;
		for (int i = 0; i < wlist.size(); i++) {
			JSONObject tmp = (JSONObject) wlist.get(i);
			if (tmp.get("PRCID").equals(prcId) && tmp.get("NODEID").equals(nodeId)) {
				wii = tmp;
				break;
			}
		}
		return wii;
	}

}
