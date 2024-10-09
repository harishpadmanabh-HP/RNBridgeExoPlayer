#ifdef RCT_NEW_ARCH_ENABLED
#import "FabricVideoPlayerView.h"

#import <react/renderer/components/RNFabricVideoPlayerViewSpec/ComponentDescriptors.h>
#import <react/renderer/components/RNFabricVideoPlayerViewSpec/EventEmitters.h>
#import <react/renderer/components/RNFabricVideoPlayerViewSpec/Props.h>
#import <react/renderer/components/RNFabricVideoPlayerViewSpec/RCTComponentViewHelpers.h>

#import "RCTFabricComponentsPlugins.h"
#import "Utils.h"

using namespace facebook::react;

@interface FabricVideoPlayerView () <RCTFabricVideoPlayerViewViewProtocol>

@end

@implementation FabricVideoPlayerView {
    UIView * _view;
}

+ (ComponentDescriptorProvider)componentDescriptorProvider
{
    return concreteComponentDescriptorProvider<FabricVideoPlayerViewComponentDescriptor>();
}

- (instancetype)initWithFrame:(CGRect)frame
{
  if (self = [super initWithFrame:frame]) {
    static const auto defaultProps = std::make_shared<const FabricVideoPlayerViewProps>();
    _props = defaultProps;

    _view = [[UIView alloc] init];

    self.contentView = _view;
  }

  return self;
}

- (void)updateProps:(Props::Shared const &)props oldProps:(Props::Shared const &)oldProps
{
    const auto &oldViewProps = *std::static_pointer_cast<FabricVideoPlayerViewProps const>(_props);
    const auto &newViewProps = *std::static_pointer_cast<FabricVideoPlayerViewProps const>(props);

    if (oldViewProps.color != newViewProps.color) {
        NSString * colorToConvert = [[NSString alloc] initWithUTF8String: newViewProps.color.c_str()];
        [_view setBackgroundColor: [Utils hexStringToColor:colorToConvert]];
    }

    [super updateProps:props oldProps:oldProps];
}

Class<RCTComponentViewProtocol> FabricVideoPlayerViewCls(void)
{
    return FabricVideoPlayerView.class;
}

@end
#endif
