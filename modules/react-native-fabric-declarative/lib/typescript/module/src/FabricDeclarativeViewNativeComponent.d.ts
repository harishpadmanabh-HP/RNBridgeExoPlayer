import type { BubblingEventHandler, Double } from 'react-native/Libraries/Types/CodegenTypes';
import type { ViewProps } from 'react-native';
interface SubmitEvent {
    input: string;
    selectedNumber: Double;
    objectResults: {
        restNumbers: Double[];
        uppercaseInput: string;
    };
}
interface NativeProps extends ViewProps {
    title: string;
    options: Double[];
    onSubmit: BubblingEventHandler<Readonly<SubmitEvent>>;
}
declare const _default: import("react-native/Libraries/Utilities/codegenNativeComponent").NativeComponentType<NativeProps>;
export default _default;
//# sourceMappingURL=FabricDeclarativeViewNativeComponent.d.ts.map