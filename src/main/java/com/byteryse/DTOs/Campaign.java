package com.byteryse.DTOs;

enum Status {
	OPEN,
	CLOSED,
	PREPARING;

	String getAsString() {
		return this.toString();
	}
}

public class Campaign {
	private String name;
	private String role_id;
	private String category_id;
	private Status status;
	private String post_id;
	private String dm_role_id;

	public Campaign(String name, String role_id, String category_id, String post_id, String dm_role_id) {
		this.name = name;
		this.role_id = role_id;
		this.category_id = category_id;
		this.post_id = post_id;
		this.dm_role_id = dm_role_id;
		this.status = Status.PREPARING;
	}

	public Campaign(String name, String role_id, String category_id, String status,
			String post_id, String dm_role_id) {
		this.name = name;
		this.role_id = role_id;
		this.status = Status.valueOf(status);
		this.category_id = category_id;
		this.post_id = post_id;
		this.dm_role_id = dm_role_id;
	}

	public String getCategory_id() {
		return category_id;
	}

	public String getName() {
		return name;
	}

	public String getPost_id() {
		return post_id;
	}

	public String getRole_id() {
		return role_id;
	}

	public Status getStatus() {
		return status;
	}

	public String getStatusString() {
		return status.toString();
	}

	public String getDm_role_id() {
		return dm_role_id;
	}

	public void open() {
		this.status = Status.OPEN;
	}

	public void close() {
		this.status = Status.CLOSED;
	}

	public boolean isOpen() {
		return this.status.equals(Status.OPEN);
	}

	public void setName(String newName) {
		this.name = newName;
	}
}