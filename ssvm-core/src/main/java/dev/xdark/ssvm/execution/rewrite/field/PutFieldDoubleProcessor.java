package dev.xdark.ssvm.execution.rewrite.field;

import dev.xdark.ssvm.asm.VMFieldInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.value.InstanceValue;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * Fast path processor for PUTFIELD.
 *
 * @author xDark
 */
public final class PutFieldDoubleProcessor implements InstructionProcessor<VMFieldInsnNode> {

	@Override
	public Result execute(VMFieldInsnNode insn, ExecutionContext<?> ctx) {
		Stack stack = ctx.getStack();
		double value = stack.popDouble();
		InstanceValue instance = ctx.getOperations().checkNotNull(stack.popReference());
		JavaField field = insn.getResolved();
		instance.getMemory().getData().writeLong(field.getOffset(), Double.doubleToRawLongBits(value));
		return Result.CONTINUE;
	}
}