import javassist.*;
import javassist.bytecode.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DynamicAgentApplication {
    private static Logger LOGGER = LoggerFactory.getLogger(DynamicAgentApplication.class);

    public static void agentmain(String arg) {
        LOGGER.info("[Agent] In agentmain method");

        addFieldToCompiledClass("bookId", "BooksReader");

        addFieldDeclarationToCompiledClass("public int bookId;", "BooksReader");

        addMethodDeclarationToCompiledClass("public void printLanguage() { System.out.println(\"en\"); }", "BooksReader");

        insertCodeAfterMethod("BooksReader", "readBook", "System.out.println(\"Code after method inserted\"); }");
    }

    private static void addFieldToCompiledClass(String fieldName, String className) {
        ClassPool classPool = ClassPool.getDefault();
        ClassFile classFile = null;
        try {
            classFile = classPool.get(className)
                    .getClassFile();
        } catch (NotFoundException e) {
            LOGGER.info(e.getMessage());
        }

        FieldInfo field = new FieldInfo(classFile.getConstPool(), fieldName, "I");
        field.setAccessFlags(AccessFlag.PUBLIC);

        try {
            classFile.addField(field);
        } catch (DuplicateMemberException e) {
            LOGGER.info(e.getMessage());
        }
    }

    private static void addFieldDeclarationToCompiledClass(String fieldDeclaration, String className) {
        try {
            CtClass ctclass = ClassPool.getDefault().get(className);
            CtField ctField = CtField.make(fieldDeclaration, ctclass);
            ctclass.addField(ctField);
        } catch (NotFoundException | CannotCompileException e) {
            LOGGER.info(e.getMessage());
        }
    }

    private static void addMethodDeclarationToCompiledClass(String methodDeclaration, String className) {
        try {
            CtClass ctclass = ClassPool.getDefault().get(className);
            CtMethod ctMethod = CtNewMethod.make(methodDeclaration, ctclass);
            ctclass.addMethod(ctMethod);
            ctclass.writeFile();
        } catch (NotFoundException | CannotCompileException | IOException e) {
            LOGGER.info(e.getMessage());
        }
    }

    private static void insertCodeAfterMethod(String className, String methodName, String codeToInsertAfter) {
        try {
            CtClass ctclass = ClassPool.getDefault().get(className);
            CtMethod ctMethod = ctclass.getDeclaredMethod(methodName);
            ctMethod.insertAfter(codeToInsertAfter);
        } catch (NotFoundException | CannotCompileException e) {
            LOGGER.info(e.getMessage());
        }
    }
}