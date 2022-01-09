package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.IntValue;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Converts int to short.
 *
 * @author xDark
 */
public final class IntToShortProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		var stack = ctx.getStack();
		stack.push(new IntValue(stack.pop().asShort()));
		return Result.CONTINUE;
	}
}
