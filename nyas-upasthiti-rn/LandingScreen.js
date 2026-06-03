import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Image } from 'react-native';

export default function LandingScreen({ navigation }) {
  return (
    <View style={styles.container}>
      <View style={styles.topSection}>
        <Text style={styles.mainHeading}>Digital Backbone for{"\n"}National Highways.</Text>
      </View>
      
      <View style={styles.card}>
        {/* Fixed to logo.png */}
        <Image 
          source={require('./assets/logo.png')} 
          style={styles.heroBanner} 
          resizeMode="cover"
        />
        <Text style={styles.portalText}>Workforce Portal</Text>
        <Text style={styles.digitalIndia}>Powered by Digital India</Text>
      </View>
      
      <TouchableOpacity style={styles.button} onPress={() => navigation.navigate('Login')}>
        <Text style={styles.buttonText}>Access Secure Portal ➔</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { 
    flex: 1, 
    backgroundColor: '#E8F0FA', 
    padding: 24, 
    justifyContent: 'space-between' 
  },
  topSection: { 
    marginTop: 60, 
    alignItems: 'center' 
  },
  mainHeading: { 
    fontSize: 26, 
    fontWeight: 'bold', 
    color: '#0D3E73', 
    textAlign: 'center',
    lineHeight: 34
  },
  card: { 
    backgroundColor: '#FFF', 
    borderRadius: 24, 
    padding: 20, 
    flex: 1, 
    marginVertical: 24, 
    elevation: 4, 
    justifyContent: 'center', 
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8
  },
  heroBanner: { 
    width: '100%', 
    height: 190, 
    borderRadius: 16, 
    marginBottom: 24,
    backgroundColor: '#F1F5F9' 
  },
  portalText: { 
    fontSize: 32, 
    fontWeight: '900', 
    color: '#0F172A', 
    textAlign: 'center',
    letterSpacing: -0.5
  },
  digitalIndia: { 
    fontSize: 14, 
    color: '#16A34A', 
    fontWeight: 'bold', 
    textAlign: 'center', 
    marginTop: 8,
    textTransform: 'uppercase',
    letterSpacing: 1
  },
  button: { 
    backgroundColor: '#0D3E73', 
    height: 56, 
    borderRadius: 16, 
    justifyContent: 'center', 
    alignItems: 'center', 
    marginBottom: 20,
    elevation: 3
  },
  buttonText: { 
    color: '#FFF', 
    fontSize: 16, 
    fontWeight: 'bold',
    letterSpacing: 0.5
  }
});