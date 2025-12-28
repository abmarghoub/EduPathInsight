from flask_restful import Resource
from flask import request, jsonify
from models import db, Anomaly
from services.anomaly_detection_service import AnomalyDetectionService
from services.presence_service import PresenceService
from services.activity_service import ActivityService
from services.notification_service import NotificationService
import os


presence_service = PresenceService(module_service_url=os.getenv('MODULE_SERVICE_URL', 'http://localhost:8083'))
activity_service = ActivityService(module_service_url=os.getenv('MODULE_SERVICE_URL', 'http://localhost:8083'))
anomaly_service = AnomalyDetectionService(presence_service, activity_service)

notification_service = NotificationService(
    host=os.getenv('RABBITMQ_HOST', 'localhost'),
    port=int(os.getenv('RABBITMQ_PORT', '5672')),
    user=os.getenv('RABBITMQ_USER', 'guest'),
    password=os.getenv('RABBITMQ_PASSWORD', 'guest'),
    exchange=os.getenv('RABBITMQ_EXCHANGE', 'edupath.exchange'),
    routing_key=os.getenv('RABBITMQ_ROUTING_KEY', 'activity.routing.key')
)
notification_service.connect()


class AnomalyListResource(Resource):
    def get(self):
        """Obtenir toutes les anomalies (filtrées par statut si fourni)"""
        status = request.args.get('status')
        module_id = request.args.get('module_id', type=int)
        
        query = Anomaly.query
        
        if status:
            from models import AnomalyStatus
            query = query.filter_by(status=AnomalyStatus[status])
        
        if module_id:
            query = query.filter_by(module_id=module_id)
        
        anomalies = query.all()
        return jsonify([a.to_dict() for a in anomalies])


class AnomalyResource(Resource):
    def get(self, anomaly_id):
        """Obtenir une anomalie par ID"""
        anomaly = Anomaly.query.get(anomaly_id)
        if not anomaly:
            return {'error': 'Anomalie non trouvée'}, 404
        return jsonify(anomaly.to_dict())
    
    def put(self, anomaly_id):
        """Mettre à jour le statut d'une anomalie"""
        data = request.json
        action = data.get('action')  # 'acknowledge' ou 'resolve'
        
        anomaly = Anomaly.query.get(anomaly_id)
        if not anomaly:
            return {'error': 'Anomalie non trouvée'}, 404
        
        if action == 'acknowledge':
            anomaly = anomaly_service.acknowledge_anomaly(anomaly_id)
        elif action == 'resolve':
            anomaly = anomaly_service.resolve_anomaly(anomaly_id)
        else:
            return {'error': 'Action invalide. Utilisez "acknowledge" ou "resolve"'}, 400
        
        return jsonify(anomaly.to_dict())


class AnomalyByStudentResource(Resource):
    def get(self, student_id):
        """Obtenir toutes les anomalies d'un étudiant"""
        active_only = request.args.get('active_only', 'false').lower() == 'true'
        
        if active_only:
            anomalies = anomaly_service.get_active_anomalies_by_student(student_id)
        else:
            anomalies = anomaly_service.get_anomalies_by_student(student_id)
        
        return jsonify([a.to_dict() for a in anomalies])


class CheckAnomaliesResource(Resource):
    def post(self):
        """Vérifier et créer des anomalies pour un étudiant et un module"""
        data = request.json
        student_id = data.get('student_id')
        module_id = data.get('module_id')
        
        if not student_id or not module_id:
            return {'error': 'student_id et module_id sont requis'}, 400
        
        anomalies = anomaly_service.check_anomalies(student_id, module_id)
        
        # Envoyer des notifications pour les nouvelles anomalies
        for anomaly in anomalies:
            notification_service.send_anomaly_notification(anomaly.to_dict())
        
        return jsonify([a.to_dict() for a in anomalies]), 201


