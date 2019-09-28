import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.YaNan.frame.jdb.database.SqlSession;
import com.YaNan.frame.jdb.database.transaction.TransactionIsolocation;
import com.YaNan.frame.jdb.database.transaction.TransactionPropagion;
import com.YaNan.frame.jdb.database.transaction.Transactions;
import com.YaNan.frame.plugin.annotations.Service;

public class Test {
	
	@Service
	private SqlSession sqlSession;
	
	@Transactions(propagion = TransactionPropagion.PROPAGATION_NESTED,isolocation = TransactionIsolocation.TRANSACTION_READ_UNCOMMITTED)
	public int testInnerTransaction() throws SQLException {
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		Map<String,String> u = new HashMap<String,String>();
		u.put("id", "1026");
		u.put("name", "1025name");
		list.add(u);
		testInnerTransaction3();
		testInnerTransaction2();
		return sqlSession.insert("com.YaNan.debug.test.insert2", list);
	}

	@Transactions(isolocation = TransactionIsolocation.TRANSACTION_READ_UNCOMMITTED)
	public int testInnerTransaction3() throws SQLException {
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		Map<String,String> u = new HashMap<String,String>();
		u.put("id", "1024");
		u.put("name", "1025name");
		list.add(u);
		return sqlSession.insert("com.YaNan.debug.test.insert", list);
	}

	@Transactions(propagion = TransactionPropagion.PROPAGATION_REQUIRES,isolocation = TransactionIsolocation.TRANSACTION_READ_UNCOMMITTED)
	public int testInnerTransaction2() throws SQLException {
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		Map<String,String> u = new HashMap<String,String>();
		u.put("id", "1025");
		u.put("name", "1025name");
		list.add(u);
		return sqlSession.insert("com.YaNan.debug.test.insert2", list);
	}
	@Transactions(isolocation = TransactionIsolocation.TRANSACTION_READ_COMMITTED)
	public int testTransaction() throws SQLException {
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		Map<String,String> u = new HashMap<String,String>();
		u.put("id", "1022");
		u.put("name", "1025name");
		list.add(u);
		sqlSession.insert("com.YaNan.debug.test.insert", list);
		return this.testInnerTransaction();
	}
}
