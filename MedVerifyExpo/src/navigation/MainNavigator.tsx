/**
 * Navigateur principal pour les écrans authentifiés
 */

import React from 'react';
import { createStackNavigator } from '@react-navigation/stack';
import HomeScreen from '@screens/Home/HomeScreen';

export type MainStackParamList = {
  Home: undefined;
};

const Stack = createStackNavigator<MainStackParamList>();

const MainNavigator: React.FC = () => {
  return (
    <Stack.Navigator
      screenOptions={{
        headerShown: false,
      }}>
      <Stack.Screen name="Home" component={HomeScreen} />
    </Stack.Navigator>
  );
};

export default MainNavigator;

