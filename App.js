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

const VideoPlayerView = requireNativeComponent('VideoPlayerManager');
const { NativeVideoPlayerBridgeModule } = NativeModules;

const App = () => {
  const [isTV, setIsTV] = useState(false);


  const [playerProps, setPlayerProps] = useState({
    videoUrl: 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
    startPosition: 0,
    isPlaying: true,
    resizeMode: 1
  });

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

    const eventEmitter = new NativeEventEmitter(NativeModules.VideoPlayerManager);
    const subscription = eventEmitter.addListener('onFullScreenResult', (result) => {
      NativeVideoPlayerBridgeModule.testLog(result.startPosition.toString());
      NativeVideoPlayerBridgeModule.testLog('caught in event emitter');
      setPlayerProps({
        videoUrl: playerProps.videoUrl,
        startPosition: result.startPosition || 0,
        isPlaying: result.isPlaying || false,
        resizeMode: playerProps.resizeMode
      });
    });

    const backHandler = BackHandler.addEventListener('hardwareBackPress', () => {
      NativeVideoPlayerBridgeModule.testLog('BackPress');
      return false;
    });

    return () => {
      subscription.remove();
      backHandler.remove();
    };
  }, [playerProps]);

  const renderPhoneView = () => (
    <View style={styles.container}>
      <Text style={styles.title}></Text>
      <Text style={styles.description}>{"startPosition : " + playerProps.startPosition.toString()}</Text>
      <VideoPlayerView
        ref={videoPlayerRef}
        style={styles.videoPlayer}
        playerProps={playerProps}
      />
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
    padding: 16,
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
    height: 200,
    marginBottom: 16,
  },
  fullScreenVideoPlayer: {
    flex: 1,
    width: '100%',
    height: '100%',
  },
});

export default App;
