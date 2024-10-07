import * as React from 'react';
import { View, Text, Button } from 'react-native';

function HomeScreen ({navigation}){
  return(
    <View style={{flex: 1, justifyContent: 'center', alignItems: 'center'}}>
      <Text>Home Screen</Text>
      <Button
        title='Go to detail'
        onPress={() => navigation.navigate('Details')} />
    </View>
  );
}

export default HomeScreen