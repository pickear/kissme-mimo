package com.kissme.mimo.domain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Sets;

/**
 * 
 * @author loudyn
 * 
 */
public class EmailConf implements Serializable {
	private static final long serialVersionUID = 1L;

	private String smtpServer;
	private String sender;
	private String username;
	private String password;

	private String receivers;
	private String events;

	public String getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getReceivers() {
		return receivers;
	}

	public void setReceivers(String receivers) {
		this.receivers = receivers;
	}

	public boolean hasReceiver() {
		return !getReceiversSet().isEmpty();
	}

	public Set<String> getReceiversSet() {
		List<String> asList = Arrays.asList(StringUtils.split(getReceivers(), ";"));
		return Sets.newHashSet(asList);
	}

	public String getEvents() {
		return events;
	}

	public void setEvents(String events) {
		this.events = events;
	}

	public boolean supportEvent(String event) {
		return StringUtils.indexOf(getEvents(), event) != -1;
	}

}
