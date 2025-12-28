import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  RefreshControl,
} from 'react-native';
import { notificationsService } from '../services/notificationsService';
import { useAuth } from '../context/AuthContext';
import Icon from '../components/Icon';

const NotificationsScreen = () => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [activeTab, setActiveTab] = useState('notifications');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      if (user?.id) {
        const [notificationsRes, alertsRes] = await Promise.all([
          notificationsService.getMyNotifications(user.id),
          notificationsService.getMyAlerts(user.id),
        ]);
        setNotifications(notificationsRes.data || []);
        setAlerts(alertsRes.data || []);
      }
      setLoading(false);
    } catch (error) {
      console.error('Error loading notifications:', error);
      setLoading(false);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadData();
    setRefreshing(false);
  };

  const handleAcknowledgeAlert = async (alertId) => {
    try {
      await notificationsService.acknowledgeAlert(alertId);
      loadData();
    } catch (error) {
      console.error('Error acknowledging alert:', error);
    }
  };

  const renderNotification = ({ item }) => (
    <View style={styles.notificationCard}>
      <View style={styles.notificationHeader}>
        <Icon
          name="notifications"
          size={20}
          color={
            item.type === 'HIGH_RISK_STUDENT' || item.type === 'ALERT_CREATED'
              ? '#e74c3c'
              : '#3498db'
          }
        />
        <View style={styles.notificationInfo}>
          <Text style={styles.notificationTitle}>{item.title}</Text>
          <Text style={styles.notificationTime}>
            {item.sentAt
              ? new Date(item.sentAt).toLocaleString()
              : new Date(item.createdAt).toLocaleString()}
          </Text>
        </View>
      </View>
      {item.message && (
        <Text style={styles.notificationMessage}>{item.message}</Text>
      )}
    </View>
  );

  const renderAlert = ({ item }) => (
    <View
      style={[
        styles.alertCard,
        item.severity === 'CRITICAL' && styles.alertCardCritical,
        item.severity === 'HIGH' && styles.alertCardHigh,
      ]}
    >
      <View style={styles.alertHeader}>
        <Icon
          name="warning"
          size={24}
          color={
            item.severity === 'CRITICAL'
              ? '#e74c3c'
              : item.severity === 'HIGH'
              ? '#f39c12'
              : '#3498db'
          }
        />
        <View style={styles.alertInfo}>
          <Text style={styles.alertTitle}>{item.title}</Text>
          <Text style={styles.alertType}>{item.alertType}</Text>
        </View>
        {!item.acknowledgedAt && (
          <TouchableOpacity
            style={styles.acknowledgeButton}
            onPress={() => handleAcknowledgeAlert(item.id)}
          >
            <Text style={styles.acknowledgeButtonText}>Marquer lu</Text>
          </TouchableOpacity>
        )}
      </View>
      {item.message && (
        <Text style={styles.alertMessage}>{item.message}</Text>
      )}
      <Text style={styles.alertTime}>
        {new Date(item.triggeredAt).toLocaleString()}
      </Text>
    </View>
  );

  if (loading) {
    return (
      <View style={styles.centerContainer}>
        <Text>Chargement...</Text>
      </View>
    );
  }

  const data = activeTab === 'notifications' ? notifications : alerts;
  const renderItem =
    activeTab === 'notifications' ? renderNotification : renderAlert;

  return (
    <View style={styles.container}>
      <View style={styles.tabs}>
        <TouchableOpacity
          style={[styles.tab, activeTab === 'notifications' && styles.tabActive]}
          onPress={() => setActiveTab('notifications')}
        >
          <Text
            style={[
              styles.tabText,
              activeTab === 'notifications' && styles.tabTextActive,
            ]}
          >
            Notifications
          </Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.tab, activeTab === 'alerts' && styles.tabActive]}
          onPress={() => setActiveTab('alerts')}
        >
          <Text
            style={[
              styles.tabText,
              activeTab === 'alerts' && styles.tabTextActive,
            ]}
          >
            Alertes ({alerts.filter((a) => !a.acknowledgedAt).length})
          </Text>
        </TouchableOpacity>
      </View>

      <FlatList
        data={data}
        renderItem={renderItem}
        keyExtractor={(item) => item.id.toString()}
        contentContainerStyle={styles.listContent}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Icon name="notifications-outline" size={64} color="#bdc3c7" />
            <Text style={styles.emptyText}>
              {activeTab === 'notifications'
                ? 'Aucune notification'
                : 'Aucune alerte'}
            </Text>
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
  tabs: {
    flexDirection: 'row',
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#ecf0f1',
  },
  tab: {
    flex: 1,
    paddingVertical: 15,
    alignItems: 'center',
    borderBottomWidth: 2,
    borderBottomColor: 'transparent',
  },
  tabActive: {
    borderBottomColor: '#3498db',
  },
  tabText: {
    fontSize: 16,
    color: '#7f8c8d',
    fontWeight: '600',
  },
  tabTextActive: {
    color: '#3498db',
  },
  listContent: {
    padding: 15,
  },
  notificationCard: {
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
  notificationHeader: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 10,
  },
  notificationInfo: {
    flex: 1,
    marginLeft: 10,
  },
  notificationTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#2c3e50',
  },
  notificationTime: {
    fontSize: 12,
    color: '#7f8c8d',
    marginTop: 5,
  },
  notificationMessage: {
    fontSize: 14,
    color: '#34495e',
    marginTop: 10,
  },
  alertCard: {
    backgroundColor: '#fff',
    borderRadius: 10,
    padding: 15,
    marginBottom: 15,
    borderLeftWidth: 4,
    borderLeftColor: '#3498db',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  alertCardHigh: {
    borderLeftColor: '#f39c12',
    backgroundColor: '#fef9e7',
  },
  alertCardCritical: {
    borderLeftColor: '#e74c3c',
    backgroundColor: '#fdedec',
  },
  alertHeader: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 10,
  },
  alertInfo: {
    flex: 1,
    marginLeft: 10,
  },
  alertTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#2c3e50',
  },
  alertType: {
    fontSize: 12,
    color: '#7f8c8d',
    marginTop: 5,
    textTransform: 'uppercase',
  },
  acknowledgeButton: {
    backgroundColor: '#3498db',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 15,
  },
  acknowledgeButtonText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '600',
  },
  alertMessage: {
    fontSize: 14,
    color: '#34495e',
    marginTop: 10,
  },
  alertTime: {
    fontSize: 12,
    color: '#7f8c8d',
    marginTop: 10,
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

export default NotificationsScreen;


