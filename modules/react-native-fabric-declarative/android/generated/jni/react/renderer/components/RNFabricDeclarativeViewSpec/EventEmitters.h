
/**
 * This code was generated by [react-native-codegen](https://www.npmjs.com/package/react-native-codegen).
 *
 * Do not edit this file as changes may cause incorrect behavior and will be lost
 * once the code is regenerated.
 *
 * @generated by codegen project: GenerateEventEmitterH.js
 */
#pragma once

#include <react/renderer/components/view/ViewEventEmitter.h>


namespace facebook::react {
class FabricDeclarativeViewEventEmitter : public ViewEventEmitter {
 public:
  using ViewEventEmitter::ViewEventEmitter;

  struct OnSubmitObjectResults {
      std::vector<double> restNumbers;
    std::string uppercaseInput;
    };

  struct OnSubmit {
      std::string input;
    double selectedNumber;
    OnSubmitObjectResults objectResults;
    };
  void onSubmit(OnSubmit value) const;
};
} // namespace facebook::react
