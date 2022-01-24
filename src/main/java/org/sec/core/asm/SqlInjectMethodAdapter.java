package org.sec.core.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.sec.core.jvm.CoreMethodAdapter;

import java.util.List;

public class SqlInjectMethodAdapter extends CoreMethodAdapter<Boolean> {
    private final int access;
    private final String desc;
    private final int methodArgIndex;
    private final List<Boolean> save;

    public SqlInjectMethodAdapter(int methodArgIndex, List<Boolean> save, int api, MethodVisitor mv,
                                  String owner, int access, String name, String desc) {
        super(api, mv, owner, access, name, desc);
        this.access = access;
        this.desc = desc;
        this.methodArgIndex = methodArgIndex;
        this.save = save;
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
        boolean buildSqlCondition = owner.equals("java/lang/StringBuilder") &&
                name.equals("append") &&
                desc.equals("(Ljava/lang/String;)Ljava/lang/StringBuilder;");

        boolean toStringCondition = owner.equals("java/lang/StringBuilder") &&
                name.equals("toString") &&
                desc.equals("()Ljava/lang/String;");

        boolean jdbcOneParamCondition = owner.equals("org/springframework/jdbc/core/JdbcTemplate") &&
                (name.equals("update") || name.equals("execute"));

        boolean jdbcTwoParamCondition = owner.equals("org/springframework/jdbc/core/JdbcTemplate") &&
                (name.equals("query") ||
                        name.equals("queryForStream") ||
                        name.equals("queryForList") ||
                        name.equals("queryForMap") ||
                        name.equals("queryForObject"));

        boolean stmtExecuteCondition = owner.equals("java/sql/Statement") &&
                (name.equals("executeQuery") || name.equals("execute") || name.equals("executeUpdate"));

        if (buildSqlCondition) {
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
        if (jdbcOneParamCondition || stmtExecuteCondition) {
            if (operandStack.get(0).contains(true)) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                save.add(true);
                return;
            }
        }
        if (jdbcTwoParamCondition) {
            if (operandStack.get(1).contains(true)) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                save.add(true);
                return;
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
