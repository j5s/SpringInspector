package org.sec.core.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.sec.core.jvm.CoreMethodAdapter;

import java.util.Map;

public class RedirectMethodAdapter extends CoreMethodAdapter<Boolean> {
    private final int access;
    private final String desc;
    private final Map<String, Boolean> pass;
    private boolean flag;

    public RedirectMethodAdapter(Map<String, Boolean> pass, int api, MethodVisitor mv,
                                 String owner, int access, String name, String desc) {
        super(api, mv, owner, access, name, desc);
        this.access = access;
        this.desc = desc;
        this.pass = pass;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        int localIndex = 0;
        if ((this.access & Opcodes.ACC_STATIC) == 0) {
            localIndex += 1;
        }
        for (Type argType : Type.getArgumentTypes(desc)) {
            localVariables.set(localIndex, true);
            localIndex += argType.getSize();
        }
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.ARETURN) {
            if (operandStack.get(0).contains(true)) {
                if (flag) {
                    pass.put("SPRING", true);
                }
            }
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitLdcInsn(Object cst) {
        if (((String) cst).contains("redirect://")) {
            flag = true;
        }
        super.visitLdcInsn(cst);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        boolean getParamCondition = owner.equals("javax/servlet/http/HttpServletRequest") &&
                name.equals("getParameter") && desc.equals("(Ljava/lang/String;)Ljava/lang/String;");
        boolean sendRedirectCondition = owner.equals("javax/servlet/http/HttpServletResponse") &&
                name.equals("sendRedirect") && desc.equals("(Ljava/lang/String;)V");

        boolean buildStrCondition = owner.equals("java/lang/StringBuilder") &&
                name.equals("append") &&
                desc.equals("(Ljava/lang/String;)Ljava/lang/StringBuilder;");

        boolean toStringCondition = owner.equals("java/lang/StringBuilder") &&
                name.equals("toString") &&
                desc.equals("()Ljava/lang/String;");
        boolean modelAndViewCondition = owner.equals("org/springframework/web/servlet/ModelAndView") &&
                name.equals("<init>") && desc.equals("(Ljava/lang/String;)V");

        if (getParamCondition) {
            if (operandStack.get(1).contains(true)) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                operandStack.set(0, true);
                return;
            }
        }
        if (sendRedirectCondition) {
            if (operandStack.get(0).contains(true)) {
                pass.put("SERVLET", true);
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }
        }
        if (buildStrCondition) {
            if (operandStack.get(0).contains(true) ||
                    operandStack.get(1).contains(true)) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                operandStack.set(0, true);
                return;
            }
        }
        if (toStringCondition || modelAndViewCondition) {
            if (operandStack.get(0).contains(true)) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                operandStack.set(0, true);
                return;
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
