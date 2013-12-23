package com.lkh.mwfclient.test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.lkh.mwfclient.AWEClient;

public class Burnit {
	AWEClient awe = AWEClient.newInstance("aliyun", "psammead");

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		Burnit burnit = new Burnit();
		ExecutorService executor = Executors.newCachedThreadPool();
		try {
			for (int i = 0; i < 3; i++) {
				System.out.println(Thread.currentThread().getId() + "\tround:" + i);
				// runTask aTask = burnit.new runTask();
				// executor.execute(aTask);
				Task aTask = burnit.new Task();
				Future<String> future = executor.submit(aTask);
				Object obj = future.get(60, TimeUnit.SECONDS);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			executor.shutdownNow();
		}

	}

	public class runTask implements Runnable {
		@Override
		public void run() {
			Burnit burnit = new Burnit();
			for (int i = 0; i < 1; i++) {
				try {
					burnit.burnDemo_TIMEOUT();
					Thread.sleep(50);
				} catch (Exception ex) {
					// ex.printStackTrace();
				}
				try {
					burnit.burnDemo_WEB();
					Thread.sleep(50);
				} catch (Exception ex) {
					// ex.printStackTrace();
				}
				try {
					burnit.burnLeaveApplication();
					Thread.sleep(50);
				} catch (Exception ex) {
					// ex.printStackTrace();
				}
				try {
					burnit.burnVoucherAdmin();
					Thread.sleep(50);
				} catch (Exception ex) {
					// ex.printStackTrace();
				}
				try {
					burnit.burnSJYJ();
					Thread.sleep(50);
				} catch (Exception ex) {
					// ex.printStackTrace();
				}
			}

		}
	}

	public class Task implements Callable<String> {
		@Override
		public String call() throws Exception {
			Burnit burnit = new Burnit();
			for (int i = 0; i < 1; i++) {
				try {
					burnit.burnDemo_TIMEOUT();
					Thread.sleep(50);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				try {
					burnit.burnDemo_WEB();
					Thread.sleep(50);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				try {
					burnit.burnLeaveApplication();
					Thread.sleep(50);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				try {
					burnit.burnVoucherAdmin();
					Thread.sleep(50);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				try {
					burnit.burnSJYJ();
					Thread.sleep(50);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			return "";
		}
	}

	private void burnDemo_WEB() throws Exception {
		System.out.println(Thread.currentThread().getId() + "\tburnDemo_WEB");
		String wftName = "Demo_WEB";
		String teamName = null;
		String instanceName = "v1";
		JSONObject ctx = null;
		String actor = null;

		AWEClient awe = AWEClient.newInstance("aliyun", "psammead");

		actor = "peixin.baipx";
		String prcid = awe.startWorkflowByName(actor, wftName, teamName, instanceName, ctx);
		try {
			JSONObject input = new JSONObject();
			input.put("days", 20);
			input.put("reason", "go home");
			JSONArray wlist = awe.getWorklist(actor);
			JSONObject wii = getWii(wlist, prcid, actor);
			doTask(actor, prcid, (String) wii.get("NODEID"), (String) wii.get("SESSID"), "", input.toString());

			actor = "peixin.baipx";
			wlist = awe.getWorklist(actor);
			wii = getWii(wlist, prcid, actor);
			// System.out.println("20>10 should go to Long Leave, get " +
			// wii.get("NAME"));
			doTask(actor, prcid, (String) wii.get("NODEID"), (String) wii.get("SESSID"), "", null);

			JSONObject prcInfo = awe.getPrcInfo(prcid);
			// System.out.println("Process status: " + prcInfo.get("STATUS"));
		} finally {
			awe.deleteProcess(prcid);
		}

	}

	private void burnDemo_TIMEOUT() throws Exception {
		String wftName = "Demo_WEB_TIMEOUT";
		String teamName = null;
		String instanceName = "v1";
		JSONObject ctx = null;
		String actor = null;

		System.out.println(Thread.currentThread().getId() + "\tburnDemo_TIMEOUT");

		// loadTestUsers(token);
		actor = "peixin.baipx";
		String prcid = awe.startWorkflowByName(actor, wftName, teamName, instanceName, ctx);
		try {
			JSONObject input = new JSONObject();
			input.put("days", 20);
			input.put("reason", "go home");
			JSONArray wlist = awe.getWorklist(actor);
			JSONObject wii = getWii(wlist, prcid, actor);
			doTask(actor, prcid, (String) wii.get("NODEID"), (String) wii.get("SESSID"), "", input.toString());

			actor = "peixin.baipx";
			wlist = awe.getWorklist(actor);
			wii = getWii(wlist, prcid, actor);
			// System.out.println("Should go to Error, get " + wii.get("NAME"));
			doTask(actor, prcid, (String) wii.get("NODEID"), (String) wii.get("SESSID"), "", null);

			JSONObject prcInfo = awe.getPrcInfo(prcid);
			// System.out.println("Process status: " + prcInfo.get("STATUS"));
		} finally {
			awe.deleteProcess(prcid);
		}

	}

	private void burnSJYJ() throws Exception {
		String wftName = "数据英佳";
		String teamName = null;
		String instanceName = "v1";
		JSONObject ctx = null;
		String actor = null;

		System.out.println(Thread.currentThread().getId() + "\tburnSJYJ");
		// loadTestUsers(token);
		actor = "peixin.baipx";
		String prcid = awe.startWorkflowByName(actor, wftName, teamName, instanceName, ctx);
		try {
			JSONObject input = new JSONObject();
			input.put("month", 2);
			JSONArray wlist = awe.getWorklist(actor);
			JSONObject wii = getWii(wlist, prcid, actor);
			doTask(actor, prcid, (String) wii.get("NODEID"), (String) wii.get("SESSID"), "", input.toString());

			actor = "peixin.baipx";
			wlist = awe.getWorklist(actor);
			wii = getWii(wlist, prcid, actor);
			input = new JSONObject();
			input.put("methodG1", "代金券");
			input.put("methodG2", "优惠价");
			doTask(actor, prcid, (String) wii.get("NODEID"), (String) wii.get("SESSID"), "", input.toString());

			actor = "peixin.baipx";
			wlist = awe.getWorklist(actor);
			wii = getWii(wlist, prcid, actor);
			doTask(actor, prcid, (String) wii.get("NODEID"), (String) wii.get("SESSID"), "", null);

			JSONObject prcInfo = awe.getPrcInfo(prcid);
			// System.out.println("Process status: " + prcInfo.get("STATUS"));
		} finally {
			awe.deleteProcess(prcid);
		}
	}

	private void burnLeaveApplication() throws Exception {
		String wftName = "Leave Application";
		String teamName = "LeaveApprovalTeam";
		String instanceName = "v1";
		JSONObject ctx = null;
		String actor = null;

		System.out.println(Thread.currentThread().getId() + "\tburnLeaveApplication");
		// loadTestUsers(token);
		actor = "peixin.baipx";
		String prcid = awe.startWorkflowByName(actor, wftName, teamName, instanceName, ctx);
		try {
			JSONObject input = new JSONObject();
			input.put("days", 11);
			input.put("reason", "go home");
			JSONArray wlist = awe.getWorklist(actor);
			JSONObject wii = getWii(wlist, prcid, actor);
			doTask(actor, prcid, (String) wii.get("NODEID"), (String) wii.get("SESSID"), "", input.toString());

			actor = "allen";
			wlist = awe.getWorklist(actor);
			wii = getWii(wlist, prcid, actor);
			input = new JSONObject();
			input.put("approveComment", actor + "-Okay");
			doTask(actor, prcid, (String) wii.get("NODEID"), (String) wii.get("SESSID"), "1.Approve it.", input.toString());

			actor = "peixin.baipx";
			wlist = awe.getWorklist(actor);
			wii = getWii(wlist, prcid, actor);
			doTask(actor, prcid, (String) wii.get("NODEID"), (String) wii.get("SESSID"), "", null);

			JSONObject prcInfo = awe.getPrcInfo(prcid);
			// System.out.println("Process status: " + prcInfo.get("STATUS"));
		} finally {
			awe.deleteProcess(prcid);
		}
	}

	/**
	 * @throws Exception
	 */
	private void burnVoucherAdmin() throws Exception {
		String wftName = "Voucher Application";
		String teamName = "VoucherAdmin1";
		String instanceName = "v1";
		JSONObject ctx = null;
		String actor = null;
		System.out.println(Thread.currentThread().getId() + "\tburnVoucherAdmin");
		// loadTestUsers(token);
		actor = "peixin.baipx";
		String prcid = awe.startWorkflowByName(actor, wftName, teamName, instanceName, ctx);
		try {
			JSONObject input = new JSONObject();
			input.put("projectName", "myProject");
			input.put("purpose", "Promotion");
			input.put("number", 100);
			input.put("due", "2012-12-31");
			input.put("eventpurpose", "sell 1000");
			input.put("eventrule", "rule");
			input.put("atturl", "http://www");
			input.put("memo", "thanks");
			input.put("eventdate", "2012-11-15");
			input.put("figure", 100);
			input.put("service", "VM");
			JSONArray wlist = awe.getWorklist(actor);
			JSONObject wii = getWii(wlist, prcid, actor);
			doTask(actor, prcid, (String) wii.get("NODEID"), (String) wii.get("SESSID"), "", input.toString());

			actor = "allen";
			wlist = awe.getWorklist(actor);
			wii = getWii(wlist, prcid, actor);
			input = new JSONObject();
			input.put("1stapproveComment", actor + "-Okay");
			doTask(actor, prcid, (String) wii.get("NODEID"), (String) wii.get("SESSID"), "Approve", input.toString());

			actor = "huali.shihl";
			wlist = awe.getWorklist(actor);
			wii = getWii(wlist, prcid, actor);
			input = new JSONObject();
			input.put("voucherNumber", actor + "-100-1000");
			doTask(actor, prcid, (String) wii.get("NODEID"), (String) wii.get("SESSID"), "", input.toString());

			actor = "peixin.baipx";
			wlist = awe.getWorklist(actor);
			wii = getWii(wlist, prcid, actor);
			doTask(actor, prcid, (String) wii.get("NODEID"), (String) wii.get("SESSID"), "", null);

			JSONObject prcInfo = awe.getPrcInfo(prcid);
			// System.out.println("Process status: " + prcInfo.get("STATUS"));
		} finally {
			awe.deleteProcess(prcid);
		}
	}

	private void doTask(String actor, String prcId, String nodeId, String sessId, String option, String input) throws Exception {
		awe.doTask(actor, prcId, nodeId, sessId, option, input);
		// System.out.println(actor + " do task with option[" + option +
		// "] input[" + input + "]");
	}

	private JSONObject getWii(JSONArray wlist, String prcid, String actor) {
		JSONObject wii = null;
		for (int i = 0; i < wlist.size(); i++) {
			JSONObject tmp = (JSONObject) wlist.get(i);
			if (tmp.get("PRCID").equals(prcid) && tmp.get("DOER").equals(actor)) {
				wii = tmp;
				break;
			}
		}
		return wii;
	}

	private void loadTestUsers(AWEClient awe) throws Exception {
		awe.addUser("peixin.baipx", "Bai Peixin", "peixin.baipx@alibaba-inc.com", "GMT+08:00", "zh-CN", "E");
		awe.addUser("allen", "Zhang Jing", "allen@alibaba-inc.com", "GMT+08:00", "zh-CN", "E");
		awe.addUser("cjp1957", "Chen Jinpei", "cjp1957@alibaba-inc.com", "GMT+08:00", "zh-CN", "E");
		awe.addUser("huali.shihl", "Shi Huali", "huali.shihl@alibaba-inc.com", "GMT+08:00", "zh-CN", "E");
		awe.addUser("kehong.liu", "Liu Kehong", "kehong.liu@alibaba-inc.com", "GMT+08:00", "zh-CN", "E");
		awe.addUser("nick.yixy", "Yi Xinyu", "nick.yixy@alibaba-inc.com", "GMT+08:00", "zh-CN", "E");
		awe.addUser("ying.zhouying", "Zhou Ying", "ying.zhouying@alibaba-inc.com", "GMT+08:00", "zh-CN", "E");
		awe.addUser("zhengyongsheng", "Zheng Yongsheng", "zhengyongsheng@alibaba-inc.com", "GMT+08:00", "zh-CN", "E");
	}
}
