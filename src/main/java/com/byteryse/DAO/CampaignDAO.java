package com.byteryse.DAO;

import java.util.List;

import com.byteryse.DTO.Campaign;
import com.byteryse.Database.DatabaseController;

public class CampaignDAO {
	private final DatabaseController db;

	public CampaignDAO(DatabaseController dbCon) {
		this.db = dbCon;
	}

	public Campaign getCampaignByName(String name) {
		List<String> results = db.executeSQL("SELECT * FROM campaigns WHERE campaign_name = ?", name).get(0);
		return new Campaign(results.get(0), results.get(1), results.get(2), results.get(3), results.get(4),
				results.get(5));
	}

	public Campaign getCampaignByCategory(String id) {
		List<String> results = db.executeSQL("SELECT * FROM campaigns WHERE category_id = ?", id).get(0);
		return new Campaign(results.get(0), results.get(1), results.get(2), results.get(3), results.get(4),
				results.get(5));
	}

	public Campaign getCampaignByRole(String id) {
		List<String> results = db.executeSQL("SELECT * FROM campaigns WHERE role_id = ?", id).get(0);
		return new Campaign(results.get(0), results.get(1), results.get(2), results.get(3), results.get(4),
				results.get(5));
	}

	public Campaign getCampaignByPost(String id) {
		List<String> results = db.executeSQL("SELECT * FROM campaigns WHERE post_id = ?", id).get(0);
		return new Campaign(results.get(0), results.get(1), results.get(2), results.get(3), results.get(4),
				results.get(5));
	}

	public void updateCampaign(Campaign campaign) {
		db.executeSQL("UPDATE campaigns SET campaign_name = ?, status = ? WHERE category_id = ?", campaign.getName(),
				campaign.getStatusString().toString(), campaign.getCategory_id());
	}

	public void createCampaign(Campaign campaign) {
		db.executeSQL(
				"INSERT INTO campaigns (campaign_name, role_id, category_id, status, post_id, dm_role_id) VALUES (?,?,?,?,?,?)",
				campaign.getName(), campaign.getRole_id(), campaign.getCategory_id(), campaign.getStatusString(),
				campaign.getPost_id(), campaign.getDm_role_id());
	}

	public void deleteCampaign(Campaign campaign) {
		db.executeSQL("DELETE FROM campaigns WHERE category_id = ?", campaign.getCategory_id());
	}
}
