import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Alert,
} from 'react-native';
import { modulesService } from '../services/modulesService';
import { useAuth } from '../context/AuthContext';
import Icon from '../components/Icon';

const ModuleDetailsScreen = ({ route, navigation }) => {
  const { module: moduleParam } = route.params;
  const { user } = useAuth();
  const [module, setModule] = useState(moduleParam);
  const [enrollment, setEnrollment] = useState(null);

  useEffect(() => {
    loadModuleDetails();
  }, []);

  const loadModuleDetails = async () => {
    try {
      const [moduleRes, enrollmentsRes] = await Promise.all([
        modulesService.getModuleById(module.id),
        modulesService.getMyEnrollments(),
      ]);
      setModule(moduleRes.data);
      const myEnrollment = enrollmentsRes.data.find(
        (e) => e.moduleId === module.id
      );
      setEnrollment(myEnrollment);
    } catch (error) {
      console.error('Error loading module details:', error);
    }
  };

  const handleEnroll = async () => {
    Alert.alert(
      'Inscription',
      `Voulez-vous vous inscrire au module "${module.name}" ?`,
      [
        { text: 'Annuler', style: 'cancel' },
        {
          text: 'S\'inscrire',
          onPress: async () => {
            try {
              await modulesService.enrollInModule(
                module.id,
                user.id,
                user.username,
                user.email
              );
              Alert.alert('Succès', 'Votre demande d\'inscription a été envoyée');
              loadModuleDetails();
            } catch (error) {
              Alert.alert(
                'Erreur',
                error.response?.data?.message || 'Erreur lors de l\'inscription'
              );
            }
          },
        },
      ]
    );
  };

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.code}>{module.code}</Text>
        <Text style={styles.name}>{module.name}</Text>
      </View>

      {module.description && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Description</Text>
          <Text style={styles.description}>{module.description}</Text>
        </View>
      )}

      <View style={styles.section}>
        <View style={styles.infoRow}>
          <Icon name="star" size={20} color="#f39c12" />
          <Text style={styles.infoText}>{module.credits} crédits</Text>
        </View>
        <View style={styles.infoRow}>
          <Icon
            name={
              module.active
                ? 'checkmark-circle'
                : 'close-circle'
            }
            size={20}
            color={module.active ? '#2ecc71' : '#e74c3c'}
          />
          <Text style={styles.infoText}>
            {module.active ? 'Module actif' : 'Module inactif'}
          </Text>
        </View>
      </View>

      {enrollment && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Statut d'inscription</Text>
          <View style={styles.enrollmentStatus}>
            <Icon
              name={
                enrollment.status === 'APPROVED'
                  ? 'checkmark-circle'
                  : enrollment.status === 'PENDING'
                  ? 'time'
                  : 'close-circle'
              }
              size={24}
              color={
                enrollment.status === 'APPROVED'
                  ? '#2ecc71'
                  : enrollment.status === 'PENDING'
                  ? '#f39c12'
                  : '#e74c3c'
              }
            />
            <Text style={styles.enrollmentStatusText}>
              {enrollment.status === 'APPROVED'
                ? 'Inscription approuvée'
                : enrollment.status === 'PENDING'
                ? 'En attente d\'approbation'
                : 'Inscription rejetée'}
            </Text>
          </View>
        </View>
      )}

      {!enrollment && (
        <TouchableOpacity style={styles.enrollButton} onPress={handleEnroll}>
          <Text style={styles.enrollButtonText}>S'inscrire au module</Text>
        </TouchableOpacity>
      )}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  header: {
    backgroundColor: '#2c3e50',
    padding: 20,
  },
  code: {
    fontSize: 16,
    color: '#ecf0f1',
    fontWeight: '600',
  },
  name: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#fff',
    marginTop: 5,
  },
  section: {
    backgroundColor: '#fff',
    padding: 15,
    marginTop: 15,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#2c3e50',
    marginBottom: 10,
  },
  description: {
    fontSize: 16,
    color: '#34495e',
    lineHeight: 24,
  },
  infoRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
  },
  infoText: {
    fontSize: 16,
    color: '#34495e',
    marginLeft: 10,
  },
  enrollmentStatus: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  enrollmentStatusText: {
    fontSize: 16,
    color: '#34495e',
    marginLeft: 10,
    fontWeight: '600',
  },
  enrollButton: {
    backgroundColor: '#3498db',
    margin: 15,
    padding: 15,
    borderRadius: 10,
    alignItems: 'center',
  },
  enrollButtonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: 'bold',
  },
});

export default ModuleDetailsScreen;


