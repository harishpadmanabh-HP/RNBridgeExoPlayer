import React, { useEffect, useState, useRef } from 'react';
import {
  View,
  Text,
  StyleSheet,
  NativeModules,
  NativeEventEmitter,
  Button,
  requireNativeComponent,
  BackHandler,
  Platform,
  UIManager
} from 'react-native';
import Orientation from 'react-native-orientation-locker'; // Import Orientation locker module

const VideoPlayerView = requireNativeComponent('VideoPlayerManager');
const { NativeVideoPlayerBridgeModule } = NativeModules;

const testDashUrlWithSubTitle = 'https://po.cdn.onair.events/test-demo/test-5min-uhd-sdr-sur-lossless-subtitle-vtt-eng-dut-drm-/hd/no-drm/index.mpd'
const testUrl = 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4'
const multiLangTestUrl = 'https://cdn.bitmovin.com/content/assets/sintel/sintel.mpd'
const multiSubtitle = 'https://dash.akamaized.net/dash264/TestCasesIOP41/CMAF/UnifiedStreaming/ToS_AVC_MultiRate_MultiRes_AAC_Eng_WebVTT.mpd'
const multiLangs = 'https://dash.akamaized.net/dash264/TestCasesIOP41/MultiTrack/alternative_content/6/manifest_alternative_lang.mpd'
const drmStreamUrl = 'https://api.vod.onair.events/multi-cdn?url=https://po.cdn.onair.events/vod/ad2437e2-6f54-11ef-aa2b-e9534bd37b13/u4q1upiu/dedjjo3s/index.mpd'
const drmLicense = 'https://multi-drm.dev-onair.events/generate-auth-xml/license-acquisition?sku=drm-preview&accessToken=ZaFyuMgyuH&preview=true&drmType=widevine'

const App = () => {
  const [isTV, setIsTV] = useState(false);
  const [isFullScreen, setIsFullScreen] = useState(false);
  

  const playerProps = {
    videoUrl: drmStreamUrl,
    startPosition: 0,
    isPlaying: true,
    resizeMode: 1,
    videoTitle : 'Venus Tour',
    videoDescription : 'Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industrys standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum',
    hasDrm : true,
    drmLicenseUrl : drmLicense
  }

  const videoPlayerRef = useRef(null);

  useEffect(() => {
   // Detect if the app is running on a TV device
   const detectTV = () => {
   const isTVDevice = Platform.isTV ||
    UIManager.getViewManagerConfig('TVText') !== undefined;
    setIsTV(isTVDevice);
    NativeVideoPlayerBridgeModule.testLog("Is on TV " + isTV.toString());
  };

   detectTV();

    const backHandler = BackHandler.addEventListener('hardwareBackPress', () => {
      NativeVideoPlayerBridgeModule.testLog('BackPress');
      return false;
    });


    const eventEmitter = new NativeEventEmitter(NativeModules.VideoPlayerManager);
    const fullScreenChangedListener = eventEmitter.addListener('onFullScreenChanged', (result) => {
      NativeVideoPlayerBridgeModule.testLog(result.isFullScreen.toString());
      NativeVideoPlayerBridgeModule.testLog('caught in event emitter');
      setIsFullScreen(result.isFullScreen);
      if (result.isFullScreen) {
        Orientation.lockToLandscape(); // Lock to landscape on full screen
      } else {
        Orientation.unlockAllOrientations(); // Reset orientation on exit full screen
      }
    });
    const playerErrorListener = eventEmitter.addListener('playerError',(result) => {
      NativeVideoPlayerBridgeModule.testLog(result);
      NativeVideoPlayerBridgeModule.showToast(result.toString());
    });

    return () => {
      backHandler.remove();
      fullScreenChangedListener.remove();
      playerErrorListener.remove();
    };
  }, [isFullScreen]);

  const renderPhoneView = () => (
    <View style={styles.container}>
      <VideoPlayerView
        ref={videoPlayerRef}
        style={isFullScreen ? styles.fullScreenVideoPlayer : styles.videoPlayer}
        playerProps={playerProps}
      />
      {!isFullScreen && (
        <>
          <Text style={styles.title}>{playerProps.videoTitle}</Text>
          <Text style={styles.description}>{playerProps.videoDescription}</Text>
        </>
      )}
    </View>
  );

  const renderTVView = () => (
    <VideoPlayerView
      ref={videoPlayerRef}
      style={styles.fullScreenVideoPlayer}
      playerProps={playerProps}
    />
  );

  return (
    Platform.isTV ? renderTVView() : renderPhoneView()
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 0,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 8,
  },
  description: {
    fontSize: 16,
    marginBottom: 16,

  },
  videoPlayer: {
    width: '100%',
    height: 222,
    marginBottom: 16,
  },
  fullScreenVideoPlayer: {
    flex: 1,
    width: '100%',
    height: '100%',
  },
});

export default App;
