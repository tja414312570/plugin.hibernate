import java.util.Map;

import com.yanan.frame.jdb.annotation.Sql;
import com.yanan.frame.jdb.mapper.annotations.Param;
import com.yanan.frame.plugin.annotations.Service;
import com.yanan.frame.transaction.Transactions;

@Transactions
@Sql
@Service
public interface TestSql {
	
	@Transactions
	public void test(Map<String,String> map);
	public void test(@Param("name") String name,@Param("id")String id);
}
