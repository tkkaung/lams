package ntu.celt.eUreka2.modules.budget;

import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.services.attachFiles.AttachedFile;


public class TransactionAttachedFile extends AttachedFile  {
	
	private static final long serialVersionUID = 1892990796349409898L;
	private Transaction transact;
	

	public Transaction getTransact() {
		return transact;
	}

	public void setTransact(Transaction transact) {
		this.transact = transact;
	}

	public JSONObject toJSONObject(){
		JSONObject j = super.toJSONObject();
		j.put("transaction", transact.getId());
		return j;
	}
	
	public TransactionAttachedFile clone(){
		TransactionAttachedFile at = (TransactionAttachedFile) super.clone(TransactionAttachedFile.class);
		at.setTransact(transact);
		return at;
	}
	
	
}
