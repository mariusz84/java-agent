import javassist.ClassPool;
import javassist.NotFoundException;
import javassist.bytecode.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class StaticAgentApplication {
    private static Logger LOGGER = LoggerFactory.getLogger(StaticAgentApplication.class);

    public static void premain(String arg) {
        LOGGER.info("[Agent] In premain method");

        System.setProperty("mode", "push");
        LOGGER.info("[Agent] Queue was set to push");

        printClassByteCode("BooksReader", "readBook");
    }

    private static void printClassByteCode(String className, String methodName) {
        ClassPool classPool = ClassPool.getDefault();
        ClassFile classFile = null;
        try {
            classFile = classPool.get(className)
                    .getClassFile();
        } catch (NotFoundException e) {
            LOGGER.info(e.getMessage());
        }

        MethodInfo methodInfo = classFile.getMethod(methodName);
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        CodeIterator codeIterator = codeAttribute.iterator();

        List<String> operations = new LinkedList<>();
        int index = 0;
        while (codeIterator.hasNext()) {
            try {
                index = codeIterator.next();
            } catch (BadBytecode e) {
                LOGGER.info(e.getMessage());
            }
            int operation = codeIterator.byteAt(index);
            operations.add(Mnemonic.OPCODE[operation]);
        }

        operations.stream().forEach(item -> LOGGER.info(item));
    }
}