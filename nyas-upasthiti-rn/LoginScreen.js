import React, { useState, useRef } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, Animated, Vibration } from 'react-native';

const BASE_URL = 'http://192.168.1.9:5000/api';

export default function LoginScreen({ navigation }) {
  const [empId, setEmpId] = useState('');
  const [password, setPassword] = useState('');
  const [isHolding, setIsHolding] = useState(false);
  
  // The animation value for the progress bar
  const progressAnim = useRef(new Animated.Value(0)).current;
  const holdTimer = useRef(null);

  const executeLogin = async () => {
    try {
      const response = await fetch(`${BASE_URL}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ empId: empId.trim(), password })
      });
      const data = await response.json();

      if (response.ok) {
        Vibration.vibrate(100); // Final success buzz!
        navigation.replace('Dashboard', { role: data.role, employeeName: data.name, employeeId: data.empId });
      } else {
        resetButton();
        Alert.alert('Login Failed', data.error || 'Invalid credentials.');
      }
    } catch (error) {
      resetButton();
      Alert.alert('Network Error', 'Cannot reach the authentication server.');
    }
  };

  // --- THE WOW FEATURE LOGIC ---
  const handlePressIn = () => {
    if (!empId || !password) return Alert.alert('Error', 'Enter credentials first.');
    setIsHolding(true);
    Vibration.vibrate([0, 50, 100, 50, 100]); // Startup buzz
    
    // Fill the bar over 1.5 seconds
    Animated.timing(progressAnim, {
      toValue: 100,
      duration: 1500,
      useNativeDriver: false, // Must be false for width animation
    }).start();

    // If they hold it for exactly 1.5 seconds, trigger login
    holdTimer.current = setTimeout(() => {
      executeLogin();
    }, 1500);
  };

  const handlePressOut = () => {
    // If they let go before 1.5 seconds, cancel the login and reset!
    if (holdTimer.current) {
      clearTimeout(holdTimer.current);
      resetButton();
    }
  };

  const resetButton = () => {
    setIsHolding(false);
    Animated.timing(progressAnim, {
      toValue: 0,
      duration: 200,
      useNativeDriver: false,
    }).start();
  };

  // Calculate the width of the blue progress bar
  const widthInterpolated = progressAnim.interpolate({
    inputRange: [0, 100],
    outputRange: ['0%', '100%']
  });

  return (
    <View style={styles.container}>
      <View style={styles.headerBox}>
        <Text style={styles.title}>Secure Gateway</Text>
        <Text style={styles.subtitle}>Enter your NHAI credentials to continue</Text>
      </View>

      <View style={styles.card}>
        <Text style={styles.label}>Employee ID</Text>
        <TextInput 
          style={styles.input} 
          placeholder="e.g., EMP102" 
          value={empId}
          onChangeText={setEmpId}
          autoCapitalize="characters"
        />

        <Text style={styles.label}>Password</Text>
        <TextInput 
          style={styles.input} 
          placeholder="Enter password" 
          value={password}
          onChangeText={setPassword}
          secureTextEntry
        />

        {/* THE CRAZY HOLD BUTTON */}
        <TouchableOpacity 
          style={styles.holdButtonContainer} 
          activeOpacity={0.9}
          onPressIn={handlePressIn}
          onPressOut={handlePressOut}
        >
          {/* Background Progress Bar that fills up */}
          <Animated.View style={[styles.progressFill, { width: widthInterpolated }]} />
          
          <Text style={styles.buttonText}>
            {isHolding ? 'ESTABLISHING UPLINK...' : 'HOLD TO AUTHENTICATE'}
          </Text>
        </TouchableOpacity>

      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#E8F0FA', justifyContent: 'center', padding: 24 },
  headerBox: { marginBottom: 30, alignItems: 'center' },
  title: { fontSize: 28, fontWeight: '900', color: '#0D3E73' },
  subtitle: { fontSize: 14, color: '#64748B', marginTop: 5 },
  card: { backgroundColor: '#FFF', borderRadius: 20, padding: 24, elevation: 5, shadowColor: '#000', shadowOpacity: 0.1, shadowRadius: 10 },
  label: { fontSize: 13, fontWeight: 'bold', color: '#64748B', marginBottom: 6 },
  input: { backgroundColor: '#F8FAFC', borderWidth: 1, borderColor: '#CBD5E1', borderRadius: 10, padding: 15, marginBottom: 20, fontSize: 15, color: '#0F172A' },
  
  // Hold Button Styles
  holdButtonContainer: { 
    backgroundColor: '#94A3B8', // Base gray color 
    height: 56, 
    borderRadius: 12, 
    marginTop: 10,
    overflow: 'hidden', // Keeps the progress bar inside the rounded corners
    justifyContent: 'center',
    alignItems: 'center'
  },
  progressFill: {
    position: 'absolute',
    left: 0,
    top: 0,
    bottom: 0,
    backgroundColor: '#0D3E73', // Fills up with your professional blue
  },
  buttonText: { 
    color: '#FFF', 
    fontWeight: 'bold', 
    fontSize: 14, 
    letterSpacing: 1,
    zIndex: 10 // Keeps text on top of the progress bar
  }
});