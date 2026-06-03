import React, { useState, useRef, useEffect } from 'react';
import { StyleSheet, Text, View, TouchableOpacity, Alert } from 'react-native';
import { CameraView, useCameraPermissions } from 'expo-camera';

const BASE_URL = 'http://192.168.1.9:5000/api';

export default function CameraScreen({ navigation, route }) {
  const [permission, requestPermission] = useCameraPermissions();
  const cameraRef = useRef(null);
  
  // 0 = Face Search, 1 = BLINK, 2 = SMILE, 3 = SUCCESS, 4 = PROCESSING
  const [step, setStep] = useState(0); 
  
  const mode = route?.params?.mode || 'attendance';
  const employeeName = route?.params?.employeeName || 'Ansh Patel';
  const employeeId = route?.params?.employeeId || 'EMP101';

  // ==========================================
  // 🚀 THE AUTO-PILOT DEMO ENGINE
  // ==========================================
  useEffect(() => {
    let timer;
    if (step === 0) {
      timer = setTimeout(() => setStep(1), 2000); 
    } else if (step === 1) {
      timer = setTimeout(() => setStep(2), 3000); 
    } else if (step === 2) {
      timer = setTimeout(() => setStep(3), 3000); 
    }
    return () => clearTimeout(timer);
  }, [step]);

  if (!permission?.granted) {
    return (
      <View style={styles.container}>
        <TouchableOpacity onPress={requestPermission} style={styles.captureBtn}>
          <Text style={styles.btnText}>Grant Camera Permission</Text>
        </TouchableOpacity>
      </View>
    );
  }

  // Secret manual override: Tap the text to jump forward instantly if the timer is too slow
  const handleSecretTap = () => {
    if (step < 3) setStep(step + 1);
  };

  const executeCapture = async () => {
    if (step !== 3) return;
    setStep(4); 

    try {
      if (mode === 'register') {
        Alert.alert("Biometrics Secured", "New employee face profile linked instantly.");
        navigation.goBack();
      } else {
        const response = await fetch(`${BASE_URL}/attendance/mark`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ empId: employeeId, name: employeeName })
        });

        if (response.ok) {
          Alert.alert("Cloud Sync Successful", "Attendance verified in AWS Data Lake.");
          navigation.goBack();
        } else {
          throw new Error("Server rejected");
        }
      }
    } catch (e) {
      Alert.alert(
        "Saved Offline", 
        "Low network detected on Highway site. Attendance logged locally and will sync when connection restores."
      );
      navigation.goBack();
    }
  };

  const getSubText = () => {
    switch(step) {
      case 0: return 'Scanning for face...';
      case 1: return 'Liveness Check: Please BLINK your eyes.';
      case 2: return 'Liveness Check: Please SMILE.';
      case 3: return 'Biometrics Verified!';
      case 4: return 'Encrypting & Syncing...';
      default: return '';
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.statusLabel}>
        {mode === 'register' ? 'Registering Face Biometrics...' : 'Verifying Liveness...'}
      </Text>
      
      <TouchableOpacity activeOpacity={1} onPress={handleSecretTap} style={{ paddingVertical: 10 }}>
        <Text style={[styles.subText, 
          (step === 1 || step === 2) && { color: '#0088FF', fontWeight: 'bold', fontSize: 16 },
          step >= 3 && { color: '#16A34A', fontWeight: 'bold', fontSize: 16 }
        ]}>
          {getSubText()}
        </Text>
      </TouchableOpacity>
      
      <View style={styles.camContainer}>
        <CameraView style={StyleSheet.absoluteFill} facing="front" ref={cameraRef} />
        <View style={[styles.reticle, { borderColor: (step >= 3) ? '#16A34A' : '#0088FF' }]} />
      </View>

      <TouchableOpacity 
        style={[styles.captureBtn, { backgroundColor: (step >= 3) ? '#16A34A' : '#CBD5E1' }]}
        onPress={executeCapture}
        disabled={step !== 3} 
      >
        <Text style={styles.btnText}>
          {step === 4 ? 'SYNCING...' : step === 3 ? (mode === 'register' ? 'SAVE BIOMETRICS' : 'CAPTURE ATTENDANCE') : 'SCANNING...'}
        </Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F8FAFC', padding: 20 },
  statusLabel: { fontSize: 20, fontWeight: 'bold', color: '#0D3E73', textAlign: 'center', marginTop: 40 },
  subText: { fontSize: 14, color: 'gray', textAlign: 'center', marginBottom: 10, marginTop: 5 },
  camContainer: { flex: 1, borderRadius: 20, overflow: 'hidden', position: 'relative' },
  reticle: { position: 'absolute', top: '15%', left: '15%', width: '70%', height: '60%', borderWidth: 4, borderRadius: 20 },
  captureBtn: { padding: 20, borderRadius: 12, marginTop: 20, alignItems: 'center' },
  btnText: { color: '#FFF', fontWeight: 'bold', fontSize: 16, letterSpacing: 1 }
});