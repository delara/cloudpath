package pathstore.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.utils.UUIDs;

public class QueryCacheEntry  {
	String keyspace;
	String table;
	List<Clause> clauses;
	byte[] clausesSerialized = null;
	QueryCacheEntry isCovered = null;
	List<QueryCacheEntry> covers = null;
	private boolean ready = false;
	private UUID parentTimeStamp= UUID.fromString("7db079d1-5340-1118-8c57-5d749fc26b49");
	public int limit;
	
	public QueryCacheEntry(String keyspace, String table, List<Clause> clauses, int limit) {
		this.keyspace = keyspace;
		this.table = table;
		this.clauses = clauses;
		covers = new ArrayList<>();
		this.limit=limit;
		
	}

	public QueryCacheEntry getIsCovered()
	{
		return this.isCovered;
	}

	
	//I'm chaning this yall
	public boolean isSame(List<Clause> clauses2) {
		if (clauses.size() != clauses2.size())
			return false;
		
		for (int i=0; i < clauses.size(); i++)
		{
			//if (clauses.get(i).toString().compareTo(clauses2.get(i).toString())!=0)
			if (clauses.get(i).getName().toString().compareTo(clauses2.get(i).getName().toString())!=0 || 
					clauses.get(i).getValue().toString().compareTo(clauses2.get(i).getValue().toString())!=0)
				return false;
		}
		return true;
	}

	synchronized public void setReady() {
		this.ready = true;
		this.notifyAll();
	}
	
	public void waitUntilReady() {
		if (this.ready)
			return;
		synchronized(this) {
			while(this.ready==false)
				try {
					this.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public boolean isReady() {
		return this.ready;
	}

	
	private boolean firstContainsSecond(List<Clause> clauses1, List<Clause> clauses2) {
		// Trivial case.  First query does not have where clauses, and second has at least one clause
		if (clauses1.size()==0 && clauses2.size() > 0)
			return true;
		return false;
		
	}
	
	public boolean isSuperSet(List<Clause> clauses2) {
		return firstContainsSecond(this.clauses,clauses2);
	}

	public boolean isSubSet(List<Clause> clauses2) {
		return firstContainsSecond(clauses2,this.clauses);
	}

	public UUID getParentTimeStamp() {
		return parentTimeStamp;
	}

	public void setParentTimeStamp(UUID parentTimeStamp) {
		this.parentTimeStamp = parentTimeStamp;
	}

	public String getKeyspace() {
		return keyspace;
	}

	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public List<Clause> getClauses() {
		return clauses;
	}

	public void setClauses(List<Clause> clauses) {
		this.clauses = clauses;
	}

	public byte[] getClausesSerialized() throws IOException {
		
		if (clausesSerialized == null) {
			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		    ObjectOutputStream oos = new ObjectOutputStream(bytesOut);
		    oos.writeObject(this.clauses);
		    oos.flush();
		    byte[] bytes = bytesOut.toByteArray();
		    bytesOut.close();
		    oos.close();
		    this.clausesSerialized = bytes;
		}
		
		return this.clausesSerialized;
	}

	public void setClausesSerialized(byte[] clausesSerialized) {
		this.clausesSerialized = clausesSerialized;
	}

	
	
	
}
