package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.util.AsmUtil;
import dev.xdark.ssvm.util.BiIntPredicate;
import org.objectweb.asm.tree.JumpInsnNode;

import java.util.function.IntPredicate;

/**
 * Jumps if predicate on two ints succeeds.
 *
 * @author xDark
 */
public final class BiIntJumpProcessor implements InstructionProcessor<JumpInsnNode> {

	private final BiIntPredicate condition;

	/**
	 * @param condition
	 * 		Predicate to check.
	 */
	public BiIntJumpProcessor(BiIntPredicate condition) {
		this.condition = condition;
	}

	@Override
	public Result execute(JumpInsnNode insn, ExecutionContext ctx) {
		var stack = ctx.getStack();
		var v2 = stack.pop().asInt();
		var v1 = stack.pop().asInt();
		if (condition.test(v1, v2)) {
			ctx.setInsnPosition(AsmUtil.getIndex(insn.label));
		}
		return Result.CONTINUE;
	}
}
