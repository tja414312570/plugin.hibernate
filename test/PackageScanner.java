

import java.io.File;
import java.util.Arrays;

import com.YaNan.frame.utils.resource.Path.PathInter;
import com.YaNan.frame.utils.resource.ResourceManager;
import com.YaNan.frame.utils.resource.Path;

/**
 * 支持通配符,按照PathMath匹配模式
 * @author yanan
 *
 */
public class PackageScanner {
	private String[] packageDirs;
	private boolean ignoreLoadingException;
	
	public static int getMarkIndex(String string, int offset) {
		int hI = string.indexOf("{",offset);
		int sI = string.indexOf("*",offset);
		int qI = string.indexOf("?",offset);
		int i = -1;
		if(hI!=-1)
			if(i != -1)
				i = Math.min(i, hI);
			else i = hI;
		if(sI!=-1)
			if(i != -1)
				i = Math.min(i, sI);
			else i = sI;
		if(qI!=-1)
			if(i != -1)
				i = Math.min(i, qI);
			else i = qI;
		return i;
	}
	
	public String[] getScanPath() {
		return packageDirs == null?null:Arrays.copyOf(packageDirs, packageDirs.length);
	}
	public void addScanPath(String... paths) {
		if(paths.length>0) {
			if(packageDirs==null) {
				packageDirs =paths;
			}else {
				String[] pathCache = new String[paths.length+packageDirs.length];
				System.arraycopy(packageDirs, 0, pathCache, 0, packageDirs.length);
				System.arraycopy(paths, 0, pathCache, packageDirs.length, paths.length);
				this.packageDirs = pathCache;
			}
		}
	}
	public void setScanPath(String paths) {
		if(paths == null)
			throw new IllegalArgumentException();
		String[] tempPaths = paths.split(",");
		this.packageDirs = null;
		this.addScanPath(tempPaths);
	}
	public PackageScanner(String classPath) {
		this.addScanPath(classPath);
	}
	public PackageScanner(){
	}
	public void doScanner(final ClassInter inter) {
		if(this.packageDirs==null)
			this.addScanPath(ResourceManager.classPath());
		String sourcePath = null;
		String packagePathName;
		File scannerPath = null;
		String resourcePathName;
		String filter = null;
		for (String packDir : packageDirs) {
			if (packDir == null)
				continue;
			if(packDir.indexOf(":")!=-1||packDir.startsWith("/")) {
				int packMark = packDir.indexOf(".",packDir.lastIndexOf("/"));
				if(packMark!=-1) {
					sourcePath = packDir.substring(0,packMark);//资源目录
					packagePathName = packDir.substring(packMark);
				}else {
					sourcePath = packDir;//资源目录
					packagePathName = "";
				}
			}else {
				sourcePath = ResourceManager.classPath();//资源目录
				packagePathName = packDir;
			}
			String scannerPathName = sourcePath+packagePathName.replace(".", File.separator);
			int filterIndex = getMarkIndex(scannerPathName, 0);
			if(filterIndex>-1) {
				filter = scannerPathName;
				scannerPathName = scannerPathName.substring(0,scannerPathName.lastIndexOf(File.separator));
			}
			scannerPath= new File(scannerPathName);//扫描路径
			resourcePathName = sourcePath;
			if(!resourcePathName.endsWith(File.separator)) {
				resourcePathName+=File.separator;
			}
			this.scanner(scannerPath,resourcePathName,inter,filter);
		}
	}
	private void scanner(File path,String resourcePath,ClassInter inter, String filter) {
		Path scanner = new Path(path);
		if(filter != null)
			scanner.filter(filter);
		scanner.scanner(new PathInter(){
			@Override
			public void find(File file) {
				if(file.getName().endsWith(".class")){
					String className = file.getAbsolutePath().replace(resourcePath, "").replace("\\","." ).replace("/", ".").replace("$", ".").replace(".class", "");
					className= className.substring(0, 1).equals(".")?className.substring(1, className.length()):className;
					try {
						inter.find(Class.forName(className));
					} catch (Throwable e) {
						if(!ignoreLoadingException&&!e.getClass().equals(ClassNotFoundException.class))
							throw new RuntimeException("failed to load class \""+className+"\" at class file \""+file+"\"",e);
					}
				}
				
			}
		});
	}
	public boolean isIgnoreLoadingException() {
		return ignoreLoadingException;
	}
	public void setIgnoreLoadingException(boolean ignoreLoadingException) {
		this.ignoreLoadingException = ignoreLoadingException;
	}
	public static interface ClassInter {
		public void find(Class<?> cls);
	}
}
