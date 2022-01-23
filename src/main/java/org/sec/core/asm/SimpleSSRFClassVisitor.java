package org.sec.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.sec.model.MethodReference;

import java.util.ArrayList;
import java.util.List;

public class SimpleSSRFClassVisitor extends ClassVisitor {
    private String name;
    private final MethodReference.Handle methodHandle;
    private final int methodArgIndex;
    private final List<Boolean> pass;

    public SimpleSSRFClassVisitor(MethodReference.Handle targetMethod, int targetIndex) {
        super(Opcodes.ASM6);
        this.methodHandle = targetMethod;
        this.methodArgIndex = targetIndex;
        this.pass = new ArrayList<>();
    }

    public List<Boolean> getPass() {
        return pass;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.name = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (name.equals(this.methodHandle.getName())) {
            SimpleSSRFMethodAdapter ssrfMethodAdapter = new SimpleSSRFMethodAdapter(
                    this.methodArgIndex, this.pass, Opcodes.ASM6, mv,
                    this.name, access, name, descriptor);
            return new JSRInlinerAdapter(ssrfMethodAdapter,
                    access, name, descriptor, signature, exceptions);
        }
        return mv;
    }
}
