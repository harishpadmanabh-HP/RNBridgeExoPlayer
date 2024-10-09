#ifdef RCT_NEW_ARCH_ENABLED
#import "FabricDeclarativeView.h"

#import <react/renderer/components/RNFabricDeclarativeViewSpec/ComponentDescriptors.h>
#import <react/renderer/components/RNFabricDeclarativeViewSpec/EventEmitters.h>
#import <react/renderer/components/RNFabricDeclarativeViewSpec/Props.h>
#import <react/renderer/components/RNFabricDeclarativeViewSpec/RCTComponentViewHelpers.h>

#import "RCTFabricComponentsPlugins.h"
#import "Utils.h"

using namespace facebook::react;

@interface FabricDeclarativeView () <RCTFabricDeclarativeViewViewProtocol>

@end

@implementation FabricDeclarativeView {
    UIView * _view;
}

+ (ComponentDescriptorProvider)componentDescriptorProvider
{
    return concreteComponentDescriptorProvider<FabricDeclarativeViewComponentDescriptor>();
}

- (instancetype)initWithFrame:(CGRect)frame
{
  if (self = [super initWithFrame:frame]) {
    static const auto defaultProps = std::make_shared<const FabricDeclarativeViewProps>();
    _props = defaultProps;

    _view = [[UIView alloc] init];

    self.contentView = _view;
  }

  return self;
}

- (void)updateProps:(Props::Shared const &)props oldProps:(Props::Shared const &)oldProps
{
    const auto &oldViewProps = *std::static_pointer_cast<FabricDeclarativeViewProps const>(_props);
    const auto &newViewProps = *std::static_pointer_cast<FabricDeclarativeViewProps const>(props);

    if (oldViewProps.color != newViewProps.color) {
        NSString * colorToConvert = [[NSString alloc] initWithUTF8String: newViewProps.color.c_str()];
        [_view setBackgroundColor: [Utils hexStringToColor:colorToConvert]];
    }

    [super updateProps:props oldProps:oldProps];
}

Class<RCTComponentViewProtocol> FabricDeclarativeViewCls(void)
{
    return FabricDeclarativeView.class;
}

@end
#endif
