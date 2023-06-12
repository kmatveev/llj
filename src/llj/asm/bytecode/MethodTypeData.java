package llj.asm.bytecode;

import llj.packager.jclass.FormatException;
import llj.packager.jclass.constants.ConstantRef;
import llj.packager.jclass.constants.NameTypePairConstant;
import llj.packager.jclass.constants.StringConstant;

import java.util.List;

public class MethodTypeData {

    public final Type resultType;
    public final List<Type> paramTypes;

    public MethodTypeData(Type resultType, List<Type> paramTypes) {
        this.resultType = resultType;
        this.paramTypes = paramTypes;
    }

    public static MethodTypeData read(ConstantRef<StringConstant> typeRef) throws FormatException {
        String typeSignature = typeRef.resolve().value;
        List<Type> paramTypes = MethodData.extractParamTypes(typeSignature);
        Type resultType = MethodData.extractReturnType(typeSignature);
        return new MethodTypeData(resultType, paramTypes);
    }


}
