package dev.xdark.ssvm.api;

import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface to configure/adjust VM.
 *
 * @author xDark
 */
public final class VMInterface {

	private final InstructionProcessor[] processors = new InstructionProcessor[Opcodes.IFNONNULL + 1];
	private final Map<VMCall, MethodInvoker> invokerMap = new HashMap<>();

	/**
	 * Gets an instruction processor.
	 *
	 * @param <I>
	 * 		Type of the instruction.
	 * @param insn
	 * 		Instruction to get processor for.
	 */
	public <I extends AbstractInsnNode> InstructionProcessor<I> getProcessor(I insn) {
		return processors[insn.getOpcode()];
	}

	/**
	 * Gets an instruction processor by opcode.
	 *
	 * @param opcode
	 * 		Instruction opcode.
	 */
	public InstructionProcessor<?> getProcessor(int opcode) {
		return processors[opcode];
	}

	/**
	 * Sets an instruction processor.
	 *
	 * @param opcode
	 * 		Opcode of the instruction.
	 * @param processor
	 * 		Processor of the opcode.
	 */
	public void setProcessor(int opcode, InstructionProcessor<?> processor) {
		processors[opcode] = processor;
	}

	/**
	 * Returns method invoker based off call info.
	 *
	 * @param call
	 * 		Call info.
	 *
	 * @return method invoker.
	 */
	public MethodInvoker getInvoker(VMCall call) {
		return invokerMap.get(call);
	}

	/**
	 * Sets an invoker for the method.
	 *
	 * @param call
	 * 		Call information.
	 * @param invoker
	 * 		Method invoker.
	 */
	public void setInvoker(VMCall call, MethodInvoker invoker) {
		invokerMap.put(call, invoker);
	}

	/**
	 * Sets an invoker for the method.
	 *
	 * @param jc
	 * 		Instance class.
	 * @param name
	 * 		Name of the method.
	 * @param desc
	 * 		Descriptor of the method.
	 * @param invoker
	 * 		Method invoker.
	 *
	 * @throws IllegalStateException
	 * 		If method was not found.
	 */
	public void setInvoker(InstanceJavaClass jc, String name, String desc, MethodInvoker invoker) {
		var method = jc.getMethod(name, desc);
		if (method == null) {
			throw new IllegalStateException("Method not found: " + name + desc);
		}
		setInvoker(new VMCall(jc, method), invoker);
	}
}
