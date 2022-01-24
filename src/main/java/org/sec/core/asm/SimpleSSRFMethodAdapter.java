package org.sec.core.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.sec.core.jvm.CoreMethodAdapter;

import java.util.Map;

public class SimpleSSRFMethodAdapter extends CoreMethodAdapter<Boolean> {
    private final int access;
    private final String desc;
    private final int methodArgIndex;
    private final Map<String, Boolean> pass;

    public SimpleSSRFMethodAdapter(int methodArgIndex, Map<String, Boolean> pass, int api, MethodVisitor mv,
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
        boolean urlCondition = owner.equals("java/net/URL") && name.equals("<init>") &&
                desc.equals("(Ljava/lang/String;)V");
        boolean urlOpenCondition = owner.equals("java/net/URL") && name.equals("openConnection") &&
                desc.equals("()Ljava/net/URLConnection;");
        boolean urlInputCondition = owner.equals("java/net/HttpURLConnection") &&
                name.equals("getInputStream") && desc.equals("()Ljava/io/InputStream;");
        boolean apacheHttpInitCondition = owner.equals("org/apache/http/client/methods/HttpGet") &&
                name.equals("<init>") && desc.equals("(Ljava/lang/String;)V");
        boolean apacheHttpExecuteCondition = owner.equals("org/apache/http/impl/client/CloseableHttpClient") &&
                name.equals("execute") && desc.equals("(Lorg/apache/http/client/methods/HttpUriRequest;)" +
                "Lorg/apache/http/client/methods/CloseableHttpResponse;");

        if (urlCondition) {
            if (operandStack.get(0).contains(true)) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                operandStack.set(0, true);
                return;
            }
        }
        if (urlOpenCondition) {
            if (operandStack.get(0).contains(true)) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                operandStack.set(0, true);
                return;
            }
        }
        if (urlInputCondition) {
            if (operandStack.get(0).contains(true)) {
                pass.put("JDK", true);
                return;
            }
        }

        if (apacheHttpInitCondition) {
            if (operandStack.get(0).contains(true)) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                operandStack.set(0, true);
                return;
            }
        }
        if (apacheHttpExecuteCondition) {
            if (operandStack.get(0).contains(true)) {
                pass.put("APACHE", true);
                return;
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
