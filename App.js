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

  const playerProps = {
    videoUrl: 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
    startPosition: 0,
    isPlaying: true,
    resizeMode: 1,
    videoTitle : 'Big Bucks',
    videoDescription : 'Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industrys standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum'

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

    return () => {
      backHandler.remove();
    };
  }, []);

  const renderPhoneView = () => (
    <View style={styles.container}>
        <VideoPlayerView
        ref={videoPlayerRef}
        style={styles.videoPlayer}
        playerProps={playerProps}
      />
      <Text style={styles.title}>{playerProps.videoTitle}</Text>
      <Text style={styles.description}>{playerProps.videoDescription}</Text>
   
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
    height: 300,
    marginBottom: 16,
  },
  fullScreenVideoPlayer: {
    flex: 1,
    width: '100%',
    height: '100%',
  },
});

export default App;
