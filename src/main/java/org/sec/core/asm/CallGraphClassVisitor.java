package org.sec.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.sec.core.inherit.InheritanceMap;
import org.sec.model.CallGraph;
import org.sec.model.ClassReference;
import org.sec.model.MethodReference;

import java.util.Map;
import java.util.Set;

@SuppressWarnings("all")
public class CallGraphClassVisitor extends ClassVisitor {
    private final Set<CallGraph> discoveredCalls;
    private String name;
    private String signature;
    private String superName;
    private String[] interfaces;

    public CallGraphClassVisitor(Set<CallGraph> discoveredCalls) {
        super(Opcodes.ASM6);
        this.discoveredCalls = discoveredCalls;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.name = name;
        this.signature = signature;
        this.superName = superName;
        this.interfaces = interfaces;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        CallGraphMethodAdapter callGraphMethodVisitor = new CallGraphMethodAdapter(api, discoveredCalls,
                mv, this.name, access, name, desc, signature, exceptions);
        return new JSRInlinerAdapter(callGraphMethodVisitor, access, name, desc, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
