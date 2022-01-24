package org.sec.core.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.sec.core.jvm.CoreMethodAdapter;

import java.util.Map;

public class RCEMethodAdapter extends CoreMethodAdapter<Boolean> {
    private final int access;
    private final String desc;
    private final int methodArgIndex;
    private final Map<String, Boolean> pass;

    public RCEMethodAdapter(int methodArgIndex, Map<String, Boolean> pass, int api, MethodVisitor mv,
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
        boolean runtimeCondition = owner.equals("java/lang/Runtime") && name.equals("exec") &&
                desc.equals("(Ljava/lang/String;)Ljava/lang/Process;");
        boolean processInitCondition = owner.equals("java/lang/ProcessBuilder") && name.equals("<init>") &&
                desc.equals("([Ljava/lang/String;)V");
        boolean processStartCondition = owner.equals("java/lang/ProcessBuilder") && name.equals("start") &&
                desc.equals("()Ljava/lang/Process;");
        boolean groovyCondition = owner.equals("groovy/lang/GroovyShell") && name.equals("evaluate") &&
                desc.equals("(Ljava/lang/String;)Ljava/lang/Object;");

        if (processInitCondition) {
            if (operandStack.get(0).contains(true)) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                operandStack.set(0, true);
                return;
            }
        }
        if (runtimeCondition) {
            if (operandStack.get(0).contains(true)) {
                pass.put("RUNTIME", true);
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }
        }
        if (processStartCondition) {
            if (operandStack.get(0).contains(true)) {
                pass.put("PROCESS", true);
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }
        }
        if (groovyCondition) {
            if (operandStack.get(0).contains(true)) {
                pass.put("GROOVY", true);
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
