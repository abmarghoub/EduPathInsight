import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  RefreshControl,
  TouchableOpacity,
} from 'react-native';
import { predictionsService } from '../services/predictionsService';
import { useAuth } from '../context/AuthContext';
import Icon from '../components/Icon';

const PredictionsScreen = () => {
  const { user } = useAuth();
  const [predictions, setPredictions] = useState([]);
  const [riskModules, setRiskModules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    loadPredictions();
  }, []);

  const loadPredictions = async () => {
    try {
      if (user?.id) {
        const response = await predictionsService.getMyPredictions(user.id);
        setPredictions(response.data || []);
      }
      setLoading(false);
    } catch (error) {
      console.error('Error loading predictions:', error);
      setLoading(false);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadPredictions();
    setRefreshing(false);
  };

  const getRiskColor = (riskScore) => {
    if (riskScore >= 0.7) return '#e74c3c';
    if (riskScore >= 0.5) return '#f39c12';
    return '#2ecc71';
  };

  if (loading) {
    return (
      <View style={styles.centerContainer}>
        <Text>Chargement...</Text>
      </View>
    );
  }

  return (
    <ScrollView
      style={styles.container}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
      }
    >
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Mes Prédictions</Text>
        {predictions.length === 0 ? (
          <View style={styles.emptyContainer}>
            <Icon name="analytics-outline" size={64} color="#bdc3c7" />
            <Text style={styles.emptyText}>
              Aucune prédiction disponible
            </Text>
          </View>
        ) : (
          predictions.map((prediction, index) => (
            <View key={index} style={styles.predictionCard}>
              <View style={styles.predictionHeader}>
                <Text style={styles.moduleName}>
                  {prediction.moduleCode} - {prediction.moduleName}
                </Text>
              </View>
              <View style={styles.metricsContainer}>
                <View style={styles.metric}>
                  <Text style={styles.metricLabel}>Probabilité de réussite</Text>
                  <View
                    style={[
                      styles.progressBar,
                      {
                        width: `${(prediction.successProbability * 100).toFixed(0)}%`,
                        backgroundColor: '#2ecc71',
                      },
                    ]}
                  />
                  <Text style={styles.metricValue}>
                    {(prediction.successProbability * 100).toFixed(1)}%
                  </Text>
                </View>
                <View style={styles.metric}>
                  <Text style={styles.metricLabel}>Probabilité d'abandon</Text>
                  <View
                    style={[
                      styles.progressBar,
                      {
                        width: `${(prediction.dropoutProbability * 100).toFixed(0)}%`,
                        backgroundColor: '#e74c3c',
                      },
                    ]}
                  />
                  <Text style={styles.metricValue}>
                    {(prediction.dropoutProbability * 100).toFixed(1)}%
                  </Text>
                </View>
              </View>
            </View>
          ))
        )}
      </View>

      {riskModules.length > 0 && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Modules à risque</Text>
          {riskModules.map((module, index) => (
            <View key={index} style={styles.riskCard}>
              <View style={styles.riskHeader}>
                <Icon name="warning" size={24} color={getRiskColor(module.riskScore)} />
                <Text style={styles.riskModuleName}>{module.moduleName}</Text>
              </View>
              <Text style={styles.riskText}>
                Score de risque: {(module.riskScore * 100).toFixed(1)}%
              </Text>
            </View>
          ))}
        </View>
      )}
    </ScrollView>
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
  section: {
    padding: 15,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#2c3e50',
    marginBottom: 15,
  },
  predictionCard: {
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
  predictionHeader: {
    marginBottom: 15,
  },
  moduleName: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#2c3e50',
  },
  metricsContainer: {
    marginTop: 10,
  },
  metric: {
    marginBottom: 15,
  },
  metricLabel: {
    fontSize: 14,
    color: '#7f8c8d',
    marginBottom: 5,
  },
  progressBar: {
    height: 20,
    borderRadius: 10,
    marginBottom: 5,
  },
  metricValue: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#2c3e50',
  },
  riskCard: {
    backgroundColor: '#fff3cd',
    borderRadius: 10,
    padding: 15,
    marginBottom: 15,
    borderLeftWidth: 4,
    borderLeftColor: '#f39c12',
  },
  riskHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
  },
  riskModuleName: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#856404',
    marginLeft: 10,
  },
  riskText: {
    fontSize: 14,
    color: '#856404',
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

export default PredictionsScreen;


