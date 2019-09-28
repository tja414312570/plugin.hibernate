package asm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;

import com.YaNan.frame.jdb.database.ConnectionPools;
import com.YaNan.frame.plugin.hot.ClassRemapper;

public class ASMTest{
	public static void main(String[] args) throws IOException {
		String className = ASMTest.class.getName();
		String clazzName = className+"$YConnecton";
		ClassReader reader = new ClassReader(className);
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor addField = new AddFieldAdapter(writer,
                "connectionPools",
                Opcodes.ACC_PRIVATE ,
                Type.getDescriptor(ConnectionPools.class),
                null
        );
        reader.accept(addField, ClassReader.EXPAND_FRAMES);
		Map<String, String> nameMappings = new HashMap<String, String>();
		nameMappings.put(className.replace(".", "/"), clazzName.replace(".", "/"));
		Remapper mapper = new SimpleRemapper(nameMappings);
		ClassVisitor remapper = new ClassRemapper(writer, mapper);
		reader.accept(remapper, ClassReader.SKIP_FRAMES);
		byte[] bytes = writer.toByteArray();
		int index = clazzName.indexOf(".");
		if(index>0) {
			clazzName = clazzName.substring(index+1);	
		}
		File file = new File("/Users/yanan/Desktop/asm/"+clazzName+".class");
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(bytes);
		fos.flush();
		fos.close();
		System.out.println(bytes);
	}
}
