import React from 'react';
import { Provider } from 'react-redux';
import { NavigationContainer } from '@react-navigation/native';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import Toast from 'react-native-toast-message';

import { store } from './src/store/store';
import AppNavigator from './src/navigation/AppNavigator';
import { initializeDatabase } from './src/database/schema';
import './src/locales/i18n';

// Initialize SQLite database
initializeDatabase().catch(console.error);

const App = () => {
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <Provider store={store}>
        <SafeAreaProvider>
          <NavigationContainer>
            <AppNavigator />
          </NavigationContainer>
          <Toast />
        </SafeAreaProvider>
      </Provider>
    </GestureHandlerRootView>
  );
};

export default App;


