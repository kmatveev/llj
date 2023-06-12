package llj.asm.bytecode;

import llj.packager.jclass.FormatException;
import llj.packager.jclass.constants.InvokeDynamicConstant;
import llj.packager.jclass.constants.NameTypePairConstant;

import java.util.List;

public class CallSiteData {

    public final BootstrapMethodData bootstrapMethodData;
    public final String methodName;
    public final MethodTypeData methodTypeData;

    public CallSiteData(BootstrapMethodData bootstrapMethodData, String methodName, MethodTypeData methodTypeData) {
        this.bootstrapMethodData = bootstrapMethodData;
        this.methodName = methodName;
        this.methodTypeData = methodTypeData;
    }

    public static CallSiteData readFrom(InvokeDynamicConstant callSiteDesc, List<BootstrapMethodData> bootstrapMethods) throws FormatException {
        BootstrapMethodData bootstrapMethodData = bootstrapMethods.get(callSiteDesc.bootstrapMethodAttrIndex);
        NameTypePairConstant nameTypePairDesc = callSiteDesc.nameTypeRef.resolve();
        String name = nameTypePairDesc.resolveName();
        MethodTypeData methodTypeData = MethodTypeData.read(nameTypePairDesc.typeRef);
        return new CallSiteData(bootstrapMethodData, name, methodTypeData);
    }

}
