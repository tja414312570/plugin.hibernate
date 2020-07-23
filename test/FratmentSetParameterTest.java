import java.util.HashMap;
import java.util.Map;

import com.yanan.frame.jdb.fragment.FragmentSet;

public class FratmentSetParameterTest {

	public static void main(String[] args) {
		FragmentSet fragmentSet = new FragmentSet();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("id", "1024");
		Map<String,Integer> page = new HashMap<>();
		page.put("offset", 10);
		page.put("limit", 10);
		RowBounds rb = new RowBounds();
		params.put("RowBounds", rb);
		System.out.println(fragmentSet.decodeParameter("RowBounds.row.status", params));
	}
}
