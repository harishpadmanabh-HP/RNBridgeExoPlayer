import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';
import type {
  BubblingEventHandler,
  Double,
} from 'react-native/Libraries/Types/CodegenTypes';
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

export default codegenNativeComponent<NativeProps>('FabricDeclarativeView');