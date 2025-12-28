import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  RefreshControl,
  Alert,
} from 'react-native';
import { modulesService } from '../services/modulesService';
import { useAuth } from '../context/AuthContext';
import Icon from '../components/Icon';

const ModulesScreen = ({ navigation }) => {
  const { user } = useAuth();
  const [modules, setModules] = useState([]);
  const [myEnrollments, setMyEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [modulesRes, enrollmentsRes] = await Promise.all([
        modulesService.getAllModules(),
        modulesService.getMyEnrollments(),
      ]);
      setModules(modulesRes.data || []);
      setMyEnrollments(enrollmentsRes.data || []);
      setLoading(false);
    } catch (error) {
      console.error('Error loading modules:', error);
      setLoading(false);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadData();
    setRefreshing(false);
  };

  const isEnrolled = (moduleId) => {
    return myEnrollments.some(
      (e) => e.moduleId === moduleId && e.status === 'APPROVED'
    );
  };

  const handleEnroll = async (module) => {
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
              loadData();
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

  const renderModule = ({ item }) => (
    <TouchableOpacity
      style={styles.moduleCard}
      onPress={() => navigation.navigate('ModuleDetails', { module: item })}
    >
      <View style={styles.moduleHeader}>
        <View style={styles.moduleInfo}>
          <Text style={styles.moduleCode}>{item.code}</Text>
          <Text style={styles.moduleName}>{item.name}</Text>
        </View>
        {isEnrolled(item.id) && (
          <View style={styles.enrolledBadge}>
            <Icon name="checkmark-circle" size={20} color="#2ecc71" />
            <Text style={styles.enrolledText}>Inscrit</Text>
          </View>
        )}
      </View>
      {item.description && (
        <Text style={styles.moduleDescription} numberOfLines={2}>
          {item.description}
        </Text>
      )}
      <View style={styles.moduleFooter}>
        <View style={styles.creditsContainer}>
          <Icon name="star" size={16} color="#f39c12" />
          <Text style={styles.creditsText}>{item.credits} crédits</Text>
        </View>
        {!isEnrolled(item.id) && (
          <TouchableOpacity
            style={styles.enrollButton}
            onPress={() => handleEnroll(item)}
          >
            <Text style={styles.enrollButtonText}>S'inscrire</Text>
          </TouchableOpacity>
        )}
      </View>
    </TouchableOpacity>
  );

  if (loading) {
    return (
      <View style={styles.centerContainer}>
        <Text>Chargement...</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={modules}
        renderItem={renderModule}
        keyExtractor={(item) => item.id.toString()}
        contentContainerStyle={styles.listContent}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Icon name="book-outline" size={64} color="#bdc3c7" />
            <Text style={styles.emptyText}>Aucun module disponible</Text>
          </View>
        }
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  listContent: {
    padding: 15,
  },
  moduleCard: {
    backgroundColor: '#fff',
    borderRadius: 10,
    padding: 15,
    marginBottom: 15,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  moduleHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 10,
  },
  moduleInfo: {
    flex: 1,
  },
  moduleCode: {
    fontSize: 14,
    color: '#7f8c8d',
    fontWeight: '600',
  },
  moduleName: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#2c3e50',
    marginTop: 5,
  },
  enrolledBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#d5f4e6',
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 15,
  },
  enrolledText: {
    color: '#27ae60',
    fontSize: 12,
    fontWeight: '600',
    marginLeft: 5,
  },
  moduleDescription: {
    fontSize: 14,
    color: '#7f8c8d',
    marginBottom: 15,
  },
  moduleFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  creditsContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  creditsText: {
    fontSize: 14,
    color: '#7f8c8d',
    marginLeft: 5,
  },
  enrollButton: {
    backgroundColor: '#3498db',
    paddingHorizontal: 20,
    paddingVertical: 8,
    borderRadius: 20,
  },
  enrollButtonText: {
    color: '#fff',
    fontWeight: '600',
    fontSize: 14,
  },
  emptyContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 60,
  },
  emptyText: {
    fontSize: 16,
    color: '#bdc3c7',
    marginTop: 15,
  },
});

export default ModulesScreen;


