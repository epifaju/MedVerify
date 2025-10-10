module.exports = {
  presets: ['module:metro-react-native-babel-preset'],
  plugins: [
    [
      'module-resolver',
      {
        root: ['./src'],
        extensions: ['.ios.js', '.android.js', '.js', '.ts', '.tsx', '.json'],
        alias: {
          '@components': './src/components',
          '@screens': './src/screens',
          '@services': './src/services',
          '@store': './src/store',
          '@utils': './src/utils',
          '@types': './src/types',
          '@navigation': './src/navigation',
          '@database': './src/database',
          '@locales': './src/locales',
          '@assets': './assets',
        },
      },
    ],
    'react-native-reanimated/plugin',
  ],
};


