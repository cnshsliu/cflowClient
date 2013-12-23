package com.lkh.mwfclient;

import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * AWEClient is a MWF Restful API wrapper class. AWEClient use <a href="http://hc.apache.org/">Apache HttpComponents</a> to invoke http request to MWF Server.
 * 
 * @author Nean
 * 
 * @version 0.9
 * 
 */
public class AWEClient {
	private static Logger logger = Logger.getLogger(AWEClient.class);
	private HttpClient httpclient = new DefaultHttpClient();
	private HttpContext localContext = new BasicHttpContext();
	private JSONParser parser = new JSONParser();
	private String lastToken = null;
	JSONArray masters = null;
	RemoteAccess ra = new RemoteAccess();
	private String lastMaster = null;
	private String lastServer = null;
	private String lastDev = null;
	private HashMap<String, Boolean> context = new HashMap<String, Boolean>();

	private String[] mwfMasters = null;

	public HashMap<String, Boolean> getContext() {
		return this.context;
	}

	public String getServer() {
		return lastServer;
	}

	/**
	 * Initiate a AWEClient instance.
	 * 
	 * @param hostname
	 *            The hostname of MWF server, for example, "www.myworldflow.com"
	 * @return a AWEClient instance
	 * @throws Exception
	 */
	public static AWEClient newInstance(String accessId, String accessKey) {
		AWEClient awe = null;
		try {
			awe = new AWEClient();
			awe.lastDev = accessId;
			JSONParser parser = new JSONParser();
			awe.masters = (JSONArray) parser.parse(new InputStreamReader(awe.getClass().getResourceAsStream("mwfmaster.cfg")));
			awe.mwfMasters = new String[awe.masters.size()];
			for (int i = 0; i < awe.masters.size(); i++) {
				JSONObject obj = (JSONObject) awe.masters.get(i);
				awe.mwfMasters[i] = (String) obj.get("host");
			}
			if (awe.lastServer == null) {
				awe.electMWFServer(awe.lastDev);
				logger.debug("Connect to " + awe.lastServer + " via " + awe.lastMaster);
			}
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("accessId", accessId));
			formparams.add(new BasicNameValuePair("accessKey", accessKey));

			URIBuilder builder = new URIBuilder();
			builder.setScheme("http").setHost(awe.lastServer).setPath("/cflow/rest/dev/login");
			RestResponse ret;
			try {
				ret = awe.myPost(builder.build(), formparams);
				if (ret.code != RestResponse.SUCCESS) {
					logger.error(ret.msg);
					awe = null;
				} else {
					awe.lastToken = ret.msg;
				}
			} catch (Exception e) {
				e.printStackTrace();
				awe = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			awe = null;
		}
		return awe;
	}

	/**
	 * Delete a workflow template on MWF server
	 * 
	 * @param token
	 *            current access session key
	 * @param wftid
	 *            the id of workflow template to be deleted.
	 * @return true if deleted successfully.
	 * @throws Exception
	 */
	public String deleteWft(String wftId) throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/wft").setParameter("wftid", wftId).setParameter("token", lastToken);
		RestResponse ret = myDelete(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		}
		return ret.msg;
	}

	/**
	 * Delete a process (workflow instance) by id
	 * 
	 * @param token
	 *            current token
	 * @param prcId
	 *            the id of process to be deleted
	 * @return true if deleted successfully
	 * @throws Exception
	 */
	public String deleteProcess(String prcId) throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/process").setParameter("prcid", prcId).setParameter("token", lastToken);
		RestResponse ret = myDelete(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			ret = null;
			throw new Exception(ret.msg);
		}

		return ret.msg;

	}

	/**
	 * Do a task. When a task is done, MWF will drive the process to move forward.
	 * 
	 * @param token
	 *            current token.
	 * @param prcId
	 *            the id of the process which the task belong to.
	 * @param nodeId
	 *            the nodeId of the current task, which is the id of it's corresponding workflow node.
	 * @param sessId
	 *            the sessId of current task
	 * @param option
	 *            the option value you provided.
	 * @param attachments
	 *            the attachments to this task.
	 * @return the sessId of next task if the current task is done successfully.
	 * @throws Exception
	 */
	public String doTask(String actor, String prcId, String nodeId, String sessId, String option, String attachments) throws Exception {
		ensureMWFServer();
		if (option == null)
			option = "DEFAULT";
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("actor", actor));
		formparams.add(new BasicNameValuePair("prcid", prcId));
		formparams.add(new BasicNameValuePair("nodeid", nodeId));
		formparams.add(new BasicNameValuePair("sessid", sessId));
		formparams.add(new BasicNameValuePair("option", option));
		formparams.add(new BasicNameValuePair("token", lastToken));
		if (attachments != null)
			formparams.add(new BasicNameValuePair("attachments", attachments));

		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/task");
		RestResponse ret = myPost(builder.build(), formparams);
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		}

		return ret.msg;
	}

	public String releaseToken() throws Exception {
		ensureMWFServer();

		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/dev/logout").setParameter("token", lastToken);
		RestResponse ret = myGet(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else
			return ret.msg;

	}

	/**
	 * Get user's profile by identity
	 * 
	 * @return a JSONObject represents user profile attributes.
	 * @see MWF Restful API
	 * @throws Exception
	 */
	public JSONObject getUserInfo(String identity) throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/user").setParameter("token", lastToken).setParameter("identity", identity);
		RestResponse ret = myGet(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else
			return (JSONObject) parser.parse(ret.msg);
	}

	public JSONArray getAllUsers() throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/user/all").setParameter("token", lastToken);
		RestResponse ret = myGet(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else
			return (JSONArray) parser.parse(ret.msg);
	}

	public JSONObject getTask(String tid) throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/task").setParameter("token", lastToken).setParameter("tid", tid);
		RestResponse ret = myGet(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else
			return (JSONObject) parser.parse(ret.msg);
	}

	/**
	 * Add an user
	 * 
	 * @param userid
	 * @param username
	 * @param email
	 * @param tzid
	 * @param lang
	 * @return
	 * @throws Exception
	 */
	public String addUser(String identity, String username, String email, String tzid, String lang, String notify) throws Exception {
		ensureMWFServer();

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("identity", identity));
		formparams.add(new BasicNameValuePair("username", username));
		formparams.add(new BasicNameValuePair("email", email));
		formparams.add(new BasicNameValuePair("tzid", tzid));
		formparams.add(new BasicNameValuePair("lang", lang));
		formparams.add(new BasicNameValuePair("notify", notify));
		formparams.add(new BasicNameValuePair("token", lastToken));

		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/user/new");
		return myPost(builder.build(), formparams).msg;
	}

	public String updateUser(String identity, String username, String email, String tzid, String lang, String notify) throws Exception {
		ensureMWFServer();

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("identity", identity));
		formparams.add(new BasicNameValuePair("username", username));
		formparams.add(new BasicNameValuePair("email", email));
		formparams.add(new BasicNameValuePair("tzid", tzid));
		formparams.add(new BasicNameValuePair("lang", lang));
		formparams.add(new BasicNameValuePair("notify", notify));
		formparams.add(new BasicNameValuePair("token", lastToken));

		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/user/update");
		return myPutForm(builder.build(), formparams).msg;
	}

	/**
	 * Delete a user
	 * 
	 * @param userid
	 * @return
	 * @throws Exception
	 */
	public String deleteUser(String identity) throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/user").setParameter("identity", identity).setParameter("token", lastToken);
		return myDelete(builder.build()).msg;
	}

	/**
	 * Get a user's work list.
	 * 
	 * current token
	 * 
	 * @return a JSONArray contains all works assigned to the user.
	 * @throws Exception
	 */
	public synchronized JSONArray getWorklist(String doer) throws Exception {
		ensureMWFServer();
		JSONArray retObj = null;
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/worklist").setParameter("doer", doer).setParameter("token", lastToken);
		RestResponse ret = myGet(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else {
			try {
				retObj = (JSONArray) parser.parse(ret.msg);
			} catch (Exception ex) {
				System.out.println(ret);
				ex.printStackTrace();
				retObj = null;
			}
		}
		return retObj;
	}

	public synchronized JSONArray getWorklist(String doer, String prcId) throws Exception {
		ensureMWFServer();
		JSONArray retObj = null;
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/worklist").setParameter("doer", doer).setParameter("prcid", prcId).setParameter("token", lastToken);
		RestResponse ret = myGet(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			retObj = null;
			throw new Exception(ret.msg);
		} else {
			retObj = (JSONArray) parser.parse(ret.msg);
		}

		return retObj;
	}

	/**
	 * Upload a workflow template XML to MWF server
	 * 
	 * @param wft
	 *            the content of template XML
	 * @param wftname
	 *            the name of this template
	 * @return the id of newly created workflow template.
	 * @throws Exception
	 */
	public String uploadWft(String wft, String wftname) throws Exception {
		ensureMWFServer();
		RestResponse ret = null;

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("wft", wft));
		formparams.add(new BasicNameValuePair("wftname", wftname));
		formparams.add(new BasicNameValuePair("token", lastToken));

		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/wft");
		ret = myPost(builder.build(), formparams);
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		}

		return ret.msg;

	}

	/**
	 * Get content of a workflow template
	 * 
	 * @param wftId
	 *            the id of workflow template
	 * @return the content of the workflow template
	 * @throws Exception
	 */
	public String getWftDoc(String wftId) throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/wft/doc").setParameter("wftid", wftId).setParameter("token", lastToken);
		RestResponse ret = myGet(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else
			return ret.msg;
	}

	/**
	 * Start a workflow. The corresponding process will be created
	 * 
	 * current token
	 * 
	 * @param startBy
	 *            the id of the starter
	 * @param wftId
	 *            the id of tempalte
	 * @param teamId
	 *            the id of team members of which will participate in the process.
	 * @param instanceName
	 *            give the newly created process a instance name
	 * @return the id of the newly created process
	 * @throws Exception
	 */
	public String startWorkflow(String startBy, String wftId, String teamId, String instanceName, JSONObject ctx) throws Exception {
		ensureMWFServer();
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("wftid", wftId));
		if (teamId != null && teamId.trim().length() > 0)
			formparams.add(new BasicNameValuePair("teamid", teamId));
		formparams.add(new BasicNameValuePair("instancename", instanceName));
		formparams.add(new BasicNameValuePair("startby", startBy));
		formparams.add(new BasicNameValuePair("token", lastToken));
		if (ctx != null) {
			formparams.add(new BasicNameValuePair("ctx", ctx.toString()));
		}

		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/process");
		RestResponse ret = myPost(builder.build(), formparams);
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else {
			return ret.msg;
		}

	}

	public String startWorkflowByName(String startBy, String wftName, String teamName, String instanceName, JSONObject ctx) throws Exception {
		ensureMWFServer();
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("wftname", wftName));
		formparams.add(new BasicNameValuePair("teamname", teamName));
		formparams.add(new BasicNameValuePair("instancename", instanceName));
		formparams.add(new BasicNameValuePair("startby", startBy));
		formparams.add(new BasicNameValuePair("token", lastToken));
		if (ctx != null) {
			formparams.add(new BasicNameValuePair("ctx", ctx.toString()));
		}

		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/process/byname");
		RestResponse ret = myPost(builder.build(), formparams);
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else {
			return ret.msg;
		}

	}

	/**
	 * Get contextual variables and their values of a process
	 * 
	 * current token
	 * 
	 * @param prcId
	 *            the id of the process
	 * @return a JSONObject contains all process contextual varialbes and their values.
	 * @see MWF Restful API
	 * @throws Exception
	 * 
	 */
	public JSONObject getPrcVariables(String prcId) throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/process/variables").setParameter("prcid", prcId).setParameter("token", lastToken);
		RestResponse ret = myGet(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else
			return (JSONObject) parser.parse(ret.msg);
	}

	/**
	 * Create a team
	 * 
	 * current token
	 * 
	 * @param teamName
	 *            the name
	 * @param teamMemo
	 *            the memo
	 * @return the id of newly created team
	 * @throws Exception
	 */
	public String createTeam(String teamName, String teamMemo) throws Exception {
		ensureMWFServer();
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("teamname", teamName));
		formparams.add(new BasicNameValuePair("memo", teamMemo));
		formparams.add(new BasicNameValuePair("token", lastToken));

		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/team");
		return myPost(builder.build(), formparams).msg;
	}

	/**
	 * Add multiple members to a team
	 * 
	 * current token
	 * 
	 * @param teamId
	 *            the id of the team
	 * @param memberships
	 *            memberships description in JSON format. For example; {"USER_ID1":"USER_ROLE1", "USER_ID2":"USER_ROLE2"} It's better to construct a JSONObject like this:<BR>
	 *            JSONObject members = new JSONObject(); <BR>
	 *            members.put("U3307", "Approver"); <BR>
	 *            members.put("U3308", "Auditor"); client.addTeamMembers( teamId, members.toString());
	 * @return
	 * @throws Exception
	 */
	public String addTeamMembers(String teamId, String memberships) throws Exception {
		ensureMWFServer();
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("teamid", teamId));
		formparams.add(new BasicNameValuePair("memberships", memberships));
		formparams.add(new BasicNameValuePair("token", lastToken));

		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/team/members");
		RestResponse ret = myPost(builder.build(), formparams);
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else {
			return ret.msg;
		}
	}

	public String addTeamMembers(String teamId, JSONObject membershipsObject) throws Exception {
		ensureMWFServer();
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("teamid", teamId));
		formparams.add(new BasicNameValuePair("memberships", membershipsObject.toString()));
		formparams.add(new BasicNameValuePair("token", lastToken));

		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/team/members");
		RestResponse ret = myPost(builder.build(), formparams);
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else {
			return ret.msg;
		}
	}

	public JSONArray getTeamMembers(String teamId) throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/team/members").setParameter("token", lastToken).setParameter("teamid", teamId);
		RestResponse ret = myGet(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else
			return (JSONArray) parser.parse(ret.msg);
	}

	public JSONArray getTeamMembersByRole(String teamId, String role) throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/team/membersbyrole").setParameter("token", lastToken).setParameter("teamid", teamId).setParameter("role", role);
		RestResponse ret = myGet(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else
			return (JSONArray) parser.parse(ret.msg);
	}

	public String removeTeamMember(String teamId, String memberId) throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/team/member").setParameter("teamid", teamId).setParameter("memberid", memberId).setParameter("token", lastToken);
		RestResponse ret = myDelete(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else
			return ret.msg;
	}

	/**
	 * Delete a team from WMF server by it's id
	 * 
	 * current token
	 * 
	 * @param teamId
	 *            id of the team to be deleted
	 * @return true if success
	 * @throws Exception
	 */
	public String deleteTeamById(String teamId) throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/team").setParameter("teamid", teamId).setParameter("token", lastToken);
		RestResponse ret = myDelete(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else
			return ret.msg;
	}

	/**
	 * Get all teams of a user
	 * 
	 * current token.
	 * 
	 * @return a JSONArray contains user's teams
	 * @see MWF Restful API
	 * @throws Exception
	 */
	public JSONArray getTeams() throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/team/all").setParameter("token", lastToken);
		RestResponse ret = myGet(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else
			return (JSONArray) parser.parse(ret.msg);
	}

	/**
	 * Get team information by it's id
	 * 
	 * @param teamName
	 *            the name of the team
	 * @return return a JSONObject describe the team
	 * @throws Exception
	 */
	public JSONObject getTeamByName(String teamName) throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/team/byname").setParameter("teamname", teamName).setParameter("token", lastToken);
		RestResponse ret = myGet(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else
			return (JSONObject) parser.parse(ret.msg);
	}

	public JSONObject getTeamById(String teamId) throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/team/byid").setParameter("teamid", teamId).setParameter("token", lastToken);
		RestResponse ret = myGet(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else
			return (JSONObject) parser.parse(ret.msg);
	}

	/**
	 * Get the context value of a process
	 * 
	 * current token
	 * 
	 * @param prcId
	 *            id of the process
	 * @return a JSONObject represent the context value.
	 * @throws Exception
	 */
	public JSONObject getPrcInfo(String prcId) throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/process").setParameter("prcid", prcId).setParameter("token", lastToken);
		RestResponse ret = myGet(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else
			try {
				return (JSONObject) parser.parse(ret.msg);
			} catch (Exception ex) {
				System.out.println(ret);
				return null;
			}
	}

	/**
	 * Get processes by status
	 * 
	 * current token
	 * 
	 * @param status
	 *            process status, should be one of "running", "suspended", "finished", "canceled"
	 * @return a JSONArray of processes.
	 * @throws Exception
	 */
	public JSONArray getProcessesByStatus(String status) throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/process/all").setParameter("status", status).setParameter("token", lastToken);
		RestResponse ret = myGet(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else
			return (JSONArray) parser.parse(ret.msg);
	}

	/**
	 * Get workflow templates user can use
	 * 
	 * current token
	 * 
	 * @return a JSONArray of workflow templates information
	 * @throws Exception
	 */
	public JSONArray getWfts() throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/wft/all").setParameter("token", lastToken);
		RestResponse ret = myGet(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else
			return (JSONArray) parser.parse(ret.msg);
	}

	/**
	 * Get workflow info
	 * 
	 * current token
	 * 
	 * @return a JSONArray of workflow templates information
	 * @throws Exception
	 */
	public JSONObject getWftInfo(String wftId) throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/wft").setParameter("token", lastToken).setParameter("wftid", wftId);
		RestResponse ret = myGet(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else
			return (JSONObject) parser.parse(ret.msg);
	}

	public String getWftExample(String modelId) throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/wft/example").setParameter("token", lastToken).setParameter("modelid", modelId);
		RestResponse ret = myGet(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else
			return ret.msg;
	}

	/**
	 * Suspend a process
	 * 
	 * current token
	 * 
	 * @param prcId
	 *            id of the process to be suspended
	 * @return
	 * @throws Exception
	 */
	public String suspendProcess(String prcId) throws Exception {
		ensureMWFServer();
		return _changeProcessStatus(prcId, "suspendProcess");
	}

	/**
	 * Resume a process
	 * 
	 * current token
	 * 
	 * @param prcId
	 *            id of the process to be resumed
	 * @return
	 * @throws Exception
	 */
	public String resumeProcess(String prcId) throws Exception {
		ensureMWFServer();
		return _changeProcessStatus(prcId, "resumeProcess");
	}

	/**
	 * Suspend a work
	 * 
	 * current token
	 * 
	 * @param prcId
	 *            id of the process this work belongs to
	 * @param nodeId
	 *            id of the node to be suspended
	 * @return
	 * @throws Exception
	 */
	public String suspendWork(String prcId, String nodeId) throws Exception {
		ensureMWFServer();
		return _changeWorkStatus(prcId, nodeId, "suspendWork");
	}

	/**
	 * Resume a work
	 * 
	 * current token
	 * 
	 * @param prcId
	 *            id of the process this work belongs to
	 * @param nodeId
	 *            id of the node to be resumed
	 * @return
	 * @throws Exception
	 */
	public String resumeWork(String prcId, String nodeId) throws Exception {
		ensureMWFServer();
		return _changeWorkStatus(prcId, nodeId, "resumeWork");
	}

	/**
	 * @param prcId
	 * @param action
	 * @return
	 * @throws Exception
	 */
	private String _changeProcessStatus(String prcId, String action) throws Exception {
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/process/status").setParameter("prcid", prcId).setParameter("action", action).setParameter("token", lastToken);
		RestResponse ret = myPut(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		}

		return ret.msg;
	}

	/**
	 * @param prcId
	 * @param nodeId
	 * @param action
	 * @return
	 * @throws Exception
	 */
	private String _changeWorkStatus(String prcId, String nodeId, String action) throws Exception {
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/process/status").setParameter("prcid", prcId).setParameter("nodeid", nodeId).setParameter("action", action).setParameter("token", lastToken);
		return myPut(builder.build()).msg;
	}

	/**
	 * Delete a task from delegater to delegatee
	 * 
	 * current token
	 * 
	 * @param prcId
	 *            process id
	 * @param sessId
	 *            sessId of the task
	 * @param delegater
	 *            delegate from delegater
	 * @param delegatee
	 *            delegate to delegatee
	 * @return
	 * @throws Exception
	 */
	public String delegate(String prcId, String sessId, String delegater, String delegatee) throws Exception {
		ensureMWFServer();
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("delegater", delegater));
		formparams.add(new BasicNameValuePair("delegatee", delegatee));
		formparams.add(new BasicNameValuePair("prcid", prcId));
		formparams.add(new BasicNameValuePair("sessid", sessId));
		formparams.add(new BasicNameValuePair("token", lastToken));

		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/delegation");
		RestResponse ret = myPost(builder.build(), formparams);
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		}

		return ret.msg;
	}

	public String uploadVt(String content, String vtname) throws Exception {
		ensureMWFServer();

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("content", content));
		formparams.add(new BasicNameValuePair("vtname", vtname));
		formparams.add(new BasicNameValuePair("token", lastToken));

		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/vt");
		RestResponse ret = myPost(builder.build(), formparams);
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		}
		return ret.msg;
	}

	public String deleteVt(String vtname) throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/vt").setParameter("vtname", vtname).setParameter("token", lastToken);
		RestResponse ret = myDelete(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		}
		return ret.msg;
	}

	public JSONArray getVts() throws Exception {
		ensureMWFServer();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(lastServer).setPath("/cflow/rest/vt/all").setParameter("token", lastToken);
		RestResponse ret = myGet(builder.build());
		if (ret.code != RestResponse.SUCCESS) {
			logger.error(ret.msg);
			throw new Exception(ret.msg);
		} else
			return (JSONArray) parser.parse(ret.msg);
	}

	private void ensureMWFServer() {
		if (context.get("ensureConnection") != null) {
			if (checkServerAliveness(lastServer) == false) {
				electMWFServer(lastDev);
			}
		}
	}

	private RestResponse myGet(URI url) throws Exception {
		RestResponse rr = null;
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = httpclient.execute(httpGet, localContext);
			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity entity_return = response.getEntity();
			rr = new RestResponse(statusCode, EntityUtils.toString(entity_return));
			EntityUtils.consume(entity_return);
		} catch (Exception ex) {
			ex.printStackTrace();
			rr = new RestResponse(888, "Exception:" + ex.getLocalizedMessage());
		} finally {
			httpGet.releaseConnection();
		}
		return rr;
	}

	private RestResponse myDelete(URI url) throws Exception {
		RestResponse rr = null;
		HttpDelete httpDelete = new HttpDelete(url);
		try {
			HttpResponse response = httpclient.execute(httpDelete, localContext);
			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity entity_return = response.getEntity();
			rr = new RestResponse(statusCode, EntityUtils.toString(entity_return));
			EntityUtils.consume(entity_return);
		} catch (Exception ex) {
			ex.printStackTrace();
			rr = new RestResponse(888, "Exception:" + ex.getLocalizedMessage());
		} finally {
			httpDelete.releaseConnection();
		}
		return rr;
	}

	private RestResponse myPut(URI url) throws Exception {
		RestResponse rr = null;
		HttpPut httpPut = new HttpPut(url);
		try {
			HttpResponse response = httpclient.execute(httpPut, localContext);
			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity entity_return = response.getEntity();
			rr = new RestResponse(statusCode, EntityUtils.toString(entity_return));
			EntityUtils.consume(entity_return);

		} catch (Exception ex) {
			ex.printStackTrace();
			rr = new RestResponse(888, "Exception:" + ex.getLocalizedMessage());
		} finally {
			httpPut.releaseConnection();
		}
		return rr;
	}

	private RestResponse myPutForm(URI url, List<NameValuePair> formparams) throws Exception {
		RestResponse rr = null;

		HttpPut httpPut = new HttpPut(url);
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
		httpPut.setEntity(entity);

		try {
			HttpResponse response = httpclient.execute(httpPut, localContext);
			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity entity_return = response.getEntity();
			rr = new RestResponse(statusCode, EntityUtils.toString(entity_return));
			EntityUtils.consume(entity);
		} catch (Exception ex) {
			ex.printStackTrace();
			rr = new RestResponse(888, "Exception:" + ex.getLocalizedMessage());
		} finally {
			httpPut.releaseConnection();
		}
		return rr;
	}

	private RestResponse myPost(URI url, List<NameValuePair> formparams) throws Exception {
		RestResponse rr = null;

		HttpPost httpPost = new HttpPost(url);
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
		httpPost.setEntity(entity);

		try {
			HttpResponse response = httpclient.execute(httpPost, localContext);
			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity entity_return = response.getEntity();
			rr = new RestResponse(statusCode, EntityUtils.toString(entity_return));
			EntityUtils.consume(entity);
		} catch (Exception ex) {
			ex.printStackTrace();
			rr = new RestResponse(888, "Exception:" + ex.getLocalizedMessage());
		} finally {
			httpPost.releaseConnection();
		}
		return rr;
	}

	private class RestResponse {
		public final static int SUCCESS = 200;
		public String msg = "";
		public int code = SUCCESS;

		public RestResponse(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}
	}

	private void electMWFServer(String dev) {
		URIBuilder builder = new URIBuilder();
		String useMaster = null;
		Elector elector = new Elector(mwfMasters.length);
		if (lastMaster != null) {
			useMaster = lastMaster;
		} else {
			int elected = elector.elect();
			useMaster = mwfMasters[elected];
		}

		for (int i = 0; i < 100; i++) {
			try {
				URI uri = builder.setScheme("http").setHost(useMaster).setPath("/cflow/rest/ha/server").setParameter("dev", dev).build();
				RestResponse rr = ra.myGet(uri);
				if (rr.code == RestResponse.SUCCESS) {
					lastMaster = useMaster;
					lastServer = rr.msg;
					break;
				}
				int elected = elector.elect();
				useMaster = mwfMasters[elected];
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	private boolean checkServerAliveness(String server) {
		boolean ret = false;
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(server).setPath("/cflow/rest/ha/check");
		try {
			RestResponse rr = ra.myGet(builder.build());
			boolean result = true;
			if (rr.code == rr.SUCCESS) {
				ret = true;
			} else {
				ret = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	private class RemoteAccess {
		private HttpClient httpclient = new DefaultHttpClient();
		private HttpContext localContext = new BasicHttpContext();

		public RestResponse myGet(URI url) throws Exception {
			return myGet(url, 5000, 5000);
		}

		public RestResponse myGet(URI url, int conn_timeout, int so_timeout) throws Exception {
			RestResponse rr = null;
			HttpGet httpGet = new HttpGet(url);
			httpGet.getParams().setParameter("http.socket.timeout", new Integer(so_timeout));
			synchronized (httpclient) {
				try {
					httpclient.getParams().setParameter("http.connection.timeout", new Integer(conn_timeout));
					HttpResponse response = httpclient.execute(httpGet, localContext);

					int statusCode = response.getStatusLine().getStatusCode();
					HttpEntity entity_return = response.getEntity();
					rr = new RestResponse(statusCode, EntityUtils.toString(entity_return));
					EntityUtils.consume(entity_return);
					httpGet.releaseConnection();
				} catch (Exception ex) {
					httpGet.releaseConnection();
					rr = new RestResponse(888, "Exception:" + ex.getLocalizedMessage());
				} finally {
					httpGet.releaseConnection();
				}
			}
			return rr;
		}
	}

	private class Elector {
		int number = 0;
		int[] elected = null;
		int electSer = 0;

		public Elector(int number) {
			this.number = number;
			elected = new int[number];
			for (int e = 0; e < number; e++) {
				elected[e] = -1;
			}
		}

		private void reset() {
			for (int e = 0; e < number; e++) {
				elected[e] = -1;
			}
			electSer = 0;
		}

		public int elect() {
			int selected = -1;
			if (electSer >= number) {
				reset();
			}
			for (; electSer < number;) {
				double d = java.lang.Math.random();
				selected = (int) java.lang.Math.floor(d * number);
				boolean found = false;
				for (int e = 0; e < elected.length; e++) {
					if (elected[e] == selected) {
						found = true;
						break;
					}
				}
				if (!found) {
					elected[electSer] = selected;
					electSer++;
					break;
				}
			}

			return selected;
		}
	}

}
