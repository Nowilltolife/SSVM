package dev.xdark.ssvm.execution.rewrite;

import dev.xdark.ssvm.asm.VMFieldInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaField;
import dev.xdark.ssvm.value.InstanceValue;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * Fast path processor for GETFIELD.
 *
 * @author xDark
 */
public final class GetFieldReferenceProcessor implements InstructionProcessor<VMFieldInsnNode> {

	@Override
	public Result execute(VMFieldInsnNode insn, ExecutionContext<?> ctx) {
		Stack stack = ctx.getStack();
		InstanceValue instance = ctx.getHelper().checkNotNull(stack.popReference());
		JavaField field = insn.getResolved();
		if (field == null) {
			InstanceJavaClass klass = instance.getJavaClass();
			FieldInsnNode delegate = insn.getDelegate();
			field = ctx.getLinkResolver().resolveVirtualField(klass, klass, delegate.name, delegate.desc);
		}
		MemoryManager memory = ctx.getMemoryManager();
		stack.pushReference(memory.readReference(instance, memory.valueBaseOffset(instance) + field.getOffset()));
		return Result.CONTINUE;
	}
}
