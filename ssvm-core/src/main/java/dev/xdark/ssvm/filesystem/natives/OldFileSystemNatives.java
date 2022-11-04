package dev.xdark.ssvm.filesystem.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.filesystem.FileManager;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 * Initializes file system in java.io package.
 *
 * @author xDark
 */
@UtilityClass
public class OldFileSystemNatives {

	/**
	 * Initializes win32/unix file system class.
	 *
	 * @param vm          VM instance.
	 * @param fileManager File manager.
	 */
	public void init(VirtualMachine vm, FileManager fileManager) {
		InstanceClass fs = (InstanceClass) vm.findBootstrapClass("java/io/WinNTFileSystem");
		boolean unix = false;
		if (fs == null) {
			fs = (InstanceClass) vm.findBootstrapClass("java/io/UnixFileSystem");
			if (fs == null) {
				throw new IllegalStateException("Unable to locate file system implementation class for java.io package");
			}
			unix = true;
		}
		VMInterface vmi = vm.getInterface();
		vmi.setInvoker(fs, "initIDs", "()V", MethodInvoker.noop());
		vmi.setInvoker(fs, "canonicalize0", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			VMOperations ops = vm.getOperations();
			String path = ops.readUtf8(ctx.getLocals().loadReference(1));
			try {
				ctx.setResult(ops.newUtf8(fileManager.canonicalize(path)));
			} catch (IOException ex) {
				ops.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fs, unix ? "getBooleanAttributes0" : "getBooleanAttributes", "(Ljava/io/File;)I", ctx -> {
			VMOperations ops = vm.getOperations();
			ObjectValue value = ctx.getLocals().loadReference(1);
			ops.checkNotNull(value);
			try {
				String path = ops.readUtf8(ops.getReference(value, "path", "Ljava/lang/String;"));
				BasicFileAttributes attributes = fileManager.getAttributes(path, BasicFileAttributes.class);
				if (attributes == null) {
					ctx.setResult(0);
				} else {
					int res = 1;
					if (attributes.isDirectory()) {
						res |= 4;
					} else {
						res |= 2;
					}
					ctx.setResult(res);
				}
			} catch (IOException ex) {
				ops.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fs, "list", "(Ljava/io/File;)[Ljava/lang/String;", ctx -> {
			VMOperations ops = vm.getOperations();
			ObjectValue value = ctx.getLocals().loadReference(1);
			ops.checkNotNull(value);
			String path = ops.readUtf8(vm.getOperations().getReference(value, "path", "Ljava/lang/String;"));
			String[] list = fileManager.list(path);
			if (list == null) {
				ctx.setResult(vm.getMemoryManager().nullValue());
			} else {
				ArrayValue values = ops.allocateArray(vm.getSymbols().java_lang_String(), list.length);
				for (int i = 0; i < list.length; i++) {
					values.setReference(i, ops.newUtf8(list[i]));
				}
				ctx.setResult(values);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fs, "canonicalizeWithPrefix0", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			ctx.setResult(ctx.getLocals().loadReference(2));
			return Result.ABORT;
		});
		vmi.setInvoker(fs, "getLastModifiedTime", "(Ljava/io/File;)J", ctx -> {
			VMOperations ops = vm.getOperations();
			ObjectValue value = ctx.getLocals().loadReference(1);
			ops.checkNotNull(value);
			try {
				String path = ops.readUtf8(ops.getReference(value, "path", "Ljava/lang/String;"));
				BasicFileAttributes attributes = fileManager.getAttributes(path, BasicFileAttributes.class);
				if (attributes == null) {
					ctx.setResult(0L);
				} else {
					ctx.setResult(attributes.lastModifiedTime().toMillis());
				}
			} catch (IOException ex) {
				ctx.setResult(0L);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fs, "getLength", "(Ljava/io/File;)J", ctx -> {
			VMOperations ops = vm.getOperations();
			ObjectValue value = ctx.getLocals().loadReference(1);
			ops.checkNotNull(value);
			try {
				String path = ops.readUtf8(ops.getReference(value, "path", "Ljava/lang/String;"));
				BasicFileAttributes attributes = fileManager.getAttributes(path, BasicFileAttributes.class);
				if (attributes == null) {
					ctx.setResult(0L);
				} else {
					ctx.setResult(attributes.size());
				}
			} catch (IOException ex) {
				ctx.setResult(0L);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fs, "checkAccess", "(Ljava/io/File;I)Z", ctx -> {
			VMOperations ops = vm.getOperations();
			ObjectValue value = ctx.getLocals().loadReference(1);
			ops.checkNotNull(value);
			String path = ops.readUtf8(ops.getReference(value, "path", "Ljava/lang/String;"));
			int access = ctx.getLocals().loadInt(2);
			ctx.setResult(fileManager.checkAccess(path, access) ? 1 : 0);
			return Result.ABORT;
		});
		vmi.setInvoker(fs, "rename0", "(Ljava/io/File;Ljava/io/File;)Z", ctx -> {
			VMOperations ops = vm.getOperations();
			ObjectValue value = ctx.getLocals().loadReference(1);
			ops.checkNotNull(value);
			String path = ops.readUtf8(ops.getReference(value, "path", "Ljava/lang/String;"));
			value = ctx.getLocals().loadReference(2);
			ops.checkNotNull(value);
			String newPath = ops.readUtf8(ops.getReference(value, "path", "Ljava/lang/String;"));
			ctx.setResult(fileManager.rename(path, newPath) ? 1 : 0);
			return Result.ABORT;
		});
		vmi.setInvoker(fs, "delete0", "(Ljava/io/File;)Z", ctx -> {
			VMOperations ops = vm.getOperations();
			ObjectValue value = ctx.getLocals().loadReference(1);
			ops.checkNotNull(value);
			String path = ops.readUtf8(ops.getReference(value, "path", "Ljava/lang/String;"));
			ctx.setResult(fileManager.delete(path) ? 1 : 0);
			return Result.ABORT;
		});
		vmi.setInvoker(fs, "setLastModifiedTime", "(Ljava/io/File;J)Z", ctx -> {
			VMOperations ops = vm.getOperations();
			ObjectValue value = ctx.getLocals().loadReference(1);
			ops.checkNotNull(value);
			String path = ops.readUtf8(ops.getReference(value, "path", "Ljava/lang/String;"));
			long time = ctx.getLocals().loadLong(2);
			ctx.setResult(fileManager.setLastModifiedTime(path, time) ? 1 : 0);
			return Result.ABORT;
		});
		vmi.setInvoker(fs, "setReadOnly", "(Ljava/io/File;)Z", ctx -> {
			VMOperations ops = vm.getOperations();
			ObjectValue value = ctx.getLocals().loadReference(1);
			ops.checkNotNull(value);
			String path = ops.readUtf8(ops.getReference(value, "path", "Ljava/lang/String;"));
			ctx.setResult(fileManager.setReadOnly(path) ? 1 : 0);
			return Result.ABORT;
		});
		vmi.setInvoker(fs, "createFileExclusively", "(Ljava/lang/String;)Z", ctx -> {
			VMOperations ops = vm.getOperations();
			String path = ops.readUtf8(ctx.getLocals().loadReference(1));
			try {
				ctx.setResult(fileManager.createFileExclusively(path) ? 1 : 0);
			} catch (IOException ex) {
				ops.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fs, "setPermission", "(Ljava/io/File;IZZ)Z", ctx -> {
			VMOperations ops = vm.getOperations();
			ObjectValue value = ctx.getLocals().loadReference(1);
			ops.checkNotNull(value);
			String path = ops.readUtf8(ops.getReference(value, "path", "Ljava/lang/String;"));
			int access = ctx.getLocals().loadInt(2);
			boolean enable = ctx.getLocals().loadInt(3) != 0;
			boolean ownerOnly = ctx.getLocals().loadInt(4) != 0;
			ctx.setResult(fileManager.setPermission(path, access, enable, ownerOnly) ? 1 : 0);
			return Result.ABORT;
		});
		vmi.setInvoker(fs, "getSpace", "(Ljava/io/File;I)J", ctx -> {
			VMOperations ops = vm.getOperations();
			ObjectValue value = ctx.getLocals().loadReference(1);
			ops.checkNotNull(value);
			String path = ops.readUtf8(ops.getReference(value, "path", "Ljava/lang/String;"));
			int id = ctx.getLocals().loadInt(2);
			ctx.setResult(fileManager.getSpace(path, id));
			return Result.ABORT;
		});
	}
}
