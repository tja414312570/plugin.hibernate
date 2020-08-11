import java.io.File;
import java.io.IOException;

import com.yanan.utils.resource.FileUtils;
import com.yanan.utils.resource.scanner.Path;
import com.yanan.utils.resource.scanner.Path.PathInter;

public class GlobalReplace {
	public static void main(String[] args) {
		Path  path = new Path("/Volumes/GENERAL/git/plugin.ant.core");
		path.scanner(new PathInter() {
			
			@Override
			public void find(File file) {
				if(file.isFile()) {
					try {
						String str = FileUtils.read(file,"utf-8");
						str = str.replace("YaNan", "yanan");
						str = str.replace("frame.utils", "utils");
						FileUtils.write(file,str,"utf-8");
					} catch (IllegalAccessException | IOException e) {
						e.printStackTrace();
					}
					
				}
			
			}
		});
	}
}
