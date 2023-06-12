package llj.asm.bytecode;

import llj.packager.jclass.FormatException;
import llj.packager.jclass.attributes.BootstrapMethodsAttribute;
import llj.packager.jclass.constants.Constant;
import llj.packager.jclass.constants.ConstantRef;
import llj.packager.jclass.constants.ResolveException;

import java.util.ArrayList;
import java.util.List;

public class BootstrapMethodData {

    public final MethodHandle methodHandle;
    public final List<ConstantData> params;

    public BootstrapMethodData(MethodHandle methodHandle, List<ConstantData> params) {
        this.methodHandle = methodHandle;
        this.params = params;
    }

    public static BootstrapMethodData read(BootstrapMethodsAttribute.BootstrapMethod bootstrapMethodDesc) throws ResolveException, FormatException {
        MethodHandle methodHandle = MethodHandle.read(bootstrapMethodDesc.methodRef.resolve());
        List<ConstantData> params = new ArrayList<>(bootstrapMethodDesc.arguments.size());
        for (ConstantRef args : bootstrapMethodDesc.arguments) {
            Constant resolve = args.resolve();
            params.add(ConstantData.get(resolve));
        }
        return new BootstrapMethodData(methodHandle, params);
    }
}
