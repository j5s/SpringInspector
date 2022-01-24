package org.sec.core.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.sec.core.jvm.CoreMethodAdapter;

import java.util.Map;

public class XXEMethodAdapter extends CoreMethodAdapter<Boolean> {
    private final int access;
    private final String desc;
    private final int methodArgIndex;
    private final Map<String, Boolean> pass;

    public XXEMethodAdapter(int methodArgIndex, Map<String, Boolean> pass, int api, MethodVisitor mv,
                            String owner, int access, String name, String desc) {
        super(api, mv, owner, access, name, desc);
        this.access = access;
        this.desc = desc;
        this.methodArgIndex = methodArgIndex;
        this.pass = pass;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        int localIndex = 0;
        int argIndex = 0;
        if ((this.access & Opcodes.ACC_STATIC) == 0) {
            localIndex += 1;
            argIndex += 1;
        }
        for (Type argType : Type.getArgumentTypes(desc)) {
            if (argIndex == this.methodArgIndex) {
                localVariables.set(localIndex, true);
            }
            localIndex += argType.getSize();
            argIndex += 1;
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        boolean fileCondition = owner.equals("java/io/File") && name.equals("<init>") &&
                desc.equals("(Ljava/lang/String;)V");
        boolean fileInputStreamCondition = owner.equals("java/io/FileInputStream") && name.equals("<init>") &&
                desc.equals("(Ljava/lang/String;)V");
        boolean inputSourceCondition = owner.equals("org/xml/sax/InputSource") && name.equals("<init>") &&
                desc.equals("(Ljava/io/InputStream;)V");
        boolean streamSourceCondition = owner.equals("javax/xml/transform/stream/StreamSource") &&
                name.equals("<init>");

        boolean saxBuilderCondition = owner.equals("org/jdom2/input/SAXBuilder") && name.equals("build");
        boolean saxParserCondition = owner.equals("javax/xml/parsers/SAXParser") && name.equals("parse");
        boolean saxTransformerFactoryCondition = owner.equals("javax/xml/transform/sax/SAXTransformerFactory") &&
                name.equals("newTransformerHandler");
        boolean schemaFactoryCondition = owner.equals("javax/xml/validation/SchemaFactory") &&
                name.equals("newSchema");
        boolean transformerFactory = owner.equals("javax/xml/transform/Transformer") && name.equals("transform");
        boolean validatorCondition = owner.equals("javax/xml/validation/Validator") && name.equals("validate");
        boolean xmlReaderCondition = owner.equals("org/xml/sax/XMLReader") && name.equals("parse");

        if (fileCondition || fileInputStreamCondition || inputSourceCondition || streamSourceCondition) {
            if (operandStack.get(0).contains(true)) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                operandStack.set(0, true);
                return;
            }
        }
        if (saxBuilderCondition) {
            if (operandStack.get(0).contains(true)) {
                pass.put("SAX-BUILDER", true);
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }
        }
        if (saxParserCondition) {
            if (operandStack.get(1).contains(true)) {
                pass.put("SAX-PARSER", true);
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }
        }
        if (saxTransformerFactoryCondition) {
            if (operandStack.get(0).contains(true)) {
                pass.put("SAX-TRANSFORMER-FACTORY", true);
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }
        }
        if (schemaFactoryCondition) {
            if (operandStack.get(0).contains(true)) {
                pass.put("SCHEMA-FACTORY", true);
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }
        }
        if (transformerFactory) {
            if (operandStack.get(1).contains(true)) {
                pass.put("TRANSFORMER-FACTORY", true);
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }
        }
        if (validatorCondition) {
            if (operandStack.get(0).contains(true)) {
                pass.put("VALIDATOR", true);
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }
        }
        if (xmlReaderCondition) {
            if (operandStack.get(0).contains(true)) {
                pass.put("XML-READER", true);
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
