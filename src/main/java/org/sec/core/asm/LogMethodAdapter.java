package org.sec.core.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.sec.core.jvm.CoreMethodAdapter;

import java.util.Map;

public class LogMethodAdapter extends CoreMethodAdapter<Boolean> {
    private final int access;
    private final String desc;
    private final int methodArgIndex;
    private final Map<String, Boolean> pass;

    public LogMethodAdapter(int methodArgIndex, Map<String, Boolean> pass, int api, MethodVisitor mv,
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
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.AASTORE) {
            super.visitInsn(opcode);
            if (operandStack.size() >= 1) {
                operandStack.set(0, true);
            }
            return;
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        boolean toStringCondition = owner.equals("java/lang/StringBuilder") &&
                name.equals("toString") &&
                desc.equals("()Ljava/lang/String;");
        boolean buildStrCondition = owner.equals("java/lang/StringBuilder") &&
                name.equals("append") &&
                desc.equals("(Ljava/lang/String;)Ljava/lang/StringBuilder;");

        boolean slf4jCondition = owner.equals("org/slf4j/Logger") &&
                (name.equals("info") || name.equals("warn") || name.equals("error"));
        boolean log4jCondition = owner.equals("org/apache/logging/log4j/Logger") &&
                (name.equals("info") || name.equals("warn") || name.equals("error"));

        if (buildStrCondition) {
            if (operandStack.get(0).contains(true) ||
                    operandStack.get(1).contains(true)) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                operandStack.set(0, true);
                return;
            }
        }
        if (toStringCondition) {
            if (operandStack.get(0).contains(true)) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                operandStack.set(0, true);
                return;
            }
        }
        if (slf4jCondition) {
            if (operandStack.get(0).contains(true)) {
                pass.put("SLF4J", true);
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }
        }
        if(log4jCondition){
            if (operandStack.get(0).contains(true)) {
                pass.put("LOG4J", true);
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
