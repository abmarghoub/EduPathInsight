import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  RefreshControl,
  TouchableOpacity,
} from 'react-native';
import { notesService } from '../services/notesService';
import Icon from '../components/Icon';

const NotesScreen = () => {
  const [notes, setNotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [selectedModule, setSelectedModule] = useState(null);

  useEffect(() => {
    loadNotes();
  }, []);

  const loadNotes = async () => {
    try {
      const response = await notesService.getMyNotes();
      setNotes(response.data || []);
      setLoading(false);
    } catch (error) {
      console.error('Error loading notes:', error);
      setLoading(false);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadNotes();
    setRefreshing(false);
  };

  const getGradeColor = (percentage) => {
    if (percentage >= 80) return '#2ecc71';
    if (percentage >= 60) return '#f39c12';
    return '#e74c3c';
  };

  const renderNote = ({ item }) => (
    <View style={styles.noteCard}>
      <View style={styles.noteHeader}>
        <View style={styles.noteInfo}>
          <Text style={styles.moduleCode}>{item.moduleCode}</Text>
          <Text style={styles.evaluationTitle}>{item.evaluationTitle}</Text>
          <Text style={styles.evaluationType}>{item.evaluationType}</Text>
        </View>
        <View
          style={[
            styles.gradeContainer,
            { backgroundColor: getGradeColor(item.percentage) + '20' },
          ]}
        >
          <Text
            style={[styles.gradeText, { color: getGradeColor(item.percentage) }]}
          >
            {item.percentage?.toFixed(1)}%
          </Text>
        </View>
      </View>
      <View style={styles.noteDetails}>
        <Text style={styles.scoreText}>
          {item.score} / {item.maxScore}
        </Text>
        {item.evaluationDate && (
          <Text style={styles.dateText}>
            {new Date(item.evaluationDate).toLocaleDateString()}
          </Text>
        )}
      </View>
      {item.comments && (
        <Text style={styles.commentsText}>{item.comments}</Text>
      )}
    </View>
  );

  if (loading) {
    return (
      <View style={styles.centerContainer}>
        <Text>Chargement...</Text>
      </View>
    );
  }

  const uniqueModules = [
    ...new Set(notes.map((note) => note.moduleCode)),
  ].filter(Boolean);

  return (
    <View style={styles.container}>
      {uniqueModules.length > 0 && (
        <View style={styles.filterContainer}>
          <TouchableOpacity
            style={[
              styles.filterButton,
              !selectedModule && styles.filterButtonActive,
            ]}
            onPress={() => setSelectedModule(null)}
          >
            <Text
              style={[
                styles.filterButtonText,
                !selectedModule && styles.filterButtonTextActive,
              ]}
            >
              Tous
            </Text>
          </TouchableOpacity>
          {uniqueModules.map((moduleCode) => (
            <TouchableOpacity
              key={moduleCode}
              style={[
                styles.filterButton,
                selectedModule === moduleCode && styles.filterButtonActive,
              ]}
              onPress={() => setSelectedModule(moduleCode)}
            >
              <Text
                style={[
                  styles.filterButtonText,
                  selectedModule === moduleCode &&
                    styles.filterButtonTextActive,
                ]}
              >
                {moduleCode}
              </Text>
            </TouchableOpacity>
          ))}
        </View>
      )}

      <FlatList
        data={notes.filter(
          (note) => !selectedModule || note.moduleCode === selectedModule
        )}
        renderItem={renderNote}
        keyExtractor={(item) => item.id.toString()}
        contentContainerStyle={styles.listContent}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Icon name="clipboard-outline" size={64} color="#bdc3c7" />
            <Text style={styles.emptyText}>Aucune note disponible</Text>
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
  filterContainer: {
    flexDirection: 'row',
    padding: 10,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#ecf0f1',
  },
  filterButton: {
    paddingHorizontal: 15,
    paddingVertical: 8,
    borderRadius: 20,
    marginRight: 10,
    backgroundColor: '#ecf0f1',
  },
  filterButtonActive: {
    backgroundColor: '#3498db',
  },
  filterButtonText: {
    fontSize: 14,
    color: '#7f8c8d',
    fontWeight: '600',
  },
  filterButtonTextActive: {
    color: '#fff',
  },
  listContent: {
    padding: 15,
  },
  noteCard: {
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
  noteHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 10,
  },
  noteInfo: {
    flex: 1,
  },
  moduleCode: {
    fontSize: 14,
    color: '#7f8c8d',
    fontWeight: '600',
  },
  evaluationTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#2c3e50',
    marginTop: 5,
  },
  evaluationType: {
    fontSize: 12,
    color: '#95a5a6',
    marginTop: 5,
    textTransform: 'uppercase',
  },
  gradeContainer: {
    paddingHorizontal: 15,
    paddingVertical: 8,
    borderRadius: 20,
  },
  gradeText: {
    fontSize: 18,
    fontWeight: 'bold',
  },
  noteDetails: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 10,
    paddingTop: 10,
    borderTopWidth: 1,
    borderTopColor: '#ecf0f1',
  },
  scoreText: {
    fontSize: 16,
    color: '#34495e',
    fontWeight: '600',
  },
  dateText: {
    fontSize: 14,
    color: '#7f8c8d',
  },
  commentsText: {
    fontSize: 14,
    color: '#7f8c8d',
    marginTop: 10,
    fontStyle: 'italic',
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

export default NotesScreen;


