import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  RefreshControl,
} from 'react-native';
import { useAuth } from '../context/AuthContext';
import { modulesService } from '../services/modulesService';
import { notesService } from '../services/notesService';
import { predictionsService } from '../services/predictionsService';
import { notificationsService } from '../services/notificationsService';
import Icon from '../components/Icon';

const HomeScreen = ({ navigation }) => {
  const { user } = useAuth();
  const [refreshing, setRefreshing] = useState(false);
  const [stats, setStats] = useState({
    enrolledModules: 0,
    totalNotes: 0,
    unreadNotifications: 0,
    riskModules: 0,
  });

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      const [enrollmentsRes, notesRes, notificationsRes] = await Promise.all([
        modulesService.getMyEnrollments(),
        notesService.getMyNotes(),
        notificationsService.getUnreadNotifications(user?.id || ''),
      ]);

      setStats({
        enrolledModules: enrollmentsRes.data?.length || 0,
        totalNotes: notesRes.data?.length || 0,
        unreadNotifications: notificationsRes.data?.length || 0,
        riskModules: 0, // TODO: Get from predictions
      });
    } catch (error) {
      console.error('Error loading stats:', error);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadStats();
    setRefreshing(false);
  };

  return (
    <ScrollView
      style={styles.container}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
      }
    >
      <View style={styles.header}>
        <Text style={styles.greeting}>Bonjour,</Text>
        <Text style={styles.username}>{user?.username || 'Étudiant'}</Text>
      </View>

      <View style={styles.statsContainer}>
        <TouchableOpacity
          style={styles.statCard}
          onPress={() => navigation.navigate('Modules')}
        >
          <Icon name="book" size={30} color="#3498db" />
          <Text style={styles.statValue}>{stats.enrolledModules}</Text>
          <Text style={styles.statLabel}>Modules</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.statCard}
          onPress={() => navigation.navigate('Notes')}
        >
          <Icon name="clipboard" size={30} color="#2ecc71" />
          <Text style={styles.statValue}>{stats.totalNotes}</Text>
          <Text style={styles.statLabel}>Notes</Text>
        </TouchableOpacity>
      </View>

      <View style={styles.statsContainer}>
        <TouchableOpacity
          style={[styles.statCard, stats.unreadNotifications > 0 && styles.statCardAlert]}
          onPress={() => navigation.navigate('Notifications')}
        >
          <Icon name="notifications" size={30} color="#e74c3c" />
          <Text style={styles.statValue}>{stats.unreadNotifications}</Text>
          <Text style={styles.statLabel}>Notifications</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.statCard, stats.riskModules > 0 && styles.statCardWarning]}
          onPress={() => navigation.navigate('Prédictions')}
        >
          <Icon name="warning" size={30} color="#f39c12" />
          <Text style={styles.statValue}>{stats.riskModules}</Text>
          <Text style={styles.statLabel}>Modules à risque</Text>
        </TouchableOpacity>
      </View>

      <View style={styles.quickActions}>
        <Text style={styles.sectionTitle}>Actions rapides</Text>
        
        <TouchableOpacity
          style={styles.actionButton}
          onPress={() => navigation.navigate('Modules')}
        >
          <Icon name="add-circle" size={24} color="#3498db" />
          <Text style={styles.actionButtonText}>S'inscrire à un module</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.actionButton}
          onPress={() => navigation.navigate('Notes')}
        >
          <Icon name="document-text" size={24} color="#2ecc71" />
          <Text style={styles.actionButtonText}>Consulter mes notes</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.actionButton}
          onPress={() => navigation.navigate('Prédictions')}
        >
          <Icon name="analytics" size={24} color="#9b59b6" />
          <Text style={styles.actionButtonText}>Voir mes prédictions</Text>
        </TouchableOpacity>
      </View>
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
    paddingTop: 40,
  },
  greeting: {
    fontSize: 16,
    color: '#ecf0f1',
  },
  username: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#fff',
    marginTop: 5,
  },
  statsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    padding: 15,
  },
  statCard: {
    backgroundColor: '#fff',
    borderRadius: 10,
    padding: 20,
    alignItems: 'center',
    width: '45%',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  statCardAlert: {
    borderLeftWidth: 4,
    borderLeftColor: '#e74c3c',
  },
  statCardWarning: {
    borderLeftWidth: 4,
    borderLeftColor: '#f39c12',
  },
  statValue: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#2c3e50',
    marginTop: 10,
  },
  statLabel: {
    fontSize: 14,
    color: '#7f8c8d',
    marginTop: 5,
  },
  quickActions: {
    padding: 15,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#2c3e50',
    marginBottom: 15,
  },
  actionButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#fff',
    borderRadius: 10,
    padding: 15,
    marginBottom: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  actionButtonText: {
    fontSize: 16,
    color: '#2c3e50',
    marginLeft: 15,
  },
});

export default HomeScreen;


