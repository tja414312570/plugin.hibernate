import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.YaNan.frame.jdb.database.DBFactory;
import com.YaNan.frame.jdb.database.SqlSession;
import com.YaNan.frame.jdb.database.DBInterface.LOGIC_STATUS;
import com.YaNan.frame.jdb.database.operate.Insert;
import com.YaNan.frame.jdb.database.operate.Query;
import com.YaNan.frame.jdb.database.operate.Update;
import com.YaNan.frame.plugin.PlugsFactory;

public class HibernateTest {
	public static void main(String[] args) {
		
		
		System.out.println(new File("/Volumes/GENERAL/git/plugin.hibernate/target/test-classes/hibernate.xml").exists());
		DBFactory.getDBFactory().init();
		SqlSession sql = PlugsFactory.getPlugsInstance(SqlSession.class);
		System.out.println(sql);
		
		TestSql s = PlugsFactory.getPlugsInstance(TestSql.class);
		System.out.println(s);
//		
//		
//		
//		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
//		Map<String,String> u = new HashMap<String,String>();
//		u.put("id", "1024");
//		u.put("name", "1024name");
//		list.add(u);
//		System.out.println(sql.selectList("com.YaNan.debug.test.test", u));
//		Query query = new Query(UserTokenModel.class);
//		query.addColumnCondition("token", "124");
//		query.addColumnCondition("status", LOGIC_STATUS.NORMAL);
//		UserTokenModel us = null;
//		for(int i = 0;i<10;i++)
//			us = query.queryOne();
//		us.setId(1);
//		us.setToken("125");
//		Update update = new Update(us);
//		update.addCondition("id", 2);
//		update.update();
//		System.out.println("=====");
//		query = new Query(UserTokenModel.class);
//		query.addColumnCondition("token", "125");
//		query.addColumnCondition("status", LOGIC_STATUS.NORMAL);
//		us = null;
//		for(int i = 0;i<10;i++)
//			us = query.queryOne();
//		
//		System.out.println("=====");
//		query = new Query(UserTokenModel.class);
//		query.addColumnCondition("token", "12");
//		query.addColumnCondition("status", LOGIC_STATUS.NORMAL);
//		for(int i = 0;i<10;i++)
//			query.queryOne();
	}
}
