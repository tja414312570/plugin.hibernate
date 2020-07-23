import java.util.HashMap;
import java.util.Map;

import com.yanan.frame.jdb.SqlSession;
import com.yanan.frame.jdb.operate.Query;
import com.yanan.frame.plugin.PlugsFactory;

public class HibernateBuilderTest2 {
	
	public static void main(String[] args) throws InterruptedException {
//		//转化上下文
		PlugsFactory.init();
//		//创建数据源
//		DataSource defaultDataSource = PlugsFactory.getPlugsInstance(DataSource.class);
//		//初始化JDB上下文
//		JDBContext hibernateBuilder = PlugsFactory.getPlugsInstance(JDBContext.class,defaultDataSource);
//		//初始化Mapper构造器
//		MapperInterfaceProxyBuilder proxy = PlugsFactory.getPlugsInstance(MapperInterfaceProxyBuilder.class,hibernateBuilder);
//		//参数
		Map<String,String> params = new HashMap<String,String>();
		params.put("name", "1024");
//		SqlFragment frag = hibernateBuilder.getSqlFragmentManger().getSqlFragment("TestSq.test1");
//		System.out.println(frag.getPreparedSql(params));
//		frag = hibernateBuilder.getSqlFragmentManger().getSqlFragment("TestSq.test");
//		System.out.println(frag.getPreparedSql(params));
		//SqlSession
		PlugsFactory ins = PlugsFactory.getInstance();
		System.out.println(PlugsFactory.getPluginsInstance("sqlSession")+"");
		SqlSession sql = PlugsFactory.getPluginsInstance(SqlSession.class);
		System.out.println(sql.selectOne("TestSq.test", params)+"");
		System.out.println(sql.selectOne("TestSq.test1", params)+"");
		Query query = new Query(UserTokenModel.class);
		query.query();
		TestSql testSql = PlugsFactory.getPluginsInstance(TestSql.class);
		testSql.test("1024", "");
	}
}
